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
package org.lisoft.lsml.view_fx.util;

import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.database.ModifiersDB;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.view_fx.controls.HardPointPane;
import org.lisoft.lsml.view_fx.style.FilteredModifierFormatter;
import org.lisoft.lsml.view_fx.style.HardPointFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains helper methods for making table setup with various types easier.
 *
 * @author Li Song
 */
public class FxTableUtils {
    public static final Comparator<String> NUMERICAL_ORDERING;
    public static final String STAT_FMT = "#.##";

    static {
        NUMERICAL_ORDERING = new Comparator<String>() {
            final Pattern p = Pattern.compile("((?:\\d+)?[.,]?\\d*).*");

            @Override
            public int compare(String aLHS, String aRHS) {
                final Matcher matcherLHS = p.matcher(aLHS);
                final Matcher matcherRHS = p.matcher(aRHS);
                if (matcherLHS.matches() && matcherRHS.matches()) {
                    final String lhsValStr = matcherLHS.group(1);
                    final String rhsValStr = matcherRHS.group(1);
                    if (!lhsValStr.isEmpty() && !rhsValStr.isEmpty()) {
                        final double lhsVal = Double.parseDouble(lhsValStr.replace(',', '.'));
                        final double rhsVal = Double.parseDouble(rhsValStr.replace(',', '.'));
                        return Double.compare(lhsVal, rhsVal);
                    }
                }
                return String.CASE_INSENSITIVE_ORDER.compare(aLHS, aRHS);
            }
        };
    }

    public static <T> void addAttributeColumn(TableView<T> aTable, String aName, String aStat, String aToolTip) {
        aTable.getColumns().add(makeAttributeColumn(aName, aStat, aToolTip));
    }

    public static void addColumnToolTip(TableColumn<?, ?> aColumn, String aToolTip) {
        final Label header = new Label();
        header.setTooltip(new Tooltip(aToolTip));
        aColumn.setGraphic(header);
        header.textProperty().bindBidirectional(aColumn.textProperty());
        header.getStyleClass().add("column-header-label");
        header.setMaxWidth(Double.MAX_VALUE); // Makes it take up the full width of the table column header and tooltip
        // is shown more easily.
        header.setMaxHeight(Double.MAX_VALUE);
    }

    public static void addHardPointsColumn(TableView<DisplayLoadout> aTable, Location aLocation) {
        aTable.getColumns().add(makeHardPointsColumn(aLocation));
    }

    public static <T> void addPropertyColumn(TableView<T> aTable, String aName, String aStat, String aToolTip) {
        final TableColumn<T, String> col = makePropertyColumn(aName, aStat, aToolTip);
        aTable.getColumns().add(col);
    }

    public static void addStatColumn(TableView<Weapon> aTable, String aName, String aStat, String aTooltip) {
        final TableColumn<Weapon, String> col = new TableColumn<>(aName);
        col.setCellValueFactory(aFeatures -> {
            return FxBindingUtils.formatValue(STAT_FMT, true, aFeatures.getValue().getStat(aStat, null));
        });
        col.setComparator(FxTableUtils.NUMERICAL_ORDERING);
        aTable.getColumns().add(col);
        addColumnToolTip(col, aTooltip);
    }

    public static void addTopSpeedColumn(TableView<DisplayLoadout> aTable) {
        final TableColumn<DisplayLoadout, String> col = new TableColumn<>("Spd");
        col.setCellValueFactory(aFeatures -> {
            final Loadout loadout = aFeatures.getValue().loadout;
            final Collection<Modifier> rawModifiers = aFeatures.getValue().rawModifiers;
            final Chassis chassis = loadout.getChassis();
            final int rating;
            if (chassis instanceof ChassisStandard) {
                final ChassisStandard chassisStandard = (ChassisStandard) chassis;
                rating = chassisStandard.getEngineMax();

            } else {
                rating = loadout.getEngine().getRating();
            }

            final double speed = TopSpeed.calculate(rating, loadout.getMovementProfile(), chassis.getMassMax(),
                                                    rawModifiers);
            return FxBindingUtils.formatValue("#.#", true, speed);
        });
        col.setComparator(FxTableUtils.NUMERICAL_ORDERING);
        aTable.getColumns().add(col);
        addColumnToolTip(col, "Top speed of the mech with largest possible engine.");
    }

