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
package lisong_mechlab.view.mechlab.equipment;

import javax.swing.event.TreeModelEvent;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.MechGarage;
import lisong_mechlab.model.loadout.MechGarage.Message.Type;
import lisong_mechlab.util.MessageXBar;

class GarageCathegory extends AbstractTreeCathegory implements MessageXBar.Reader{
   private MechGarage garage = null;

   public GarageCathegory(String aName, TreeCathegory aParent, GarageTreeModel aModel, MessageXBar xbar){
      super(aName, aParent, aModel);
      xbar.attach(this);
   }

   @Override
   public void receive(MessageXBar.Message aMsg){
      if( aMsg instanceof MechGarage.Message ){
         MechGarage.Message msg = (MechGarage.Message)aMsg;
         if( msg.type == Type.NewGarage ){
            garage = msg.garage;
         }
         getModel().notifyTreeChange(new TreeModelEvent(this, getPath()));
      }
      else if( aMsg instanceof Loadout.Message ){
         // Loadout.Message message = (Message)aMsg;

         getModel().notifyTreeChange(new TreeModelEvent(this, getPath()));
      }
   }

   @Override
   public int getChildCount(){
      if( null == garage )
         return 0;
      return garage.getMechs().size();
   }

   @Override
   public int getIndex(Object aChild){
      if( null == garage )
         return -1;
      return garage.getMechs().indexOf(aChild);
   }

   @Override
   public Object getChild(int aIndex){
      if( null == garage )
         return null;
      return garage.getMechs().get(aIndex);
   }
}
