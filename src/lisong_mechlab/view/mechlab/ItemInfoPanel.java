/*
 * @formatter:off
 * Li Song Mech Lab - A 'mech building tool for PGI's MechWarrior: Online.
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
package lisong_mechlab.view.mechlab;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;

/**
 * This class implements a panel that will show information about an item.
 * 
 * @author Li Song
 */
public class ItemInfoPanel extends JPanel{
   private static final long serialVersionUID = -1180217243714551398L;
   private final JLabel      name             = new JLabel();
   private final JLabel      slots            = new JLabel();
   private final JLabel      mass             = new JLabel();
   private final JTextArea   description      = new JTextArea();

   // Weapon info
   private final JLabel      damage           = new JLabel();
   private final JLabel      heat             = new JLabel();
   private final JLabel      cycletime        = new JLabel();

   // Ammo info
   private final JLabel      ammoperton       = new JLabel();

   public ItemInfoPanel(){
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setBorder(new TitledBorder("Description"));
      add(Box.createHorizontalGlue());

      description.setColumns(30);
      description.setRows(5);
      description.setWrapStyleWord(true);
      description.setEditable(false);
      description.setFocusable(false);
      description.setLineWrap(true);
      description.setMinimumSize(new Dimension(300, 50));
      description.setMaximumSize(new Dimension(2000, 50));
      description.setAlignmentX(LEFT_ALIGNMENT);
      description.setFont(name.getFont());
      description.setBackground(name.getBackground());

      add(name);
      JPanel basicInfo = new JPanel();
      basicInfo.setLayout(new BoxLayout(basicInfo, BoxLayout.X_AXIS));
      basicInfo.setAlignmentX(LEFT_ALIGNMENT);
      basicInfo.add(slots);
      basicInfo.add(Box.createHorizontalGlue());
      basicInfo.add(mass);
      add(basicInfo);
      add(damage);
      add(heat);
      add(cycletime);
      add(ammoperton);
      add(description);

      showItem(null);
   }

   public void showItem(Item anItem){
      if( anItem == null ){
         name.setText("Name: N/A");
         slots.setText("Slots: N/A");
         mass.setText("Tons: N/A");
         description.setText("Description:\nN/A");

         damage.setVisible(false);
         heat.setVisible(false);
         cycletime.setVisible(false);
         ammoperton.setVisible(false);
      }
      else{
         name.setText("Name: " + anItem.getName());
         slots.setText("Slots: " + anItem.getNumCriticalSlots(null));
         mass.setText("Tons: " + anItem.getMass(null));
         description.setText("Description:\n" + anItem.getDescription());

         if( anItem instanceof Weapon ){
            Weapon weapon = (Weapon)anItem;

            damage.setVisible(true);
            heat.setVisible(true);
            cycletime.setVisible(true);

            damage.setText("Damage: " + weapon.getDamagePerShot());
            heat.setText("Heat: " + weapon.getHeat());
            cycletime.setText("Cooldown: " + weapon.getSecondsPerShot());

            if( weapon instanceof AmmoWeapon ){
               AmmoWeapon ammoWeapon = (AmmoWeapon)weapon;

               ammoperton.setVisible(true);
               ammoperton.setText("Ammo per ton: " + ammoWeapon.getAmmoType(null).getShotsPerTon());
            }
            else{
               ammoperton.setVisible(false);
            }
         }
         else{
            damage.setVisible(false);
            heat.setVisible(false);
            cycletime.setVisible(false);

            if( anItem instanceof Ammunition ){
               Ammunition ammunition = (Ammunition)anItem;
               ammoperton.setText("Ammo per ton: " + ammunition.getShotsPerTon());
               ammoperton.setVisible(true);
            }
            else{
               ammoperton.setVisible(false);
            }

         }
      }
   }
}
