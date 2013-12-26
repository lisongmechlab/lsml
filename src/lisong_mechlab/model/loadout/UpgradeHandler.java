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
package lisong_mechlab.model.loadout;

import lisong_mechlab.model.item.Item;

/**
 * This interface represents a base for handling changing upgrades on a mech. Such as armor type, internal structure,
 * guidance and double heat sinks.
 * 
 * @author Emily Björk
 */
public interface UpgradeHandler{
   /**
    * Return true if the {@link Loadout} given in the constructor can handle the upgrade in the {@link Item}.
    * 
    * @param anUpgradeItem
    *           The upgrade to apply.
    * @return <code>true</code> if the {@link Loadout} can take the upgrade and still remain in a legal state.
    */
   public boolean canApplyUpgrade(Item anUpgradeItem);

   /**
    * Will apply the given upgrade to the {@link Loadout} given in the constructor. If the {@link Loadout} is unable to
    * accommodate the upgrade, an exception is thrown.
    * 
    * @param anUpgradeItem
    * @throws IllegalArgumentException
    *            Thrown if the {@link Loadout} would end up in an illegal state after the upgrade.
    */
   public void applyUpgrade(Item anUpgradeItem) throws IllegalArgumentException;
}
