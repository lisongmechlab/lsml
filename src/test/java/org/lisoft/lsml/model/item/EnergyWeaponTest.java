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
package org.lisoft.lsml.model.item;

import org.junit.Test;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.WeaponRangeProfile.RangeNode;
import org.lisoft.lsml.model.item.WeaponRangeProfile.RangeNode.InterpolationType;
import org.lisoft.lsml.model.metrics.helpers.IntegratedSignal;
import org.lisoft.lsml.model.modifiers.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test suite for {@link EnergyWeapon} class.
 *
 * @author Li Song
 */
public class EnergyWeaponTest {

    @Test
    public void testCompare_flamers_flamers() throws Exception {
        final EnergyWeapon weapon = (EnergyWeapon) ItemDB.lookup("FLAMER");
        assertEquals(0, new ItemComparator(false).compare(weapon, weapon));
        assertEquals(0, new ItemComparator(true).compare(weapon, weapon));
    }

    @Test
    public void testGetDurationModified() {
        Collection<String> selectors = Arrays.asList("my_laser");
        ModifierDescription desc = new ModifierDescription("", "", Operation.MUL, selectors,
                                                           ModifierDescription.SPEC_WEAPON_DURATION,
                                                           ModifierType.NEGATIVE_GOOD);
        Modifier modifier = new Modifier(desc, -0.5);
        EnergyWeapon cut = makeLaser(1, 2.0, 4.0, 5.0, 3.0, 250, 500, selectors);
        assertEquals(1.5, cut.getDuration(Arrays.asList(modifier)), 0.0);
    }

    @Test
    public void testGetDurationNoModifiers() {
        EnergyWeapon cut = makeLaser(1, 2.0, 4.0, 5.0, 1.0, 250, 500, Arrays.asList("my_laser"));
        assertEquals(1.0, cut.getDuration(null), 0.0);
    }

    @Test
    public void testGetExpectedHeatSignalLasers() {
        EnergyWeapon cut = makeLaser(1, 2.0, 4.0, 5.0, 1.5, 250, 500, Arrays.asList("my_laser"));

        IntegratedSignal signal = cut.getExpectedHeatSignal(null);

        assertEquals(4.0, signal.integrateFromZeroTo(1.5), 0.0);
        assertEquals(4.0, signal.integrateFromZeroTo(6.499), 0.0);
        assertEquals(8.0, signal.integrateFromZeroTo(8), 0.0);
    }

    @Test
    public void testGetExpectedHeatSignalPPC() {
        EnergyWeapon cut = makePPC(1, 2.0, 5.0, 5.0, 1500, 250, 500, Arrays.asList("my_ppc"));

        IntegratedSignal signal = cut.getExpectedHeatSignal(null);

        assertEquals(5.0, signal.integrateFromZeroTo(0.0), 0.0);
        assertEquals(5.0, signal.integrateFromZeroTo(4.99), 0.0);
        assertEquals(10.0, signal.integrateFromZeroTo(5.0), 0.0);
    }

    @Test
    public void testRawFiringPeriodIncludesBurnTime() {
        EnergyWeapon cut = makeLaser(1, 2.0, 4.0, 5.0, 1.0, 250, 500, Arrays.asList("my_laser"));
        assertEquals(6.0, cut.getRawFiringPeriod(null), 0.0);
    }

    @Test
    public void testRawFiringPeriodInfiniteBurnTime() {
        // Some weapons such as flamers have infinite burn times (limited by player holding down the button)
        // for those, only the cooldown is returned.
        EnergyWeapon cut = makeLaser(1, 2.0, 4.0, 5.0, Double.POSITIVE_INFINITY, 250, 500, Arrays.asList("my_laser"));
        assertEquals(5.0, cut.getRawFiringPeriod(null), 0.0);
    }

    private EnergyWeapon makeLaser(int aSlots, double aTons, double aHeat, double aCooldown, double aBurnTime,
                                   double aLongRange, double aMaxRange, Collection<String> aSelectors) {
        Attribute heat = new Attribute(aHeat, aSelectors, ModifierDescription.SPEC_WEAPON_HEAT);
        Attribute cooldown = new Attribute(aCooldown, aSelectors, ModifierDescription.SPEC_WEAPON_COOL_DOWN);
        Attribute projectileSpeed = new Attribute(Double.POSITIVE_INFINITY, aSelectors,
                                                  ModifierDescription.SPEC_WEAPON_PROJECTILE_SPEED);
        Attribute freeAlpha = new Attribute(-1, aSelectors, ModifierDescription.SPEC_WEAPON_MAX_FREE_ALPHA);
        Attribute burnTime = new Attribute(aBurnTime, aSelectors, ModifierDescription.SPEC_WEAPON_DURATION);

        Attribute nearRange = new Attribute(0, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);
        Attribute longRange = new Attribute(aLongRange, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);
        Attribute maxRange = new Attribute(aMaxRange, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);

        List<RangeNode> nodes = Arrays.asList(new RangeNode(nearRange, InterpolationType.LINEAR, 1.0),
                                              new RangeNode(longRange, InterpolationType.LINEAR, 1.0),
                                              new RangeNode(maxRange, InterpolationType.LINEAR, 0));
        WeaponRangeProfile rangeProfile = new WeaponRangeProfile(null, nodes);

        return new EnergyWeapon("", "", "", 0, aSlots, aTons, 10.0, Faction.INNERSPHERE, heat, cooldown, rangeProfile,
                                1, 1, 1, projectileSpeed, -1, 0, freeAlpha, 0, 0, burnTime);
    }

    private EnergyWeapon makePPC(int aSlots, double aTons, double aHeat, double aCooldown, double aProjectileSpeed,
                                 double aLongRange, double aMaxRange, Collection<String> aSelectors) {
        Attribute heat = new Attribute(aHeat, aSelectors, ModifierDescription.SPEC_WEAPON_HEAT);
        Attribute cooldown = new Attribute(aCooldown, aSelectors, ModifierDescription.SPEC_WEAPON_COOL_DOWN);
        Attribute projectileSpeed = new Attribute(aProjectileSpeed, aSelectors,
                                                  ModifierDescription.SPEC_WEAPON_PROJECTILE_SPEED);
        Attribute freeAlpha = new Attribute(-1, aSelectors, ModifierDescription.SPEC_WEAPON_MAX_FREE_ALPHA);
        Attribute burnTime = new Attribute(0.0, aSelectors, ModifierDescription.SPEC_WEAPON_DURATION);

        Attribute nearRange = new Attribute(0, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);
        Attribute longRange = new Attribute(aLongRange, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);
        Attribute maxRange = new Attribute(aMaxRange, aSelectors, ModifierDescription.SPEC_WEAPON_RANGE);

        List<RangeNode> nodes = Arrays.asList(new RangeNode(nearRange, InterpolationType.LINEAR, 1.0),
                                              new RangeNode(longRange, InterpolationType.LINEAR, 1.0),
                                              new RangeNode(maxRange, InterpolationType.LINEAR, 0));
        WeaponRangeProfile rangeProfile = new WeaponRangeProfile(null, nodes);

        return new EnergyWeapon("", "", "", 0, aSlots, aTons, 10.0, Faction.INNERSPHERE, heat, cooldown, rangeProfile,
                                1, 1, 1, projectileSpeed, -1, 0, freeAlpha, 0, 0, burnTime);
    }
}
