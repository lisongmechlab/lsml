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
package org.lisoft.lsml.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.Type;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.message.MessageDelivery;

/**
 * This operation automatically places an item at a suitable location on the {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class CmdAutoAddItem extends CmdLoadoutBase {
    private class Node implements Comparable<Node> {
        final LoadoutBase<?> data;
        final Location       source;
        final Location       target;
        final Item           item;
        final Node           parent;
        final int            score;

        final Item targetItem;

        Node(LoadoutBase<?> aRoot, Item aItem) {
            parent = null;
            item = aItem;
            source = null;
            target = null;
            targetItem = null;
            data = aRoot;
            score = score();
        }

        Node(Node aParent, Location aSource, Location aTarget, Item aItem) throws Exception {
            data = DefaultLoadoutFactory.instance.produceClone(aParent.data);
            parent = aParent;
            source = aSource;
            target = aTarget;
            targetItem = null;
            item = aItem;
            stack.pushAndApply(new CmdRemoveItem(null, data, data.getComponent(source), item));
            stack.pushAndApply(new CmdAddItem(null, data, data.getComponent(target), item));
            score = score();
        }

        Node(Node aParent, Location aSourcePart, Location aTargetPart, Item aSourceItem, Item aTargetItem)
                throws Exception {
            data = DefaultLoadoutFactory.instance.produceClone(aParent.data);
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
        public boolean equals(Object aObject) {
            if (aObject == null || !(aObject instanceof Node))
                return false;
            return data.equals(((Node) aObject).data);
        }

        @Override
        public int compareTo(Node aRhs) {
            return -Integer.compare(score, aRhs.score);
        }

        private int score() {
            if (itemToPlace instanceof Engine && ((Engine) itemToPlace).getType() == EngineType.XL) {
                int slotsFreeCt = Math.min(itemToPlace.getNumCriticalSlots(),
                        data.getComponent(Location.CenterTorso).getSlotsFree());
                int slotsFreeLt = Math.min(3, data.getComponent(Location.LeftTorso).getSlotsFree());
                int slotsFreeRt = Math.min(3, data.getComponent(Location.RightTorso).getSlotsFree());
                return (slotsFreeCt + slotsFreeLt + slotsFreeRt);
            }
            int maxFree = 0;
            for (Location location : validLocations) {
                maxFree = Math.max(maxFree,
                        data.getComponent(location).getSlotsFree()
                                * (data.getComponent(location).getInternalComponent().isAllowed(item, data.getEngine())
                                        ? 1 : 0));
            }
            return maxFree;

        }
    }

    private final Item           itemToPlace;
    private final List<Location> validLocations = new ArrayList<>();
    private final List<Location> partTraversalOrder;
    private final CommandStack   stack          = new CommandStack(0);

    public CmdAutoAddItem(LoadoutBase<?> aLoadout, MessageDelivery aMessageDelivery, Item anItem) {
        super(aLoadout, aMessageDelivery, "auto place item");
        itemToPlace = anItem;
        for (ConfiguredComponentBase part : aLoadout.getCandidateLocationsForItem(itemToPlace)) {
            validLocations.add(part.getInternalComponent().getLocation());
        }
        partTraversalOrder = getPartTraversalOrder();
    }

    @Override
    protected void buildCommand() throws EquipResult {
        // If it can go into the engine, put it there.
        ConfiguredComponentBase ct = loadout.getComponent(Location.CenterTorso);
        if (itemToPlace instanceof HeatSink && ct.getEngineHeatsinks() < ct.getEngineHeatsinksMax()
                && EquipResult.SUCCESS == ct.canEquip(itemToPlace)) {
            addOp(new CmdAddItem(messageBuffer, loadout, ct, itemToPlace));
            return;
        }

        List<Node> closed = new ArrayList<>();
        List<Node> open = new ArrayList<>();

        // Initial node
        open.add(new Node(loadout, itemToPlace));
        while (!open.isEmpty()) {
            Node node = open.remove(0);
            closed.add(node);

            // Are we there yet?
            if (EquipResult.SUCCESS == node.data.canEquip(itemToPlace)) {
                applySolution(node);
                return; // Yes we are!
            }

            // Not yet sweetie
            for (Location part : partTraversalOrder) {
                ConfiguredComponentBase component = node.data.getComponent(part);
                for (Item i : component.getItemsEquipped()) {
                    if (i instanceof Internal)
                        continue;
                    List<Node> branches = getBranches(node, part, i);
                    for (Node branch : branches) {
                        if (!closed.contains(branch) && !open.contains(branch))
                            open.add(branch);
                    }
                }
            }
            Collections.sort(open); // Greedy search, I need *a* solution, not the best one.
        }

        throw EquipResult.make(Type.NotEnoughSlots);
    }

    private void applySolution(Node node) {
        List<Command> ops = new LinkedList<>();
        Node n = node;
        while (n.parent != null) {
            if (n.targetItem != null) {
                ops.add(0, new CmdAddItem(messageBuffer, loadout, loadout.getComponent(n.target), n.item));
                ops.add(0, new CmdAddItem(messageBuffer, loadout, loadout.getComponent(n.source), n.targetItem));
                ops.add(0, new CmdRemoveItem(messageBuffer, loadout, loadout.getComponent(n.target), n.targetItem));
                ops.add(0, new CmdRemoveItem(messageBuffer, loadout, loadout.getComponent(n.source), n.item));
            }
            else {
                ops.add(0, new CmdAddItem(messageBuffer, loadout, loadout.getComponent(n.target), n.item));
                ops.add(0, new CmdRemoveItem(messageBuffer, loadout, loadout.getComponent(n.source), n.item));
            }
            n = n.parent;
        }
        // Look at the solution node to find which part in the original loadout the item should
        // be added to.
        for (Location part : partTraversalOrder) {
            ConfiguredComponentBase loadoutPart = node.data.getComponent(part);
            if (EquipResult.SUCCESS == loadoutPart.canEquip(itemToPlace)) {
                ops.add(new CmdAddItem(messageBuffer, loadout, loadout.getComponent(part), itemToPlace));
                break;
            }
        }
        while (!ops.isEmpty())
            addOp(ops.remove(0));
    }

    /**
     * Get all possible ways to move the given item out of the source part on the node.
     * 
     * @param aParent
     *            The parent {@link Node} that we're branching from.
     * @param aSourcePart
     *            The source part that we shall remove the {@link Item} from.
     * @param aItem
     *            The {@link Item} to be removed.
     * @return A {@link List} of {@link Node}s with all possible ways to move the item out of the given node.
     */
    private List<Node> getBranches(Node aParent, Location aSourcePart, Item aItem) {
        List<Node> ans = new ArrayList<>();

        // Create a temporary loadout where the item has been removed and find all
        // ways it can be placed on another part.
        LoadoutBase<?> tempLoadout = DefaultLoadoutFactory.instance.produceClone(aParent.data);
        try {
            stack.pushAndApply(new CmdRemoveItem(null, tempLoadout, tempLoadout.getComponent(aSourcePart), aItem));
        }
        catch (Exception e) {
            // Item can't be removed? Just skip the branch entirely.
            return ans;
        }

        ConfiguredComponentBase srcPart = tempLoadout.getComponent(aSourcePart);
        for (Location targetPart : Location.values()) {
            if (aSourcePart == targetPart)
                continue;

            ConfiguredComponentBase dstPart = tempLoadout.getComponent(targetPart);
            if (EquipResult.SUCCESS == dstPart.canEquip(aItem)) {
                // Don't consider swaps if the item can be directly moved. A swap will be generated in another point
                // of the search tree anyway when we move an item from that component back to this.
                try {
                    ans.add(new Node(aParent, aSourcePart, targetPart, aItem));
                }
                catch (Exception e) {
                    /* If creating the node failed for some reason we just skip the branch. */
                }
            }
            else if (dstPart.getInternalComponent().isAllowed(aItem, tempLoadout.getEngine())) {
                // The part couldn't take the item directly, see if we can swap with some item in the part.
                final int minItemSize = aItem.getNumCriticalSlots() - dstPart.getSlotsFree();
                HardPointType requiredType = aItem.getHardpointType();
                if (requiredType != HardPointType.NONE
                        && dstPart.getItemsOfHardpointType(requiredType) < dstPart.getHardPointCount(requiredType)) {
                    requiredType = HardPointType.NONE; // There is at least one free hard point, we don't need to swap
                                                       // with a
                                                       // item of the required type.
                }
                for (Item item : dstPart.getItemsEquipped()) {
                    // The item has to clear enough room to make our item fit.
                    if (item instanceof HeatSink && dstPart.getEngineHeatsinks() > 0)
                        continue; // Engine HS will not clear slots...
                    if (item.getNumCriticalSlots() < minItemSize)
                        continue;

                    // The item has to free a hard point of the required type if applicable.
                    if (requiredType != HardPointType.NONE && item.getHardpointType() != requiredType)
                        continue;
                    // Skip NOPs
                    if (item == aItem)
                        continue;

                    // We can't move engine internals
                    if (item == ConfiguredComponentBase.ENGINE_INTERNAL
                            || item == ConfiguredComponentBase.ENGINE_INTERNAL_CLAN)
                        continue;

                    if (EquipResult.SUCCESS == srcPart.canEquip(item)) {
                        try {
                            ans.add(new Node(aParent, aSourcePart, targetPart, aItem, item));
                        }
                        catch (Exception e) {
                            /* If creating the node failed for some reason we just skip the branch. */
                        }
                    }
                }
            }
        }
        return ans;
    }

    private List<Location> getPartTraversalOrder() {
        Location[] partOrder = new Location[] { Location.RightArm, Location.RightTorso, Location.RightLeg,
                Location.Head, Location.CenterTorso, Location.LeftTorso, Location.LeftLeg, Location.LeftArm };

        List<Location> order = new ArrayList<>();
        for (Location part : partOrder) {
            if (validLocations.contains(part))
                order.add(part);
        }
        for (Location part : partOrder) {
            if (!order.contains(part))
                order.add(part);
        }
        return Collections.unmodifiableList(order);
    }
}
