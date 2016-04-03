/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
package org.lisoft.lsml.model.helpers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.Component;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.upgrades.ArmorUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;

/**
 * The purpose of this class is to provide a default mock structure of a {@link LoadoutStandard} which is easy to
 * configure for a particular test.
 * 
 * @author Emily Björk
 */
public class MockLoadoutContainer {
    final public Chassis             chassis;
    final public Loadout             loadout;
    final public Upgrades            upgrades;
    final public Efficiencies        efficiencies;
    final public WeaponGroups        weaponGroups;
    final public MovementProfile     movementProfile;
    final public Component           ira;
    final public Component           irt;
    final public Component           irl;
    final public Component           ihd;
    final public Component           ict;
    final public Component           ilt;
    final public Component           ill;
    final public Component           ila;
    final public ConfiguredComponent ra;
    final public ConfiguredComponent rt;
    final public ConfiguredComponent rl;
    final public ConfiguredComponent hd;
    final public ConfiguredComponent ct;
    final public ConfiguredComponent lt;
    final public ConfiguredComponent ll;
    final public ConfiguredComponent la;
    final public ArmorUpgrade        armorUpgrade;
    final public HeatSinkUpgrade     heatSinkUpgrade;

    public MockLoadoutContainer() {
        chassis = mock(Chassis.class);
        loadout = mock(Loadout.class);
        upgrades = mock(Upgrades.class);
        efficiencies = mock(Efficiencies.class);
        weaponGroups = mock(WeaponGroups.class);
        movementProfile = mock(MovementProfile.class);
        armorUpgrade = mock(ArmorUpgrade.class);
        heatSinkUpgrade = mock(HeatSinkUpgrade.class);

        when(upgrades.getArmor()).thenReturn(armorUpgrade);
        when(upgrades.getHeatSink()).thenReturn(heatSinkUpgrade);

        ira = mock(Component.class);
        irt = mock(Component.class);
        irl = mock(Component.class);
        ihd = mock(Component.class);
        ict = mock(Component.class);
        ilt = mock(Component.class);
        ill = mock(Component.class);
        ila = mock(Component.class);
        ra = mock(ConfiguredComponent.class);
        rt = mock(ConfiguredComponent.class);
        rl = mock(ConfiguredComponent.class);
        hd = mock(ConfiguredComponent.class);
        ct = mock(ConfiguredComponent.class);
        lt = mock(ConfiguredComponent.class);
        ll = mock(ConfiguredComponent.class);
        la = mock(ConfiguredComponent.class);

        when(ira.getLocation()).thenReturn(Location.RightArm);
        when(irt.getLocation()).thenReturn(Location.RightTorso);
        when(irl.getLocation()).thenReturn(Location.RightLeg);
        when(ihd.getLocation()).thenReturn(Location.Head);
        when(ict.getLocation()).thenReturn(Location.CenterTorso);
        when(ilt.getLocation()).thenReturn(Location.LeftTorso);
        when(ill.getLocation()).thenReturn(Location.LeftLeg);
        when(ila.getLocation()).thenReturn(Location.LeftArm);

        when(ra.getInternalComponent()).thenReturn(ira);
        when(rt.getInternalComponent()).thenReturn(irt);
        when(rl.getInternalComponent()).thenReturn(irl);
        when(hd.getInternalComponent()).thenReturn(ihd);
        when(ct.getInternalComponent()).thenReturn(ict);
        when(lt.getInternalComponent()).thenReturn(ilt);
        when(ll.getInternalComponent()).thenReturn(ill);
        when(la.getInternalComponent()).thenReturn(ila);

        when(ra.toString()).thenReturn("RA");
        when(rt.toString()).thenReturn("RT");
        when(rl.toString()).thenReturn("RL");
        when(hd.toString()).thenReturn("HD");
        when(ct.toString()).thenReturn("CT");
        when(lt.toString()).thenReturn("LT");
        when(ll.toString()).thenReturn("LL");
        when(la.toString()).thenReturn("LA");
        when(loadout.getComponent(Location.RightArm)).thenReturn(ra);
        when(loadout.getComponent(Location.RightTorso)).thenReturn(rt);
        when(loadout.getComponent(Location.RightLeg)).thenReturn(rl);
        when(loadout.getComponent(Location.Head)).thenReturn(hd);
        when(loadout.getComponent(Location.CenterTorso)).thenReturn(ct);
        when(loadout.getComponent(Location.LeftTorso)).thenReturn(lt);
        when(loadout.getComponent(Location.LeftLeg)).thenReturn(ll);
        when(loadout.getComponent(Location.LeftArm)).thenReturn(la);
        when(loadout.getComponents()).thenReturn(Arrays.asList(ra, rt, rl, hd, ct, lt, ll, la));
        when(loadout.getUpgrades()).thenReturn(upgrades);
        when(loadout.getEfficiencies()).thenReturn(efficiencies);
        when(loadout.getChassis()).thenReturn(chassis);
        when(loadout.getWeaponGroups()).thenReturn(weaponGroups);
        when(loadout.getMovementProfile()).thenReturn(movementProfile);
    }

    public Internal makeInternal(String aName, int aNumSlots) {
        return makeInternal(aName, aNumSlots, 0.0);
    }

    public Internal makeInternal(int aNumSlots) {
        return makeInternal("unnamed", aNumSlots, 0.0);
    }

    public Internal makeInternal(String aName, int aNumSlots, double aTons) {
        Internal internalItem = mock(Internal.class);
        when(internalItem.toString()).thenReturn(aName);
        when(internalItem.getSlots()).thenReturn(aNumSlots);
        when(internalItem.getMass()).thenReturn(aTons);
        when(internalItem.compareTo(any(Item.class))).thenCallRealMethod();
        when(internalItem.getHardpointType()).thenReturn(HardPointType.NONE);
        return internalItem;
    }
}
