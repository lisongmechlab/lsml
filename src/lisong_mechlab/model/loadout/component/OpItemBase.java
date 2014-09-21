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
package lisong_mechlab.model.loadout.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lisong_mechlab.model.NotificationMessage;
import lisong_mechlab.model.NotificationMessage.Severity;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.Faction;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.ComponentMessage;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.ComponentMessage.Type;
import lisong_mechlab.util.OperationStack.Operation;
import lisong_mechlab.util.message.MessageDelivery;

/**
 * A helper class for implementing {@link Operation}s that affect items on a {@link ConfiguredComponentBase}.
 * 
 * @author Emily Björk
 */
public abstract class OpItemBase extends Operation {
	private int								numEngineHS		= 0;
	private final MessageDelivery			messageDelivery;
	protected final ConfiguredComponentBase	component;
	protected final LoadoutBase<?>			loadout;
	protected final Item					item;
	protected final Map<Item, Boolean>		oldToggleStates	= new HashMap<>();

	/**
	 * Creates a new {@link OpItemBase}. The deriving classes shall throw if the the operation with the given item would
	 * violate the {@link LoadoutStandard} or {@link ConfiguredComponentBase} invariant.
	 * 
	 * @param aMessageDelivery
	 *            The {@link MessageDelivery} to send messages to when changes occur.
	 * @param aLoadout
	 *            The {@link LoadoutBase} to operate on.
	 * @param aComponent
	 *            The {@link ConfiguredComponentBase} that this operation will affect.
	 * @param aItem
	 *            The {@link Item} to add or remove.
	 */
	protected OpItemBase(MessageDelivery aMessageDelivery, LoadoutBase<?> aLoadout, ConfiguredComponentBase aComponent,
			Item aItem) {
		if (aItem instanceof Internal)
			throw new IllegalArgumentException("Can't add/remove internals to/from a loadout!");

		loadout = aLoadout;
		component = aComponent;
		messageDelivery = aMessageDelivery;
		item = aItem;
	}

	/**
	 * @return The {@link Item} that is the argument of the operation.
	 */
	public Item getItem() {
		return item;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((component == null) ? 0 : component.hashCode());
		result = prime * result + numEngineHS;
		return result;
	}

	@Override
	public boolean equals(Object aObject) {
		if (!(aObject instanceof OpItemBase))
			return false;

		OpItemBase other = (OpItemBase) aObject;
		return component == other.component;
	}

	/**
	 * Removes an item without checks. Will count up the numEngineHS variable to the number of heat sinks removed.
	 * 
	 * @param aItem
	 *            The item to remove.
	 */
	protected void removeItem(Item aItem) {

		if (aItem instanceof Engine) {
			Engine engine = (Engine) aItem;
			if (engine.getType() == EngineType.XL) {
				ConfiguredComponentBase lt = loadout.getComponent(Location.LeftTorso);
				ConfiguredComponentBase rt = loadout.getComponent(Location.RightTorso);

				Internal xlSide = engine.getFaction() == Faction.Clan ? ConfiguredComponentBase.ENGINE_INTERNAL_CLAN
						: ConfiguredComponentBase.ENGINE_INTERNAL;
				lt.removeItem(xlSide);
				rt.removeItem(xlSide);
				if (messageDelivery != null) {
					messageDelivery.post(new ComponentMessage(lt, Type.ItemRemoved));
					messageDelivery.post(new ComponentMessage(rt, Type.ItemRemoved));
				}
			}

			int engineHsLeft = component.getEngineHeatsinks();
			HeatSink heatSinkType = loadout.getUpgrades().getHeatSink().getHeatSinkType();
			while (engineHsLeft > 0) {
				engineHsLeft--;
				numEngineHS++;
				component.removeItem(heatSinkType);
			}
		}

		restoreForcedToggles(aItem);

		component.removeItem(aItem);
		if (messageDelivery != null) {
			messageDelivery.post(new ComponentMessage(component, Type.ItemRemoved));
		}
	}

