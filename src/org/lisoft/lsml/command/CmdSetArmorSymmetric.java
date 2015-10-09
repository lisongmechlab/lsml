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

import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;
import org.lisoft.lsml.util.message.MessageDelivery;
import org.lisoft.lsml.util.message.MessageXBar;

/**
 * This {@link Command} sets armor symmetrically on both sides of a {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class CmdSetArmorSymmetric extends CompositeCommand {
    private final ConfiguredComponentBase component;
    private final ArmorSide               side;
    private final boolean                 manual;
    private final LoadoutBase<?>          loadout;

    /**
     * Creates a new {@link CmdSetArmorSymmetric}.
     * 
     * @param aMessageDelivery
     *            The {@link MessageXBar} to announce changes to.
     * @param aLoadout
     *            The {@link LoadoutBase} to operate on.
     * @param aLoadoutPart
     *            The primary side {@link ConfiguredComponentBase} to change (the opposite side will be changed
     *            automatically).
     * @param aArmorSide
     *            The side to set the armor for.
     * @param aArmorAmount
     *            The amount to set the armor to.
     * @param aManualSet
     *            True if this set operation is done manually. Will disable automatic armor assignments.
     */
    public CmdSetArmorSymmetric(MessageDelivery aMessageDelivery, LoadoutBase<?> aLoadout,
            ConfiguredComponentBase aLoadoutPart, ArmorSide aArmorSide, int aArmorAmount, boolean aManualSet) {
        super("change armor", aMessageDelivery);
        loadout = aLoadout;
        component = aLoadoutPart;
        side = aArmorSide;
        manual = aManualSet;

        Location otherSide = aLoadoutPart.getInternalComponent().getLocation().oppositeSide();
        if (otherSide == null)
            throw new IllegalArgumentException(
                    "Symmetric armor operation is only usable with components that have an opposing side.");

        addOp(new CmdSetArmor(messageBuffer, aLoadout, aLoadoutPart, aArmorSide, aArmorAmount, aManualSet));
        addOp(new CmdSetArmor(messageBuffer, aLoadout, aLoadout.getComponent(otherSide), aArmorSide, aArmorAmount,
                aManualSet));
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
        if (!(aOperation instanceof CmdSetArmorSymmetric))
            return false;
        CmdSetArmorSymmetric that = (CmdSetArmorSymmetric) aOperation;
        if (that.manual != manual)
            return false;
        if (that.component != component
                && that.component != loadout
                        .getComponent(component.getInternalComponent().getLocation().oppositeSide()))
            return false;
        if (that.side != side)
            return false;
        return true;
    }

    @Override
    public void buildCommand() {
        // No-op The preparation is invariant of time and performed in constructor
    }

}
