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
package lisong_mechlab.view.render;

import java.util.Collection;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.ComponentMessage.Type;
import lisong_mechlab.util.message.Message;
import lisong_mechlab.util.message.MessageXBar;

/**
 * This class is a helper class to map a display list index to an item and associated render state.
 * 
 * @author Emily Björk
 */
public class ComponentRenderer implements Message.Recipient {
	public enum RenderType {
		Empty, MultiSlot, Item, EngineHeatSink, LastSlot
	}

	public class RenderState {
		private RenderType	renderType;
		private Item		item;
		private boolean		isFixed;

		public RenderType getRenderType() {
			return renderType;
		}

		public Item getItem() {
			return item;
		}

		public boolean isFixed() {
			return isFixed;
		}
	}

	private final ConfiguredComponentBase	component;
	private final RenderState[]				states;
	private boolean							dirty			= true;
	private int								compactOffest;
	private final boolean					isCompact;
	private int								engineHsLeft	= 0;

	public ComponentRenderer(MessageXBar aXBar, ConfiguredComponentBase aComponent, boolean aCompact) {
		aXBar.attach(this);
		component = aComponent;
		isCompact = aCompact;

		states = new RenderState[component.getInternalComponent().getSlots()];
		for (int i = 0; i < states.length; ++i) {
			states[i] = new RenderState();
		}
	}

	public RenderState getRenderState(int aIndex) {
		if (dirty) {
			updateStates();
		}
		return states[compactOffest + aIndex];
	}

	private void updateStates() {
		engineHsLeft = component.getEngineHeatsinksMax();
		int offs = updateStates(0, component.getItemsFixed(), true);
		offs = updateStates(offs, component.getItemsEquipped(), false);
		while (offs < states.length) {
			states[offs].renderType = RenderType.Empty;
			states[offs].item = null;
			states[offs].isFixed = false;
			offs++;
		}

		compactOffest = 0;
		if (isCompact) {
			for (Item item : component.getInternalComponent().getFixedItems()) {
				compactOffest += item.getNumCriticalSlots();
			}
		}

		dirty = false;
	}

	private int updateStates(int aOffset, Collection<Item> aItems, boolean aIsFixed) {
		int idx = aOffset;

		for (Item item : aItems) {
			if (item instanceof HeatSink && engineHsLeft > 0) {
				engineHsLeft--;
				continue;
			}

			int slots = item.getNumCriticalSlots();
			states[idx].renderType = RenderType.Item;
			states[idx].isFixed = aIsFixed;
			states[idx].item = item;

			for (int slot = 1; slot < item.getNumCriticalSlots(); ++slot) {
				if (slot == item.getNumCriticalSlots() - 1) {
					if (item instanceof Engine) {
						states[idx + slot].renderType = RenderType.EngineHeatSink;
					} else {
						states[idx + slot].renderType = RenderType.LastSlot;
					}
				} else {
					states[idx + slot].renderType = RenderType.MultiSlot;
				}

				states[idx + slot].item = item;
				states[idx + slot].isFixed = aIsFixed;
			}

			idx += slots;
		}
		return idx;
	}

	public void setDirty() {
		dirty = true;
	}

	public int getFirstEmpty() {
		if (dirty) {
			updateStates();
		}
		for (int i = 0; i < states.length; ++i) {
			if (states[i].renderType == RenderType.Empty)
				return i - compactOffest;
		}
		return states.length - compactOffest;
	}

	public int getVisibleCount() {
		if (dirty) {
			updateStates();
		}
		return states.length - compactOffest;
	}

	@Override
	public void receive(Message aMsg) {
		if (aMsg instanceof ConfiguredComponentBase.ComponentMessage) {
			ConfiguredComponentBase.ComponentMessage message = (ConfiguredComponentBase.ComponentMessage) aMsg;

			if (message.component == component) {
				if (message.type == Type.ArmorChanged || message.type == Type.ArmorDistributionUpdateRequest)
					return;
				setDirty();
			}
		}

	}
}
