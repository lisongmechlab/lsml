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

import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.messages.UpgradesMessage.ChangeMsg;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.upgrades.ArmorUpgrade;
import org.lisoft.lsml.model.upgrades.UpgradesMutable;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This {@link Command} can change the armor type of a {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class CmdSetArmorType extends CmdUpgradeBase {
    private final ArmorUpgrade    oldValue;
    private final ArmorUpgrade    newValue;
    private final UpgradesMutable upgrades;
    private final LoadoutStandard loadout;

    /**
     * Creates a {@link CmdSetArmorType} that only affects a stand-alone {@link UpgradesMutable} object This is useful
     * only for altering {@link UpgradesMutable} objects which are not attached to a {@link LoadoutStandard} in any way.
     * 
     * @param aUpgrades
     *            The {@link UpgradesMutable} object to alter with this {@link Command}.
     * @param aArmorUpgrade
     *            The new armor type when this upgrades has been applied.
     */
    public CmdSetArmorType(UpgradesMutable aUpgrades, ArmorUpgrade aArmorUpgrade) {
        super(null, aArmorUpgrade.getName());
        upgrades = aUpgrades;
        loadout = null;
        oldValue = upgrades.getArmor();
        newValue = aArmorUpgrade;
    }

    /**
     * Creates a new {@link CmdSetStructureType} that will change the armor type of a {@link LoadoutStandard}.
     * 
     * @param aMessageDelivery
     *            A {@link MessageDelivery} to signal changes in internal structure on.
     * @param aLoadout
     *            The {@link LoadoutStandard} to alter.
     * @param aArmorUpgrade
     *            The new armor type this upgrades is applied.
     */
    public CmdSetArmorType(MessageDelivery aMessageDelivery, LoadoutStandard aLoadout, ArmorUpgrade aArmorUpgrade) {
        super(aMessageDelivery, aArmorUpgrade.getName());
        upgrades = aLoadout.getUpgrades();
        loadout = aLoadout;
        oldValue = upgrades.getArmor();
        newValue = aArmorUpgrade;
    }

    @Override
    protected void apply() throws EquipException {
        set(newValue);
    }

    @Override
    protected void undo() {
        try {
            set(oldValue);
        }
        catch (EquipException e) {
            // Undo must not throw
        }
    }

    protected void set(ArmorUpgrade aValue) throws EquipException {
        if (aValue != upgrades.getArmor()) {
            ArmorUpgrade old = upgrades.getArmor();
            upgrades.setArmor(aValue);

            EquipResult result = verifyLoadoutInvariant(loadout);
            if (result != EquipResult.SUCCESS) {
                upgrades.setArmor(old);
                EquipException.checkAndThrow(result);
            }

            if (messageDelivery != null)
                messageDelivery.post(new UpgradesMessage(ChangeMsg.ARMOR, upgrades));
        }
    }
}
