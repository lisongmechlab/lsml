package lisong_mechlab.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.MessageXBar.Message;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.LoadoutPart;

public class PartList extends JList<String>{
   private static final long serialVersionUID = 5995694414450060827L;
   private final LoadoutPart partConf;

   private class Model extends AbstractListModel<String> implements MessageXBar.Reader{
      private static final String HEATSINKS_STRING = "Heatsinks: ";
      private static final String EMPTY            = "empty";
      private static final String MULTISLOT        = "---";
      private static final long   serialVersionUID = 2438473891359444131L;

      public Model(MessageXBar aXBar){
         aXBar.attach(this);
      }

      public boolean putElement(Item anItem, int anIndex, boolean aShouldReplace){
         String it = null;
         while( (it = (String)getElementAt(anIndex)).equals(MULTISLOT) ){
            anIndex--;
         }
         if( it.equals(EMPTY) ){
            if( partConf.canAddItem(anItem) ){
               partConf.addItem(anItem);
               return true;
            }
            return false;
         }
         else if(it.startsWith(HEATSINKS_STRING) && anItem instanceof HeatSink && partConf.getNumEngineHeatsinks() < partConf.getNumEngineHeatsinksMax()){
            if( partConf.canAddItem(anItem) ){
               partConf.addItem(anItem);
               return true;
            }
            else
               return false;
         }
         else{
            try{
               if( aShouldReplace ){
                  Item rem = ItemDB.lookup(it);
                  partConf.removeItem(rem);
               }
               partConf.addItem(anItem);
               return true;
            }
            catch( Exception e ){
               return false;
            }
         }

         // TODO Handle Ferro Fibrous
         // TODO Handle Endo Steel
      }

      @Override
      public String getElementAt(int arg0){
         int total_slots = partConf.getInternalPart().getNumCriticalslots();
         int hs_slots = partConf.getNumEngineHeatsinksMax();
         List<String> strings = new ArrayList<>(total_slots);
         for(int i = 0; i < partConf.getItems().size(); ++i){
            final Item item = partConf.getItems().get(i);
            if( hs_slots > 0 && item instanceof HeatSink ){
               hs_slots--;
               continue;
            }

            strings.add(partConf.getItemDisplayName(i));
            int spacers_left = partConf.getItemCriticalSlots(i) - 1;
            while( spacers_left > 0 ){
               if( spacers_left == 1 && item instanceof Engine && ((Engine)item).getNumHeatsinkSlots() > 0 )
                  strings.add(HEATSINKS_STRING + partConf.getNumEngineHeatsinks() + " / " + ((Engine)item).getNumHeatsinkSlots());
               else
                  strings.add(MULTISLOT);
               spacers_left--;
            }
         }
         while( strings.size() < total_slots )
            strings.add(EMPTY);
         return strings.get(arg0);

         /*
          * int critslot = 0; int item = 0; Item lastItem = null; int engHsLeft = partConf.getNumEngineHeatsinksMax();
          * List<Item> items = new ArrayList<>(partConf.getItems()); for(int i = 0; i < arg0; ++i){ if( 0 == critslot ){
          * while( engHsLeft > 0 && item < items.size() && items.get(item) instanceof HeatSink ){ engHsLeft--; item++;
          * lastItem = null; } if( item < items.size() ){ lastItem = items.get(item); critslot =
          * partConf.getItemCriticalSlots(item);// lastItem.getNumCriticalSlots(); item++; } else{ break; } }
          * critslot--; } while( engHsLeft > 0 && lastItem instanceof HeatSink ){ engHsLeft--; item++; lastItem = null;
          * } // Case 1: Empty slot if( item >= items.size() && 0 == critslot ){ return EMPTY; } // Case 1.5: Engine
          * with space for heatsinks else if( lastItem instanceof Engine && ((Engine)lastItem).getNumHeatsinkSlots() > 0
          * && critslot == 1 ){ return "Heatsinks: " + partConf.getNumEngineHeatsinks() + " / " +
          * ((Engine)lastItem).getNumHeatsinkSlots(); } // Case 2: Part of a multi-slot item else if( critslot != 0 ){
          * return MULTISLOT; } // Case 3: Name of a part else{ // return partConf.getItems().get(item).getName();
          * return partConf.getItemDisplayName(item); } // TODO: Case 4 Dynamic Armor // TODO: Case 5 Internal Structure
          */
      }

