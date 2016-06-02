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
package org.lisoft.lsml.command;

import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.messages.UpgradesMessage.ChangeMsg;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.UpgradesMutable;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This {@link Command} can change the armour type of a {@link LoadoutStandard}.
 *
 * @author Emily Björk
 */
public class CmdSetArmourType extends CmdUpgradeBase {
    private final ArmourUpgrade oldValue;
    private final ArmourUpgrade newValue;
    private final UpgradesMutable upgrades;
    private final LoadoutStandard loadout;

    /**
     * Creates a new {@link CmdSetStructureType} that will change the armour type of a {@link LoadoutStandard}.
     *
     * @param aMessageDelivery
     *            A {@link MessageDelivery} to signal changes in internal structure on.
     * @param aLoadout
     *            The {@link LoadoutStandard} to alter.
     * @param aArmourUpgrade
     *            The new armour type this upgrades is applied.
     */
    public CmdSetArmourType(MessageDelivery aMessageDelivery, LoadoutStandard aLoadout, ArmourUpgrade aArmourUpgrade) {
        super(aMessageDelivery, aArmourUpgrade.getName());
        upgrades = aLoadout.getUpgrades();
        loadout = aLoadout;
        oldValue = upgrades.getArmour();
        newValue = aArmourUpgrade;
    }

    /**
     * Creates a {@link CmdSetArmourType} that only affects a stand-alone {@link UpgradesMutable} object This is useful
     * only for altering {@link UpgradesMutable} objects which are not attached to a {@link LoadoutStandard} in any way.
     *
     * @param aUpgrades
     *            The {@link UpgradesMutable} object to alter with this {@link Command}.
     * @param aArmourUpgrade
     *            The new armour type when this upgrades has been applied.
     */
    public CmdSetArmourType(UpgradesMutable aUpgrades, ArmourUpgrade aArmourUpgrade) {
        super(null, aArmourUpgrade.getName());
        upgrades = aUpgrades;
        loadout = null;
        oldValue = upgrades.getArmour();
        newValue = aArmourUpgrade;
    }

    @Override
    public void apply() throws EquipException {
        set(newValue);
    }

    @Override
    public void undo() {
        try {
            set(oldValue);
        }
        catch (final EquipException e) {
            // Undo must not throw
        }
    }

    protected void set(ArmourUpgrade aValue) throws EquipException {
        if (aValue != upgrades.getArmour()) {
            final ArmourUpgrade old = upgrades.getArmour();
            upgrades.setArmour(aValue);

            final EquipResult result = verifyLoadoutInvariant(loadout);
            if (result != EquipResult.SUCCESS) {
                upgrades.setArmour(old);
                EquipException.checkAndThrow(result);
            }

            if (messageDelivery != null) {
                messageDelivery.post(new UpgradesMessage(ChangeMsg.ARMOUR, upgrades));
            }
        }
    }
}
