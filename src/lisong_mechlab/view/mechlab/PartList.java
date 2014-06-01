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
package lisong_mechlab.view.mechlab;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
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

import lisong_mechlab.model.DynamicSlotDistributor;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.Message.Type;
import lisong_mechlab.model.loadout.component.OpAddItem;
import lisong_mechlab.model.loadout.component.OpRemoveItem;
import lisong_mechlab.model.metrics.CriticalItemDamage;
import lisong_mechlab.model.metrics.CriticalStrikeProbability;
import lisong_mechlab.model.metrics.ItemEffectiveHP;
import lisong_mechlab.model.metrics.helpers.ComponentDestructionSimulator;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.Pair;
import lisong_mechlab.view.ItemTransferHandler;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.render.StyleManager;

public class PartList extends JList<Item>{
   private static final long                   serialVersionUID = 5995694414450060827L;
   private final ConfiguredComponentBase           component;
   private final DynamicSlotDistributor        slotDistributor;
   private OperationStack                      opStack;

   private final DecimalFormat                 df               = new DecimalFormat("###.#");
   private final DecimalFormat                 df2              = new DecimalFormat("###.##");
   private final ItemEffectiveHP               effectiveHP;
   private final CriticalStrikeProbability     criticalStrikeProbability;
   private final LoadoutBase<?>             loadout;

   private final ComponentDestructionSimulator cds;

   private enum ListEntryType{
      Empty, MultiSlot, Item, EngineHeatSink, LastSlot
   }

   private class Renderer extends JLabel implements ListCellRenderer<Object>{
      private static final long serialVersionUID = -8157859670319431469L;

      void setTooltipForItem(Item aItem){
         if( aItem instanceof Internal ){
            setToolTipText("");
            return;
         }

         StringBuilder sb = new StringBuilder();

         sb.append("<html>");
         sb.append("<b>");
         sb.append(aItem.getName());
         if( !aItem.getName().equals(aItem.getShortName()) ){
            sb.append(" (").append(aItem.getShortName()).append(")");
         }
         sb.append("</b>");

         sb.append("<table width=\"100%\" cellspacing=\"1\" border=\"0\" cellpadding=\"0\">");
         sb.append("<tr><td width=\"30%\">Critical hit:</td><td> ").append(df.format(100 * criticalStrikeProbability.calculate(aItem)))
           .append("%</td></tr>");
         sb.append("<tr><td>Destroyed:</td><td> ").append(df2.format(100 * cds.getProbabilityOfDestruction(aItem))).append("%</td></tr>");
         sb.append("<tr><td>HP:</td><td> ").append(aItem.getHealth()).append("</td></tr>");
         sb.append("<tr><td>SIE-HP:</td><td> ").append(df.format(effectiveHP.calculate(aItem))).append("</td></tr>");
         sb.append("</table>");
         sb.append("<br/>");

         sb.append("<div style='width:300px'>")
           .append("<p>")
           .append("<b>Critical hit</b> is the probability that a shot on this component's internal structure will deal damage to this item. "
                         + "When other items break, the crit % increases as it's more likely this item will be hit. "
                         + "If the weapon dealing damage does equal to or more damage than the HP of this item, it will break in one shot.")
           .append("</p><p>")
           .append("<b>Destroyed</b> is the probability that this item will be destroyed before the component is destroyed. "
                         + "A high value indicates that the item is poorly buffered and can be expected to be lost soon after the internal structure is exposed. "
                         + "A low value means that the item is likely to survive until the component is completely destroyed.").append("</p><p>")
           .append("<b>SIE-HP</b> is the Statistical, Infinitesmal Effective-HP of this component. Under the assumption that damage is ")
           .append("applied in small chunks (lasers) this is how much damage the component can take before this item breaks on average. "
                         + "For MG, LB 10-X AC and flamers this is lower as they have higher chance to crit and higher crit multiplier.")
           .append("</p>").append("</div>");

         sb.append("</html>");
         setToolTipText(sb.toString());
      }

      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){
         JList.DropLocation dropLocation = list.getDropLocation();
         if( dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index ){
            setCursor(null);
         }

         Pair<ListEntryType, Item> pair = ((Model)getModel()).getElementTypeAt(index);
         setBorder(BorderFactory.createEmptyBorder());
         Item item = pair.second;
         switch( pair.first ){
            case Empty:{
               if( isDynArmor(index + ((Model)getModel()).compactCompensationSlots) ){
                  StyleManager.styleDynamicEntry(this);
                  setText(Model.DYN_ARMOR);
               }
               else if( isDynStructure(index + ((Model)getModel()).compactCompensationSlots) ){
                  StyleManager.styleDynamicEntry(this);
                  setText(Model.DYN_STRUCT);
               }
               else{
                  StyleManager.styleItem(this);
                  setText(Model.EMPTY);
               }
               setToolTipText("");
               break;
            }
            case Item:{
               setTooltipForItem(item);
               if( ProgramInit.lsml().preferences.uiPreferences.getCompactMode() ){
                  setText(item.getShortName());
               }
               else{
                  setText(item.getName());
               }

               if( item.getNumCriticalSlots() == 1 ){
                  StyleManager.styleItem(this, item);
               }
               else{
                  StyleManager.styleItemTop(this, item);
               }
               break;
            }
            case LastSlot:{
               setText(Model.MULTISLOT);
               setTooltipForItem(item);
               StyleManager.styleItemBottom(this, item);
               break;
            }
            case MultiSlot:{
               setText(Model.MULTISLOT);
               setTooltipForItem(item);
               StyleManager.styleItemMiddle(this, item);
               break;
            }
            case EngineHeatSink:{
               setTooltipForItem(item);
               if( ProgramInit.lsml().preferences.uiPreferences.getCompactMode() ){
                  setText(Model.HEATSINKS_COMPACT_STRING + component.getEngineHeatsinks() + "/" + component.getEngineHeatsinksMax());
               }
               else{
                  setText(Model.HEATSINKS_STRING + component.getEngineHeatsinks() + "/" + component.getEngineHeatsinksMax());
               }
               StyleManager.styleItemBottom(this, item);
               break;
            }
         }
         /*
          * if( isSelected && pair.first != ListEntryType.Empty ){ setBackground(getBackground().brighter()); }
          */
         return this;
      }

