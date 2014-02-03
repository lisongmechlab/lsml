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
package lisong_mechlab.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.RemoveItemOperation;
import lisong_mechlab.util.Pair;
import lisong_mechlab.view.mechlab.ItemLabel;
import lisong_mechlab.view.mechlab.LoadoutFrame;
import lisong_mechlab.view.mechlab.PartList;
import lisong_mechlab.view.mechlab.equipment.GarageTree;
import lisong_mechlab.view.render.ItemRenderer;

public class ItemTransferHandler extends TransferHandler{
   private static final long  serialVersionUID = -8109855943478269304L;
   private static LoadoutPart sourcePart       = null;

   @Override
   public int getSourceActions(JComponent aComponent){
      return TransferHandler.COPY_OR_MOVE;
   }

   @Override
   protected Transferable createTransferable(JComponent aComponent){
      assert (SwingUtilities.isEventDispatchThread());
      if( aComponent instanceof PartList ){
         PartList partList = (PartList)aComponent;
         synchronized( partList ){

            List<Pair<Item, Integer>> sourceItems = partList.getSelectedItems();
            sourcePart = partList.getPart();

            if( sourceItems.size() < 1 || sourcePart == null )
               return null;

            Container f = aComponent;
            while( !(f instanceof LoadoutFrame) ){
               f = f.getParent();
            }

            LoadoutFrame frame = (LoadoutFrame)f;
            StringBuffer buff = new StringBuffer();
            for(Pair<Item, Integer> it : sourceItems){
               buff.append(it.first.getName()).append('\n');
               frame.getOpStack().pushAndApply(new RemoveItemOperation(ProgramInit.lsml().xBar, sourcePart, it.first));
            }

            Point mouse = partList.getMousePosition();
            mouse.y -= partList.getFixedCellHeight() * sourceItems.get(0).second;
            setDragImage(ItemRenderer.render(sourceItems.get(0).first, sourcePart.getLoadout().getUpgrades()));
            setDragImageOffset(mouse);
            return new StringSelection(buff.toString());
         }
      }
      else if( aComponent instanceof GarageTree ){
         sourcePart = null;
         GarageTree equipmentPane = (GarageTree)aComponent;

         if( equipmentPane.getSelectionPath() == null )
            return null;

         Object dragged = equipmentPane.getSelectionPath().getLastPathComponent();
         Item item = null;
         if( dragged instanceof String ){
            item = ItemDB.lookup((String)dragged);
         }
         else if( dragged instanceof Item ){
            item = (Item)dragged;
         }
         else{
            return null;
         }
         Loadout loadout = ProgramInit.lsml().mechLabPane.getCurrentLoadout();
         setPreview(item, loadout);
         return new StringSelection(item.getName());
      }
      else if( aComponent instanceof ItemLabel ){
         Loadout loadout = ProgramInit.lsml().mechLabPane.getCurrentLoadout();
         Item item = ((ItemLabel)aComponent).getItem();
         setPreview(item, loadout);
         return new StringSelection(item.getName());
      }
      return null;
   }

   private void setPreview(Item anItem, Loadout aLoadout){
      Image preview = ItemRenderer.render(anItem, aLoadout != null ? aLoadout.getUpgrades() : null);
      setDragImage(preview);
      Point mouse = new Point(getDragImage().getWidth(null) / 2, ItemRenderer.ITEM_BASE_HEIGHT / 2);
      setDragImageOffset(mouse);
   }

   @Override
   protected void exportDone(JComponent c, Transferable t, int action){
      // NO-OP
      // The items are removed during the import, otherwise the drop
      // may fail because of loadout tonnage limits etc.
   }

   @Override
   public boolean canImport(TransferHandler.TransferSupport aInfo){
      Component component = aInfo.getComponent();
      if( component instanceof PartList ){
         List<Item> items = parseItems(aInfo);
         if( null == items )
            return false;

         LoadoutPart part = ((PartList)component).getPart();
         for(Item item : items){
            if( !part.canAddItem(item) )
               return false;
         }
         return true;
      }
      return parseItems(aInfo) != null;
   }

   /**
    * Perform the actual import. This only supports drag and drop.
    */
   @Override
   public boolean importData(TransferHandler.TransferSupport info){
      if( !info.isDrop() ){
         return false;
      }

      if( null != sourcePart && info.getDropAction() != COPY ){
         sourcePart = null;
      }

      Component component = info.getComponent();
      if( component instanceof PartList ){
         PartList model = (PartList)component;
         int dropIndex = ((JList.DropLocation)info.getDropLocation()).getIndex();
         try{
            boolean first = true;
            for(Item item : parseItems(info)){
               model.putElement(item, dropIndex, first);
               dropIndex++;
               first = false;
            }
         }
         catch( Exception e ){
            return false;
         }
      }
      // Allow the user to drop the item to get it removed
      return true;
   }

   private List<Item> parseItems(TransferHandler.TransferSupport aInfo){
      if( !aInfo.isDataFlavorSupported(DataFlavor.stringFlavor) ){
         return null;
      }
      List<Item> items = new ArrayList<>();
      try{
         for(String itemName : ((String)aInfo.getTransferable().getTransferData(DataFlavor.stringFlavor)).split("\n")){
            items.add(ItemDB.lookup(itemName));
         }
      }
      catch( Exception e ){
         return null;
      }
      return items;
   }
}
