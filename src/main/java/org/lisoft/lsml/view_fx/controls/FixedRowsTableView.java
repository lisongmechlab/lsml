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

import static javafx.beans.binding.Bindings.selectDouble;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

/**
 * This control displays a fixed number of rows with equal height where the cells can span multiple
 * rows.
 *
 * <p>Any custom cell factory used with this list view must return cells of the type {@link
 * FixedTableRow} or inheriting from it.
 *
 * @param <T> The type to show in the list.
 * @author Li Song
 */
public class FixedRowsTableView<T> extends TableView<T> {
  /**
   * A custom cell for {@link FixedRowsTableView}. Makes sure the cells have the correct size.
   *
   * @param <T> The type contained in the this cell is for {@link FixedRowsTableView}.
   * @author Li Song
   */
  public static class FixedTableRow<T> extends TableRow<T> {
    public static final int DEFAULT_SIZE = 1;
    protected final IntegerProperty rowSpan = new SimpleIntegerProperty(DEFAULT_SIZE);

    public FixedTableRow(FixedRowsTableView<T> aItemView) {
      prefHeightProperty().bind(rowSpan.multiply(aItemView.rowHeight));

      minHeightProperty().bind(prefHeightProperty());
      maxHeightProperty().bind(prefHeightProperty());
    }

    /**
     * @return The current value of the {@link #rowSpanProperty()}.
     */
    public int getRowSpan() {
      return rowSpan.get();
    }

    /**
     * @return The size in rows of this cell. By default 1.
     */
    public IntegerProperty rowSpanProperty() {
      return rowSpan;
    }

    /**
     * Sets the {@link #rowSpanProperty()} to the given value.
     *
     * @param aRows A new size.
     */
    public void setRowSpan(int aRows) {
      if (!(aRows > 0)) {
        throw new IllegalArgumentException("Size must be larger than 0");
      }
      rowSpan.set(aRows);
    }
  }

  public static final double DEFAULT_HEIGHT = 25.0;
  private static final int DEFAULT_ROWS = 6;
  private final DoubleProperty rowHeight = new SimpleDoubleProperty(DEFAULT_HEIGHT);
  private final IntegerProperty rows = new SimpleIntegerProperty(DEFAULT_ROWS);

  public FixedRowsTableView() {
    setRowFactory((aTable) -> new FixedTableRow<>((FixedRowsTableView<T>) aTable));
    final DoubleBinding padding =
        selectDouble(paddingProperty(), "bottom").add(selectDouble(paddingProperty(), "top"));

    prefHeightProperty().bind(rowHeight.multiply(rows.add(1.2)).add(padding).add(30));
    maxHeightProperty().bind(prefHeightProperty());
    minHeightProperty().bind(prefHeightProperty());
  }

  public double getRowHeight() {
    return rowHeight.get();
  }

  public int getVisibleRows() {
    return rows.get();
  }

  public DoubleProperty rowHeightProperty() {
    return rowHeight;
  }

  public void setRowHeight(double aNewValue) {
    rowHeight.set(aNewValue);
  }

  public void setVisibleRows(int aNewValue) {
    rows.set(aNewValue);
  }

  /**
   * @return The property for the number of rows that should be shown.
   */
  public IntegerProperty visibleRowsProperty() {
    return rows;
  }
}
