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
package org.lisoft.lsml.model.upgrades;

import org.lisoft.lsml.command.CmdAddItem;
import org.lisoft.lsml.command.CmdRemoveItem;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.upgrades.Upgrades.UpgradesMessage;
import org.lisoft.lsml.model.upgrades.Upgrades.UpgradesMessage.ChangeMsg;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.message.MessageDelivery;

/**
 * This {@link Command} can alter the heat sink upgrade status of a {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class OpSetHeatSinkType extends CompositeCommand {
    private final HeatSinkUpgrade oldValue;
    private final HeatSinkUpgrade newValue;
    private final UpgradesMutable upgrades;
    private final LoadoutStandard loadout;

    /**
     * Creates a {@link OpSetHeatSinkType} that only affects a stand-alone {@link UpgradesMutable} object This is useful
     * only for altering {@link UpgradesMutable} objects which are not attached to a {@link LoadoutStandard} in any way.
     * 
     * @param aUpgrades
     *            The {@link UpgradesMutable} object to alter with this {@link Command}.
     * @param aHeatsinkUpgrade
     *            The new heat sink type.
     */
    public OpSetHeatSinkType(UpgradesMutable aUpgrades, HeatSinkUpgrade aHeatsinkUpgrade) {
        super(aHeatsinkUpgrade.getName(), null);
        upgrades = aUpgrades;
        loadout = null;
        oldValue = upgrades.getHeatSink();
        newValue = aHeatsinkUpgrade;
    }

    /**
     * Creates a new {@link OpSetHeatSinkType} that will change the heat sink type of a {@link LoadoutStandard}.
     * 
     * @param aMessageDelivery
     *            A {@link MessageDelivery} to signal changes in DHS status on.
     * @param aLoadout
     *            The {@link LoadoutStandard} to alter.
     * @param aHeatsinkUpgrade
     *            The new heat sink type.
     */
    public OpSetHeatSinkType(MessageDelivery aMessageDelivery, LoadoutStandard aLoadout,
            HeatSinkUpgrade aHeatsinkUpgrade) {
        super(aHeatsinkUpgrade.getName(), aMessageDelivery);
        upgrades = aLoadout.getUpgrades();
        loadout = aLoadout;
        oldValue = upgrades.getHeatSink();
        newValue = aHeatsinkUpgrade;
    }

    @Override
    protected void apply() {
        set(newValue);
        super.apply();
    }

    @Override
    protected void undo() {
        set(oldValue);
        super.undo();
    }

    protected void set(HeatSinkUpgrade aValue) {
        if (aValue != upgrades.getHeatSink()) {
            upgrades.setHeatSink(aValue);

            messageBuffer.post(new UpgradesMessage(ChangeMsg.HEATSINKS, upgrades));
        }
    }

    @Override
    public void buildCommand() {
        if (oldValue != newValue) {
            HeatSink oldHsType = oldValue.getHeatSinkType();
            HeatSink newHsType = newValue.getHeatSinkType();

            int globallyRemoved = 0;
            int globalEngineHs = 0;

            for (ConfiguredComponentBase component : loadout.getComponents()) {
                int locallyRemoved = 0;
                for (Item item : component.getItemsEquipped()) {
                    if (item instanceof HeatSink) {
                        addOp(new CmdRemoveItem(messageBuffer, loadout, component, item));
                        globallyRemoved++;
                        locallyRemoved++;
                    }
                }
                // Note: This will not be correct for omnimechs, but you can't change heat sink upgrades on them
                // anyways.
                globalEngineHs += Math.min(locallyRemoved, component.getEngineHeatsinksMax());
            }

            int globalSlotsFree = (globallyRemoved - globalEngineHs) * oldHsType.getNumCriticalSlots()
                    + loadout.getNumCriticalSlotsFree();
            int globalHsLag = 0;

            for (ConfiguredComponentBase component : loadout.getComponents()) {
                int hsRemoved = 0;
                for (Item item : component.getItemsEquipped()) { // Don't remove fixed HS, not that we could on an
                                                                 // omnimech
                                                                 // anyways.
                    if (item instanceof HeatSink) {
                        hsRemoved++;
                    }
                }

                int hsInEngine = Math.min(hsRemoved, component.getEngineHeatsinksMax());
                int slotsFreed = (hsRemoved - hsInEngine) * oldHsType.getNumCriticalSlots();
                int slotsFree = Math.min(slotsFreed + component.getSlotsFree(), globalSlotsFree);
                int hsToAdd = Math.min(hsRemoved + globalHsLag,
                        hsInEngine + slotsFree / newHsType.getNumCriticalSlots());

                globalSlotsFree -= newHsType.getNumCriticalSlots() * (hsToAdd - component.getEngineHeatsinksMax());
                globalHsLag += hsRemoved - hsToAdd;

                while (hsToAdd > 0) {
                    hsToAdd--;
                    addOp(new CmdAddItem(messageBuffer, loadout, component, newHsType));
                }
            }
        }
    }
}
