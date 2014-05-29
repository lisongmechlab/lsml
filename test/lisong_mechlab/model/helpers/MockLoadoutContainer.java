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
package lisong_mechlab.model.helpers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.ComponentStandard;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.upgrades.Upgrades;

/**
 * The purpose of this class is to provide a default mock structure of a {@link LoadoutStandard} which is easy to configure for
 * a particular test.
 * 
 * @author Emily Bjoerk
 */
public class MockLoadoutContainer{
   final public ChassisStandard             chassi;
   final public LoadoutStandard             loadout;
   final public Upgrades            upgrades;
   final public Efficiencies        efficiencies;
   final public ComponentStandard   ira;
   final public ComponentStandard   irt;
   final public ComponentStandard   irl;
   final public ComponentStandard   ihd;
   final public ComponentStandard   ict;
   final public ComponentStandard   ilt;
   final public ComponentStandard   ill;
   final public ComponentStandard   ila;
   final public ConfiguredComponentBase ra;
   final public ConfiguredComponentBase rt;
   final public ConfiguredComponentBase rl;
   final public ConfiguredComponentBase hd;
   final public ConfiguredComponentBase ct;
   final public ConfiguredComponentBase lt;
   final public ConfiguredComponentBase ll;
   final public ConfiguredComponentBase la;

   public MockLoadoutContainer(){
      chassi = mock(ChassisStandard.class);
      loadout = mock(LoadoutStandard.class);
      upgrades = mock(Upgrades.class);
      efficiencies = mock(Efficiencies.class);

      ira = mock(ComponentStandard.class);
      irt = mock(ComponentStandard.class);
      irl = mock(ComponentStandard.class);
      ihd = mock(ComponentStandard.class);
      ict = mock(ComponentStandard.class);
      ilt = mock(ComponentStandard.class);
      ill = mock(ComponentStandard.class);
      ila = mock(ComponentStandard.class);
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

   public Internal makeInternal(String aName, int aNumSlots){
      return makeInternal(aName, aNumSlots, 0.0);
   }

   public Internal makeInternal(int aNumSlots){
      return makeInternal("unnamed", aNumSlots, 0.0);
   }

   public Internal makeInternal(String aName, int aNumSlots, double aTons){
      Internal internalItem = mock(Internal.class);
      when(internalItem.toString()).thenReturn(aName);
      when(internalItem.getNumCriticalSlots()).thenReturn(aNumSlots);
      when(internalItem.getMass()).thenReturn(aTons);
      when(internalItem.compareTo(any(Item.class))).thenCallRealMethod();
      when(internalItem.getHardpointType()).thenReturn(HardPointType.NONE);
      return internalItem;
   }
}
