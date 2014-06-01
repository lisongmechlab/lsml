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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.MessageXBar.Reader;
import lisong_mechlab.view.mechlab.ItemInfoPanel;
import lisong_mechlab.view.mechlab.ItemLabel;
import lisong_mechlab.view.mechlab.LoadoutDesktop;
import lisong_mechlab.view.mechlab.LoadoutFrame;
import lisong_mechlab.view.render.ModifiedFlowLayout;
import lisong_mechlab.view.render.ScrollablePanel;

/**
 * This class renders the equipment panel that contains all the equippable items on the selected loadout.
 * 
 * @author Li Song
 */
public class EquipmentPanel extends JPanel implements Reader, InternalFrameListener{
   private static final long     serialVersionUID = -8126726006921797207L;
   private final ItemInfoPanel   infoPanel        = new ItemInfoPanel();
   private final List<ItemLabel> itemLabels       = new ArrayList<>();
   private final JPanel          energyItems      = new JPanel(new ModifiedFlowLayout());
   private final JPanel          ballisticItems   = new JPanel(new ModifiedFlowLayout());
   private final JPanel          missileItems     = new JPanel(new ModifiedFlowLayout());
   private LoadoutBase<?>     currentLoadout;

   public EquipmentPanel(LoadoutDesktop aDesktop, MessageXBar aXBar){
      aXBar.attach(this);
      aDesktop.addInternalFrameListener(this);

      setLayout(new BorderLayout());
      List<Item> items = ItemDB.lookup(Item.class);
      Collections.sort(items);

      JPanel itemFlowPanel = new ScrollablePanel();
      energyItems.setBorder(BorderFactory.createTitledBorder("Energy"));
      ballisticItems.setBorder(BorderFactory.createTitledBorder("Ballistic"));
      missileItems.setBorder(BorderFactory.createTitledBorder("Missile"));
      JPanel miscItems = new JPanel(new ModifiedFlowLayout());
      miscItems.setBorder(BorderFactory.createTitledBorder("Misc"));
      JPanel engineItems = new JPanel(new ModifiedFlowLayout());
      engineItems.setBorder(BorderFactory.createTitledBorder("Engine - STD"));
      JPanel engineXlItems = new JPanel(new ModifiedFlowLayout());
      engineXlItems.setBorder(BorderFactory.createTitledBorder("Engine - XL"));
      for(Item item : items){
         ItemLabel itemLabel = new ItemLabel(item, this, infoPanel, aXBar);
         if( item instanceof Ammunition ){
            Ammunition ammunition = (Ammunition)item;
            switch( ammunition.getWeaponHardpointType() ){
               case BALLISTIC:
                  ballisticItems.add(itemLabel);
                  break;
               case ENERGY:
                  energyItems.add(itemLabel);
                  break;
               case MISSILE:
                  missileItems.add(itemLabel);
                  break;
               case AMS: // Fall-through
               case ECM: // Fall-through
               case NONE: // Fall-through
               default:
                  miscItems.add(itemLabel);
                  break;
            }
         }
         else if( item instanceof EnergyWeapon ){
            energyItems.add(itemLabel);
         }
         else if( item instanceof BallisticWeapon ){
            ballisticItems.add(itemLabel);
         }
         else if( item instanceof MissileWeapon ){
            missileItems.add(itemLabel);
         }
         else if( item instanceof Engine ){
            if( ((Engine)item).getType() == EngineType.XL ){
               engineXlItems.add(itemLabel);
            }
            else{
               engineItems.add(itemLabel);
            }
         }
         else{
            miscItems.add(itemLabel);
         }
         itemLabels.add(itemLabel);
      }

      itemFlowPanel.add(energyItems);
      itemFlowPanel.add(ballisticItems);
      itemFlowPanel.add(missileItems);
      itemFlowPanel.add(miscItems);
      itemFlowPanel.add(engineItems);
      itemFlowPanel.add(engineXlItems);
      itemFlowPanel.setLayout(new BoxLayout(itemFlowPanel, BoxLayout.PAGE_AXIS));
      JScrollPane itemFlowScrollPanel = new JScrollPane(itemFlowPanel);
      itemFlowScrollPanel.setAlignmentX(LEFT_ALIGNMENT);

      infoPanel.setAlignmentX(LEFT_ALIGNMENT);
      add(itemFlowScrollPanel, BorderLayout.CENTER);
      add(infoPanel, BorderLayout.SOUTH);
      changeLoadout(null);
   }

   @Override
   public void internalFrameActivated(InternalFrameEvent aArg0){
      LoadoutFrame frame = (LoadoutFrame)aArg0.getInternalFrame();
      changeLoadout(frame.getLoadout());
   }

   @Override
   public void internalFrameDeactivated(InternalFrameEvent aE){
      changeLoadout(null);
   }

   @Override
   public void internalFrameIconified(InternalFrameEvent aE){
      changeLoadout(null);
   }

   @Override
   public void internalFrameDeiconified(InternalFrameEvent aArg0){
      LoadoutFrame frame = (LoadoutFrame)aArg0.getInternalFrame();
      changeLoadout(frame.getLoadout());
   }

   private void changeLoadout(LoadoutBase<?> aLoadout){
      currentLoadout = aLoadout;
      for(ItemLabel itemLabel : itemLabels){
         itemLabel.updateVisibility(aLoadout);
      }
      if( aLoadout != null ){
         energyItems.setVisible(aLoadout.getHardpointsCount(HardPointType.ENERGY) > 0);
         missileItems.setVisible(aLoadout.getHardpointsCount(HardPointType.MISSILE) > 0);
         ballisticItems.setVisible(aLoadout.getHardpointsCount(HardPointType.BALLISTIC) > 0);
      }
      else{
         energyItems.setVisible(true);
         missileItems.setVisible(true);
         ballisticItems.setVisible(true);
      }
   }

   @Override
   public void receive(Message aMsg){
      if( currentLoadout == null || aMsg.isForMe(currentLoadout) ){
         changeLoadout(currentLoadout);
      }
   }

   @Override
   public void internalFrameClosed(InternalFrameEvent aArg0){/* NO-OP */}

   @Override
   public void internalFrameClosing(InternalFrameEvent aArg0){/* NO-OP */}

   @Override
   public void internalFrameOpened(InternalFrameEvent aArg0){/* NO-OP */}

   public LoadoutBase<?> getCurrentLoadout(){
      return currentLoadout;
   }
}
