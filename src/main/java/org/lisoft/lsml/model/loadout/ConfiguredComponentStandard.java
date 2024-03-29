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
package org.lisoft.lsml.model.loadout;

import java.util.Collection;
import java.util.List;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.mwo_data.equipment.Engine;
import org.lisoft.mwo_data.equipment.HeatSink;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.mechs.ComponentStandard;
import org.lisoft.mwo_data.mechs.HardPoint;
import org.lisoft.mwo_data.mechs.HardPointType;

/**
 * This class implements {@link ConfiguredComponent} for {@link LoadoutStandard}.
 *
 * @author Li Song
 */
public class ConfiguredComponentStandard extends ConfiguredComponent {

  public ConfiguredComponentStandard(ComponentStandard aInternalPart, boolean aManualArmour) {
    super(aInternalPart, aManualArmour);
  }

  public ConfiguredComponentStandard(ConfiguredComponentStandard aComponent) {
    super(aComponent);
  }

  @Override
  public EquipResult canEquip(Item aItem) {
    final EquipResult superResult = super.canEquip(aItem);
    if (superResult != EquipResult.SUCCESS) {
      return superResult;
    }

    if (aItem instanceof HeatSink && getEngineHeatSinks() < getEngineHeatSinksMax()) {
      return EquipResult.SUCCESS;
    }

    if (aItem == ItemDB.CASE && getItemsEquipped().contains(ItemDB.CASE)) {
      return EquipResult.make(
          getInternalComponent().getLocation(), EquipResultType.ComponentAlreadyHasCase);
    }

    int engineHsDiscount = 0;
    if (aItem instanceof final Engine engine) {
      int heatsinks = 0;
      HeatSink hsType = null;
      for (final Item item : getItemsEquipped()) {
        if (item instanceof HeatSink) {
          heatsinks++;
          hsType = (HeatSink) item;
        }
      }
      if (hsType != null) {
        engineHsDiscount = Math.min(heatsinks, engine.getNumHeatsinkSlots()) * hsType.getSlots();
      }
    }

    if (getSlotsFree() + engineHsDiscount < aItem.getSlots()) {
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
  public ComponentStandard getInternalComponent() {
    return (ComponentStandard) super.getInternalComponent();
  }

  @Override
  public List<Item> getItemsFixed() {
    return getInternalComponent().getFixedItems();
  }

  @Override
  public boolean hasMissileBayDoors() {
    return getInternalComponent().hasMissileBayDoors();
  }
}
