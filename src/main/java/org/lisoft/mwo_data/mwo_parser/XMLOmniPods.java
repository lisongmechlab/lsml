/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.lisoft.mwo_data.mwo_parser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.mechs.HardPoint;
import org.lisoft.mwo_data.mechs.Location;
import org.lisoft.mwo_data.mechs.OmniPod;
import org.lisoft.mwo_data.mechs.OmniPodSetBonus;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * This class is used for parsing {@link OmniPod}s from "chassis-omnipods.xml" files.
 *
 * @author Li Song
 */
@SuppressWarnings("SpellCheckingInspection")
class XMLOmniPods {
  @XStreamImplicit(itemFieldName = "Set")
  List<XMLOmniPodsSet> sets;

  static XMLOmniPods fromXml(InputStream is) {
    final XStream xstream = GameVFS.makeMwoSuitableXStream();
    xstream.alias("OmniPods", XMLOmniPods.class);
    return (XMLOmniPods) xstream.fromXML(is);
  }

  private static List<OmniPodSetBonus> getAllSetBonusLevels(
      PartialDatabase aPartialDatabase, XMLOmniPodsSet set) {
    final List<OmniPodSetBonus> setBonuses = new ArrayList<>();
    for (final XMLOmniPodsBonus bonusLevel : set.SetBonuses.bonuses) {
      final List<Modifier> setQuirks = new ArrayList<>();
      for (final XMLQuirk quirk : bonusLevel.quirks) {
        setQuirks.add(QuirkModifiers.createModifier(quirk, aPartialDatabase));
      }
      final int minPieces = bonusLevel.PieceCount <= 0 ? 8 : bonusLevel.PieceCount;
      final OmniPodSetBonus omniPodSetBonus = new OmniPodSetBonus(minPieces, setQuirks);
      setBonuses.add(omniPodSetBonus);
    }
    setBonuses.sort(Comparator.comparingInt(OmniPodSetBonus::getMinPieces));
    return setBonuses;
  }

  List<OmniPod> asOmniPods(
      RawMergedXML mergedXML, XMLHardpoints aHardPointsXML, PartialDatabase aPartialDatabase) {
    final List<OmniPod> ans = new ArrayList<>();

    // This map allows lookup like: set2component2id[set][component] == id
    final Map<String, Map<String, XMLOmniPod>> set2component2type = new HashMap<>();
    for (final XMLOmniPod omniPodType : mergedXML.OmniPodList) {
      // Okay so PGI has made yet another SNAFU in their data files:
      // Libs/Items/OmniPods.xml has this:
      // <OmniPod id="30775" chassis="summoner" set="smn-ml" component="right_leg" />
      // <OmniPod id="30776" chassis="summoner" set="smn-ml" component="right_leg" />
      // Yeah, the first number is the correct one.
      // So we need to prevent the second entry from overwriting the first by using
      // `putIfAbsent` instead of `put`.
      //
      // Update 2023-11-04: This data issue is still present.
      set2component2type
          .computeIfAbsent(omniPodType.set, key -> new HashMap<>())
          .putIfAbsent(omniPodType.component, omniPodType);
    }

    for (final XMLOmniPodsSet set : sets) {
      final List<OmniPodSetBonus> bonusLevels = getAllSetBonusLevels(aPartialDatabase, set);

      for (final XMLOmniPodsComponent component : set.omniPods) {
        final XMLOmniPod omniPod = set2component2type.get(set.name).get(component.name);
        if (omniPod == null) {
          throw new IllegalArgumentException("No matching omnipod in itemstats.xml");
        }

        int maxJumpJets = 0;
        final List<Modifier> quirksList = new ArrayList<>();
        if (null != component.quirks) {
          for (final XMLQuirk quirk : component.quirks) {
            if ("jumpjetslots_additive".equalsIgnoreCase(quirk.name)) {
              maxJumpJets = (int) quirk.value;
            } else {
              quirksList.add(QuirkModifiers.createModifier(quirk, aPartialDatabase));
            }
          }
        }

        final Location location = Location.fromMwoName(component.name);
        final List<HardPoint> hardPoints =
            MdfComponent.getHardPoints(
                location, aHardPointsXML, component.hardpoints, component.CanEquipECM, set.name);
        final Faction faction = Faction.CLAN;

        final List<Item> fixedItems =
            MdfComponent.getFixedItems(aPartialDatabase, component.internals, component.fixedItems);
        final List<Item> toggleableItems =
            Stream.concat(
                    MdfComponent.getToggleableItems(aPartialDatabase, component.internals),
                    MdfComponent.getToggleableItems(aPartialDatabase, component.fixedItems))
                .collect(Collectors.toList());

        ans.add(
            new OmniPod(
                omniPod.id,
                location,
                omniPod.chassis,
                set.name,
                bonusLevels,
                quirksList,
                hardPoints,
                fixedItems,
                toggleableItems,
                maxJumpJets,
                faction));
      }
    }
    return ans;
  }

  private static class XMLOmniPodsComponent {
    @XStreamAsAttribute int CanEquipECM;

    @XStreamImplicit(itemFieldName = "Fixed")
    List<MdfItem> fixedItems;

    @XStreamImplicit(itemFieldName = "Hardpoint")
    List<MdfComponent.MdfHardpoint> hardpoints;

    @XStreamImplicit(itemFieldName = "Internal")
    List<MdfItem> internals;

    @XStreamAsAttribute String name;

    @XStreamImplicit(itemFieldName = "Quirk")
    List<XMLQuirk> quirks;
  }

  private static class XMLOmniPodsSetBonuses {
    @XStreamImplicit(itemFieldName = "Bonus")
    List<XMLOmniPodsBonus> bonuses;
  }

  private static class XMLOmniPodsBonus {
    @XStreamAsAttribute int PieceCount;

    @XStreamImplicit(itemFieldName = "Quirk")
    List<XMLQuirk> quirks;
  }

  private static class XMLOmniPodsSet {
    XMLOmniPodsSetBonuses SetBonuses;

    @XStreamImplicit(itemFieldName = "component")
    List<XMLOmniPodsComponent> omniPods;

    @XStreamAsAttribute String name;
  }
}
