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

import org.lisoft.lsml.messages.*;
import org.lisoft.lsml.messages.ItemMessage.Type;
import org.lisoft.lsml.messages.UpgradesMessage.ChangeMsg;
import org.lisoft.lsml.model.chassi.*;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.loadout.*;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.upgrades.*;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This {@link Command} can change the armour type of a {@link LoadoutStandard}.
 *
 * @author Li Song
 */
public class CmdSetArmourType extends CmdUpgradeBase {
    private ArmourUpgrade oldValue;
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
     * @throws EquipException
     *             If the upgrade is not suitable for the chassis of this loadout.
     */
    public CmdSetArmourType(MessageDelivery aMessageDelivery, LoadoutStandard aLoadout, ArmourUpgrade aArmourUpgrade) {
        super(aMessageDelivery, aArmourUpgrade.getName());
        upgrades = aLoadout.getUpgrades();
        loadout = aLoadout;
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
        newValue = aArmourUpgrade;
    }

    @Override
    public void apply() throws EquipException {

        if (!loadout.getChassis().canUseUpgrade(newValue)) {
            throw new EquipException(EquipResult.make(EquipResultType.NotSupported));
        }

        oldValue = upgrades.getArmour();
        set(newValue, oldValue);
    }

    private void post(Message aMsg) {
        if (messageDelivery != null) {
            messageDelivery.post(aMsg);
        }
    }

    @Override
    public void undo() {
        try {
            set(oldValue, newValue);
        }
        catch (final EquipException e) {
            // Undo must not throw
        }
    }

    protected void set(ArmourUpgrade aNew, ArmourUpgrade aOld) throws EquipException {
        if (aNew != aOld) {
            final int slotDelta = aNew.getTotalSlots() - aOld.getTotalSlots();
            final double massDelta = aNew.getTotalTons(loadout) - aOld.getTotalTons(loadout);
            if (slotDelta > loadout.getFreeSlots()) {
                EquipException.checkAndThrow(EquipResult.make(EquipResultType.NotEnoughSlots));
            }

            if (massDelta > loadout.getFreeMass()) {
                EquipException.checkAndThrow(EquipResult.make(EquipResultType.TooHeavy));
            }

            for (final Location l : Location.values()) {
                final int localSlotDelta = aNew.getFixedSlotsFor(l) - aOld.getFixedSlotsFor(l);
                if (localSlotDelta > loadout.getComponent(l).getSlotsFree()) {
                    EquipException.checkAndThrow(EquipResult.make(l, EquipResultType.NotEnoughSlots));
                }
            }

            if (aNew == UpgradeDB.IS_STEALTH_ARMOUR && loadout.getItemsOfHardPointType(HardPointType.ECM) < 1) {
                EquipException.checkAndThrow(EquipResult.make(EquipResultType.NeedEcm));
            }

            // We are now sure that we can equip.
            upgrades.setArmour(aNew);

            aOld.getFixedSlotItem().ifPresent(oldFixedItem -> {
                for (final Location l : Location.values()) {
                    final ConfiguredComponentStandard component = loadout.getComponent(l);
                    while (component.getItemsEquipped().contains(oldFixedItem)) {
                        final int idx = component.removeItem(oldFixedItem);
                        post(new ItemMessage(component, Type.Removed, oldFixedItem, idx));
                    }
                }
            });

            aNew.getFixedSlotItem().ifPresent(newFixedItem -> {
                for (final Location l : Location.values()) {
                    final ConfiguredComponentStandard component = loadout.getComponent(l);
                    for (int i = 0; i < aNew.getFixedSlotsFor(l); ++i) {
                        final int idx = component.addItem(newFixedItem);
                        post(new ItemMessage(component, Type.Added, newFixedItem, idx));
                    }
                }
            });

            post(new UpgradesMessage(ChangeMsg.ARMOUR, upgrades));
        }
    }
}
