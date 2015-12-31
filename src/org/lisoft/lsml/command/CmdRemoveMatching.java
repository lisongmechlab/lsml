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
package org.lisoft.lsml.command;

import java.util.function.Predicate;

import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This class removes all items matching a given predicate.
 * 
 * @author Li Song
 */
public class CmdRemoveMatching extends CompositeCommand {

    private final Predicate<Item> predicate;
    private final LoadoutBase<?>  loadout;

    public static Command removeWeaponSystem(MessageDelivery aMessageTarget, LoadoutBase<?> aLoadout, Weapon aWeapon) {
        if (aWeapon instanceof AmmoWeapon) {
            final AmmoWeapon ammoWeapon = (AmmoWeapon) aWeapon;
            final Ammunition ammo = (Ammunition) ItemDB.lookup(ammoWeapon.getAmmoType());
            final Ammunition ammoHalf = (Ammunition) ItemDB.lookup(ammoWeapon.getAmmoType() + "half");
            return new CmdRemoveMatching("remove all " + aWeapon.getName() + " and ammo", aMessageTarget, aLoadout,
                    aItem -> aItem == aWeapon || aItem == ammo || aItem == ammoHalf);
        }
        return new CmdRemoveMatching("remove all " + aWeapon.getName(), aMessageTarget, aLoadout,
                aItem -> aItem == aWeapon);
    }

    /**
     * @param aDescription
     * @param aMessageTarget
     * @param aLoadout
     * @param aPredicate
     */
    public CmdRemoveMatching(String aDescription, MessageDelivery aMessageTarget, LoadoutBase<?> aLoadout,
            Predicate<Item> aPredicate) {
        super(aDescription, aMessageTarget);
        predicate = aPredicate;
        loadout = aLoadout;
    }

    @Override
    protected void buildCommand() throws EquipException {
        for (final ConfiguredComponentBase confComp : loadout.getComponents()) {
            for (Item equippedItem : confComp.getItemsEquipped()) {
                if (predicate.test(equippedItem)) {
                    addOp(new CmdRemoveItem(messageBuffer, loadout, confComp, equippedItem));
                }
            }
        }
    }
}
