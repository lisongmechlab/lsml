/*
 * @formatter:off
 * Li Song Mech Lab - A 'mech building tool for PGI's MechWarrior: Online.
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
package lisong_mechlab.view.mechlab;

import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;

import lisong_mechlab.model.chassi.ChassiClass;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.MechGarage;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.MessageXBar.Reader;

/**
 * This class renders a panel containing the user's garage.
 * 
 * @author Emily Björk
 */
public class GaragePanel extends JPanel implements Reader{
   /**
    * A simple {@link ListModel} that shows all the mechs in the garage with a given {@link ChassiClass}.
    * 
    * @author Emily Björk
    */
   class GarageListModel extends AbstractListModel<Loadout>{
      private static final long serialVersionUID = 8864655566407601639L;
      private final ChassiClass chassiClass;

      public GarageListModel(ChassiClass aChassiClass){
         chassiClass = aChassiClass;
      }

      @Override
      public Loadout getElementAt(int i){
         for(Loadout mech : garage.getMechs()){
            if( mech.getChassi().getChassiClass() == chassiClass ){
               if( i == 0 )
                  return mech;
               i--;
            }
         }
         return null;
      }

      @Override
      public int getSize(){
         int ans = 0;
         for(Loadout mech : garage.getMechs()){
            if( mech.getChassi().getChassiClass() == chassiClass ){
               ans++;
            }
         }
         return ans;
      }

   }

   private static final long serialVersionUID = -2541363446737406747L;
   private final MechGarage  garage;
   private final MessageXBar xBar;

   public GaragePanel(MechGarage aGarage, MessageXBar aXBar){
      garage = aGarage;
      xBar = aXBar;
      xBar.attach(this);

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      add(new JList<>(new GarageListModel(ChassiClass.LIGHT)));
      add(new JList<>(new GarageListModel(ChassiClass.MEDIUM)));
      add(new JList<>(new GarageListModel(ChassiClass.HEAVY)));
      add(new JList<>(new GarageListModel(ChassiClass.ASSAULT)));

   }

   @Override
   public void receive(Message aMsg){

   }

}
