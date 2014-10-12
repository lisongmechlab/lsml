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
package lisong_mechlab.model.helpers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ComponentBase;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.upgrades.Upgrades;

/**
 * The purpose of this class is to provide a default mock structure of a {@link LoadoutStandard} which is easy to
 * configure for a particular test.
 * 
 * @author Li Song
 */
public class MockLoadoutContainer {
    final public ChassisBase                          chassi;
    final public LoadoutBase<ConfiguredComponentBase> loadout;
    final public Upgrades                             upgrades;
    final public Efficiencies                         efficiencies;
    final public ComponentBase                        ira;
    final public ComponentBase                        irt;
    final public ComponentBase                        irl;
    final public ComponentBase                        ihd;
    final public ComponentBase                        ict;
    final public ComponentBase                        ilt;
    final public ComponentBase                        ill;
    final public ComponentBase                        ila;
    final public ConfiguredComponentBase              ra;
    final public ConfiguredComponentBase              rt;
    final public ConfiguredComponentBase              rl;
    final public ConfiguredComponentBase              hd;
    final public ConfiguredComponentBase              ct;
    final public ConfiguredComponentBase              lt;
    final public ConfiguredComponentBase              ll;
    final public ConfiguredComponentBase              la;

    public MockLoadoutContainer() {
        chassi = mock(ChassisBase.class);
        loadout = mock(LoadoutBase.class);
        upgrades = mock(Upgrades.class);
        efficiencies = mock(Efficiencies.class);

        ira = mock(ComponentBase.class);
        irt = mock(ComponentBase.class);
        irl = mock(ComponentBase.class);
        ihd = mock(ComponentBase.class);
        ict = mock(ComponentBase.class);
        ilt = mock(ComponentBase.class);
        ill = mock(ComponentBase.class);
        ila = mock(ComponentBase.class);
        ra = mock(ConfiguredComponentBase.class);
        rt = mock(ConfiguredComponentBase.class);
        rl = mock(ConfiguredComponentBase.class);
        hd = mock(ConfiguredComponentBase.class);
        ct = mock(ConfiguredComponentBase.class);
        lt = mock(ConfiguredComponentBase.class);
        ll = mock(ConfiguredComponentBase.class);
        la = mock(ConfiguredComponentBase.class);

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
        when(loadout.getChassis()).thenReturn(chassi);
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
        when(internalItem.getNumCriticalSlots()).thenReturn(aNumSlots);
        when(internalItem.getMass()).thenReturn(aTons);
        when(internalItem.compareTo(any(Item.class))).thenCallRealMethod();
        when(internalItem.getHardpointType()).thenReturn(HardPointType.NONE);
        return internalItem;
    }
}
