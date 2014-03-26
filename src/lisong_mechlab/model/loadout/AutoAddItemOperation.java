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
package lisong_mechlab.model.loadout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.RemoveItemOperation;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This operation automatically places an item at a suitable location on the {@link Loadout}.
 * 
 * @author Emily Björk
 */
public class AutoAddItemOperation extends LoadoutOperation{
   private class Node implements Comparable<Node>{
      final Loadout data;
      final Part    source;
      final Part    target;
      final Item    item;
      final Node    parent;
      final int     score;

      final Item    targetItem;

      Node(Loadout aRoot, Item aItem){
         parent = null;
         item = aItem;
         source = null;
         target = null;
         targetItem = null;
         data = aRoot;
         score = score();
      }

      Node(Node aParent, Part aSource, Part aTarget, Item aItem){
         data = new Loadout(aParent.data, null);
         parent = aParent;
         source = aSource;
         target = aTarget;
         targetItem = null;
         item = aItem;
         stack.pushAndApply(new RemoveItemOperation(null, data.getPart(source), item));
         stack.pushAndApply(new AddItemOperation(null, data.getPart(target), item));
         score = score();
      }

      /**
       * @param aParent
       * @param aSourcePart
       * @param aTargetPart
       * @param aSourceItem
       * @param aTargetItem
       */
      Node(Node aParent, Part aSourcePart, Part aTargetPart, Item aSourceItem, Item aTargetItem){
         data = new Loadout(aParent.data, null);
         parent = aParent;
         source = aSourcePart;
         target = aTargetPart;
         targetItem = aTargetItem;
         item = aSourceItem;

         stack.pushAndApply(new RemoveItemOperation(null, data.getPart(target), aTargetItem));
         stack.pushAndApply(new RemoveItemOperation(null, data.getPart(source), aSourceItem));
         stack.pushAndApply(new AddItemOperation(null, data.getPart(target), aSourceItem));
         stack.pushAndApply(new AddItemOperation(null, data.getPart(source), aTargetItem));
         score = score();
      }

      @Override
      public boolean equals(Object aObject){
         if( aObject == null || !(aObject instanceof Node) )
            return false;
         return data.equals(((Node)aObject).data);
      }

      @Override
      public int compareTo(Node aRhs){
         return -Integer.compare(score, aRhs.score);
      }

      private int score(){
         int maxFree = 0;
         for(Part part : validParts){
            maxFree = Math.max(maxFree, data.getPart(part).getNumCriticalSlotsFree() * (data.getPart(part).getInternalPart().isAllowed(item) ? 1 : 0));
         }
         return maxFree;
      }
   }

   private final Item           itemToPlace;
   private final List<Part>     validParts = new ArrayList<>();
   private final List<Part>     partTraversalOrder;
   private final OperationStack stack      = new OperationStack(0);

   public AutoAddItemOperation(Loadout aLoadout, MessageXBar anXBar, Item anItem){
      super(aLoadout, anXBar, "auto place item");
      itemToPlace = anItem;
      for(LoadoutPart part : aLoadout.getCandidateLocationsForItem(itemToPlace)){
         validParts.add(part.getInternalPart().getType());
      }
      partTraversalOrder = getPartTraversalOrder();

      // If it can go into the engine, put it there.
      LoadoutPart ct = loadout.getPart(Part.CenterTorso);
      if( anItem instanceof HeatSink && ct.getNumEngineHeatsinks() < ct.getNumEngineHeatsinksMax() && ct.canEquip(anItem) ){
         addOp(new AddItemOperation(xBar, ct, anItem));
         return;
      }

      List<Node> closed = new ArrayList<>();
      List<Node> open = new ArrayList<>();

      // Initial node
      open.add(new Node(aLoadout, anItem));
      while( !open.isEmpty() ){
         Node node = open.remove(0);
         closed.add(node);

         // Are we there yet?
         if( node.data.canEquip(anItem) ){
            applySolution(node);
            return; // Yes we are!
         }

         // Not yet sweetie
         for(Part part : partTraversalOrder){
            LoadoutPart loadoutPart = node.data.getPart(part);
            for(Item i : loadoutPart.getItems()){
               if( i instanceof Internal )
                  continue;
               List<Node> branches = getBranches(node, part, i);
               for(Node branch : branches){
                  if( !closed.contains(branch) && !open.contains(branch) )
                     open.add(branch);
               }
            }
         }
         Collections.sort(open); // Greedy search, I need *a* solution, not the best one.
      }

      throw new IllegalArgumentException("Not possible");
   }

