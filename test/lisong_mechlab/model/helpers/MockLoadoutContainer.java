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
import lisong_mechlab.model.chassi.Chassis;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.InternalComponent;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.ConfiguredComponent;
import lisong_mechlab.model.upgrades.Upgrades;

/**
 * The purpose of this class is to provide a default mock structure of a {@link Loadout} which is easy to configure for
 * a particular test.
 * 
 * @author Emily Bjoerk
 */
public class MockLoadoutContainer{
   final public Chassis       chassi;
   final public Loadout      loadout;
   final public Upgrades     upgrades;
   final public Efficiencies efficiencies;
   final public InternalComponent ira;
   final public InternalComponent irt;
   final public InternalComponent irl;
   final public InternalComponent ihd;
   final public InternalComponent ict;
   final public InternalComponent ilt;
   final public InternalComponent ill;
   final public InternalComponent ila;
   final public ConfiguredComponent  ra;
   final public ConfiguredComponent  rt;
   final public ConfiguredComponent  rl;
   final public ConfiguredComponent  hd;
   final public ConfiguredComponent  ct;
   final public ConfiguredComponent  lt;
   final public ConfiguredComponent  ll;
   final public ConfiguredComponent  la;

   public MockLoadoutContainer(){
      chassi = mock(Chassis.class);
      loadout = mock(Loadout.class);
      upgrades = mock(Upgrades.class);
      efficiencies = mock(Efficiencies.class);

      ira = mock(InternalComponent.class);
      irt = mock(InternalComponent.class);
      irl = mock(InternalComponent.class);
      ihd = mock(InternalComponent.class);
      ict = mock(InternalComponent.class);
      ilt = mock(InternalComponent.class);
      ill = mock(InternalComponent.class);
      ila = mock(InternalComponent.class);
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

      when(ra.getInternalPart()).thenReturn(ira);
      when(rt.getInternalPart()).thenReturn(irt);
      when(rl.getInternalPart()).thenReturn(irl);
      when(hd.getInternalPart()).thenReturn(ihd);
      when(ct.getInternalPart()).thenReturn(ict);
      when(lt.getInternalPart()).thenReturn(ilt);
      when(ll.getInternalPart()).thenReturn(ill);
      when(la.getInternalPart()).thenReturn(ila);

      when(ra.getLoadout()).thenReturn(loadout);
      when(rt.getLoadout()).thenReturn(loadout);
      when(rl.getLoadout()).thenReturn(loadout);
      when(hd.getLoadout()).thenReturn(loadout);
      when(ct.getLoadout()).thenReturn(loadout);
      when(lt.getLoadout()).thenReturn(loadout);
      when(ll.getLoadout()).thenReturn(loadout);
      when(la.getLoadout()).thenReturn(loadout);
      
      when(ra.toString()).thenReturn("RA");
      when(rt.toString()).thenReturn("RT");
      when(rl.toString()).thenReturn("RL");
      when(hd.toString()).thenReturn("HD");
      when(ct.toString()).thenReturn("CT");
      when(lt.toString()).thenReturn("LT");
      when(ll.toString()).thenReturn("LL");
      when(la.toString()).thenReturn("LA");
      when(loadout.getPart(Location.RightArm)).thenReturn(ra);
      when(loadout.getPart(Location.RightTorso)).thenReturn(rt);
      when(loadout.getPart(Location.RightLeg)).thenReturn(rl);
      when(loadout.getPart(Location.Head)).thenReturn(hd);
      when(loadout.getPart(Location.CenterTorso)).thenReturn(ct);
      when(loadout.getPart(Location.LeftTorso)).thenReturn(lt);
      when(loadout.getPart(Location.LeftLeg)).thenReturn(ll);
      when(loadout.getPart(Location.LeftArm)).thenReturn(la);
      when(loadout.getPartLoadOuts()).thenReturn(Arrays.asList(ra, rt, rl, hd, ct, lt, ll, la));
      when(loadout.getUpgrades()).thenReturn(upgrades);
      when(loadout.getEfficiencies()).thenReturn(efficiencies);
      when(loadout.getChassi()).thenReturn(chassi);
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
      when(internalItem.getNumCriticalSlots(any(Upgrades.class))).thenReturn(aNumSlots);
      when(internalItem.getMass(any(Upgrades.class))).thenReturn(aTons);
      when(internalItem.compareTo(any(Item.class))).thenCallRealMethod();
      when(internalItem.getHardpointType()).thenReturn(HardPointType.NONE);
      return internalItem;
   }
}