	/**
	 * Adds an item without checks. Will add numEngineHS heat sinks if the item is an engine.
	 * 
	 * @param aItem
	 *            The item to add.
	 */
	protected void addItem(Item aItem) {
		if (aItem instanceof Engine) {
			Engine engine = (Engine) aItem;
			if (engine.getType() == EngineType.XL) {
				ConfiguredComponentBase lt = loadout.getComponent(Location.LeftTorso);
				ConfiguredComponentBase rt = loadout.getComponent(Location.RightTorso);

				Internal xlSide = engine.getFaction() == Faction.Clan ? ConfiguredComponentBase.ENGINE_INTERNAL_CLAN
						: ConfiguredComponentBase.ENGINE_INTERNAL;
				lt.addItem(xlSide);
				rt.addItem(xlSide);
				if (messageDelivery != null) {
					messageDelivery.post(new ComponentMessage(lt, Type.ItemAdded));
					messageDelivery.post(new ComponentMessage(rt, Type.ItemAdded));
				}
			}
			while (numEngineHS > 0) {
				numEngineHS--;
				component.addItem(loadout.getUpgrades().getHeatSink().getHeatSinkType());
			}
		}

		applyForcedToggles(aItem);

		checkCaseXLWarning(aItem);
		checkManyGaussWarning(aItem);

		component.addItem(aItem);
		if (messageDelivery != null) {
			messageDelivery.post(new ComponentMessage(component, Type.ItemAdded));
		}
	}

	private void applyForcedToggles(Item aItem) {
		if (!(aItem instanceof Weapon) || !(component instanceof ConfiguredComponentOmniMech))
			return;

		Weapon weapon = (Weapon) aItem;
		if (weapon.isLargeBore()) {
			if (!oldToggleStates.isEmpty()) {
				// Restore toggle state
				for (Entry<Item, Boolean> entry : oldToggleStates.entrySet()) {
					ConfiguredComponentOmniMech ccom = (ConfiguredComponentOmniMech) component;
					ccom.setToggleState(entry.getKey(), entry.getValue());
				}
				oldToggleStates.clear();
			} else {
				// Force toggle off on HA/LAA
				ConfiguredComponentOmniMech ccom = (ConfiguredComponentOmniMech) component;

				if (ccom.getToggleState(ItemDB.HA)) {
					ccom.setToggleState(ItemDB.HA, false);
					oldToggleStates.put(ItemDB.HA, true);
				}
				if (ccom.getToggleState(ItemDB.LAA)) {
					ccom.setToggleState(ItemDB.LAA, false);
					oldToggleStates.put(ItemDB.LAA, true);
				}
			}
		}
	}

	private void restoreForcedToggles(Item aItem) {
		if (!(aItem instanceof Weapon) || !(component instanceof ConfiguredComponentOmniMech))
			return;

		Weapon weapon = (Weapon) aItem;
		if (weapon.isLargeBore()) {
			if (!oldToggleStates.isEmpty()) {
				// Restore toggle state
				for (Entry<Item, Boolean> entry : oldToggleStates.entrySet()) {
					ConfiguredComponentOmniMech ccom = (ConfiguredComponentOmniMech) component;
					ccom.setToggleState(entry.getKey(), entry.getValue());
				}
				oldToggleStates.clear();
			} else {
				ConfiguredComponentOmniMech ccom = (ConfiguredComponentOmniMech) component;

				component.removeItem(aItem); // Work around, checks would fail on the item to be removed otherwise.

				if (loadout.getNumCriticalSlotsFree() > 0 && ccom.canToggleOn(ItemDB.LAA)) {
					oldToggleStates.put(ItemDB.LAA, ccom.getToggleState(ItemDB.LAA));
					ccom.setToggleState(ItemDB.LAA, true);
				}
				if (loadout.getNumCriticalSlotsFree() > 0 && ccom.canToggleOn(ItemDB.HA)) {
					oldToggleStates.put(ItemDB.HA, ccom.getToggleState(ItemDB.HA));
					ccom.setToggleState(ItemDB.HA, true);
				}

				component.addItem(aItem);
			}
		}
	}

	private void checkCaseXLWarning(Item aItem) {
		if (null != messageDelivery) {
			Engine engine = loadout.getEngine();
			if (aItem == ItemDB.CASE && engine != null && engine.getType() == EngineType.XL) {
				messageDelivery.post(new NotificationMessage(Severity.WARNING, loadout,
						"C.A.S.E. together with XL engine has no effect."));
			}
		}
	}

	private void checkManyGaussWarning(Item aItem) {
		if (null != messageDelivery) {
			if (aItem instanceof BallisticWeapon && aItem.getName().contains("GAUSS")) {
				int rifles = 0;
				for (ConfiguredComponentBase componentOmniMech : loadout.getComponents()) {
					boolean done = false;
					for (Item itemToCheck : componentOmniMech.getItemsEquipped()) { // Surely we won't have a fixed
																					// gauss
																					// rifle?
						if (itemToCheck instanceof BallisticWeapon && itemToCheck.getName().contains("GAUSS")) {
							rifles++;
							if (rifles >= 2) {
								messageDelivery.post(new NotificationMessage(Severity.WARNING, loadout,
										"Only two gauss rifles can be charged simultaneously."));
								done = true;
								break;
							}
						}
					}
					if (done)
						break;
				}
			}
		}
	}
}