    public static void addTotalHardPointsColumn(ObservableList<TableColumn<Loadout, ?>> aColumns,
                                                HardPointType aHardPointType) {
        final TableColumn<Loadout, Integer> col = new TableColumn<>(aHardPointType.shortName());
        col.setCellValueFactory(
                aFeatures -> new ReadOnlyObjectWrapper<>(aFeatures.getValue().getHardpointsCount(aHardPointType)));
        col.setCellFactory(aView -> new TableCell<Loadout, Integer>() {
            @Override
            protected void updateItem(Integer aObject, boolean aEmpty) {
                if (null != aObject && !aEmpty) {
                    final Label l = new Label(aObject.toString());
                    l.getStyleClass().add(StyleManager.CLASS_HARDPOINT);
                    StyleManager.changeStyle(l, EquipmentCategory.classify(aHardPointType));
                    setGraphic(l);
                } else {
                    setGraphic(null);
                }
                setText(null);
            }
        });
        aColumns.add(col);
        addColumnToolTip(col, "Total number of hard points of type: " + aHardPointType.name() + ".");
    }

    public static <T> TableColumn<T, String> makeAttributeColumn(String aName, String aStat, String aTooltip) {
        final TableColumn<T, String> col = new TableColumn<>(aName);
        col.setCellValueFactory(aFeatures -> {
            Object obj = aFeatures.getValue();
            final String[] bits = aStat.split("\\.");

            for (final String bit : bits) {
                final String getMethodName = "get" + Character.toUpperCase(bit.charAt(0)) + bit.substring(1);
                final String propertyMethodName = bit + "Property";

                boolean found = false;
                for (final Method method : obj.getClass().getMethods()) {
                    if (method.getName().equals(getMethodName)) {
                        try {
                            obj = method.invoke(obj, new Object[method.getParameterCount()]);
                            found = true;
                            break;
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else if (method.getName().equals(propertyMethodName)) {
                        try {
                            obj = method.invoke(obj);
                            found = true;
                            break;
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if (!found) {
                    throw new RuntimeException("Couldn't find property: " + aStat);
                }
            }

            if (obj instanceof Number) {
                return FxBindingUtils.formatValue(STAT_FMT, true, ((Number) obj).doubleValue());
            } else if (obj instanceof ObservableValue) {
                return StringExpression.stringExpression((ObservableValue<?>) obj);
            }
            return new ReadOnlyStringWrapper(obj.toString());
        });
        col.setComparator(FxTableUtils.NUMERICAL_ORDERING);
        addColumnToolTip(col, aTooltip);
        return col;
    }

    public static TableColumn<DisplayLoadout, ConfiguredComponent> makeHardPointsColumn(Location aLocation) {
        final TableColumn<DisplayLoadout, ConfiguredComponent> col = new TableColumn<>(aLocation.shortName());

        col.setCellValueFactory(
                aFeatures -> new ReadOnlyObjectWrapper<>(aFeatures.getValue().loadout.getComponent(aLocation)));

        col.setCellFactory(aView -> new TableCell<DisplayLoadout, ConfiguredComponent>() {
            final HardPointPane hardPointPane = new HardPointPane(new HardPointFormatter());

            @Override
            protected void updateItem(ConfiguredComponent aComponent, boolean aEmpty) {
                setText(null);
                if (null != aComponent && !aEmpty) {
                    hardPointPane.updateHardPoints(aComponent);
                    setGraphic(hardPointPane);
                } else {
                    setGraphic(null);
                }
            }
        });
        col.setSortable(false);
        addColumnToolTip(col, "The total number of hard points of each type that the chassis can support.");
        return col;
    }

    public static <T> TableColumn<T, String> makePropertyColumn(String aName, String aProperty, String aToolTip) {
        final TableColumn<T, String> col = new TableColumn<>(aName);
        col.setCellValueFactory(new PropertyValueFactory<>(aProperty));
        col.setComparator(FxTableUtils.NUMERICAL_ORDERING);
        addColumnToolTip(col, aToolTip);
        return col;
    }

    public static TableColumn<Loadout, Collection<Modifier>> makeQuirkColumn(Class<? extends Weapon> aClass,
                                                                             HardPointType aHardPointType) {

        final TableColumn<Loadout, Collection<Modifier>> col = new TableColumn<>(aHardPointType.shortName());
        col.setCellValueFactory(aFeatures -> new ReadOnlyObjectWrapper<>(aFeatures.getValue().getAllModifiers()));
        col.setCellFactory(aView -> new TableCell<Loadout, Collection<Modifier>>() {
            final Collection<String> selectors = ModifiersDB.getAllSelectors(aClass);
            final FilteredModifierFormatter formatter = new FilteredModifierFormatter(selectors);

            @Override
            protected void updateItem(Collection<Modifier> aModifiers, boolean aEmpty) {
                if (null != aModifiers && !aEmpty) {
                    final VBox g = new VBox();
                    formatter.format(aModifiers, g.getChildren());
                    setGraphic(g);
                } else {
                    setGraphic(null);
                }
            }
        });
        addColumnToolTip(col,
                         "A summary of all the quirks that will affect the performance of " + aHardPointType.name() +
                         " weapons.");
        return col;
    }

    public static void resizeColumnsToFit(TableView<?> aTableView) {
        double width = 0.0;
        for (final TableColumn<?, ?> col : aTableView.getColumns()) {
            width += col.getWidth();
        }

        final double tableWidth = aTableView.getWidth();
        if (tableWidth > width) {
            aTableView.setPrefWidth(width);
        }
    }

    public static void setupChassisTable(TableView<Loadout> aTableView) {
        aTableView.getColumns().clear();
        addAttributeColumn(aTableView, "Name", "name", "The short name of the chassis.");
        addAttributeColumn(aTableView, "Tn", "chassis.massMax", "The maximal mass of the chassis.");
        addAttributeColumn(aTableView, "Fctn", "chassis.faction.uiShortName", "The faction of the chassis.");

        addAttributeColumn(aTableView, "JJ", "jumpJetsMax", "The maximal number of Jump-Jets on the chassis.");

        final TableColumn<Loadout, String> col = new TableColumn<>(HardPointType.ECM.shortName());
        col.setCellValueFactory(aFeatures -> new ReadOnlyStringWrapper(
                aFeatures.getValue().getHardpointsCount(HardPointType.ECM) > 0 ? "Yes" : "No"));
        aTableView.getColumns().add(col);
        addColumnToolTip(col, "Whether or not ECM can be equipped on the chassis.");

        final TableColumn<Loadout, String> hardPointsCol = new TableColumn<>("Hard Points");
        addTotalHardPointsColumn(hardPointsCol.getColumns(), HardPointType.ENERGY);
        addTotalHardPointsColumn(hardPointsCol.getColumns(), HardPointType.BALLISTIC);
        addTotalHardPointsColumn(hardPointsCol.getColumns(), HardPointType.MISSILE);
        aTableView.getColumns().add(hardPointsCol);
        addColumnToolTip(hardPointsCol,
                         "Summary of hard points on this chassis. For omni-mechs this is with the given combination of omni pods mandated by the filter criteria.");

        final TableColumn<Loadout, String> quirksCol = new TableColumn<>("Quirks");
        quirksCol.getColumns().add(makeQuirkColumn(EnergyWeapon.class, HardPointType.ENERGY));
        quirksCol.getColumns().add(makeQuirkColumn(BallisticWeapon.class, HardPointType.BALLISTIC));
        quirksCol.getColumns().add(makeQuirkColumn(MissileWeapon.class, HardPointType.MISSILE));
        aTableView.getColumns().add(quirksCol);
        addColumnToolTip(quirksCol, "All quirks which affect the weapon performance are shown.");

        setupSortable(aTableView, 1, 2, 0);
    }

    public static <T> void setupSortable(TableView<T> aTableView, Integer... aColumnNumbers) {
        final ObservableList<T> items = aTableView.getItems();
        SortedList<T> sorted;
        if (items instanceof SortedList) {
            sorted = (SortedList<T>) items;
        } else {
            sorted = new SortedList<>(items);
        }
        aTableView.setItems(sorted);
        sorted.comparatorProperty().bind(aTableView.comparatorProperty());

        final ObservableList<TableColumn<T, ?>> columns = aTableView.getColumns();
        final ObservableList<TableColumn<T, ?>> sortOrder = aTableView.getSortOrder();
        sortOrder.clear();
        for (final Integer col : aColumnNumbers) {
            sortOrder.add(columns.get(col));
        }
    }
}
