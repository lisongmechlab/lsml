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
package lisong_mechlab.view.mechlab;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.loadout.metrics.TopSpeed;
import lisong_mechlab.view.ItemTransferHandler;
import lisong_mechlab.view.mechlab.equipment.EquipmentPanel;
import lisong_mechlab.view.render.StyleManager;

/**
 * This class implements a JLabel to render an item that can be dragged onto a loadout.
 * 
 * @author Emily Björk
 */
public class ItemLabel extends JLabel{
   private static final long serialVersionUID = 1237952620487557121L;
   private final Item        item;

   public ItemLabel(Item anItem, final EquipmentPanel aEquipmentPanel, final ItemInfoPanel aInfoPanel){
      item = anItem;

      StyleManager.styleItem(this, item);
      setToolTipText("<html>" + item.getName() + "<p>" + item.getDescription() + "</html>");

      setTransferHandler(new ItemTransferHandler());
      addMouseListener(new MouseAdapter(){
         @Override
         public void mousePressed(MouseEvent anEvent){
            Loadout loadout = aEquipmentPanel.getCurrentLoadout();

            Component component = anEvent.getComponent();
            if( component instanceof ItemLabel ){
               aInfoPanel.showItem(item, null != loadout ? loadout.getUpgrades() : null, null != loadout ? loadout.getEfficiencies() : null);
            }

            ItemLabel button = (ItemLabel)anEvent.getSource();
            ItemTransferHandler handle = (ItemTransferHandler)button.getTransferHandler();
            handle.exportAsDrag(button, anEvent, TransferHandler.COPY);

            if( SwingUtilities.isLeftMouseButton(anEvent) && anEvent.getClickCount() >= 2 ){
               if( null != loadout )
                  loadout.addItem(item, true);
            }
         }
      });

      updateVisibility(null);
   }

   private void updateText(Loadout aLoadout){
      Upgrades anUpgrades = aLoadout == null ? null : aLoadout.getUpgrades();
      StringBuilder builder = new StringBuilder();
      builder.append("<html>");
      builder.append(item.getShortName(anUpgrades));
      builder.append("<br/><span style=\"font-size:x-small;\">");
      builder.append("Tons: ").append(item.getMass(anUpgrades)).append("<br/>Slots: ").append(item.getNumCriticalSlots(anUpgrades));
      if( item instanceof Engine && aLoadout != null ){
         Engine engine = (Engine)item;
         double speed = TopSpeed.calculate(engine.getRating(), aLoadout.getChassi(), aLoadout.getEfficiencies().getSpeedModifier());
         DecimalFormat decimalFormat = new DecimalFormat("###");
         builder.append("<br/>" + decimalFormat.format(speed) + "kph");
      }
      builder.append("</span></html>");

      setText(builder.toString());
   }

   public Item getItem(){
      return item;
   }

   public void updateVisibility(Loadout aLoadout){
      if( aLoadout != null ){
         updateText(aLoadout);
         if( !item.isEquippableOn(aLoadout) ){
            setVisible(false);
         }
         else{
            if( !aLoadout.isEquippable(item) ){
               StyleManager.colourInvalid(this);
            }
            else{
               StyleManager.styleItem(this, item);
            }

            if( item instanceof Ammunition ){
               Ammunition ammunition = (Ammunition)item;
               if( aLoadout.getChassi().getHardpointsCount(ammunition.getWeaponHardpointType()) < 1 ){
                  setVisible(false);
               }
               else{
                  for(Item it : aLoadout.getAllItems()){
                     if( it instanceof AmmoWeapon ){
                        if( ((AmmoWeapon)it).getAmmoType(aLoadout.getUpgrades()) == ammunition ){
                           setVisible(true);
                           return;
                        }
                     }
                  }
                  setVisible(false);
               }
            }
            else
               setVisible(true);
         }
      }
      else{
         updateText(null);
         StyleManager.styleItem(this, item);
         setVisible(true);
      }
   }
}
