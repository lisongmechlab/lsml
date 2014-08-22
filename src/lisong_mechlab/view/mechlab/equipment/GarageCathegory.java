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
package lisong_mechlab.view.mechlab.equipment;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;

import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisClass;
import lisong_mechlab.model.garage.MechGarage;
import lisong_mechlab.model.garage.MechGarage.Message.Type;
import lisong_mechlab.model.item.Faction;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutMessage;
import lisong_mechlab.util.MessageXBar;

class GarageCathegory extends FilterTreeCathegory<LoadoutBase<?>>{
   private MechGarage         garage = null;
   private final ChassisClass chassiClass;
   private final Faction faction;

   public GarageCathegory(String aName, TreeCathegory aParent, GarageTreeModel aModel, MessageXBar xbar, ChassisClass aChassiClass,
                          JTextField aFilterBar, GarageTree aGarageTree, Faction aFaction){
      super(xbar, aName, aParent, aModel, aFilterBar, aGarageTree);
      chassiClass = aChassiClass;
      faction = aFaction;
   }

   @Override
   public void receive(MessageXBar.Message aMsg){
      assert (SwingUtilities.isEventDispatchThread());
      if( aMsg instanceof MechGarage.Message ){
         MechGarage.Message msg = (MechGarage.Message)aMsg;
         if( msg.type == Type.NewGarage ){
            garage = msg.garage;
         }
         garageChanged();
      }
      else if( aMsg instanceof LoadoutMessage ){
         LoadoutMessage message = (LoadoutMessage)aMsg;
         if( message.type == LoadoutMessage.Type.CREATE || message.type == LoadoutMessage.Type.RENAME ){
            garageChanged();
         }
      }
      super.receive(aMsg);
   }

   @Override
   protected boolean filter(LoadoutBase<?> aLoadout){
      ChassisBase chassi = aLoadout.getChassis();
      return aLoadout.getName().toLowerCase().contains(getFilterString()) || chassi.getName().toLowerCase().contains(getFilterString());
   }

   private void garageChanged(){
      children.clear();
      if( garage != null ){
         for(LoadoutBase<?> loadout : garage.getMechs()){
            if( loadout.getChassis().getChassiClass() == chassiClass && loadout.getChassis().getFaction().isCompatible(faction))
               children.add(loadout);
         }
      }
      setDirtyBit();
      getModel().notifyTreeChange(new TreeModelEvent(this, getPath()));
      garageTree.expandPath(getPath());
   }
}
