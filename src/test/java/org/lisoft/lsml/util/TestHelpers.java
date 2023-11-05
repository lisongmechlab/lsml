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
package org.lisoft.lsml.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.lisoft.lsml.application.ConsoleErrorReporter;
import org.lisoft.lsml.model.export.BasePGICoder;
import org.lisoft.lsml.model.export.MWOCoder;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.Weapon;
import org.lisoft.mwo_data.equipment.WeaponRangeProfile;
import org.lisoft.mwo_data.equipment.WeaponRangeProfile.RangeNode;
import org.lisoft.mwo_data.equipment.WeaponRangeProfile.RangeNode.InterpolationType;
import org.lisoft.mwo_data.mechs.HardPointType;
import org.lisoft.mwo_data.mechs.Location;
import org.lisoft.mwo_data.modifiers.Attribute;
import org.lisoft.mwo_data.modifiers.Modifier;
import org.lisoft.mwo_data.modifiers.ModifierDescription;

/**
 * This class contains various static helpers to make writing tests easier.
 *
 * @author Li Song
 */
public class TestHelpers {
  private static final MWOCoder coder =
      new MWOCoder(new BasePGICoder(), new DefaultLoadoutFactory(), new ConsoleErrorReporter());

  public static Weapon makeWeapon(
      final double zeroRange,
      final double minRange,
      final double longRange,
      final double maxRange,
      final boolean isOffensive,
      double dps,
      String aName,
      Collection<Modifier> aModifiers) {
    return makeWeapon(
        zeroRange,
        minRange,
        longRange,
        maxRange,
        0.0,
        1.0,
        1.0,
        0.0,
        isOffensive,
        dps,
        aName,
        aModifiers);
  }

  public static Weapon makeWeapon(
      final double zeroRange,
      final double minRange,
      final double longRange,
      final double maxRange,
      final double zeroRangeEff,
      final double minRangeEff,
      final double longRangeEff,
      final double maxRangeEff,
      final boolean isOffensive,
      double dps,
      String aName,
      Collection<Modifier> aModifiers) {
    final Weapon weapon = mock(Weapon.class);
    when(weapon.getName()).thenReturn(aName);
    when(weapon.isOffensive()).thenReturn(isOffensive);

    final List<RangeNode> nodes = new ArrayList<>();
    nodes.add(new RangeNode(rangeNode(zeroRange), InterpolationType.STEP, zeroRangeEff));
    nodes.add(new RangeNode(rangeNode(minRange), InterpolationType.LINEAR, minRangeEff));
    nodes.add(new RangeNode(rangeNode(longRange), InterpolationType.LINEAR, longRangeEff));
    nodes.add(new RangeNode(rangeNode(maxRange), InterpolationType.LINEAR, maxRangeEff));

    final WeaponRangeProfile rangeProfile = new WeaponRangeProfile(nodes);

    when(weapon.getRangeProfile()).thenReturn(rangeProfile);
    when(weapon.getRangeMax(aModifiers)).thenReturn(maxRange);
    when(weapon.getStat("d/s", aModifiers)).thenReturn(dps);
    return weapon;
  }

  public static Item makeItemMock(int aSlots, double aTons, Faction aFaction) {
    Item item = mock(Item.class);
    when(item.getMass()).thenReturn(aTons);
    when(item.getSlots()).thenReturn(aSlots);
    when(item.getFaction()).thenReturn(aFaction);
    when(item.isCompatible(any())).thenReturn(true);
    when(item.getHardpointType()).thenReturn(HardPointType.NONE);
    when(item.getAllowedComponents()).thenReturn(Location.RIGHT_TO_LEFT);

    return item;
  }

  public static Loadout parse(String aMWOCode) throws Exception {
    return coder.decode(aMWOCode);
  }

  public static Attribute rangeNode(double aRange) {
    return new Attribute(
        aRange, ModifierDescription.SEL_ALL, ModifierDescription.SPEC_WEAPON_RANGE);
  }
}