      @Override
      public int getSize(){
         return partConf.getInternalPart().getNumCriticalslots();
      }

      @Override
      public void receive(Message aMsg){
         // TODO be a bit more selective when to update
         fireContentsChanged(this, 0, partConf.getInternalPart().getNumCriticalslots());
      }
   }

   private static class Transfer extends TransferHandler{
      private static final long serialVersionUID = -8109855943478269304L;

      @Override
      public boolean canImport(TransferHandler.TransferSupport aInfo){
         if( aInfo.isDataFlavorSupported(DataFlavor.stringFlavor) ){
            try{
               for(String itemName : ((String)aInfo.getTransferable().getTransferData(DataFlavor.stringFlavor)).split("\n")){
                  Item item = ItemDB.lookup(itemName);

                  if( !ourPart.canAddItem(item) ){
                     return false;
                  }
               }
            }
            catch( Exception e ){
               return false;
            }
            return true;
         }
         return false;
      }

      @Override
      public int getSourceActions(JComponent aComponent){
         return TransferHandler.COPY_OR_MOVE;
      }

      final private LoadoutPart ourPart;

      Transfer(LoadoutPart aPartConf){
         ourPart = aPartConf;
      }

      private static LoadoutPart sourcePart  = null;
      private static List<Item>  sourceItems = null;

      @Override
      protected Transferable createTransferable(JComponent aComponent){
         sourceItems = ((PartList)aComponent).getSelectedItems();
         sourcePart = ((PartList)aComponent).partConf;

         StringBuffer buff = new StringBuffer();
         for(Item it : sourceItems){
            buff.append(it.getName()).append('\n');
         }
         return new StringSelection(buff.toString());
      }

      /**
       * Perform the actual import. This only supports drag and drop.
       */
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

         Model model = (Model)((PartList)info.getComponent()).getModel();
         int dropIndex = ((JList.DropLocation)info.getDropLocation()).getIndex();
         try{
            String rawData = (String)info.getTransferable().getTransferData(DataFlavor.stringFlavor);
            String[] itemNames = rawData.split("\n");
            boolean first = true;
            for(String itemName : itemNames){
               model.putElement(ItemDB.lookup(itemName), dropIndex, first);
               dropIndex++;
               first = false;
            }
         }
         catch( Exception e ){
            return false;
         }
         return true;
      }
   }

   PartList(LoadoutPart aPartConf, MessageXBar anXBar){
      partConf = aPartConf;
      setModel(new Model(anXBar));
      setDragEnabled(true);
      setDropMode(DropMode.ON);
      setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      setTransferHandler(new Transfer(partConf));

      addKeyListener(new KeyListener(){

         @Override
         public void keyTyped(KeyEvent aArg0){
         }

         @Override
         public void keyReleased(KeyEvent aArg0){
         }

         @Override
         public void keyPressed(KeyEvent aArg0){
            if( aArg0.getKeyCode() == KeyEvent.VK_DELETE ){
               for(Item item : getSelectedItems()){
                  partConf.removeItem(item);
               }
            }
         }
      });
   }

   private List<Item> getSelectedItems(){
      List<Item> items = new ArrayList<Item>();
      int[] idxs = getSelectedIndices();
      List<Integer> removed = new ArrayList<>();
      for(int i : idxs){
         while( getModel().getElementAt(i) == Model.MULTISLOT ){
            i--;
         }
         if(getModel().getElementAt(i).startsWith(Model.HEATSINKS_STRING)){
            if(partConf.getNumEngineHeatsinks() > 0 ){
               items.add(ItemDB.lookup("STD HEAT SINK"));
               items.add(ItemDB.lookup("DOUBLE HEAT SINK"));
            }
         }
         else if( !removed.contains(i) ){
            try{
               Item item = ItemDB.lookup(getModel().getElementAt(i));
               items.add(item);
            }
            catch( IllegalArgumentException e ){
               continue; // Not valid entry.
            }
         }
         removed.add(i);
      }
      /*
       * for(String obj : getSelectedValuesList()){ try{ Item item = ItemDB.lookup(obj); items.add(item); } catch(
       * IllegalArgumentException e ){ continue; // Not valid entry. } }
       */
      return items;
   }
}
