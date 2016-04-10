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

import org.lisoft.lsml.messages.ArmorMessage;
import org.lisoft.lsml.messages.ArmorMessage.Type;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This {@link Command} will change the armor of a {@link ConfiguredComponent}.
 * 
 * @author Li Song
 */
public class CmdSetArmor extends Command {
    private final ArmorSide side;
    private final int amount;
    private final boolean manual;
    private int oldAmount = -1;
    private boolean oldManual;
    private final MessageDelivery messageDelivery;
    private final Loadout loadout;
    private final ConfiguredComponent component;

    /**
     * Sets the armor for a given side of the component. Throws if the operation will fail.
     * 
     * @param aMessageDelivery
     *            The {@link MessageXBar} to announce changes to.
     * @param aLoadout
     *            The {@link Loadout} to change.
     * @param aComponent
     *            The {@link ConfiguredComponent} to change.
     * @param aArmorSide
     *            The side to set the armor for.
     * @param aArmorAmount
     *            The amount to set the armor to.
     * @param aManualSet
     *            True if this set operation is done manually. Will disable automatic armor assignments.
     */
    public CmdSetArmor(MessageDelivery aMessageDelivery, Loadout aLoadout, ConfiguredComponent aComponent,
            ArmorSide aArmorSide, int aArmorAmount, boolean aManualSet) {
        messageDelivery = aMessageDelivery;
        loadout = aLoadout;
        component = aComponent;
        side = aArmorSide;
        amount = aArmorAmount;
        manual = aManualSet;

        if (amount < 0)
            throw new IllegalArgumentException("Armor must be positive!");

        if (amount > component.getInternalComponent().getArmorMax())
            throw new IllegalArgumentException("Armor must be less than components max armor!");
    }

    /**
     * @see org.lisoft.lsml.util.CommandStack.Command#canCoalescele(org.lisoft.lsml.util.CommandStack.Command)
     */
    @Override
    public boolean canCoalescele(Command aOperation) {
        if (this == aOperation)
            return false;
        if (aOperation == null)
            return false;
        if (!(aOperation instanceof CmdSetArmor))
            return false;
        CmdSetArmor that = (CmdSetArmor) aOperation;
        if (that.manual != manual)
            return false;
        if (that.component != component)
            return false;
        if (that.side != side)
            return false;
        return true;
    }

    @Override
    public String describe() {
        return "change armor";
    }

    @Override
    protected void apply() throws EquipException {
        storePreviousState();
        if (operationHasEffect()) {
            operationTryToLegalize();
            setValue(amount, manual);
        }
    }

    @Override
    protected void undo() {
        if (oldAmount < 0) {
            throw new RuntimeException("Apply was not called before undo!");
        }

        if (operationHasEffect()) {
            setValue(oldAmount, oldManual);
        }
        oldAmount = -1;
    }

    private void storePreviousState() {
        oldAmount = component.getArmor(side);
        oldManual = component.hasManualArmor();
    }

    private void operationTryToLegalize() throws EquipException {
        if (amount > component.getArmorMax(side))
            EquipException.checkAndThrow(EquipResult.make(EquipResultType.ExceededMaxArmor));

        int armorDiff = amount - oldAmount;
        int totalArmor = armorDiff + loadout.getArmor(); // This is important to prevent numerical stability issues.
                                                         // Calculate whole armor in integer precision.
        double armorTons = loadout.getUpgrades().getArmor().getArmorMass(totalArmor);
        double freeTonnage = loadout.getChassis().getMassMax() - (loadout.getMassStructItems() + armorTons);

        if (freeTonnage < 0) {
            // See if the armor can be freed from a combination of automatic components. They will be redistributed
            // afterwards. FIXME: Devise a proper solution, this is ugly.
            int freed = 0;
            if (manual == true && freed < armorDiff) {
                for (ConfiguredComponent otherComponent : loadout.getComponents()) {
                    if (component != otherComponent && !otherComponent.hasManualArmor()) {
                        freed += otherComponent.getArmorTotal();
                        for (ArmorSide armorSide : ArmorSide.allSides(otherComponent.getInternalComponent())) {
                            otherComponent.setArmor(armorSide, 0, false);
                        }
                    }
                }
            }
            if (freed < armorDiff) {
                EquipException.checkAndThrow(EquipResult.make(EquipResultType.TooHeavy));
            }
        }
    }

    private boolean operationHasEffect() {
        return amount != oldAmount || oldManual != manual;
    }

    private void setValue(int aValue, boolean aManual) {
        component.setArmor(side, aValue, aManual);
        if (messageDelivery != null) {
            messageDelivery.post(new ArmorMessage(component, Type.ARMOR_CHANGED, aManual));
        }
    }
}
