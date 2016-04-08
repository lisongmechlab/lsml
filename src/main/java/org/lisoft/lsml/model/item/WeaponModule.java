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
package org.lisoft.lsml.model.item;

import java.util.Collection;
import java.util.Collections;

import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * A {@link PilotModule} that alters weapon attributes.
 * 
 * @author Emily Björk
 */
public class WeaponModule extends PilotModule implements ModifierEquipment {
    private final Collection<Modifier> modifiers;

    /**
     * @param aMwoName
     *            The name of the module in the MWO data files.
     * @param aMwoIdx
     *            The ID of the module in the MWO data files.
     * @param aName
     *            The human readable name of the module.
     * @param aDescription
     *            The human readable description of the module.
     * @param aFaction
     *            The required faction for this module.
     * @param aCathegory
     *            The {@link ModuleCathegory} for this {@link Module}.
     * @param aModuleSlot
     *            The {@link ModuleSlot} of this {@link Module}.
     * @param aModifiers
     *            The modifiers that this weapon module adds.
     */
    public WeaponModule(String aMwoName, int aMwoIdx, String aName, String aDescription, Faction aFaction,
            ModuleCathegory aCathegory, ModuleSlot aModuleSlot, Collection<Modifier> aModifiers) {
        super(aMwoName, aMwoIdx, aName, aDescription, aFaction, aCathegory, aModuleSlot);
        modifiers = Collections.unmodifiableCollection(aModifiers);
    }

    @Override
    public Collection<Modifier> getModifiers() {
        return modifiers;
    }

    /**
     * @param aWeapon
     *            The weapon to check.
     * @return <code>true</code> if this module affects the given weapon.
     */
    public boolean affectsWeapon(Weapon aWeapon) {
        for (Modifier modifier : modifiers) {
            for (String selector : modifier.getDescription().getSelectors()) {
                if (aWeapon.getAliases().contains(selector)) {
                    return true;
                }
            }
        }
        return false;
    }
}
