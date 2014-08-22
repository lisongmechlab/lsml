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
package lisong_mechlab.view.render;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.item.ItemDB;

/**
 * A renderer that can show a preview of {@link OmniPod}s.
 * 
 * @author Li Song
 */
public class OmniPodRenderer implements ListCellRenderer<OmniPod>{

   private final JLabel       active     = new JLabel();

   private final JPanel       panel      = new JPanel();
   private final JPanel       hardpoints = new JPanel();
   private final JLabel       quirks     = new JLabel();
   private final TitledBorder border     = BorderFactory.createTitledBorder("");

   private final JLabel       hpJJ       = new JLabel();

   public OmniPodRenderer(){
      panel.setLayout(new BorderLayout());
      panel.add(hardpoints, BorderLayout.NORTH);
      panel.add(quirks, BorderLayout.CENTER);
      panel.setBorder(border);

      quirks.setHorizontalAlignment(SwingConstants.LEFT);

      StyleManager.styleThinItem(hpJJ, ItemDB.lookup("JUMP JETS - CLASS V"));
   }

   @Override
   public Component getListCellRendererComponent(JList<? extends OmniPod> aList, OmniPod aValue, int aIndex, boolean aIsSelected,
                                                 boolean aCellHasFocus){

      if( aIndex < 0 ){
         // The preview in the box
         active.setText(aValue.toString());
         return active;
      }

      // Render item in list
      border.setTitle(aValue.toString());

      if( !aValue.getHardPoints().isEmpty() || 0 != aValue.getJumpJetsMax()){
         hardpoints.setVisible(true);
         hardpoints.removeAll();
         for(HardPointType hp : HardPointType.values()){
            JLabel label = new JLabel();
            StyleManager.styleHardpointLabel(label, hp, aValue.getHardPoints());
            hardpoints.add(label);
         }

         if( aValue.getJumpJetsMax() > 0 ){
            hpJJ.setVisible(true);
            hpJJ.setText(Integer.toString(aValue.getJumpJetsMax()) + " JJ");
            hardpoints.add(hpJJ);
         }
         else{
            hpJJ.setVisible(false);
         }

         // TODO: Draw pilot modules once we have decided a style for them.
      }
      else{
         hardpoints.setVisible(false);
      }

      quirks.setVisible(true);
      quirks.setText(aValue.getQuirks().describeAsHtml());
      return panel;
   }
}
