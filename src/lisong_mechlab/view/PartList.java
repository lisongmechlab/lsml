package lisong_mechlab.view;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.MessageXBar.Message;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.LoadoutPart;

public class PartList extends JList<String>{
   private static final long serialVersionUID = 5995694414450060827L;
   private final LoadoutPart part;

   class Model extends AbstractListModel<String> implements MessageXBar.Reader{
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
            if( part.canAddItem(anItem) ){
               part.addItem(anItem);
               return true;
            }
            return false;
         }
         else if( it.startsWith(HEATSINKS_STRING) && anItem instanceof HeatSink && part.getNumEngineHeatsinks() < part.getNumEngineHeatsinksMax() ){
            if( part.canAddItem(anItem) ){
               part.addItem(anItem);
               return true;
            }
            else
               return false;
         }
         else{
            try{
               if( aShouldReplace ){
                  Item rem = ItemDB.lookup(it);
                  part.removeItem(rem);
               }
               part.addItem(anItem);
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
         int total_slots = part.getInternalPart().getNumCriticalslots();
         int hs_slots = part.getNumEngineHeatsinksMax();
         List<String> strings = new ArrayList<>(total_slots);
         for(int i = 0; i < part.getItems().size(); ++i){
            final Item item = part.getItems().get(i);
            if( hs_slots > 0 && item instanceof HeatSink ){
               hs_slots--;
               continue;
            }

            strings.add(part.getItemDisplayName(i));
            int spacers_left = part.getItemCriticalSlots(i) - 1;
            while( spacers_left > 0 ){
               if( spacers_left == 1 && item instanceof Engine && ((Engine)item).getNumHeatsinkSlots() > 0 )
                  strings.add(HEATSINKS_STRING + part.getNumEngineHeatsinks() + " / " + ((Engine)item).getNumHeatsinkSlots());
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
         return part.getInternalPart().getNumCriticalslots();
      }

      @Override
      public void receive(Message aMsg){
         // TODO be a bit more selective when to update
         fireContentsChanged(this, 0, part.getInternalPart().getNumCriticalslots());
      }
   }

   PartList(LoadoutPart aPartConf, MessageXBar anXBar){
      part = aPartConf;
      setModel(new Model(anXBar));
      setDragEnabled(true);
      setDropMode(DropMode.ON);
      setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      setTransferHandler(new ItemTransferHandler());

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
                  part.removeItem(item);
               }
            }
         }
      });
   }

   List<Item> getSelectedItems(){
      List<Item> items = new ArrayList<Item>();
      int[] idxs = getSelectedIndices();
      List<Integer> removed = new ArrayList<>();
      for(int i : idxs){
         while( getModel().getElementAt(i) == Model.MULTISLOT ){
            i--;
         }
         if( getModel().getElementAt(i).startsWith(Model.HEATSINKS_STRING) ){
            if( part.getNumEngineHeatsinks() > 0 ){
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

   LoadoutPart getPart(){
      return part;
   }
}