   private void applySolution(Node node){
      List<Operation> ops = new LinkedList<>();
      Node n = node;
      while( n.parent != null ){
         if( n.targetItem != null ){
            ops.add(0, new AddItemOperation(xBar, loadout.getPart(n.target), n.item));
            ops.add(0, new AddItemOperation(xBar, loadout.getPart(n.source), n.targetItem));
            ops.add(0, new RemoveItemOperation(xBar, loadout.getPart(n.target), n.targetItem));
            ops.add(0, new RemoveItemOperation(xBar, loadout.getPart(n.source), n.item));
         }
         else{
            ops.add(0, new AddItemOperation(xBar, loadout.getPart(n.target), n.item));
            ops.add(0, new RemoveItemOperation(xBar, loadout.getPart(n.source), n.item));
         }
         n = n.parent;
      }
      // Look at the solution node to find which part in the original loadout the item should
      // be added to.
      for(Part part : partTraversalOrder){
         LoadoutPart loadoutPart = node.data.getPart(part);
         if( loadoutPart.canEquip(itemToPlace) ){
            ops.add(new AddItemOperation(xBar, loadout.getPart(part), itemToPlace));
            break;
         }
      }
      while( !ops.isEmpty() )
         addOp(ops.remove(0));
   }

   private List<Node> getBranches(Node aParent, Part aSourcePart, Item aItem){
      List<Node> ans = new ArrayList<>();
      LoadoutPart srcLoadoutPart = aParent.data.getPart(aSourcePart);
      for(Part targetPart : Part.values()){
         if( aSourcePart == targetPart )
            continue;
         LoadoutPart tgtLoadoutPart = aParent.data.getPart(targetPart);
         if( tgtLoadoutPart.canEquip(aItem) ){
            ans.add(new Node(aParent, aSourcePart, targetPart, aItem));
         }
         else if( tgtLoadoutPart.getInternalPart().isAllowed(aItem) ){
            // The part couldn't take the item directly, see if we can swap with
            // some item in the part.
            final int minItemSize = aItem.getNumCriticalSlots(aParent.data.getUpgrades()) - tgtLoadoutPart.getNumCriticalSlotsFree();
            final int maxItemSize = aItem.getNumCriticalSlots(aParent.data.getUpgrades()) + srcLoadoutPart.getNumCriticalSlotsFree();
            HardpointType requiredType = aItem.getHardpointType();
            if( requiredType != HardpointType.NONE
                && tgtLoadoutPart.getNumItemsOfHardpointType(requiredType) < tgtLoadoutPart.getInternalPart().getNumHardpoints(requiredType) ){
               requiredType = HardpointType.NONE; // There is at least one free hard point, we don't need to swap with a
                                                  // item of the required type.
            }
            for(Item item : tgtLoadoutPart.getItems()){
               if( item instanceof Internal )
                  continue;
               if( item.getNumCriticalSlots(aParent.data.getUpgrades()) < minItemSize )
                  continue;
               if( item.getNumCriticalSlots(aParent.data.getUpgrades()) > maxItemSize )
                  continue;
               if( requiredType != HardpointType.NONE && item.getHardpointType() != requiredType )
                  continue;
               if( !srcLoadoutPart.getInternalPart().isAllowed(item) )
                  continue;
               if(item == aItem)
                  continue;
               ans.add(new Node(aParent, aSourcePart, targetPart, aItem, item));
            }
         }
      }
      return ans;
   }

   private List<Part> getPartTraversalOrder(){
      Part[] partOrder = new Part[] {Part.RightArm, Part.RightTorso, Part.RightLeg, Part.Head, Part.CenterTorso, Part.LeftTorso, Part.LeftLeg,
            Part.LeftArm};

      List<Part> order = new ArrayList<>();
      for(Part part : partOrder){
         if( validParts.contains(part) )
            order.add(part);
      }
      for(Part part : partOrder){
         if( !order.contains(part) )
            order.add(part);
      }
      return Collections.unmodifiableList(order);
   }
}
