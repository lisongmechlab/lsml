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
package lisong_mechlab.model.metrics.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.metrics.CriticalStrikeProbability;
import lisong_mechlab.util.message.Message;
import lisong_mechlab.util.message.MessageXBar;

/**
 * This class performs a simulated destruction of a component by large alphas [1] and for each item calculates the
 * probability that it is destroyed before the component's health reaches 0.
 * <p>
 * [1]: By "large" we mean big enough to destroy ammunition in one shot but not larger.
 * <p>
 * Assumptions:
 * <ul>
 * <li>Internal components (actuators, gyros etc with exception of XL side torsos) can not be critically hit.</li>
 * <li>Engine can be critically hit and destroyed. However a destroyed engine does not kill you, it's simply a free
 * buffer.</li>
 * <li>Heat sinks in the engine act as a crit buffer with their original slot counts for probability.</li>
 * <li>15% of critical damage is always transferred to the component IS. FIXME: NYI</li>
 * </ul>
 * 
 * @author Li Song
 */
public class ComponentDestructionSimulator implements Message.Recipient {
	private final ConfiguredComponentBase	loadoutPart;
	private final double					P_miss;
	private final double					weaponAlpha;
	private final int						numShots;
	private final double					partHp;

	class ItemState {
		int		multiplicity;
		double	hpLeft;
		double	P_destroyed;

		ItemState(int aMulti, Item aItem) {
			multiplicity = aMulti;
			hpLeft = aItem instanceof Engine ? Double.POSITIVE_INFINITY : aItem.getHealth() * multiplicity;
			P_destroyed = 0.0;
		}

		ItemState(ItemState aState) {
			multiplicity = aState.multiplicity;
			hpLeft = aState.hpLeft;
			P_destroyed = aState.P_destroyed;
		}
	}

	// Key: Item - Value: <multiplicity, total probability>
	private Map<Item, ItemState>	state;

	private Map<Item, ItemState> cloneState(Map<Item, ItemState> aMap) {
		Map<Item, ItemState> ans = new HashMap<>(aMap.size());
		for (Entry<Item, ItemState> entry : aMap.entrySet()) {
			ans.put(entry.getKey(), new ItemState(entry.getValue()));
		}
		return ans;
	}

	/**
	 * @param aLoadoutPart
	 * @param aXBar
	 */
	public ComponentDestructionSimulator(ConfiguredComponentBase aLoadoutPart, MessageXBar aXBar) {
		loadoutPart = aLoadoutPart;
		aXBar.attach(this);

		double p_miss = 1.0;
		for (int i = 0; i < CriticalStrikeProbability.CRIT_CHANCE.length; ++i) {
			p_miss -= CriticalStrikeProbability.CRIT_CHANCE[i];
		}
		P_miss = p_miss;

		partHp = loadoutPart.getInternalComponent().getHitPoints();
		weaponAlpha = ItemDB.lookup("AC/20 AMMO").getHealth();
		numShots = (int) Math.ceil(partHp / weaponAlpha);
	}

	public double getProbabilityOfDestruction(Item aItem) {
		ItemState itemState = state.get(aItem);
		if (itemState == null)
			return 0.0;

		return itemState.P_destroyed / itemState.multiplicity;
	}

	public void simulate() {
		state = new HashMap<>();
		int slots = 0;
		for (Item item : loadoutPart.getItemsEquipped()) {
			if (!item.isCrittable())
				continue;

			slots += item.getNumCriticalSlots();

			ItemState pair = state.get(item);
			if (pair == null)
				state.put(item, new ItemState(1, item));
			else {
				pair.multiplicity++;
				pair.hpLeft += item.getHealth();
			}
		}

		for (Item item : loadoutPart.getItemsFixed()) {
			if (!item.isCrittable())
				continue;

			slots += item.getNumCriticalSlots();

			ItemState pair = state.get(item);
			if (pair == null)
				state.put(item, new ItemState(1, item));
			else {
				pair.multiplicity++;
				pair.hpLeft += item.getHealth();
			}
		}

		simulateShot(state, slots, 1.0, numShots);
	}

	private void simulateShot(Map<Item, ItemState> aState, int aTotalSlots, double aP_this, int aShotsLeft) {
		simulateRound(aState, P_miss * aP_this, aTotalSlots, 0, aShotsLeft); // No critical hits
		for (int i = 0; i < CriticalStrikeProbability.CRIT_CHANCE.length; ++i) {
			simulateRound(aState, CriticalStrikeProbability.CRIT_CHANCE[i] * aP_this, aTotalSlots, i + 1, aShotsLeft);
		}
	}

	/**
	 * HERE BE DRAGONS! DO NOT TOUCH!
	 * 
	 * @param aState
	 * @param aP_this
	 * @param aTotalSlots
	 * @param aShotsLeft
	 * @param aCritRollsLeft
	 */
	private void simulateRound(Map<Item, ItemState> aState, double aP_this, int aTotalSlots, int aCritRollsLeft,
			int aShotsLeft) {
		if (aShotsLeft <= 0)
			return;
		if (aP_this < 0.0005) {
			return; // Too small to have significant effect on results
		}

		// If there are critical hit rolls left for this shot, perform them
		if (aCritRollsLeft > 0) {
			// For every item that can be hit...
			for (Entry<Item, ItemState> entry : aState.entrySet()) {
				Item item = entry.getKey();
				int itemSlots = item.getNumCriticalSlots();
				int multi = entry.getValue().multiplicity;

				// Determine the probability that it'll be hit
				double P_hit = ((double) itemSlots) / aTotalSlots;
				P_hit *= multi;

				// Generate a new state where the item has been destroyed
				Map<Item, ItemState> newState = cloneState(aState);
				ItemState pair = newState.get(item);
				if (pair.hpLeft <= weaponAlpha + Math.ulp(weaponAlpha) * 10) {
					if (pair.multiplicity == 1) {
						newState.remove(item);
					} else {
						pair.multiplicity--;
					}
					updateResultProbability(item, P_hit * aP_this);
				} else {
					pair.hpLeft -= weaponAlpha;
					itemSlots = 0;
				}
				simulateRound(newState, aP_this * P_hit, aTotalSlots - itemSlots, aCritRollsLeft - 1, aShotsLeft);
			}
		} else {
			simulateShot(aState, aTotalSlots, aP_this, aShotsLeft - 1);
		}
	}

	private void updateResultProbability(Item aItem, double aP) {
		ItemState itemState = state.get(aItem);
		itemState.P_destroyed += aP;
	}

	@Override
	public void receive(Message aMsg) {
		if (aMsg instanceof ConfiguredComponentBase.ComponentMessage) {
			ConfiguredComponentBase.ComponentMessage message = (ConfiguredComponentBase.ComponentMessage) aMsg;
			if (message.component == loadoutPart && message.affectsHeatOrDamage()) {
				simulate();
			}
		}
	}
}
