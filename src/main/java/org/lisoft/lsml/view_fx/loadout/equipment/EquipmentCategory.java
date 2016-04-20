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
package org.lisoft.lsml.view_fx.loadout.equipment;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Equipment;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.item.PilotModule;

/**
 * Classification of equipment into categories.
 * 
 * @author Li Song
 *
 */
public enum EquipmentCategory {
    ENERGY, BALLISTIC, MISSILE, AMS, ECM, MISC, STD_ENGINE, XL_ENGINE, CONSUMABLE, MECH_MODULE, WEAPON_MODULE;

    @Override
    public String toString() {
        String string = super.toString();
        return string.replace('_', ' ');
    }

    public static EquipmentCategory classify(Equipment aItem) {
        if (aItem instanceof PilotModule) {
            return classify(((PilotModule) aItem).getSlot());
        }
        else if (aItem instanceof Item) {
            Item item = (Item) aItem;
            if (item instanceof Engine) {
                Engine engine = (Engine) item;
                if (engine.getType() == EngineType.XL)
                    return XL_ENGINE;
                return EquipmentCategory.STD_ENGINE;
            }

            final HardPointType hardPointType;
            if (item instanceof Ammunition)
                hardPointType = ((Ammunition) item).getWeaponHardpointType();
            else
                hardPointType = item.getHardpointType();
            return classify(hardPointType);
        }
        throw new RuntimeException("Unknown equipment type!");
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
            default:
                return MISC;
        }
    }

    public static EquipmentCategory classify(ModuleSlot aHardPointType) {
        switch (aHardPointType) {
            case CONSUMABLE:
                return EquipmentCategory.CONSUMABLE;
            case MECH:
                return EquipmentCategory.MECH_MODULE;
            case WEAPON:
                return WEAPON_MODULE;
            default:
                return MISC;
        }
    }
}
