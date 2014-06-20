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
package lisong_mechlab.model.loadout.component;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.LoadoutOmniMech;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.Message.Type;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This operation changes an {@link OmniPod} on a {@link ConfiguredComponentOmniMech}.
 * 
 * @author Emily Björk
 */
public class OpChangeOmniPod extends CompositeOperation{

   private final ConfiguredComponentOmniMech component;
   private final OmniPod                     newOmniPod;
   private final LoadoutOmniMech             loadout;
   private final MessageXBar                 xBar;
   private OmniPod                           oldOmniPod;

   /**
    * Creates a new {@link OmniPod} change {@link Operation}.
    * 
    * @param aXBar
    *           A {@link MessageXBar} to send messages on.
    * @param aLoadout
    *           The {@link LoadoutOmniMech} that the component is a part on.
    * @param aComponentOmniMech
    *           The component to change the {@link OmniPod} on.
    * @param aOmniPod
    *           The new {@link OmniPod} to change to.
    */
   public OpChangeOmniPod(MessageXBar aXBar, LoadoutOmniMech aLoadout, ConfiguredComponentOmniMech aComponentOmniMech, OmniPod aOmniPod){
      super("change omnipod on " + aComponentOmniMech.getInternalComponent().getLocation());
      if(aOmniPod == null)
         throw new IllegalArgumentException("Omnipod must not be null!");
      
      component = aComponentOmniMech;
      newOmniPod = aOmniPod;
      loadout = aLoadout;
      xBar = aXBar;
   }

   @Override
   public void buildOperation(){
      oldOmniPod = component.getOmniPod();
      
      // Remove any items that has a hard point requirement other than none.
      for(Item item : component.getItemsEquipped()){
         if( item.getHardpointType() != HardPointType.NONE ){
            addOp(new OpRemoveItem(xBar, loadout, component, item));
         }
      }
      
      // Make sure we respect global jump-jet limit
      int jjLeft = loadout.getJumpJetsMax() + (newOmniPod.getJumpJetsMax() - oldOmniPod.getJumpJetsMax()); 
      for(ConfiguredComponentOmniMech componentOmniMech : loadout.getComponents()){
         for(Item item : componentOmniMech.getItemsEquipped()){
            if(item instanceof JumpJet){
               if(jjLeft > 0 ){
                  jjLeft--;
               }
               else{
                  addOp(new OpRemoveItem(xBar, loadout, componentOmniMech, item));
               }
            }
         }
      }
   }

   @Override
   protected void apply(){
      super.apply();

      loadout.setOmniPod(newOmniPod);
      if( null != xBar ){
         xBar.post(new ConfiguredComponentBase.Message(component, Type.OmniPodChanged));
      }
   }

   @Override
   protected void undo(){
      loadout.setOmniPod(oldOmniPod);
      if( null != xBar ){
         xBar.post(new ConfiguredComponentBase.Message(component, Type.OmniPodChanged));
      }

      super.undo();
   }
}
