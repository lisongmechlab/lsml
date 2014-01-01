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

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Efficiencies;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.LoadoutPart;
import lisong_mechlab.model.loadout.Upgrades;

/**
 * The purpose of this class is to provide a default mock structure of a {@link Loadout} which is easy to configure for
 * a particular test.
 * 
 * @author Emily Bjoerk
 */
public class MockLoadoutContainer{
   final public Chassi       chassi;
   final public Loadout      loadout;
   final public Upgrades     upgrades;
   final public Efficiencies efficiencies;
   final public InternalPart ira;
   final public InternalPart irt;
   final public InternalPart irl;
   final public InternalPart ihd;
   final public InternalPart ict;
   final public InternalPart ilt;
   final public InternalPart ill;
   final public InternalPart ila;
   final public LoadoutPart  ra;
   final public LoadoutPart  rt;
   final public LoadoutPart  rl;
   final public LoadoutPart  hd;
   final public LoadoutPart  ct;
   final public LoadoutPart  lt;
   final public LoadoutPart  ll;
   final public LoadoutPart  la;

   public MockLoadoutContainer(){
      chassi = mock(Chassi.class);
      loadout = mock(Loadout.class);
      upgrades = mock(Upgrades.class);
      efficiencies = mock(Efficiencies.class);

      ira = mock(InternalPart.class);
      irt = mock(InternalPart.class);
      irl = mock(InternalPart.class);
      ihd = mock(InternalPart.class);
      ict = mock(InternalPart.class);
      ilt = mock(InternalPart.class);
      ill = mock(InternalPart.class);
      ila = mock(InternalPart.class);
      ra = mock(LoadoutPart.class);
      rt = mock(LoadoutPart.class);
      rl = mock(LoadoutPart.class);
      hd = mock(LoadoutPart.class);
      ct = mock(LoadoutPart.class);
      lt = mock(LoadoutPart.class);
      ll = mock(LoadoutPart.class);
      la = mock(LoadoutPart.class);

      when(ira.getType()).thenReturn(Part.RightArm);
      when(irt.getType()).thenReturn(Part.RightTorso);
      when(irl.getType()).thenReturn(Part.RightLeg);
      when(ihd.getType()).thenReturn(Part.Head);
      when(ict.getType()).thenReturn(Part.CenterTorso);
      when(ilt.getType()).thenReturn(Part.LeftTorso);
      when(ill.getType()).thenReturn(Part.LeftLeg);
      when(ila.getType()).thenReturn(Part.LeftArm);

      when(ra.getInternalPart()).thenReturn(ira);
      when(rt.getInternalPart()).thenReturn(irt);
      when(rl.getInternalPart()).thenReturn(irl);
      when(hd.getInternalPart()).thenReturn(ihd);
      when(ct.getInternalPart()).thenReturn(ict);
      when(lt.getInternalPart()).thenReturn(ilt);
      when(ll.getInternalPart()).thenReturn(ill);
      when(la.getInternalPart()).thenReturn(ila);

      when(ra.toString()).thenReturn("RA");
      when(rt.toString()).thenReturn("RT");
      when(rl.toString()).thenReturn("RL");
      when(hd.toString()).thenReturn("HD");
      when(ct.toString()).thenReturn("CT");
      when(lt.toString()).thenReturn("LT");
      when(ll.toString()).thenReturn("LL");
      when(la.toString()).thenReturn("LA");
      when(loadout.getPart(Part.RightArm)).thenReturn(ra);
      when(loadout.getPart(Part.RightTorso)).thenReturn(rt);
      when(loadout.getPart(Part.RightLeg)).thenReturn(rl);
      when(loadout.getPart(Part.Head)).thenReturn(hd);
      when(loadout.getPart(Part.CenterTorso)).thenReturn(ct);
      when(loadout.getPart(Part.LeftTorso)).thenReturn(lt);
      when(loadout.getPart(Part.LeftLeg)).thenReturn(ll);
      when(loadout.getPart(Part.LeftArm)).thenReturn(la);
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
      when(internalItem.getHardpointType()).thenReturn(HardpointType.NONE);
      return internalItem;
   }
}
