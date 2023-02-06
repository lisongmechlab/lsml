/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.loadout.*;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.mwo_data.equipment.Engine;
import org.lisoft.mwo_data.equipment.HeatSink;
import org.lisoft.mwo_data.equipment.Internal;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.mechs.HardPointType;
import org.lisoft.mwo_data.mechs.Location;

/**
 * This operation automatically places an item at a suitable location on the {@link
 * LoadoutStandard}.
 *
 * @author Li Song
 */
public class CmdAutoAddItem extends CmdLoadoutBase {
  private class Node implements Comparable<Node> {
    final Loadout data;
    final Item item;
    final Node parent;
    final int score;
    final Location source;
    final Location target;
    final Item targetItem;

    Node(Loadout aRoot, Item aItem) {
      parent = null;
      item = aItem;
      source = null;
      target = null;
      targetItem = null;
      data = aRoot;
      score = score();
    }

    Node(Node aParent, Location aSource, Location aTarget, Item aItem) throws Exception {
      data = loadoutFactory.produceClone(aParent.data);
      parent = aParent;
      source = aSource;
      target = aTarget;
      targetItem = null;
      item = aItem;
      stack.pushAndApply(new CmdRemoveItem(null, data, data.getComponent(source), item));
      stack.pushAndApply(new CmdAddItem(null, data, data.getComponent(target), item));
      score = score();
    }

    Node(
        Node aParent,
        Location aSourcePart,
        Location aTargetPart,
        Item aSourceItem,
        Item aTargetItem)
        throws Exception {
      data = loadoutFactory.produceClone(aParent.data);
      parent = aParent;
      source = aSourcePart;
      target = aTargetPart;
      targetItem = aTargetItem;
      item = aSourceItem;

      stack.pushAndApply(new CmdRemoveItem(null, data, data.getComponent(target), aTargetItem));
      stack.pushAndApply(new CmdRemoveItem(null, data, data.getComponent(source), aSourceItem));
      stack.pushAndApply(new CmdAddItem(null, data, data.getComponent(target), aSourceItem));
      stack.pushAndApply(new CmdAddItem(null, data, data.getComponent(source), aTargetItem));
      score = score();
    }

    @Override
    public int compareTo(Node aRhs) {
      return Integer.compare(aRhs.score, score);
    }

    @Override
    public boolean equals(Object aObject) {
      if (aObject == null || !(aObject instanceof Node)) {
        return false;
      }
      return data.equals(((Node) aObject).data);
    }

    @Override
    public int hashCode() {
      return data.hashCode();
    }

    private int score() {
      if (itemToPlace instanceof Engine && ((Engine) itemToPlace).getSide().isPresent()) {
        final int slotsFreeCt =
            Math.min(
                itemToPlace.getSlots(), data.getComponent(Location.CenterTorso).getSlotsFree());
        int sideSlots = ((Engine) itemToPlace).getSide().get().getSlots();
        final int slotsFreeLt =
            Math.min(sideSlots, data.getComponent(Location.LeftTorso).getSlotsFree());
        final int slotsFreeRt =
            Math.min(sideSlots, data.getComponent(Location.RightTorso).getSlotsFree());
        return slotsFreeCt + slotsFreeLt + slotsFreeRt;
      }
      int maxFree = 0;
      for (final Location location : validLocations) {
        maxFree =
            Math.max(
                maxFree,
                data.getComponent(location).getSlotsFree()
                    * (data.getComponent(location)
                            .getInternalComponent()
                            .isAllowed(item, data.getEngine())
                        ? 1
                        : 0));
      }
      return maxFree;
    }
  }

  private final Item itemToPlace;
  private final LoadoutFactory loadoutFactory;
  private final List<Location> partTraversalOrder;
  private final boolean quiet;
  private final CommandStack stack = new CommandStack(0);
  private final List<Location> validLocations = new ArrayList<>();

