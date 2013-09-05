package lisong_mechlab.view;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.LoadoutPart;

class ItemTransferHandler extends TransferHandler{
   private static final long  serialVersionUID = -8109855943478269304L;

   private static LoadoutPart sourcePart       = null;
   private static List<Item>  sourceItems      = null;

   @Override
   public int getSourceActions(JComponent aComponent){
      return TransferHandler.COPY_OR_MOVE;
   }

   @Override
   protected Transferable createTransferable(JComponent aComponent){
      if( aComponent instanceof PartList ){
         sourceItems = ((PartList)aComponent).getSelectedItems();
         sourcePart = ((PartList)aComponent).getPart();

         StringBuffer buff = new StringBuffer();
         for(Item it : sourceItems){
            buff.append(it.getName()).append('\n');
         }
         return new StringSelection(buff.toString());
      }
      else if( aComponent instanceof EquipmentPane ){
         sourcePart = null;
         return new StringSelection((String)((EquipmentPane)aComponent).getSelectionPath().getLastPathComponent());
      }
      return null;
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
      else{
         return parseItems(aInfo) != null;
      }
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
         for(Item item : sourceItems){
            sourcePart.removeItem(item);
         }
         sourcePart = null;
         sourceItems = null;
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
         return true;
      }
      else{
         // Allow the user to drop the item to get it removed
         return true;
      }
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
