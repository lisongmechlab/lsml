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
package org.lisoft.lsml.model.loadout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.command.CmdAddItem;
import org.lisoft.lsml.command.CmdAddModule;
import org.lisoft.lsml.command.CmdGarageRename;
import org.lisoft.lsml.command.CmdSetArmour;
import org.lisoft.lsml.command.CmdSetArmourType;
import org.lisoft.lsml.command.CmdSetGuidanceType;
import org.lisoft.lsml.command.CmdSetHeatSinkType;
import org.lisoft.lsml.command.CmdSetOmniPod;
import org.lisoft.lsml.command.CmdSetStructureType;
import org.lisoft.lsml.command.CmdToggleItem;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This class promises to take care of dependency issues when de-serialising any
 * loadout.
 * <p>
 * One passes the {@link Command}s that would be used to naively construct the
 * loadout to the {@link #push(Command)} method. Once all operations have been
 * pushed, one applies the operations loadout with the {@link #apply()} method.
 * The call to {@link #apply()} will re-order and apply the pushed
 * {@link Command}s in an order that allows the loadout to be constructed
 * without violating validity invariants during creation.
 *
 * @author Li Song
 */
public class LoadoutBuilder {
	private static class OperationComparator implements Comparator<Command>, Serializable {
		private static final long serialVersionUID = -5026656921652607661L;
		private final static Map<Class<? extends Command>, Integer> CLASS_PRIORITY_ORDER;

		static {
			CLASS_PRIORITY_ORDER = new HashMap<>();

			// Omnipods, upgrades, modules and renaming are independent and cannot fail on
			// an empty loadout
			CLASS_PRIORITY_ORDER.put(CmdGarageRename.class, 0);
			CLASS_PRIORITY_ORDER.put(CmdSetOmniPod.class, 1);
			CLASS_PRIORITY_ORDER.put(CmdSetGuidanceType.class, 2);
			CLASS_PRIORITY_ORDER.put(CmdSetHeatSinkType.class, 2);
			CLASS_PRIORITY_ORDER.put(CmdSetArmourType.class, 2);
			CLASS_PRIORITY_ORDER.put(CmdSetStructureType.class, 2);
			CLASS_PRIORITY_ORDER.put(CmdSetArmour.class, 3);
			CLASS_PRIORITY_ORDER.put(CmdAddModule.class, 4);

			// Toggleables have to be set before items are added
			CLASS_PRIORITY_ORDER.put(CmdToggleItem.class, 10);

			// Item operations last
			CLASS_PRIORITY_ORDER.put(CmdAddItem.class, 100);
		}

		@Override
		public int compare(Command aLHS, Command aRHS) {
			// This is needed to make sure that engines are added first
			if (aLHS instanceof CmdAddItem && aRHS instanceof CmdAddItem) {
				final boolean lhsEngine = ((CmdAddItem) aLHS).getItem() instanceof Engine;
				final boolean rhsEngine = ((CmdAddItem) aRHS).getItem() instanceof Engine;

				if (lhsEngine == rhsEngine) {
					return 0;
				} else if (lhsEngine) {
					return -1;
				} else {
					return 1;
				}
			}

			final Integer priorityLHS = CLASS_PRIORITY_ORDER.get(aLHS.getClass());
			final Integer priorityRHS = CLASS_PRIORITY_ORDER.get(aRHS.getClass());

			if (null == priorityLHS) {
				throw new IllegalArgumentException(
						"Class missing from priority map: " + aLHS.getClass().getSimpleName());
			}

			if (null == priorityRHS) {
				throw new IllegalArgumentException(
						"Class missing from priority map: " + aRHS.getClass().getSimpleName());
			}
			return priorityLHS.compareTo(priorityRHS);
		}
	}

	final private List<Command> operations = new ArrayList<>(20);
	private final List<Throwable> errors = new ArrayList<>();

	@Inject
	public LoadoutBuilder() {
		/* Nop */
	}

	public void apply() {
		final CommandStack operationStack = new CommandStack(0);
		Collections.sort(operations, new OperationComparator());

		for (final Command op : operations) {
			try {
				operationStack.pushAndApply(op);
			} catch (final Throwable t) {
				pushError(t);
			}
		}
	}

	public void push(final Command aOperation) {
		operations.add(aOperation);
	}

	/**
	 * Push a error onto the list of errors for this {@link Loadout}.
	 *
	 * @param aThrowable
	 *            The exception to push.
	 */
	public void pushError(Throwable aThrowable) {
		errors.add(aThrowable);
	}

	/**
	 * Formats a string to describe the errors that occurred while building the
	 * loadout.
	 *
	 * @param aLoadout
	 *            The loadout that the errors are for.
	 * @param aCallback
	 *            The callback to report the errors to.
	 */
	public void reportErrors(Loadout aLoadout, ErrorReporter aCallback) {
		if (!errors.isEmpty() && aCallback != null) {
			aCallback.error(null, aLoadout, errors);
		}
	}

	/**
	 * Resets this builder for re-use.
	 */
	public void reset() {
		operations.clear();
		errors.clear();
	}
}
