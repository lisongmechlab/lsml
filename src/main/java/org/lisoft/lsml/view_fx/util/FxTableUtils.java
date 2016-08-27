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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ModifiersDB;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.view_fx.ApplicationModel;
import org.lisoft.lsml.view_fx.DisplayLoadout;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.loadout.component.HardPointPane;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentCategory;
import org.lisoft.lsml.view_fx.style.FilteredModifierFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 * This class contains helper methods for making table setup with various types easier.
 *
 * @author Li Song
 *
 */
public class FxTableUtils {
    private static final String STAT_FMT = "#.##";
    public static final Comparator<String> NUMERICAL_ORDERING;

    static {
        NUMERICAL_ORDERING = new Comparator<String>() {
            Pattern p = Pattern.compile("((?:\\d+)?[.,]?\\d*).*");

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

    public static <T> void addAttributeColumn(TableView<T> aTable, String aName, String aStat) {
        aTable.getColumns().add(makeAttributeColumn(aName, aStat));
    }

    public static void addHardpointsColumn(TableView<DisplayLoadout> aTable, Location aLocation) {
        aTable.getColumns().add(makeHardpointsColumn(aLocation));
    }

    public static <T> void addPropertyColumn(TableView<T> aTable, String aName, String aStat) {
        final TableColumn<T, String> col = makePropertyColumn(aName, aStat);
        aTable.getColumns().add(col);
    }

    public static void addStatColumn(TableView<Weapon> aTable, String aName, String aStat) {
        final TableColumn<Weapon, String> col = new TableColumn<>(aName);
        col.setCellValueFactory(aFeatures -> {
            return FxBindingUtils.formatValue(STAT_FMT, true, aFeatures.getValue().getStat(aStat, null));
        });
        col.setComparator(FxTableUtils.NUMERICAL_ORDERING);
        aTable.getColumns().add(col);
    }

    public static void addTopSpeedColumn(TableView<DisplayLoadout> aTable) {
        final TableColumn<DisplayLoadout, String> col = new TableColumn<>("Speed");
        col.setCellValueFactory(aFeatures -> {
            final Loadout loadout = aFeatures.getValue().loadout;
            final Collection<Modifier> rawModifiers = aFeatures.getValue().rawModifiers;
            final Chassis chassis = loadout.getChassis();
            final int rating;
            if (chassis instanceof ChassisStandard) {
                final ChassisStandard chassisStandard = (ChassisStandard) chassis;
                rating = chassisStandard.getEngineMax();

            }
            else {
                rating = loadout.getEngine().getRating();
            }

            final double speed = TopSpeed.calculate(rating, loadout.getMovementProfile(), chassis.getMassMax(),
                    rawModifiers);
            return FxBindingUtils.formatValue("#.# km/h", true, speed);
        });
        col.setComparator(FxTableUtils.NUMERICAL_ORDERING);
        aTable.getColumns().add(col);
    }

    public static void addTotalHardpointsColumn(ObservableList<TableColumn<Loadout, ?>> aColumns,
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
                }
                else {
                    setGraphic(null);
                }
                setText(null);
            }
        });
        aColumns.add(col);
    }

    public static <T> TableColumn<T, String> makeAttributeColumn(String aName, String aStat) {
        final TableColumn<T, String> col = new TableColumn<>(aName);
        col.setCellValueFactory(aFeatures -> {
            Object obj = aFeatures.getValue();
            final String[] bits = aStat.split("\\.");

            for (final String bit : bits) {
                final String methodName = "get" + Character.toUpperCase(bit.charAt(0)) + bit.substring(1);

                boolean found = false;
                for (final Method method : obj.getClass().getMethods()) {
                    if (method.getName().equals(methodName)) {
                        try {
                            obj = method.invoke(obj, new Object[method.getParameterCount()]);
                            found = true;
                            break;
                        }
                        catch (final Exception e) {
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
            }
            return new ReadOnlyStringWrapper(obj.toString());
        });
        col.setComparator(FxTableUtils.NUMERICAL_ORDERING);
        return col;
    }

    public static TableColumn<DisplayLoadout, ConfiguredComponent> makeHardpointsColumn(Location aLocation) {
        final TableColumn<DisplayLoadout, ConfiguredComponent> col = new TableColumn<>(aLocation.shortName());

        col.setCellValueFactory(
                aFeatures -> new ReadOnlyObjectWrapper<>(aFeatures.getValue().loadout.getComponent(aLocation)));

        col.setCellFactory(aView -> new TableCell<DisplayLoadout, ConfiguredComponent>() {
            @Override
            protected void updateItem(ConfiguredComponent aObject, boolean aEmpty) {
                setText(null);
                if (null != aObject && !aEmpty) {
                    setGraphic(new HardPointPane(aObject));
                }
                else {
                    setGraphic(null);
                }
            }
        });
        col.setSortable(false);
        return col;
    }

    public static <T> TableColumn<T, String> makePropertyColumn(String aName, String aProperty) {
        final TableColumn<T, String> col = new TableColumn<>(aName);
        col.setCellValueFactory(new PropertyValueFactory<>(aProperty));
        col.setComparator(FxTableUtils.NUMERICAL_ORDERING);
        return col;
    }

    public static TableColumn<Loadout, Collection<Modifier>> makeQuirkColumn(Class<? extends Weapon> aClass,
            HardPointType aHardPointType) {

        final TableColumn<Loadout, Collection<Modifier>> col = new TableColumn<>(aHardPointType.shortName());
        col.setCellValueFactory(aFeatures -> new ReadOnlyObjectWrapper<>(aFeatures.getValue().getModifiers()));
        col.setCellFactory(aView -> new TableCell<Loadout, Collection<Modifier>>() {
            Collection<String> selectors = ModifiersDB.getAllSelectors(aClass);
            FilteredModifierFormatter formatter = new FilteredModifierFormatter(selectors);

            @Override
            protected void updateItem(Collection<Modifier> aModifiers, boolean aEmpty) {
                if (null != aModifiers && !aEmpty) {
                    final VBox g = new VBox();
                    aModifiers.removeAll(ModifiersDB.lookupEfficiencyModifiers(MechEfficiencyType.FAST_FIRE, false));
                    aModifiers.removeAll(
                            ModifiersDB.lookupEfficiencyModifiers(MechEfficiencyType.HEAT_CONTAINMENT, false));
                    aModifiers.removeAll(ModifiersDB.lookupEfficiencyModifiers(MechEfficiencyType.COOL_RUN, false));
                    aModifiers.removeAll(
                            ModifiersDB.lookupEfficiencyModifiers(MechEfficiencyType.HEAT_CONTAINMENT, true));
                    aModifiers.removeAll(ModifiersDB.lookupEfficiencyModifiers(MechEfficiencyType.COOL_RUN, true));
                    formatter.format(aModifiers, g.getChildren());
                    setGraphic(g);
                }
                else {
                    setGraphic(null);
                }
            }
        });
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

    public static void setupChassisTable(TableView<Loadout> resultsTable) {
        resultsTable.getColumns().clear();
        addAttributeColumn(resultsTable, "Name", "name");
        addAttributeColumn(resultsTable, "Mass", "chassis.massMax");
        addAttributeColumn(resultsTable, "Faction", "chassis.faction.uiShortName");

        addAttributeColumn(resultsTable, "JJ", "jumpJetsMax");

        final TableColumn<Loadout, String> col = new TableColumn<>(HardPointType.ECM.shortName());
        col.setCellValueFactory(aFeatures -> new ReadOnlyStringWrapper(
                aFeatures.getValue().getHardpointsCount(HardPointType.ECM) > 0 ? "Yes" : "No"));
        resultsTable.getColumns().add(col);

        final TableColumn<Loadout, String> hardpointsCol = new TableColumn<>("Hard Points");
        addTotalHardpointsColumn(hardpointsCol.getColumns(), HardPointType.ENERGY);
        addTotalHardpointsColumn(hardpointsCol.getColumns(), HardPointType.BALLISTIC);
        addTotalHardpointsColumn(hardpointsCol.getColumns(), HardPointType.MISSILE);
        resultsTable.getColumns().add(hardpointsCol);

        final TableColumn<Loadout, String> quirksCol = new TableColumn<>("Quirks");
        quirksCol.getColumns().add(makeQuirkColumn(EnergyWeapon.class, HardPointType.ENERGY));
        quirksCol.getColumns().add(makeQuirkColumn(BallisticWeapon.class, HardPointType.BALLISTIC));
        quirksCol.getColumns().add(makeQuirkColumn(MissileWeapon.class, HardPointType.MISSILE));
        resultsTable.getColumns().add(quirksCol);

        setupSortable(resultsTable, 1, 2, 0);

        resultsTable.setRowFactory(tv -> {
            final TableRow<Loadout> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    final Loadout l = row.getItem();
                    if (null != l) {
                        LiSongMechLab.openLoadout(ApplicationModel.model.xBar, l);
                    }
                }
            });
            return row;
        });
    }

    public static <T> void setupSortable(TableView<T> aTableView, Integer... aColumnNumbers) {
        final ObservableList<T> items = aTableView.getItems();
        SortedList<T> sorted;
        if (items instanceof SortedList) {
            sorted = (SortedList<T>) items;
        }
        else {
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
