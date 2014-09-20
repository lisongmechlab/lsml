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
package lisong_mechlab.model.loadout;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.loadout.component.OpAddItem;
import lisong_mechlab.model.loadout.component.OpChangeOmniPod;
import lisong_mechlab.model.loadout.component.OpSetArmor;
import lisong_mechlab.model.loadout.component.OpToggleItem;
import lisong_mechlab.model.upgrades.OpSetArmorType;
import lisong_mechlab.model.upgrades.OpSetGuidanceType;
import lisong_mechlab.model.upgrades.OpSetHeatSinkType;
import lisong_mechlab.model.upgrades.OpSetStructureType;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.OperationStack.Operation;

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
			if (aLHS instanceof OpAddItem && aRHS instanceof OpAddItem) {
				boolean lhsHeatSink = ((OpAddItem) aLHS).getItem() instanceof HeatSink;
				boolean rhsHeatSink = ((OpAddItem) aRHS).getItem() instanceof HeatSink;

				if (lhsHeatSink == rhsHeatSink)
					return 0;
				else if (lhsHeatSink)
					return 1;
				else
					return -1;
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

	final private PriorityQueue<Operation> operations = new PriorityQueue<>(20, new OperationComparator());

	public void push(final Operation aOperation) {
		operations.add(aOperation);
	}

	public void apply() {
		OperationStack operationStack = new OperationStack(0);

		Operation operation;
		while (null != (operation = operations.poll())) {
			operationStack.pushAndApply(operation);
		}
	}
}
