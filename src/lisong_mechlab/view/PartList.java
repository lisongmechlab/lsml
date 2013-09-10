package lisong_mechlab.view;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import lisong_mechlab.Pair;
import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.MessageXBar.Message;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.DynamicSlotDistributor;
import lisong_mechlab.model.loadout.LoadoutPart;

public class PartList extends JList<Item>{
   private static final long            serialVersionUID = 5995694414450060827L;

   private final LoadoutPart            part;

   private final DynamicSlotDistributor slotDistributor;

   private enum ListEntryType{
      Empty, MultiSlot, Item, EngineHeatSink, LastSlot
   }

   private class Renderer extends JLabel implements ListCellRenderer<Object>{

      private static final long serialVersionUID = -8157859670319431469L;

      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){
         Pair<ListEntryType, Item> pair = ((Model)getModel()).getElementTypeAt(index);
         setBorder(BorderFactory.createEmptyBorder());
         switch( pair.first ){
            case Empty:{
               if( isDynArmor(index) ){
                  StyleManager.styleDynamicEntry(this);
                  setText(Model.DYN_ARMOR);
               }
               else if( isDynStructure(index) ){
                  StyleManager.styleDynamicEntry(this);
                  setText(Model.DYN_STRUCT);
               }
               else{
                  StyleManager.styleItem(this);
                  setText(Model.EMPTY);
               }
               break;
            }
            case Item:{
               setText(part.getItemDisplayName(pair.second));
               if( part.getItemCriticalSlots(pair.second) == 1 ){
                  StyleManager.styleItem(this, pair.second);
               }
               else{
                  StyleManager.styleItemTop(this, pair.second);
               }
               break;
            }
            case LastSlot:{
               setText(Model.MULTISLOT);
               StyleManager.styleItemBottom(this, pair.second);
               break;
            }
            case MultiSlot:{
               setText(Model.MULTISLOT);
               StyleManager.styleItemMiddle(this, pair.second);
               break;
            }
            case EngineHeatSink:{
               setText(Model.HEATSINKS_STRING + part.getNumEngineHeatsinks() + "/" + part.getNumEngineHeatsinksMax());
               StyleManager.styleItemBottom(this, pair.second);
               break;
            }
         }

         if( isSelected && pair.first != ListEntryType.Empty ){
            setForeground(getForeground().brighter());
            setBackground(getBackground().brighter());
         }

         return this;
      }

      private boolean isDynStructure(int aIndex){
         int freeSlotOrdinal = aIndex - part.getNumCriticalSlotsUsed() - slotDistributor.getDynamicArmorSlots(part);
         int dynStructNum = slotDistributor.getDynamicStructureSlots(part);
         return freeSlotOrdinal >= 0 && freeSlotOrdinal < dynStructNum;
      }

