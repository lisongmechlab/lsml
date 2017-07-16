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
package org.lisoft.lsml.view_fx.util;

import java.util.function.Predicate;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.MwoObject;
import org.lisoft.lsml.model.loadout.Loadout;

import javafx.scene.control.TreeItem;

/**
 * This predicate is used for hiding items from the equipment list that are of no interest to the user at the moment.
 *
 * @author Li Song
 */
public class EquippablePredicate implements Predicate<TreeItem<Object>> {
    private final Loadout loadout;

    /**
     * Creates a new predicate instance.
     *
     * @param aLoadout
     *            The {@link Loadout} to create the predicate for.
     */
    public EquippablePredicate(Loadout aLoadout) {
        loadout = aLoadout;
    }

    @Override
    public boolean test(TreeItem<Object> aTreeItem) {
        final Object object = aTreeItem.getValue();
        if (null == object || !(object instanceof MwoObject)) {
            return false;
        }

        final MwoObject equipment = (MwoObject) aTreeItem.getValue();
        final Chassis chassis = loadout.getChassis();
        if (!equipment.getFaction().isCompatible(chassis.getFaction())) {
            return false;
        }

        if (equipment instanceof Item) {
            final Item item = (Item) equipment;

            if (!chassis.isAllowed(item)) {
                return false;
            }

            if (!item.isCompatible(loadout.getUpgrades())) {
                return false;
            }

            final HardPointType hardPoint;
            if (item instanceof Ammunition) {
                final Ammunition ammunition = (Ammunition) item;
                hardPoint = ammunition.getWeaponHardpointType();

                for (final AmmoWeapon weapon : loadout.items(AmmoWeapon.class)) {
                    if (weapon.isCompatibleAmmo(ammunition)) {
                        return true;
                    }
                }

                for (final Ammunition otherAmmo : loadout.items(Ammunition.class)) {
                    if (otherAmmo == ammunition) {
                        return true;
                    }
                }
                return false;
            }

            hardPoint = item.getHardpointType();
            if (hardPoint != HardPointType.NONE && loadout.getHardpointsCount(hardPoint) < 1) {
                return false;
            }
        }
        return true;
    }
}
