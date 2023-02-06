/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.model.loadout;

import java.io.Serializable;
import java.util.*;
import javax.inject.Inject;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.command.*;
import org.lisoft.lsml.mwo_data.ItemDB;
import org.lisoft.lsml.mwo_data.equipment.Engine;
import org.lisoft.lsml.mwo_data.equipment.Item;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This class promises to take care of dependency issues when de-serialising any loadout.
 *
 * <p>One passes the {@link Command}s that would be used to naively construct the loadout to the
 * {@link #push(Command)} method. Once all operations have been pushed, one applies the operations
 * loadout with the {@link #applyAll()} method. The call to {@link #applyAll()} will re-order and
 * apply the pushed {@link Command}s in an order that allows the loadout to be constructed without
 * violating validity invariants during creation.
 *
 * @author Li Song
 */
public class LoadoutBuilder {
  private static class OperationComparator implements Comparator<Command>, Serializable {
    private static final Map<Class<? extends Command>, Integer> CLASS_PRIORITY_ORDER;
    private static final Map<Item, Integer> PRIORITY_ITEMS;
    private static final long serialVersionUID = -5026656921652607661L;

    static {
      CLASS_PRIORITY_ORDER = new HashMap<>();

      // Omnipods, upgrades, modules and renaming are independent and cannot fail on
      // an empty loadout
      CLASS_PRIORITY_ORDER.put(CmdGarageRename.class, 0);
      CLASS_PRIORITY_ORDER.put(CmdSetOmniPod.class, 5);

      CLASS_PRIORITY_ORDER.put(CmdSetGuidanceType.class, 10);
      CLASS_PRIORITY_ORDER.put(CmdSetHeatSinkType.class, 10);
      CLASS_PRIORITY_ORDER.put(CmdSetArmourType.class, 10);
      CLASS_PRIORITY_ORDER.put(CmdSetStructureType.class, 10);

      CLASS_PRIORITY_ORDER.put(CmdSetArmour.class, 20);
      CLASS_PRIORITY_ORDER.put(CmdAddModule.class, 30);

      // Toggleables have to be set before items are added
      CLASS_PRIORITY_ORDER.put(CmdToggleItem.class, 50);

      // Item operations last
      CLASS_PRIORITY_ORDER.put(CmdAddItem.class, 100);

      PRIORITY_ITEMS = new HashMap<>();

      PRIORITY_ITEMS.put(ItemDB.ECM, 7); // Before armour
      for (final Engine e : ItemDB.lookup(Engine.class)) {
        PRIORITY_ITEMS.put(e, 6); // Before other things
      }
    }

    @Override
    public int compare(Command aLHS, Command aRHS) {
      return getPriority(aLHS).compareTo(getPriority(aRHS));
    }

    private Integer getPriority(Command aCmd) {
      if (aCmd instanceof CmdAddItem) {
        final CmdAddItem cmdAddItem = (CmdAddItem) aCmd;
        final Integer priorityItem = PRIORITY_ITEMS.get(cmdAddItem.getItem());
        if (null != priorityItem) {
          return priorityItem;
        }
      }
      final Integer priority = CLASS_PRIORITY_ORDER.get(aCmd.getClass());
      if (null == priority) {
        throw new IllegalArgumentException(
            "Class missing from priority map: " + aCmd.getClass().getSimpleName());
      }
      return priority;
    }
  }

  private final List<Throwable> errors = new ArrayList<>();
  private final List<Command> operations = new ArrayList<>(20);

  @Inject
  public LoadoutBuilder() {
    /* Nop */
  }

  public void applyAll() {
    final CommandStack operationStack = new CommandStack(0);

    for (final Command op : getAllCommands()) {
      try {
        operationStack.pushAndApply(op);
      } catch (final Throwable t) {
        pushError(t);
      }
    }
  }

  public List<Command> getAllCommands() {
    Collections.sort(operations, new OperationComparator());
    return operations;
  }

  public void push(final Command aOperation) {
    operations.add(aOperation);
  }

  /**
   * Push a error onto the list of errors for this {@link Loadout}.
   *
   * @param aThrowable The exception to push.
   */
  public void pushError(Throwable aThrowable) {
    errors.add(aThrowable);
  }

  /**
   * Formats a string to describe the errors that occurred while building the loadout.
   *
   * @param aLoadout The loadout that the errors are for.
   * @param aCallback The callback to report the errors to.
   */
  public void reportErrors(Loadout aLoadout, ErrorReporter aCallback) {
    if (!errors.isEmpty() && aCallback != null) {
      aCallback.error(null, aLoadout, errors);
    }
  }

  /** Resets this builder for re-use. */
  public void reset() {
    operations.clear();
    errors.clear();
  }
}