      private boolean isDynArmor(int aIndex){
         int freeSlotOrdinal = aIndex - part.getNumCriticalSlotsUsed();
         int dynArmorNum = slotDistributor.getDynamicArmorSlots(part);
         return freeSlotOrdinal < dynArmorNum;
      }
   }

   private class Model extends AbstractListModel<Item> implements MessageXBar.Reader{
      private static final String HEATSINKS_STRING = "HEATSINKS: ";
      private static final String EMPTY            = "EMPTY";
      private static final String MULTISLOT        = "---";
      private static final String DYN_ARMOR        = "DYNAMIC ARMOR";
      private static final String DYN_STRUCT       = "DYNAMIC STRUCTURE";
      private static final long   serialVersionUID = 2438473891359444131L;

      Model(MessageXBar aXBar){
         aXBar.attach(this);
      }

      boolean putElement(Item anItem, int anIndex, boolean aShouldReplace){
         Pair<ListEntryType, Item> target = getElementTypeAt(anIndex);
         switch( target.first ){
            case EngineHeatSink:{
               if( anItem instanceof HeatSink && part.canAddItem(anItem) ){
                  part.addItem(anItem);
                  return true;
               }
               return false;
            }
            case LastSlot: // Fall through
            case Item: // Fall through
            case MultiSlot:{
               // Drop on existing component, try to replace it if we should, otherwise just add it to the component.
               if( aShouldReplace && !(anItem instanceof HeatSink && target.second instanceof Engine) ){
                  part.removeItem(target.second);
               }
               // Fall through
            }
            case Empty:{
               if( part.canAddItem(anItem) ){
                  part.addItem(anItem);
                  return true;
               }
               return false;
            }
            default:
               break;
         }
         return false;
      }

      Pair<ListEntryType, Item> getElementTypeAt(int arg0){
         List<Item> items = new ArrayList<>(part.getItems());
         int numEngineHs = part.getNumEngineHeatsinks();
         boolean foundhs = true;
         while( numEngineHs > 0 && !items.isEmpty() && foundhs ){
            foundhs = false;
            for(Item item : items){
               if( item instanceof HeatSink ){
                  items.remove(item);
                  numEngineHs--;
                  foundhs = true;
                  break;
               }
            }
         }

         if( items.isEmpty() )
            return new Pair<ListEntryType, Item>(ListEntryType.Empty, null);

         int itemsIdx = 0;
         Item item = items.get(itemsIdx);
         itemsIdx++;

         int spaceLeft = part.getItemCriticalSlots(item);
         for(int slot = 0; slot < arg0; ++slot){
            spaceLeft--;
            if( spaceLeft == 0 ){
               if( itemsIdx < items.size() ){
                  item = items.get(itemsIdx);
                  itemsIdx++;
                  spaceLeft = part.getItemCriticalSlots(item);
               }
               else
                  return new Pair<ListEntryType, Item>(ListEntryType.Empty, null);
            }
         }
         if( spaceLeft == 1 && part.getItemCriticalSlots(item) > 1 ){
            if( item instanceof Engine )
               return new Pair<ListEntryType, Item>(ListEntryType.EngineHeatSink, item);
            return new Pair<ListEntryType, Item>(ListEntryType.LastSlot, item);
         }
         if( spaceLeft == part.getItemCriticalSlots(item) )
            return new Pair<ListEntryType, Item>(ListEntryType.Item, item);
         if( spaceLeft > 0 )
            return new Pair<ListEntryType, Item>(ListEntryType.MultiSlot, item);
         return new Pair<ListEntryType, Item>(ListEntryType.Empty, null);
      }

      @Override
      public Item getElementAt(int arg0){
         Pair<ListEntryType, Item> target = getElementTypeAt(arg0);
         if( target.first == ListEntryType.Item )
            return getElementTypeAt(arg0).second;
         return null;
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

   PartList(LoadoutPart aPartConf, MessageXBar anXBar, DynamicSlotDistributor aSlotDistributor){
      slotDistributor = aSlotDistributor;
      part = aPartConf;
      setModel(new Model(anXBar));
      setDragEnabled(true);
      setDropMode(DropMode.ON);
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      setTransferHandler(new ItemTransferHandler());
      setCellRenderer(new Renderer());

      addFocusListener(new FocusAdapter(){
         @Override
         public void focusLost(FocusEvent e){
            clearSelection();
         }
      });

      addKeyListener(new KeyAdapter(){
         @Override
         public void keyPressed(KeyEvent aArg0){
            if( aArg0.getKeyCode() == KeyEvent.VK_DELETE ){
               for(Item item : getSelectedItems()){
                  part.removeItem(item);
               }
            }
         }
      });

      addMouseListener(new MouseAdapter(){
         @Override
         public void mouseClicked(MouseEvent e){
            if( SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2 ){
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
      for(int i : idxs){
         Pair<ListEntryType, Item> pair = ((Model)getModel()).getElementTypeAt(i);
         switch( pair.first ){
            case Empty:
               break;
            case EngineHeatSink:
               if( part.getNumEngineHeatsinks() > 0 ){
                  items.add(ItemDB.SHS);
                  items.add(ItemDB.DHS);
               }
               break;
            case Item:
            case LastSlot:
            case MultiSlot:
               items.add(pair.second);
               break;
            default:
               break;

         }
      }
      return items;
   }

   LoadoutPart getPart(){
      return part;
   }

   public void putElement(Item aItem, int aDropIndex, boolean aFirst){
      ((Model)getModel()).putElement(aItem, aDropIndex, aFirst);
   }
}
