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
package org.lisoft.lsml.view_fx.util;

import org.lisoft.mwo_data.equipment.*;
import org.lisoft.mwo_data.mechs.HardPointType;

/**
 * Classification of equipment into categories.
 *
 * @author Li Song
 */
public enum EquipmentCategory {
  ENERGY,
  BALLISTIC,
  MISSILE,
  AMS,
  ECM,
  MISC,
  STD_ENGINE,
  LE_ENGINE,
  XL_ENGINE,
  STRATEGIC_STRIKE,
  UAV,
  COOLANT_FLUSH,
  UNKNOWN;

  public static final EquipmentCategory[] ORDER_LSML =
      new EquipmentCategory[] {
        ENERGY,
        BALLISTIC,
        MISSILE,
        AMS,
        ECM,
        MISC,
        STD_ENGINE,
        LE_ENGINE,
        XL_ENGINE,
        COOLANT_FLUSH,
        STRATEGIC_STRIKE,
        UAV,
        UNKNOWN
      };
  public static final EquipmentCategory[] ORDER_PGI =
      new EquipmentCategory[] {
        BALLISTIC,
        ENERGY,
        MISSILE,
        AMS,
        ECM,
        MISC,
        STD_ENGINE,
        LE_ENGINE,
        XL_ENGINE,
        COOLANT_FLUSH,
        STRATEGIC_STRIKE,
        UAV,
        UNKNOWN
      };

  public static EquipmentCategory classify(Consumable.ConsumableType aType) {
    switch (aType) {
      case STRATEGIC_STRIKE:
        return EquipmentCategory.STRATEGIC_STRIKE;
      case UAV:
        return EquipmentCategory.UAV;
      case COOLANT_FLUSH:
        return COOLANT_FLUSH;
      case UNKNOWN:
      default:
        return UNKNOWN;
    }
  }

  public static EquipmentCategory classify(HardPointType aHardPointType) {
    switch (aHardPointType) {
      case AMS:
        return AMS;
      case BALLISTIC:
        return BALLISTIC;
      case ECM:
        return ECM;
      case ENERGY:
        return ENERGY;
      case MISSILE:
        return MISSILE;
      case NONE: // Fall-through
      default:
        return MISC;
    }
  }

  public static EquipmentCategory classify(MwoObject aItem) {
    if (aItem instanceof Consumable) {
      return classify(((Consumable) aItem).getType());
    } else if (aItem instanceof final Item item) {
      if (item instanceof final Engine engine) {
        if (engine.getType() == Engine.EngineType.XL) {
          return XL_ENGINE;
        } else if (engine.getType() == Engine.EngineType.LE) {
          return LE_ENGINE;
        }
        return STD_ENGINE;
      }

      final HardPointType hardPointType;
      if (item instanceof Ammunition) {
        hardPointType = ((Ammunition) item).getWeaponHardPointType();
      } else {
        hardPointType = item.getHardpointType();
      }
      return classify(hardPointType);
    }
    throw new RuntimeException("Unknown equipment type!");
  }

  @Override
  public String toString() {
    final String string = super.toString();
    return string.replace('_', ' ');
  }
}
