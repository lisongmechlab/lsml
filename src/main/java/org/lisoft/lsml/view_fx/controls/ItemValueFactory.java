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

import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.util.Callback;
import org.lisoft.lsml.mwo_data.equipment.Item;

public class ItemValueFactory
    implements Callback<CellDataFeatures<Object, String>, ObservableValue<String>> {
  private final Function<Item, String> attribute;
  private final boolean showNonItems;

  public ItemValueFactory(Function<Item, String> aAttribute, boolean aShowNonItems) {
    attribute = aAttribute;
    showNonItems = aShowNonItems;
  }

  @Override
  public ObservableValue<String> call(CellDataFeatures<Object, String> aFeatures) {
    final TreeItem<Object> treeItem = aFeatures.getValue();
    if (treeItem != null) {
      final Object object = treeItem.getValue();
      if (object != null) {
        if (object instanceof Item) {
          final Item item = (Item) object;
          return new ReadOnlyStringWrapper(attribute.apply(item));
        } else if (showNonItems) {
          return new ReadOnlyStringWrapper(object.toString());
        }
      }
    }
    return new ReadOnlyStringWrapper("");
  }
}
