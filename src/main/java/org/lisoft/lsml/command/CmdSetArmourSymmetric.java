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
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;

/**
 * This {@link Command} sets armour symmetrically on both sides of a {@link LoadoutStandard}.
 *
 * @author Emily Björk
 */
public class CmdSetArmourSymmetric extends CompositeCommand {
    private final ConfiguredComponent component;
    private final ArmourSide side;
    private final boolean manual;
    private final Loadout loadout;

    /**
     * Creates a new {@link CmdSetArmourSymmetric}.
     *
     * @param aMessageDelivery
     *            The {@link MessageXBar} to announce changes to.
     * @param aLoadout
     *            The {@link Loadout} to operate on.
     * @param aLoadoutPart
     *            The primary side {@link ConfiguredComponent} to change (the opposite side will be changed
     *            automatically).
     * @param aArmourSide
     *            The side to set the armour for.
     * @param aArmourAmount
     *            The amount to set the armour to.
     * @param aManualSet
     *            True if this set operation is done manually. Will disable automatic armour assignments.
     */
    public CmdSetArmourSymmetric(MessageDelivery aMessageDelivery, Loadout aLoadout, ConfiguredComponent aLoadoutPart,
            ArmourSide aArmourSide, int aArmourAmount, boolean aManualSet) {
        super("change armour", aMessageDelivery);
        loadout = aLoadout;
        component = aLoadoutPart;
        side = aArmourSide;
        manual = aManualSet;

        final Location otherSide = aLoadoutPart.getInternalComponent().getLocation().oppositeSide();
        if (otherSide == null) {
            throw new IllegalArgumentException(
                    "Symmetric armour operation is only usable with components that have an opposing side.");
        }

        addOp(new CmdSetArmour(messageBuffer, aLoadout, aLoadoutPart, aArmourSide, aArmourAmount, aManualSet));
        addOp(new CmdSetArmour(messageBuffer, aLoadout, aLoadout.getComponent(otherSide), aArmourSide, aArmourAmount,
                aManualSet));
    }

    @Override
    public void buildCommand() {
        // No-op The preparation is invariant of time and performed in constructor
    }

    /**
     * @see org.lisoft.lsml.util.CommandStack.Command#canCoalescele(org.lisoft.lsml.util.CommandStack.Command)
     */
    @Override
    public boolean canCoalescele(Command aOperation) {
        if (this == aOperation) {
            return false;
        }
        if (aOperation == null) {
            return false;
        }
        if (!(aOperation instanceof CmdSetArmourSymmetric)) {
            return false;
        }
        final CmdSetArmourSymmetric that = (CmdSetArmourSymmetric) aOperation;
        if (that.manual != manual) {
            return false;
        }
        if (that.component != component && that.component != loadout
                .getComponent(component.getInternalComponent().getLocation().oppositeSide())) {
            return false;
        }
        if (that.side != side) {
            return false;
        }
        return true;
    }

}