  public CmdAutoAddItem(
      Loadout aLoadout,
      MessageDelivery aMessageDelivery,
      Item aItem,
      boolean aQuiet,
      LoadoutFactory aLoadoutFactory) {
    super(aLoadout, aMessageDelivery, "auto place item");
    itemToPlace = aItem;
    for (final ConfiguredComponent part : aLoadout.getCandidateLocationsForItem(itemToPlace)) {
      validLocations.add(part.getInternalComponent().getLocation());
    }
    partTraversalOrder = getPartTraversalOrder();
    quiet = aQuiet;
    loadoutFactory = aLoadoutFactory;
  }

  public CmdAutoAddItem(
      Loadout aLoadout,
      MessageDelivery aMessageDelivery,
      Item aItem,
      LoadoutFactory aLoadoutFactory) {
    this(aLoadout, aMessageDelivery, aItem, false, aLoadoutFactory);
  }

  @Override
  protected void buildCommand() throws EquipException {
    final EquipResult globalResult = loadout.canEquipGlobal(itemToPlace);
    if (!quiet) {
      EquipException.checkAndThrow(globalResult);
    } else if (globalResult != EquipResult.SUCCESS) {
      return;
    }

    // If it can go into the engine, put it there.
    final ConfiguredComponent ct = loadout.getComponent(Location.CenterTorso);
    if (itemToPlace instanceof HeatSink
        && ct.getEngineHeatSinks() < ct.getEngineHeatSinksMax()
        && EquipResult.SUCCESS == ct.canEquip(itemToPlace)) {
      addOp(new CmdAddItem(messageBuffer, loadout, ct, itemToPlace));
      return;
    }

    final List<Node> closed = new ArrayList<>();
    final List<Node> open = new ArrayList<>();

    // Initial node
    open.add(new Node(loadout, itemToPlace));
    while (!open.isEmpty()) {
      final Node node = open.remove(0);
      closed.add(node);

      // Are we there yet?
      if (EquipResult.SUCCESS == node.data.canEquipDirectly(itemToPlace)) {
        applySolution(node);
        return; // Yes we are!
      }

      // Not yet sweetie
      for (final Location part : partTraversalOrder) {
        final ConfiguredComponent component = node.data.getComponent(part);
        for (final Item i : component.getItemsEquipped()) {
          if (i instanceof Internal) {
            continue;
          }
          final List<Node> branches = getBranches(node, part, i);
          for (final Node branch : branches) {
            if (!closed.contains(branch) && !open.contains(branch)) {
              open.add(branch);
            }
          }
        }
      }
      Collections.sort(open); // Greedy search, I need *a* solution, not
      // the best one.
    }

    if (!quiet) {
      EquipException.checkAndThrow(EquipResult.make(EquipResultType.NotEnoughSlots));
    }
  }

  private void applySolution(Node node) {
    final List<Command> ops = new LinkedList<>();
    Node n = node;
    while (n.parent != null) {
      if (n.targetItem != null) {
        ops.add(0, new CmdAddItem(messageBuffer, loadout, loadout.getComponent(n.target), n.item));
        ops.add(
            0,
            new CmdAddItem(messageBuffer, loadout, loadout.getComponent(n.source), n.targetItem));
        ops.add(
            0,
            new CmdRemoveItem(
                messageBuffer, loadout, loadout.getComponent(n.target), n.targetItem));
        ops.add(
            0, new CmdRemoveItem(messageBuffer, loadout, loadout.getComponent(n.source), n.item));
      } else {
        ops.add(0, new CmdAddItem(messageBuffer, loadout, loadout.getComponent(n.target), n.item));
        ops.add(
            0, new CmdRemoveItem(messageBuffer, loadout, loadout.getComponent(n.source), n.item));
      }
      n = n.parent;
    }
    // Look at the solution node to find which part in the original loadout
    // the item should be added to.
    for (final Location part : partTraversalOrder) {
      final ConfiguredComponent loadoutPart = node.data.getComponent(part);
      if (EquipResult.SUCCESS == loadoutPart.canEquip(itemToPlace)) {
        ops.add(new CmdAddItem(messageBuffer, loadout, loadout.getComponent(part), itemToPlace));
        break;
      }
    }
    while (!ops.isEmpty()) {
      addOp(ops.remove(0));
    }
  }

