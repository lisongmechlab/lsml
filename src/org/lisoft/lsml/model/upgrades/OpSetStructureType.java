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
package org.lisoft.lsml.model.upgrades;

import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.upgrades.Upgrades.UpgradesMessage;
import org.lisoft.lsml.model.upgrades.Upgrades.UpgradesMessage.ChangeMsg;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.message.MessageDelivery;

/**
 * This {@link Command} can alter the internal structure of a {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class OpSetStructureType extends OpUpgradeBase {
    private final StructureUpgrade oldValue;
    private final StructureUpgrade newValue;
    private final UpgradesMutable  upgrades;
    private final LoadoutStandard  loadout;

    /**
     * Creates a {@link OpSetStructureType} that only affects a stand-alone {@link UpgradesMutable} object This is
     * useful only for altering {@link UpgradesMutable} objects which are not attached to a {@link LoadoutStandard} in
     * any way.
     * 
     * @param anUpgrades
     *            The {@link UpgradesMutable} object to alter with this {@link Command}.
     * @param aStructureUpgrade
     *            The new internal structure when this upgrades has been applied.
     */
    public OpSetStructureType(UpgradesMutable anUpgrades, StructureUpgrade aStructureUpgrade) {
        super(null, aStructureUpgrade.getName());
        upgrades = anUpgrades;
        loadout = null;
        oldValue = anUpgrades.getStructure();
        newValue = aStructureUpgrade;
    }

    /**
     * Creates a new {@link OpSetStructureType} that will change the internal structure of a {@link LoadoutStandard}.
     * 
     * @param aMessageDelivery
     *            A {@link MessageDelivery} to signal changes in internal structure on.
     * @param aLoadout
     *            The {@link LoadoutStandard} to alter.
     * @param aStructureUpgrade
     *            The new internal structure this upgrades is applied.
     */
    public OpSetStructureType(MessageDelivery aMessageDelivery, LoadoutStandard aLoadout,
            StructureUpgrade aStructureUpgrade) {
        super(aMessageDelivery, aStructureUpgrade.getName());
        upgrades = aLoadout.getUpgrades();
        loadout = aLoadout;
        oldValue = upgrades.getStructure();
        newValue = aStructureUpgrade;
    }

    @Override
    protected void apply() {
        set(newValue);
    }

    @Override
    protected void undo() {
        set(oldValue);
    }

    protected void set(StructureUpgrade aValue) {
        if (aValue != upgrades.getStructure()) {
            StructureUpgrade old = upgrades.getStructure();
            upgrades.setStructure(aValue);

            try {
                verifyLoadoutInvariant(loadout);
            }
            catch (Exception e) {
                upgrades.setStructure(old);
                throw new IllegalArgumentException("Couldn't change internal structure: ", e);
            }

            if (messageDelivery != null)
                messageDelivery.post(new UpgradesMessage(ChangeMsg.STRUCTURE, upgrades));
        }
    }
}
