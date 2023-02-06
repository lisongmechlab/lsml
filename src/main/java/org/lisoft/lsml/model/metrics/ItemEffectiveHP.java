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
package org.lisoft.lsml.model.metrics;

import java.util.ArrayList;
import java.util.List;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.mwo_data.equipment.Item;

/**
 * This class calculates the statistical effective HP of an {@link Item} when it is equipped on a
 * {@link ConfiguredComponent} under the assumption that damage is dealt in infinitesimal chunks.
 *
 * <p>This applies mostly to for lasers. MG and LB 10-X AC have higher critical hit probabilities
 * and different multipliers.
 *
 * @author Li Song
 */
public class ItemEffectiveHP implements ItemMetric {
  private static class ItemState {
    final Item item;
    double ehp;
    double hpLeft;

    ItemState(Item aItem) {
      item = aItem;
      hpLeft = aItem.getHealth();
      if (hpLeft == 0) {
        hpLeft = Double.POSITIVE_INFINITY; // Not breakable
      }
      ehp = 0;
    }
  }

  private final List<ItemState> cache = new ArrayList<>();
  private final ConfiguredComponent component;

  public ItemEffectiveHP(ConfiguredComponent aComponent) {
    component = aComponent;
  }

  @Override
  public double calculate(Item aItem) {
    updateCache();
    for (ItemState itemState : cache) {
      if (itemState.item == aItem) {
        return itemState.ehp;
      }
    }
    return Double.POSITIVE_INFINITY;
    // throw new RuntimeException("Item not found in EHP cache");
  }

  private void updateCache() {
    cache.clear();
    for (Item item : component.getItemsEquipped()) {
      if (item.canBeCriticallyHit()) {
        cache.add(new ItemState(item));
      }
    }
    for (Item item : component.getItemsFixed()) {
      if (item.canBeCriticallyHit()) {
        cache.add(new ItemState(item));
      }
    }
    final double tolerance = 10 * Math.ulp(1);

    boolean changed = true;
    while (changed) {
      int slotsLeft = 0;
      for (ItemState state : cache) {
        if (state.hpLeft > tolerance) {
          slotsLeft += state.item.getSlots();
        }
      }
      double minEHpLeft = Double.POSITIVE_INFINITY;
      for (ItemState state : cache) {
        if (state.hpLeft < tolerance) {
          continue;
        }
        minEHpLeft =
            Math.min(
                minEHpLeft,
                state.hpLeft / CriticalItemDamage.calculate(state.item.getSlots(), slotsLeft));
      }

      changed = false;
      for (ItemState state : cache) {
        double multiplier = CriticalItemDamage.calculate(state.item.getSlots(), slotsLeft);
        double actualDmg = minEHpLeft * multiplier;
        if (state.hpLeft > tolerance) {
          state.hpLeft -= actualDmg;
          state.ehp += actualDmg / multiplier;
          changed = true;
        }
      }
    }
  }
}
