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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lisoft.lsml.command.OpAddItem;
import org.lisoft.lsml.command.OpAddModule;
import org.lisoft.lsml.command.OpChangeOmniPod;
import org.lisoft.lsml.command.OpRename;
import org.lisoft.lsml.command.OpSetArmor;
import org.lisoft.lsml.command.OpToggleItem;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.upgrades.OpSetArmorType;
import org.lisoft.lsml.model.upgrades.OpSetGuidanceType;
import org.lisoft.lsml.model.upgrades.OpSetHeatSinkType;
import org.lisoft.lsml.model.upgrades.OpSetStructureType;
import org.lisoft.lsml.util.OperationStack;
import org.lisoft.lsml.util.OperationStack.Operation;

/**
 * This class promises to take care of dependency issues when de-serialising any loadout.
 * <p>
 * One passes the {@link Operation}s that would be used to naively construct the loadout to the {@link #push(Operation)}
 * method. Once all operations have been pushed, one applies the operations loadout with the {@link #apply()} method.
 * The call to {@link #apply()} will re-order and apply the pushed {@link Operation}s in an order that allows the
 * loadout to be constructed without violating validity invariants during creation.
 * 
 * @author Li Song
 */
public class LoadoutBuilder {
    private static class OperationComparator implements Comparator<Operation> {
        private final static Map<Class<? extends Operation>, Integer> CLASS_PRIORITY_ORDER;

        static {
            CLASS_PRIORITY_ORDER = new HashMap<>();

            // Omnipods, upgrades, modules and renaming are independent and cannot fail on an empty loadout
            CLASS_PRIORITY_ORDER.put(OpRename.class, 0);
            CLASS_PRIORITY_ORDER.put(OpChangeOmniPod.class, 1);
            CLASS_PRIORITY_ORDER.put(OpSetGuidanceType.class, 2);
            CLASS_PRIORITY_ORDER.put(OpSetHeatSinkType.class, 2);
            CLASS_PRIORITY_ORDER.put(OpSetArmorType.class, 2);
            CLASS_PRIORITY_ORDER.put(OpSetStructureType.class, 2);
            CLASS_PRIORITY_ORDER.put(OpSetArmor.class, 3);
            CLASS_PRIORITY_ORDER.put(OpAddModule.class, 4);

            // Toggleables have to be set before items are added
            CLASS_PRIORITY_ORDER.put(OpToggleItem.class, 10);

            // Item operations last
            CLASS_PRIORITY_ORDER.put(OpAddItem.class, 100);
        }

        @Override
        public int compare(Operation aLHS, Operation aRHS) {
            // This is needed to make sure that engines are added first
            if (aLHS instanceof OpAddItem && aRHS instanceof OpAddItem) {
                boolean lhsEngine = ((OpAddItem) aLHS).getItem() instanceof Engine;
                boolean rhsEngine = ((OpAddItem) aRHS).getItem() instanceof Engine;

                if (lhsEngine == rhsEngine)
                    return 0;
                else if (lhsEngine)
                    return -1;
                else
                    return 1;
            }

            Integer priorityLHS = CLASS_PRIORITY_ORDER.get(aLHS.getClass());
            Integer priorityRHS = CLASS_PRIORITY_ORDER.get(aRHS.getClass());

            if (null == priorityLHS) {
                throw new IllegalArgumentException("Class missing from priority map: "
                        + aLHS.getClass().getSimpleName());
            }

            if (null == priorityRHS) {
                throw new IllegalArgumentException("Class missing from priority map: "
                        + aRHS.getClass().getSimpleName());
            }
            return priorityLHS.compareTo(priorityRHS);
        }
    }

    final private List<Operation> operations = new ArrayList<>(20);
    private List<Throwable>                errors     = null;

    public void push(final Operation aOperation) {
        operations.add(aOperation);
    }

    /**
     * Formats a string to describe the errors that occurred while building the loadout.
     * 
     * @param name
     *            The name of the loadout. Used to format the error message.
     * @return <code>null</code> if there was no error. A string describing the error(s) if there was any.
     */
    public String getErrors(String name) {
        if (errors == null)
            return null;

        StringBuilder message = new StringBuilder();
        message.append("The following errors occured for loadout: ").append(name).append("\n\n");
        for (Throwable t : errors) {
            message.append(t.getMessage()).append("\n");
        }
        message.append("\nAs much as possible of the loadout has been loaded.");
        return message.toString();
    }

    public void apply() {
        OperationStack operationStack = new OperationStack(0);
        Collections.sort(operations, new OperationComparator());
        
        for(Operation op : operations){
            try {
                operationStack.pushAndApply(op);
            }
            catch (Throwable t) {
                if (null == errors)
                    errors = new ArrayList<>();
                errors.add(t);
            }
        }
    }
}
