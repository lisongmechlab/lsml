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
package org.lisoft.lsml.model.loadout.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lisoft.lsml.command.CmdAddItem;
import org.lisoft.lsml.command.CmdRemoveItem;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.Component;
import org.lisoft.lsml.model.chassi.HardPoint;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.ListArrayUtils;

/**
 * This class represents a configured {@link Component}.
 * <p>
 * This class is immutable. The only way to alter it is by creating instances of the relevant {@link Command}s and
 * adding them to an {@link CommandStack}.
 * 
 * @author Emily Björk
 */
public abstract class ConfiguredComponent {
    public final static Internal ENGINE_INTERNAL = (Internal) ItemDB.lookup(ItemDB.ENGINE_INTERNAL_ID);
    public final static Internal ENGINE_INTERNAL_CLAN = (Internal) ItemDB.lookup(ItemDB.ENGINE_INTERNAL_CLAN_ID);
    private final Map<ArmorSide, Attribute> armor = new HashMap<ArmorSide, Attribute>();
    private final Component internalComponent;
    private final List<Item> items = new ArrayList<Item>();
    private boolean manualArmor = false;

    /**
     * Copy constructor. Performs a deep copy of the argument with a new {@link LoadoutStandard} value.
     * 
     * @param aComponent
     *            The {@link ConfiguredComponent} to copy.
     */
    public ConfiguredComponent(ConfiguredComponent aComponent) {
        internalComponent = aComponent.internalComponent;
        manualArmor = aComponent.manualArmor;

        for (Map.Entry<ArmorSide, Attribute> e : aComponent.armor.entrySet()) {
            armor.put(e.getKey(),
                    new Attribute(e.getValue().value(null), e.getValue().getSelectors(), e.getValue().getSpecifier()));
        }

        for (Item item : aComponent.items) {
            items.add(item);
        }
    }

    public ConfiguredComponent(Component aInternalComponent, boolean aManualArmor) {
        internalComponent = aInternalComponent;
        manualArmor = aManualArmor;

        for (ArmorSide side : ArmorSide.allSides(internalComponent)) {
            String specifier = internalComponent.getLocation().shortName();
            if (side == ArmorSide.BACK) {
                specifier += "R";
            }
            armor.put(side, new Attribute(0, ModifierDescription.SEL_ARMOR, specifier));
        }
    }

    /**
     * Adds a new item to this component. This method does not verify loadout invariants and can put the component into
     * an illegal state. It is the caller's responsibility to make sure local and global conditions are met before
     * adding an item.
     * <p>
     * This is intended for use only from {@link CmdAddItem}, {@link CmdRemoveItem} and relatives.
     * <p>
     * Please note that {@link #canEquip(Item)} must return true prior to a call to {@link #addItem(Item)}.
     * 
     * @param aItem
     *            The item to add.
     * @return The index where the item was added or -1 if the item was consumed by another item (HS going into engine
     *         for example).
     */
    public int addItem(Item aItem) {
        items.add(aItem);

        if (aItem instanceof HeatSink && getEngineHeatSinksMax() >= getHeatSinkCount()) {
            return -1; // Consumed by engine
        }

        // This works because items are always added at the end.
        int consumedHs = getEngineHeatSinks();
        return items.size() - 1 - consumedHs;
    }

    /**
     * Checks if all local conditions for the item to be equipped on this component are full filled. Before an item can
     * be equipped, global conditions on the loadout must also be checked by {@link Loadout#canEquipDirectly(Item)}.
     * 
     * @param aItem
     *            The item to check with.
     * @return <code>true</code> if local constraints allow the item to be equipped here.
     */
    public EquipResult canEquip(Item aItem) {
        if (!getInternalComponent().isAllowed(aItem))
            return EquipResult.make(getInternalComponent().getLocation(), EquipResultType.NotSupported);

        // Check enough free hard points
        if (aItem.getHardpointType() != HardPointType.NONE
                && getItemsOfHardpointType(aItem.getHardpointType()) >= getHardPointCount(aItem.getHardpointType())) {
            return EquipResult.make(getInternalComponent().getLocation(), EquipResultType.NoFreeHardPoints);
        }
        return EquipResult.SUCCESS;
    }

