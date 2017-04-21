/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
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
//@formatter:on
package org.lisoft.lsml.model.datacache.gamedata;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lisoft.lsml.model.chassi.HardPoint;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.chassi.OmniPodSet;
import org.lisoft.lsml.model.datacache.DataCache;
import org.lisoft.lsml.model.datacache.gamedata.XMLOmniPods.XMLOmniPodsSet.XMLOmniPodsComponent;
import org.lisoft.lsml.model.datacache.gamedata.helpers.ItemStatsOmniPodType;
import org.lisoft.lsml.model.datacache.gamedata.helpers.MdfComponent;
import org.lisoft.lsml.model.datacache.gamedata.helpers.MdfComponent.MdfHardpoint;
import org.lisoft.lsml.model.datacache.gamedata.helpers.MdfItem;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class is used for parsing {@link OmniPod}s from "chassis-omnipods.xml" files.
 *
 * @author Li Song
 */
public class XMLOmniPods {
    public static class XMLOmniPodsSet {
        public static class XMLOmniPodsComponent {
            @XStreamAsAttribute
            private String name;
            @XStreamImplicit(itemFieldName = "Fixed")
            private List<MdfItem> fixedItems;
            @XStreamImplicit(itemFieldName = "Internal")
            private List<MdfItem> internals;
            @XStreamImplicit(itemFieldName = "Hardpoint")
            private List<MdfHardpoint> hardpoints;
            @XStreamImplicit(itemFieldName = "Quirk")
            private List<XMLQuirk> quirks;
            @XStreamAsAttribute
            private int CanEquipECM;
        }

        public static class XMLOmniPodsSetBonuses {
            public static class XMLOmniPodsBonus {
                @XStreamAsAttribute
                private int PieceCount;
                @XStreamImplicit(itemFieldName = "Quirk")
                private List<XMLQuirk> quirks;
            }

            private XMLOmniPodsBonus Bonus;
        }

        @XStreamAsAttribute
        private String name;

        XMLOmniPodsSetBonuses SetBonuses;

        @XStreamImplicit(itemFieldName = "component")
        List<XMLOmniPodsComponent> omniPods;
    }

    public static XMLOmniPods fromXml(InputStream is) {
        final XStream xstream = DataCache.makeMwoSuitableXStream();
        xstream.alias("OmniPods", XMLOmniPods.class);
        return (XMLOmniPods) xstream.fromXML(is);
    }

    @XStreamImplicit(itemFieldName = "Set")
    List<XMLOmniPodsSet> sets;

    public List<OmniPod> asOmniPods(XMLItemStats aItemStatsXml, XMLHardpoints aHardPointsXML,
            Map<Integer, Object> aId2obj, Map<String, ModifierDescription> aModifierDescriptors) {
        final List<OmniPod> ans = new ArrayList<>();

        // This map allows lookup like: set2component2id[set][component] == id
        final Map<String, Map<String, ItemStatsOmniPodType>> set2component2type = new HashMap<>();
        for (final ItemStatsOmniPodType omniPodType : aItemStatsXml.OmniPodList) {
            // Okay so PGI has made yet another SNAFU in their data files:
            // Libs/Items/OmniPods.xml has this:
            // <OmniPod id="30775" chassis="summoner" set="smn-ml" component="right_leg" />
            // <OmniPod id="30776" chassis="summoner" set="smn-ml" component="right_leg" />
            // Yeah, the first number is the correct one.
            // So we need to prevent the second entry from overwriting the first by using
            // `putIfAbsent` instead of `put`.
            set2component2type.computeIfAbsent(omniPodType.set, key -> new HashMap<>())
                    .putIfAbsent(omniPodType.component, omniPodType);
        }

        for (final XMLOmniPodsSet set : sets) {
            final List<Modifier> setQuirks = new ArrayList<>();
            for (final XMLQuirk quirk : set.SetBonuses.Bonus.quirks) {
                setQuirks.addAll(QuirkModifiers.createModifiers(quirk, aModifierDescriptors));
            }
            final OmniPodSet omniPodSet = new OmniPodSet(setQuirks);

            for (final XMLOmniPodsComponent component : set.omniPods) {
                final ItemStatsOmniPodType type = set2component2type.get(set.name).get(component.name);
                if (type == null) {
                    throw new IllegalArgumentException("No matching omnipod in itemstats.xml");
                }

                int maxJumpjets = 0;
                final int maxPilotModules = 0;
                final List<Modifier> quirksList = new ArrayList<>();
                if (null != component.quirks) {
                    for (final XMLQuirk quirk : component.quirks) {
                        if ("jumpjetslots_additive".equals(quirk.name.toLowerCase())) {
                            maxJumpjets = (int) quirk.value;
                        }
                        else {
                            quirksList.addAll(QuirkModifiers.createModifiers(quirk, aModifierDescriptors));
                        }
                        // TODO: check for pilot modules as soon as we know what they're called.
                    }
                }

                final Location location = Location.fromMwoName(component.name);
                final List<HardPoint> hardPoints = MdfComponent.getHardPoints(location, aHardPointsXML,
                        component.hardpoints, component.CanEquipECM, set.name);
                final Faction faction = Faction.CLAN;

                final List<Item> fixedItems = MdfComponent.getFixedItems(aId2obj, component.internals,
                        component.fixedItems);
                final List<Item> toggleableItems = MdfComponent.getToggleableItems(aId2obj, component.internals,
                        component.fixedItems);

                ans.add(new OmniPod(type.id, location, type.chassis, set.name, omniPodSet, quirksList, hardPoints,
                        fixedItems, toggleableItems, maxJumpjets, maxPilotModules, faction));
            }
        }

        return ans;
    }
}
