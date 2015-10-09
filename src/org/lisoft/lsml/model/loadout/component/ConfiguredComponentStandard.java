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

import java.util.Collection;
import java.util.List;

import org.lisoft.lsml.model.chassi.ComponentStandard;
import org.lisoft.lsml.model.chassi.HardPoint;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.LoadoutStandard;

/**
 * This class implements {@link ConfiguredComponentBase} for {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class ConfiguredComponentStandard extends ConfiguredComponentBase {

    public ConfiguredComponentStandard(ComponentStandard aInternalPart, boolean aManualArmor) {
        super(aInternalPart, aManualArmor);
    }

    public ConfiguredComponentStandard(ConfiguredComponentStandard aComponent) {
        super(aComponent);
    }

    @Override
    public EquipResult canEquip(Item aItem) {
        EquipResult superResult = super.canEquip(aItem);
        if (superResult != EquipResult.SUCCESS) {
            return superResult;
        }

        if (aItem instanceof HeatSink && getEngineHeatsinks() < getEngineHeatsinksMax()) {
            return EquipResult.SUCCESS;
        }

        if (aItem == ItemDB.CASE && getItemsEquipped().contains(ItemDB.CASE))
            return EquipResult.make(getInternalComponent().getLocation(), EquipResultType.ComponentAlreadyHasCase);

        if (getSlotsFree() < aItem.getNumCriticalSlots()) {
            return EquipResult.make(getInternalComponent().getLocation(), EquipResultType.NotEnoughSlots);
        }
        return EquipResult.SUCCESS;
    }

    @Override
    public int getHardPointCount(HardPointType aHardpointType) {
        return getInternalComponent().getHardPointCount(aHardpointType);
    }

    @Override
    public Collection<HardPoint> getHardPoints() {
        return getInternalComponent().getHardPoints();
    }

    @Override
    public List<Item> getItemsFixed() {
        return getInternalComponent().getFixedItems();
    }

    @Override
    public ComponentStandard getInternalComponent() {
        return (ComponentStandard) super.getInternalComponent();
    }

    @Override
    public boolean hasMissileBayDoors() {
        return getInternalComponent().hasMissileBayDoors();
    }
}