    /**
     * @param aHardpointType
     *            The type of {@link HardPoint}s to count.
     * @return The number of {@link HardPoint}s of the given type on this configured component.
     */
    public abstract int getHardPointCount(HardPointType aHardpointType);

    /**
     * @return A {@link Collection} of all the {@link HardPoint}s on this configured component.
     */
    public abstract Collection<HardPoint> getHardPoints();

    /**
     * Checks if the {@link Item} can be removed by the user from this component.
     * 
     * @param aItem
     *            The item to check if it can removed.
     * @return <code>true</code> if the item can be removed, <code>false</code> otherwise.
     */
    public boolean canRemoveItem(Item aItem) {
        return !(aItem instanceof Internal) && items.contains(aItem); // TODO convert to use EquipResult
    }

    /**
     * This is intended for use only from {@link CmdAddItem}, {@link CmdRemoveItem} and relatives.
     * 
     * @param aItem
     *            The item to remove.
     * @return The index of the removed item. Or -1 for engine heat sinks.
     */
    public int removeItem(Item aItem) {
        int index = items.lastIndexOf(aItem);
        if (index < 0) {
            throw new IllegalArgumentException("Can't remove nonexistent item!");
        }

        int hsBefore = 0;
        for (int i = 0; i <= index; ++i) {
            if (items.get(i) instanceof HeatSink)
                hsBefore++;
        }

        items.remove(index);
        int consumedHs = Math.min(getEngineHeatSinksMax(), hsBefore);
        return index - consumedHs;
    }

    /**
     * @return <code>true</code> if this component's armor has been set manually (otherwise it's been set
     *         automatically).
     */
    public boolean hasManualArmor() {
        return manualArmor;
    }

    @Override
    public boolean equals(Object aObject) {
        if (this == aObject)
            return true;
        if (!(aObject instanceof ConfiguredComponent))
            return false;
        ConfiguredComponent that = (ConfiguredComponent) aObject;

        if (!internalComponent.equals(that.internalComponent))
            return false;
        if (!ListArrayUtils.equalsUnordered(items, that.items))
            return false;
        if (!armor.equals(that.armor))
            return false;
        if (manualArmor != that.manualArmor)
            return false;
        return true;
    }

    /**
     * Gets the effective armor for a given side, taking modifiers into account.
     * 
     * @param aArmorSide
     *            The {@link ArmorSide} to query. Querying the wrong side results in a {@link IllegalArgumentException}.
     * @param aModifiers
     *            A {@link Collection} of {@link Modifier}s to use for calculating the actual armor amount.
     * @return The current amount of armor on the given side of this component.
     */
    public int getEffectiveArmor(ArmorSide aArmorSide, Collection<Modifier> aModifiers) {
        if (!armor.containsKey(aArmorSide))
            throw new IllegalArgumentException("No such armor side!");
        return (int) armor.get(aArmorSide).value(aModifiers);
    }

    /**
     * Gets the raw base armor value.
     * 
     * @param aArmorSide
     *            The {@link ArmorSide} to query. Querying the wrong side results in a {@link IllegalArgumentException}.
     * @return The current amount of armor on the given side of this component.
     */
    public int getArmor(ArmorSide aArmorSide) {
        return getEffectiveArmor(aArmorSide, null);
    }

    /**
     * Will return the number of armor points that can be set on the component. Taking both armor sides into account and
     * respecting the max armor limit. Does not take free tonnage into account.Querying the wrong side results in a
     * {@link IllegalArgumentException}.
     * 
     * @param aArmorSide
     *            The {@link ArmorSide} to get the max free armor for.
     * @return The number of armor points that can be maximally set (ignoring tonnage).
     */
    public int getArmorMax(ArmorSide aArmorSide) {

        switch (aArmorSide) {
            case BACK:
                return getInternalComponent().getArmorMax() - getArmor(ArmorSide.FRONT);
            case FRONT:
                return getInternalComponent().getArmorMax() - getArmor(ArmorSide.BACK);
            default:
            case ONLY:
                if (!armor.containsKey(aArmorSide))
                    throw new IllegalArgumentException("No such armor side!");
                return getInternalComponent().getArmorMax();
        }
    }

    /**
     * @return The total number of armor points on this component.
     */
    public int getArmorTotal() {
        int sum = 0;
        for (Attribute attrib : armor.values()) {
            sum += attrib.value(null);
        }
        return sum;
    }

