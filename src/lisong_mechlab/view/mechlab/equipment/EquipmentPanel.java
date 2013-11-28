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
package lisong_mechlab.view.mechlab.equipment;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.MessageXBar.Reader;
import lisong_mechlab.view.mechlab.ItemInfoPanel;
import lisong_mechlab.view.mechlab.ItemLabel;
import lisong_mechlab.view.mechlab.LoadoutDesktop;
import lisong_mechlab.view.mechlab.LoadoutFrame;
import lisong_mechlab.view.render.ModifiedFlowLayout;

/**
 * This class renders the equipment panel that contains all the equippable items on the selected loadout.
 * 
 * @author Li Song
 */
public class EquipmentPanel extends JPanel implements Reader, InternalFrameListener{
   private static final long     serialVersionUID = -8126726006921797207L;
   private final ItemInfoPanel   infoPanel        = new ItemInfoPanel();
   private final List<ItemLabel> itemLabels       = new ArrayList<>();
   private Loadout               currentLoadout;

   class ScrollablePanel extends JPanel implements Scrollable{
      private static final long serialVersionUID = -5231044372862875923L;

      @Override
      public Dimension getPreferredScrollableViewportSize(){
         return null;
      }

      @Override
      public int getScrollableBlockIncrement(Rectangle aVisibleRect, int aOrientation, int aDirection){
         return 150; // Arbitrary number, works well enough.
      }

      @Override
      public boolean getScrollableTracksViewportHeight(){
         return false;
      }

      @Override
      public boolean getScrollableTracksViewportWidth(){
         return true;
      }

      @Override
      public int getScrollableUnitIncrement(Rectangle aVisibleRect, int aOrientation, int aDirection){
         return 50; // Arbitrary number, works well enough.
      }
   }

   public EquipmentPanel(LoadoutDesktop aDesktop, MessageXBar aXBar){
      aXBar.attach(this);
      aDesktop.addInternalFrameListener(this);

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      JPanel itemFlowPanel = new ScrollablePanel();
      itemFlowPanel.setLayout(new ModifiedFlowLayout());
      List<Item> items = ItemDB.lookup(Item.class);
      Collections.sort(items);
      for(Item item : items){
         ItemLabel itemLabel = new ItemLabel(item, this, infoPanel);
         itemFlowPanel.add(itemLabel);
         itemLabels.add(itemLabel);
      }

      JScrollPane itemFlowScrollPanel = new JScrollPane(itemFlowPanel);
      itemFlowScrollPanel.setAlignmentX(LEFT_ALIGNMENT);

      infoPanel.setAlignmentX(LEFT_ALIGNMENT);
      add(itemFlowScrollPanel);
      add(infoPanel);
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

   private void changeLoadout(Loadout aLoadout){
      currentLoadout = aLoadout;
      for(ItemLabel itemLabel : itemLabels){
         itemLabel.updateVisibility(aLoadout);
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

   public Loadout getCurrentLoadout(){
      return currentLoadout;
   }
}
