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
package org.lisoft.lsml.view_fx.loadout.equipment;

import java.util.function.Predicate;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.LoadoutBase;

import javafx.scene.control.TreeItem;

/**
 * This predicate is used for hiding items from the equipment list that are of no interest to the user at the moment.
 * 
 * @author Emily Björk
 */
public class EquippablePredicate implements Predicate<TreeItem<Object>> {
    private final LoadoutBase<?> loadout;

    /**
     * Creates a new predicate instance.
     * 
     * @param aLoadout
     *            The {@link LoadoutBase} to create the predicate for.
     */
    public EquippablePredicate(LoadoutBase<?> aLoadout) {
        loadout = aLoadout;
    }

    @Override
    public boolean test(TreeItem<Object> aTreeItem) {
        Object object = aTreeItem.getValue();
        if (null == object || !(object instanceof Item))
            return false;

        Item item = (Item) aTreeItem.getValue();
        if (!loadout.getChassis().isAllowed(item))
            return false;

        if (!item.isCompatible(loadout.getUpgrades()))
            return false;

        final HardPointType hardPoint;
        if (item instanceof Ammunition) {
            Ammunition ammunition = (Ammunition) item;
            hardPoint = ammunition.getWeaponHardpointType();

            for (AmmoWeapon weapon : loadout.items(AmmoWeapon.class)) {
                if (weapon.isCompatibleAmmo(ammunition)) {
                    return true;
                }
            }

            for (Ammunition otherAmmo : loadout.items(Ammunition.class)) {
                if (otherAmmo == ammunition)
                    return true;
            }
            return false;
        }

        hardPoint = item.getHardpointType();
        if (hardPoint != HardPointType.NONE && loadout.getHardpointsCount(hardPoint) < 1)
            return false;

        return true;
    }
}
