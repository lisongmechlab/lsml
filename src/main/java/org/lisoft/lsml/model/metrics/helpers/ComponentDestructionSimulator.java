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
package org.lisoft.lsml.model.metrics.helpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.metrics.CriticalStrikeProbability;
import org.lisoft.lsml.mwo_data.equipment.Engine;
import org.lisoft.lsml.mwo_data.equipment.Item;
import org.lisoft.lsml.mwo_data.modifiers.Modifier;

/**
 * This class performs a simulated destruction of a component by large alphas [1] and for each item
 * calculates the probability that it is destroyed before the component's health reaches 0.
 *
 * <p>[1]: By "large" we mean big enough to destroy ammunition in one shot but not larger.
 *
 * <p>Assumptions:
 *
 * <ul>
 *   <li>Internal components (actuators, gyros etc with exception of XL side torsos) can not be
 *       critically hit.
 *   <li>Engine can be critically hit and destroyed. However a destroyed engine does not kill you,
 *       it's simply a free buffer.
 *   <li>Heat sinks in the engine act as a crit buffer with their original slot counts for
 *       probability.
 *   <li>15% of critical damage is always transferred to the component IS. FIXME: NYI
 * </ul>
 *
 * @author Li Song
 */
public class ComponentDestructionSimulator {
  private static class ItemState {
    double P_destroyed;
    double healthLeft;
    int multiplicity;

    ItemState() {
      multiplicity = 0;
      healthLeft = 0.0;
      P_destroyed = 0.0;
    }

    ItemState(ItemState aState) {
      multiplicity = aState.multiplicity;
      healthLeft = aState.healthLeft;
      P_destroyed = aState.P_destroyed;
    }

    void add(Item aItem) {
      final double hp;
      if (aItem instanceof Engine) {
        // Engines are currently indestructible and act as infinite crit buffer.
        hp = Double.POSITIVE_INFINITY;
      } else {
        hp = aItem.getHealth();
      }
      multiplicity++;
      healthLeft += hp;
    }
  }

  private static final double WEAPON_ALPHA = 10.0;
  private final ConfiguredComponent component;
  // Key: Item - Value: <multiplicity, total probability>
  private final Map<Item, ItemState> stateMap = new HashMap<>();

  /**
   * Creates a new {@link ComponentDestructionSimulator}.
   *
   * @param aComponent The component to simulate for.
   */
  public ComponentDestructionSimulator(ConfiguredComponent aComponent) {
    component = aComponent;
  }

  public double getProbabilityOfDestruction(Item aItem) {
    final ItemState itemState = stateMap.get(aItem);
    if (itemState == null) {
      return 0.0;
    }

    return itemState.P_destroyed / itemState.multiplicity;
  }

  /**
   * Updates the simulated results.
   *
   * @param aModifiers A {@link Collection} of {@link Modifier}s to use for affecting the
   *     simulation.
   */
  public void simulate(Collection<Modifier> aModifiers) {
    final double componentHealth = component.getInternalComponent().getHitPoints(aModifiers);
    final int numShots = (int) Math.ceil(componentHealth / WEAPON_ALPHA);
    stateMap.clear();

    int slots = 0;
    for (final Item item : component.getItemsEquipped()) {
      slots += addItemToSimulation(item);
    }

    for (final Item item : component.getItemsFixed()) {
      slots += addItemToSimulation(item);
    }

    simulateShot(stateMap, slots, 1.0, numShots);
  }

  private int addItemToSimulation(Item aItem) {
    if (!aItem.isCrittable()) {
      return 0;
    }

    stateMap.computeIfAbsent(aItem, x -> new ItemState()).add(aItem);
    return aItem.getSlots();
  }

  private Map<Item, ItemState> cloneState(Map<Item, ItemState> aMap) {
    final Map<Item, ItemState> ans = new HashMap<>(aMap.size());
    for (final Entry<Item, ItemState> entry : aMap.entrySet()) {
      ans.put(entry.getKey(), new ItemState(entry.getValue()));
    }
    return ans;
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
  private void simulateRound(
      Map<Item, ItemState> aState,
      double aP_this,
      int aTotalSlots,
      int aCritRollsLeft,
      int aShotsLeft) {
    if (aShotsLeft <= 0) {
      return;
    }
    if (aP_this < 0.0005) {
      return; // Too small to have significant effect on results
    }

    // If there are critical hit rolls left for this shot, perform them
    if (aCritRollsLeft > 0) {
      // For every item that can be hit...
      for (final Entry<Item, ItemState> entry : aState.entrySet()) {
        final Item item = entry.getKey();
        int itemSlots = item.getSlots();
        final int multi = entry.getValue().multiplicity;

        // Determine the probability that it'll be hit
        double P_hit = (double) itemSlots / aTotalSlots;
        P_hit *= multi;

        // Generate a new state where the item has been destroyed
        final Map<Item, ItemState> newState = cloneState(aState);
        final ItemState pair = newState.get(item);
        if (pair.healthLeft <= WEAPON_ALPHA + Math.ulp(WEAPON_ALPHA) * 10) {
          if (pair.multiplicity == 1) {
            newState.remove(item);
          } else {
            pair.multiplicity--;
          }
          updateResultProbability(item, P_hit * aP_this);
        } else {
          pair.healthLeft -= WEAPON_ALPHA;
          itemSlots = 0;
        }
        simulateRound(
            newState, aP_this * P_hit, aTotalSlots - itemSlots, aCritRollsLeft - 1, aShotsLeft);
      }
    } else {
      simulateShot(aState, aTotalSlots, aP_this, aShotsLeft - 1);
    }
  }

  private void simulateShot(
      Map<Item, ItemState> aState, int aTotalSlots, double aP_this, int aShotsLeft) {
    // No critical hits
    simulateRound(
        aState, CriticalStrikeProbability.MISS_CHANCE * aP_this, aTotalSlots, 0, aShotsLeft);

    for (int i = 0; i < CriticalStrikeProbability.CRITICAL_HIT_CHANCE.size(); ++i) {
      simulateRound(
          aState,
          CriticalStrikeProbability.CRITICAL_HIT_CHANCE.get(i) * aP_this,
          aTotalSlots,
          i + 1,
          aShotsLeft);
    }
  }

  private void updateResultProbability(Item aItem, double aP) {
    final ItemState itemState = stateMap.get(aItem);
    itemState.P_destroyed += aP;
  }
}