    /**
     * @return The number of heat sinks inside the engine (if any) equipped on this component. Does not count the (up
     *         to) 10 included in the engine itself, rather it only counts the external heat sink slots.
     */
    public int getEngineHeatSinks() {
        int ans = getHeatSinkCount();
        return Math.min(ans, getEngineHeatSinksMax());
    }

    private int getHeatSinkCount() {
        int ans = ListArrayUtils.countByType(items, HeatSink.class)
                + ListArrayUtils.countByType(getInternalComponent().getFixedItems(), HeatSink.class);
        return ans;
    }

    /**
     * @return The maximal number of heat sinks that the engine (if any) equipped on this component can sustain.
     */
    public int getEngineHeatSinksMax() {
        for (Item item : items) {
            if (item instanceof Engine) {
                return ((Engine) item).getNumHeatsinkSlots();
            }
        }
        for (Item item : getInternalComponent().getFixedItems()) {
            if (item instanceof Engine) {
                return ((Engine) item).getNumHeatsinkSlots();
            }
        }
        return 0;
    }

    /**
     * @return The internal component that is backing this component.
     */
    public Component getInternalComponent() {
        return internalComponent;
    }

    /**
     * @return The sum of the mass of all items on this component.
     */
    public double getItemMass() {
        double ans = 0;
        for (Item item : items) {
            ans += item.getMass();
        }
        for (Item item : getItemsFixed()) {
            ans += item.getMass();
        }
        return ans;
    }

    /**
     * @return A {@link List} of the user equipped items which can also be removed.
     */
    public List<Item> getItemsEquipped() {
        return Collections.unmodifiableList(items);
    }

    /**
     * @return A {@link List} of items that are fixed on this component.
     */
    public abstract List<Item> getItemsFixed();

    /**
     * @param aHardpointType
     *            The type of {@link HardPointType} to count.
     * @return The number of items of the given hard point of type that are equipped.
     */
    public int getItemsOfHardpointType(HardPointType aHardpointType) {
        int hardpoints = 0;
        for (Item it : getItemsEquipped()) {
            if (it.getHardpointType() == aHardpointType) {
                hardpoints++;
            }
        }
        for (Item it : getInternalComponent().getFixedItems()) {
            if (it.getHardpointType() == aHardpointType) {
                hardpoints++;
            }
        }
        return hardpoints;
    }

    /**
     * @return The number of critical slots locally available on this component. Note: may be less than globally
     *         available slots as this doesn't take floating slots (such as dynamic armor on standard mechs) into
     *         account.
     */
    public int getSlotsFree() {
        return getInternalComponent().getSlots() - getSlotsUsed();
    }

    /**
     * @return The number of critical slots that are used in this component, not counting floating slots used by dynamic
     *         armor or structure.
     */
    public int getSlotsUsed() {
        int crits = getInternalComponent().getFixedItemSlots();
        int engineHsLeft = getEngineHeatSinksMax();
        for (Item item : items) {
            if (item instanceof HeatSink && engineHsLeft > 0) {
                engineHsLeft--;
                continue;
            }
            crits += item.getSlots();
        }
        return crits;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((armor == null) ? 0 : armor.hashCode());
        result = prime * result + ((internalComponent == null) ? 0 : internalComponent.hashCode());
        result = prime * result + ((items == null) ? 0 : items.hashCode());
        return result;
    }

    public void setArmor(ArmorSide aArmorSide, int aAmount, boolean aManualArmor) {
        if (!armor.containsKey(aArmorSide))
            throw new IllegalArgumentException("No such armor side!");
        armor.get(aArmorSide).setBaseValue(aAmount);
        manualArmor = aManualArmor;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getInternalComponent().getLocation().isTwoSided()) {
            sb.append(getArmor(ArmorSide.FRONT)).append("/").append(getArmor(ArmorSide.BACK));
        }
        else {
            sb.append(getArmor(ArmorSide.ONLY));
        }
        sb.append(" ");
        for (Item item : items) {
            if (item instanceof Internal)
                continue;
            sb.append(item).append(",");
        }
        return sb.toString();
    }

    /**
     * @return <code>true</code> if this component has missile bay doors, <code>false</code> otherwise.
     */
    public abstract boolean hasMissileBayDoors();
}