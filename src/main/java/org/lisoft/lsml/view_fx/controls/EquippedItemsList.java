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
package org.lisoft.lsml.view_fx.controls;

import java.util.Arrays;
import java.util.List;
import javafx.collections.ObservableListBase;
import org.lisoft.lsml.messages.*;
import org.lisoft.lsml.messages.UpgradesMessage.ChangeMsg;
import org.lisoft.lsml.model.DynamicSlotDistributor;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.ConfiguredComponentOmniMech;
import org.lisoft.mwo_data.equipment.Engine;
import org.lisoft.mwo_data.equipment.HeatSink;
import org.lisoft.mwo_data.equipment.Internal;
import org.lisoft.mwo_data.equipment.Item;

/**
 * This is an observable, read-only list of the equipment on a component of a loadout.
 *
 * @author Li Song
 */
public class EquippedItemsList extends ObservableListBase<Item> implements MessageReceiver {
  private static class Classification {
    public final Item item;
    public final EquippedType type;

    public Classification(Item aItem, EquippedType aType) {
      item = aItem;
      type = aType;
    }
  }

  private final ConfiguredComponent component;
  private final DynamicSlotDistributor distributor;

  public EquippedItemsList(
      MessageReception aMessageReception,
      ConfiguredComponent aComponent,
      DynamicSlotDistributor aDistributor) {
    aMessageReception.attach(this);
    distributor = aDistributor;
    component = aComponent;
  }

  @Override
  public Item get(int aIndex) {
    return classify(aIndex).item;
  }

  public boolean isFixed(int aIndex) {
    return classify(aIndex).type != EquippedType.EQUIPPED;
  }

  @Override
  public void receive(Message aMsg) {
    if (!(aMsg instanceof final ItemMessage msg)) {
      if (aMsg instanceof final UpgradesMessage msg) {
        if (msg.msg == ChangeMsg.ARMOUR || msg.msg == ChangeMsg.STRUCTURE) {
          changeDynamics();
        }
      } else if (aMsg instanceof final OmniPodMessage msg) {
        if (msg.component == component) {
          beginChange();
          final int size = size();
          for (int i = 0; i < size; ++i) {
            nextUpdate(i);
          }
          endChange();
        }
      }
      return;
    }

    if (!(msg.component == component)) {
      changeDynamics();
      return;
    }

    beginChange();
    switch (msg.type) {
      case Added -> {
        if (msg.relativeIndex < 0) {
          if (msg.item instanceof HeatSink) {
            nextEngineUpdate();
          } else {
            final int fixedIdx = component.getItemsFixed().size() - 1;
            nextAdd(fixedIdx, fixedIdx + 1);
          }
        } else {
          nextAdd(msg.relativeIndex, msg.relativeIndex + 1);
        }
        break;
      }
      case Removed -> {
        if (msg.relativeIndex < 0) {
          if (msg.item instanceof HeatSink) {
            nextEngineUpdate();
          } else {
            final int fixedIdx = msg.component.getItemsFixed().size() - 1;
            nextRemove(fixedIdx, msg.item);
          }
        } else {
          nextRemove(msg.relativeIndex, msg.item);
        }
        break;
      }
      default -> throw new RuntimeException("Unknown message type!");
    }

    endChange();
  }

  @Override
  public int size() {
    final int actualSize = sizeOfItems() + sizeOfDynamics();
    // Size must always at least one to prevent "empty list" display from happening
    // when the contents are empty. This means that a "null" item will appear
    // in the rendering but this is OK as they do that already anyway.
    return Math.max(actualSize, 1);
  }

  private void changeDynamics() {
    beginChange();
    nextUpdateDynamic();
    endChange();
  }

  private Classification classify(int aIndex) {
    int visibleLeft = aIndex;
    int engineHeatSinks = component.getEngineHeatSinks();

    EquippedType type = EquippedType.FIXED;
    for (final List<Item> items :
        Arrays.asList(component.getItemsFixed(), component.getItemsEquipped())) {
      for (final Item item : items) {
        if (engineHeatSinks > 0 && item instanceof HeatSink) {
          engineHeatSinks--; // Consumed by engine
        } else {
          if (visibleLeft == 0) {
            if (item instanceof Internal) {
              type = EquippedType.FIXED;
            }
            return new Classification(item, type);
          }
          visibleLeft--;
        }
      }
      type = EquippedType.EQUIPPED;
    }

    final boolean omni = component instanceof ConfiguredComponentOmniMech;
    final int armourSlots =
        distributor.getDynamicArmourSlots(component.getInternalComponent().getLocation());
    if (visibleLeft < armourSlots) {
      return new Classification(
          omni ? ItemDB.FIX_ARMOUR : ItemDB.DYN_ARMOUR, EquippedType.DYN_ARMOUR);
    }

    visibleLeft -= armourSlots;
    if (visibleLeft
        < distributor.getDynamicStructureSlots(component.getInternalComponent().getLocation())) {
      return new Classification(
          omni ? ItemDB.FIX_STRUCT : ItemDB.DYN_STRUCT, EquippedType.DYN_STRUCTURE);
    }
    return new Classification(null, EquippedType.EMPTY);
  }

  private void nextEngineUpdate() {
    int enginePos = 0;
    while (enginePos < size() && !(get(enginePos) instanceof Engine)) {
      enginePos++;
    }
    nextUpdate(enginePos);
  }

  private void nextUpdateDynamic() {
    int start = sizeOfItems();
    // We have to go to the end here as we don't know how many slots of
    // dynamic armour were here originally.
    final int end = component.getInternalComponent().getSlots();
    while (start < end) {
      nextUpdate(start);
      start++;
    }
  }

  private int sizeOfDynamics() {
    final int armour =
        distributor.getDynamicArmourSlots(component.getInternalComponent().getLocation());
    final int structure =
        distributor.getDynamicStructureSlots(component.getInternalComponent().getLocation());
    return armour + structure;
  }

  private int sizeOfItems() {
    final List<Item> fixed = component.getItemsFixed();
    final List<Item> equipped = component.getItemsEquipped();

    final int engineHS = component.getEngineHeatSinks();
    return fixed.size() + equipped.size() - engineHS;
  }

  enum EquippedType {
    FIXED,
    EQUIPPED,
    DYN_ARMOUR,
    DYN_STRUCTURE,
    EMPTY
  }
}
