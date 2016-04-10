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
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.model.upgrades.UpgradesMutable;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This {@link Command} changes the guidance status of a {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class CmdSetGuidanceType extends CompositeCommand {
    private final GuidanceUpgrade oldValue;
    private final GuidanceUpgrade newValue;
    private final Upgrades upgrades;
    private final Loadout loadout;

    /**
     * Creates a {@link CmdSetGuidanceType} that only affects a stand-alone {@link UpgradesMutable} object This is
     * useful only for altering {@link UpgradesMutable} objects which are not attached to a {@link Loadout} in any way.
     * 
     * @param aUpgrades
     *            The {@link UpgradesMutable} object to alter with this {@link Command}.
     * @param aGuidanceUpgrade
     *            The new upgrade to use.
     */
    public CmdSetGuidanceType(Upgrades aUpgrades, GuidanceUpgrade aGuidanceUpgrade) {
        super(aGuidanceUpgrade.getName(), null);
        upgrades = aUpgrades;
        loadout = null;
        oldValue = upgrades.getGuidance();
        newValue = aGuidanceUpgrade;
    }

    /**
     * Creates a new {@link CmdSetGuidanceType} that will change the guidance upgrade of a {@link LoadoutStandard}.
     * 
     * @param aMessageDelivery
     *            A {@link MessageDelivery} to signal changes in guidance status on.
     * @param aLoadout
     *            The {@link Loadout} to alter.
     * @param aGuidanceUpgrade
     *            The new upgrade to use.
     */
    public CmdSetGuidanceType(MessageDelivery aMessageDelivery, Loadout aLoadout, GuidanceUpgrade aGuidanceUpgrade) {
        super(aGuidanceUpgrade.getName(), aMessageDelivery);
        upgrades = aLoadout.getUpgrades();
        loadout = aLoadout;
        oldValue = upgrades.getGuidance();
        newValue = aGuidanceUpgrade;
    }

    @Override
    public void buildCommand() throws EquipException {
        if (loadout != null) {
            if (newValue.getExtraSlots(loadout) > loadout.getNumCriticalSlotsFree())
                EquipException.checkAndThrow(EquipResult.make(EquipResultType.NotEnoughSlots));

            for (ConfiguredComponent part : loadout.getComponents()) {
                if (newValue.getExtraSlots(part) > part.getSlotsFree())
                    EquipException.checkAndThrow(EquipResult.make(part.getInternalComponent().getLocation(),
                            EquipResultType.NotEnoughSlots));
            }

            if (newValue.getExtraTons(loadout) > loadout.getFreeMass()) {
                EquipException.checkAndThrow(EquipResult.make(EquipResultType.TooHeavy));
            }

            addOp(new CommandStack.Command() {
                private void set(GuidanceUpgrade aValue) {
                    if (aValue != upgrades.getGuidance()) {
                        upgrades.setGuidance(aValue);
                        messageBuffer.post(new UpgradesMessage(ChangeMsg.GUIDANCE, upgrades));
                    }
                }

                @Override
                protected void undo() {
                    set(oldValue);
                }

                @Override
                public String describe() {
                    return "Set guidance (internal)";
                }

                @Override
                protected void apply() {
                    set(newValue);
                }
            });

            for (ConfiguredComponent component : loadout.getComponents()) {
                for (Item item : component.getItemsEquipped()) {
                    // FIXME: What about fixed missile launchers?
                    if (item instanceof MissileWeapon) {
                        MissileWeapon oldWeapon = (MissileWeapon) item;
                        MissileWeapon newWeapon = newValue.upgrade(oldWeapon);
                        if (oldWeapon != newWeapon) {
                            addOp(new CmdRemoveItem(messageBuffer, loadout, component, oldWeapon));
                            addOp(new CmdAddItem(messageBuffer, loadout, component, newWeapon));
                        }
                    }
                    else if (item instanceof Ammunition) {
                        Ammunition oldAmmo = (Ammunition) item;
                        Ammunition newAmmo = newValue.upgrade(oldAmmo);
                        if (oldAmmo != newAmmo) {
                            addOp(new CmdRemoveItem(messageBuffer, loadout, component, oldAmmo));
                            addOp(new CmdAddItem(messageBuffer, loadout, component, newAmmo));
                        }
                    }
                }
            }
        }
    }
}