  /**
   * Get all possible ways to move the given item out of the source part on the node.
   *
   * @param aParent The parent {@link Node} that we're branching from.
   * @param aSourcePart The source part that we shall remove the {@link Item} from.
   * @param aItem The {@link Item} to be removed.
   * @return A {@link List} of {@link Node}s with all possible ways to move the item out of the
   *     given node.
   */
  private List<Node> getBranches(Node aParent, Location aSourcePart, Item aItem) {
    final List<Node> ans = new ArrayList<>();

    // Create a temporary loadout where the item has been removed and find
    // all
    // ways it can be placed on another part.
    final Loadout tempLoadout = loadoutFactory.produceClone(aParent.data);
    try {
      stack.pushAndApply(
          new CmdRemoveItem(null, tempLoadout, tempLoadout.getComponent(aSourcePart), aItem));
    } catch (final Exception e) {
      // Item can't be removed? Just skip the branch entirely.
      return ans;
    }

    final ConfiguredComponent srcPart = tempLoadout.getComponent(aSourcePart);
    for (final Location targetPart : Location.values()) {
      if (aSourcePart == targetPart) {
        continue;
      }

      final ConfiguredComponent dstPart = tempLoadout.getComponent(targetPart);
      if (EquipResult.SUCCESS == dstPart.canEquip(aItem)) {
        // Don't consider swaps if the item can be directly moved. A
        // swap will be generated in another point
        // of the search tree anyway when we move an item from that
        // component back to this.
        try {
          ans.add(new Node(aParent, aSourcePart, targetPart, aItem));
        } catch (final Exception e) {
          /*
           * If creating the node failed for some reason we just skip the branch.
           */
        }
      } else if (dstPart.getInternalComponent().isAllowed(aItem, tempLoadout.getEngine())) {
        // The part couldn't take the item directly, see if we can swap
        // with some item in the part.
        final int minItemSize = aItem.getSlots() - dstPart.getSlotsFree();
        HardPointType requiredType = aItem.getHardpointType();
        if (requiredType != HardPointType.NONE
            && dstPart.getItemsOfHardpointType(requiredType)
                < dstPart.getHardPointCount(requiredType)) {
          requiredType = HardPointType.NONE; // There is at least one
          // free hard point, we
          // don't need to swap
          // with a
          // item of the required
          // type.
        }
        for (final Item item : dstPart.getItemsEquipped()) {
          // The item has to clear enough room to make our item fit.
          if (item instanceof HeatSink && dstPart.getEngineHeatSinks() > 0) {
            continue; // Engine HS will not clear slots...
          }
          if (item.getSlots() < minItemSize) {
            continue;
          }

          // The item has to free a hard point of the required type if
          // applicable.
          if (requiredType != HardPointType.NONE && item.getHardpointType() != requiredType) {
            continue;
          }
          // Skip NOPs
          if (item == aItem) {
            continue;
          }

          // We can't move engine internals
          if (item instanceof Internal) {
            continue;
          }

          if (EquipResult.SUCCESS == srcPart.canEquip(item)) {
            try {
              ans.add(new Node(aParent, aSourcePart, targetPart, aItem, item));
            } catch (final Exception e) {
              /*
               * If creating the node failed for some reason we just skip the branch.
               */
            }
          }
        }
      }
    }
    return ans;
  }

  private List<Location> getPartTraversalOrder() {
    final Location[] partOrder =
        new Location[] {
          Location.RightArm,
          Location.RightTorso,
          Location.RightLeg,
          Location.Head,
          Location.CenterTorso,
          Location.LeftTorso,
          Location.LeftLeg,
          Location.LeftArm
        };

    final List<Location> order = new ArrayList<>();
    for (final Location part : partOrder) {
      if (validLocations.contains(part)) {
        order.add(part);
      }
    }
    for (final Location part : partOrder) {
      if (!order.contains(part)) {
        order.add(part);
      }
    }
    return Collections.unmodifiableList(order);
  }
}