      private boolean isDynStructure(int aIndex){
         int freeSlotOrdinal = aIndex - component.getSlotsUsed() - slotDistributor.getDynamicArmorSlots(component);
         int dynStructNum = slotDistributor.getDynamicStructureSlots(component);
         return freeSlotOrdinal >= 0 && freeSlotOrdinal < dynStructNum;
      }

      private boolean isDynArmor(int aIndex){
         int freeSlotOrdinal = aIndex - component.getSlotsUsed();
         int dynArmorNum = slotDistributor.getDynamicArmorSlots(component);
         return freeSlotOrdinal < dynArmorNum;
      }
   }

   private class Model extends AbstractListModel<Item> implements MessageXBar.Reader{
      private static final String HEATSINKS_STRING         = "HEAT SINKS: ";
      private static final String HEATSINKS_COMPACT_STRING = "HS: ";
      private static final String EMPTY                    = "EMPTY";
      private static final String MULTISLOT                = "";
      private static final String DYN_ARMOR                = "DYNAMIC ARMOR";
      private static final String DYN_STRUCT               = "DYNAMIC STRUCTURE";
      private static final long   serialVersionUID         = 2438473891359444131L;
      private final MessageXBar   xBar;

      private final int           compactCompensationSlots;

      Model(MessageXBar aXBar){
         xBar = aXBar;
         xBar.attach(this);

         int c = 0;
         if( ProgramInit.lsml().preferences.uiPreferences.getCompactMode() ){
            for(Item item : component.getInternalComponent().getFixedItems()){
               c += item.getNumCriticalSlots();
            }
         }
         compactCompensationSlots = c;
      }

      boolean putElement(Item anItem, int anIndex, boolean aShouldReplace){
         Pair<ListEntryType, Item> target = getElementTypeAt(anIndex);
         switch( target.first ){
            case EngineHeatSink:{
               if( anItem instanceof HeatSink && loadout.canEquip(anItem) && component.canAddItem(anItem) ){
                  opStack.pushAndApply(new OpAddItem(xBar, loadout, component, anItem));
                  return true;
               }
               return false;
            }
            case LastSlot: // Fall through
            case Item: // Fall through
            case MultiSlot:{
               // Drop on existing component, try to replace it if we should, otherwise just add it to the component.
               if( aShouldReplace && !(anItem instanceof HeatSink && target.second instanceof Engine) ){
                  opStack.pushAndApply(new OpRemoveItem(xBar, loadout, component, target.second));
               }
               // Fall through
            }
            case Empty:{
               if( loadout.canEquip(anItem) && component.canAddItem(anItem) ){
                  opStack.pushAndApply(new OpAddItem(xBar, loadout, component, anItem));
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
         List<Item> items = new ArrayList<>(component.getItemsAll());
         if( ProgramInit.lsml().preferences.uiPreferences.getCompactMode() ){
            items.removeAll(component.getInternalComponent().getFixedItems());
         }
         int numEngineHs = component.getEngineHeatsinks();
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

         int spaceLeft = item.getNumCriticalSlots();
         for(int slot = 0; slot < arg0; ++slot){
            spaceLeft--;
            if( spaceLeft == 0 ){
               if( itemsIdx < items.size() ){
                  item = items.get(itemsIdx);
                  itemsIdx++;
                  spaceLeft = item.getNumCriticalSlots();
               }
               else
                  return new Pair<ListEntryType, Item>(ListEntryType.Empty, null);
            }
         }
         if( spaceLeft == 1 && item.getNumCriticalSlots() > 1 ){
            if( item instanceof Engine )
               return new Pair<ListEntryType, Item>(ListEntryType.EngineHeatSink, item);
            return new Pair<ListEntryType, Item>(ListEntryType.LastSlot, item);
         }
         if( spaceLeft == item.getNumCriticalSlots() )
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
         return component.getInternalComponent().getSlots() - compactCompensationSlots;
      }

      @Override
      public void receive(Message aMsg){
         if( !aMsg.isForMe(loadout) ){
            return;
         }

         // Only update on item changes or upgrades
         if( aMsg instanceof ConfiguredComponentBase.Message || aMsg instanceof Upgrades.Message ){
            if( aMsg instanceof ConfiguredComponentBase.Message && ((ConfiguredComponentBase.Message)aMsg).type == Type.ArmorChanged ){
               return; // Don't react to armor changes
            }
            fireContentsChanged(this, 0, component.getInternalComponent().getSlots());
         }
      }
   }

   PartList(OperationStack aStack, final LoadoutBase<?> aLoadout, final ConfiguredComponentBase aComponent, final MessageXBar aXBar, DynamicSlotDistributor aSlotDistributor){
      slotDistributor = aSlotDistributor;
      opStack = aStack;
      component = aComponent;
      loadout = aLoadout;
      effectiveHP = new ItemEffectiveHP(component);
      cds = new ComponentDestructionSimulator(component, aXBar);
      cds.simulate();
      new CriticalItemDamage(component);
      criticalStrikeProbability = new CriticalStrikeProbability(component);
      setModel(new Model(aXBar));
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
               for(Pair<Item, Integer> itemPair : getSelectedItems()){
                  if( itemPair.first instanceof Internal )
                     continue;
                  opStack.pushAndApply(new OpRemoveItem(aXBar, aLoadout, aComponent, itemPair.first));
               }
            }
         }
      });

      addMouseListener(new MouseAdapter(){
         @Override
         public void mouseClicked(MouseEvent e){
            if( SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2 ){
               for(Pair<Item, Integer> itemPair : getSelectedItems()){
                  if( itemPair.first instanceof Internal )
                     continue;
                  opStack.pushAndApply(new OpRemoveItem(aXBar, aLoadout, aComponent, itemPair.first));
               }
            }
         }
      });
   }

   public List<Pair<Item, Integer>> getSelectedItems(){
      List<Pair<Item, Integer>> items = new ArrayList<>();
      int[] idxs = getSelectedIndices();
      for(int i : idxs){
         Pair<ListEntryType, Item> pair = ((Model)getModel()).getElementTypeAt(i);
         int rootId = i;
         while( rootId >= 0 && ((Model)getModel()).getElementAt(rootId) == null )
            rootId--;

         switch( pair.first ){
            case Empty:
               break;
            case EngineHeatSink:
               if( component.getEngineHeatsinks() > 0 ){
                  Item heatSink = loadout.getUpgrades().getHeatSink().getHeatSinkType();
                  items.add(new Pair<Item, Integer>(heatSink, i));
               }
               break;
            case Item:
            case LastSlot:
            case MultiSlot:
               items.add(new Pair<Item, Integer>(pair.second, rootId));
               break;
            default:
               break;

         }
      }
      return items;
   }

   public ConfiguredComponentBase getPart(){
      return component;
   }
   
   public LoadoutBase<?> getLoadout(){
      return loadout;
   }

   public void putElement(Item aItem, int aDropIndex, boolean aFirst){
      ((Model)getModel()).putElement(aItem, aDropIndex, aFirst);
   }
}
