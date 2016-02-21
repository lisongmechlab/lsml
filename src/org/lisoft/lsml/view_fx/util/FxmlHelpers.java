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

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.view_fx.LiSongMechLab;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Assorted helper methods for dealing with FXML.
 * 
 * @author Li Song
 */
public class FxmlHelpers {
    public static final DecimalFormat      MASS_FMT  = new DecimalFormat("#.## t");
    public static final DecimalFormat      SPEED_FMT = new DecimalFormat("#.# km/h");
    public static final DecimalFormat      DF2       = new DecimalFormat("#.##");

    public static final Comparator<String> NUMERICAL_ORDERING;

    static {
        NUMERICAL_ORDERING = new Comparator<String>() {
            Pattern p = Pattern.compile("((?:\\d+)?[.,]?\\d*).*");

            @Override
            public int compare(String aLHS, String aRHS) {
                Matcher matcherLHS = p.matcher(aLHS);
                Matcher matcherRHS = p.matcher(aRHS);
                if (matcherLHS.matches() && matcherRHS.matches()) {
                    String lhsValStr = matcherLHS.group(1);
                    String rhsValStr = matcherRHS.group(1);
                    if (!lhsValStr.isEmpty() && !rhsValStr.isEmpty()) {
                        double lhsVal = Double.parseDouble(lhsValStr.replace(',', '.'));
                        double rhsVal = Double.parseDouble(rhsValStr.replace(',', '.'));
                        return Double.compare(lhsVal, rhsVal);
                    }
                }
                return String.CASE_INSENSITIVE_ORDER.compare(aLHS, aRHS);
            }
        };
    }

    public static void loadFxmlControl(final Object aControl) {
        final String fxmlFile = aControl.getClass().getSimpleName() + ".fxml";
        final FXMLLoader fxmlLoader = new FXMLLoader(aControl.getClass().getResource(fxmlFile));
        fxmlLoader.setControllerFactory((aClass) -> aControl);
        fxmlLoader.setRoot(aControl);
        try {
            fxmlLoader.load();
        }
        catch (final IOException e) {
            // Failure to load XML is a program error and cannot be recovered from, promote to unchecked.
            throw new RuntimeException(e);
        }
    }

    public static void createStage(final Stage aStage, final Parent aRoot) {
        // aRoot.getStyleClass().add(0, "root");
        // aStage.initStyle(StageStyle.UNDECORATED);
        // aStage.setScene(new Scene(new WindowDecoration(aStage, aRoot)));
        aStage.getIcons().add(new Image(LiSongMechLab.class.getResourceAsStream("/resources/icon.png")));
        aStage.setScene(new Scene(aRoot));
        aStage.sizeToScene();
        aStage.show();
        aStage.toFront();
    }

    public static void setToggleText(ToggleButton aButton, String aSelected, String aUnSelected) {
        StringBinding textBinding = Bindings.when(aButton.selectedProperty()).then(aSelected).otherwise(aUnSelected);
        aButton.textProperty().bind(textBinding);
    }

    /**
     * @param aCheckBox
     * @param aBooleanExpression
     * @param aSuccess
     */
    public static void bindTogglable(final CheckBox aCheckBox, final BooleanExpression aBooleanExpression,
            final Predicate<Boolean> aSuccess) {
        aCheckBox.setSelected(aBooleanExpression.get());
        aBooleanExpression.addListener((aObservable, aOld, aNew) -> {
            aCheckBox.setSelected(aNew);
        });

        aCheckBox.setOnAction(e -> {
            final boolean value = aCheckBox.isSelected();
            final boolean oldValue = aBooleanExpression.get();
            if (value != oldValue && !aSuccess.test(value)) {
                aCheckBox.setSelected(oldValue);
            }
        });
    }

    public static void bindTogglable(final ToggleButton aCheckBox, final BooleanExpression aBooleanExpression,
            final Predicate<Boolean> aSuccess) {
        aCheckBox.setSelected(aBooleanExpression.get());
        aBooleanExpression.addListener((aObservable, aOld, aNew) -> {
            aCheckBox.setSelected(aNew);
        });

        aCheckBox.setOnAction(e -> {
            final boolean value = aCheckBox.isSelected();
            final boolean oldValue = aBooleanExpression.get();
            if (value != oldValue && !aSuccess.test(value)) {
                aCheckBox.setSelected(oldValue);
            }
        });
    }

    public static List<String> getTreePath(final TreeItem<?> aNode) {
        final List<String> path = new ArrayList<>();
        TreeItem<?> node = aNode;
        do {
            path.add(0, node.toString());
            node = node.getParent();
        } while (node != null);
        return path;
    }

    public static <T> Optional<TreeItem<T>> resolveTreePath(final TreeItem<T> aRoot, final List<String> aPath) {
        final Iterator<String> pathIt = aPath.iterator();
        if (!pathIt.hasNext() || !pathIt.next().equals(aRoot.toString())) {
            return Optional.empty();
        }

        TreeItem<T> node = aRoot;
        while (pathIt.hasNext()) {
            final String pathComponent = pathIt.next();
            boolean foundChild = false;
            for (final TreeItem<T> child : node.getChildren()) {
                if (child.toString().equals(pathComponent)) {
                    node = child;
                    foundChild = true;
                    break;
                }
            }
            if (!foundChild) {
                return Optional.empty();
            }
        }
        return Optional.of(node);
    }

    public static <T> void addPropertyColumn(TableView<T> aTable, String aName, String aStat) {
        TableColumn<T, String> col = makePropertyColumn(aName, aStat);
        aTable.getColumns().add(col);
    }

    public static <T> TableColumn<T, String> makePropertyColumn(String aName, String aProperty) {
        TableColumn<T, String> col = new TableColumn<>(aName);
        col.setCellValueFactory(new PropertyValueFactory<>(aProperty));
        col.setComparator(NUMERICAL_ORDERING);
        return col;
    }

    public static void addStatColumn(TableView<Weapon> aTable, String aName, String aStat) {
        TableColumn<Weapon, String> col = new TableColumn<>(aName);
        col.setCellValueFactory(aFeatures -> {
            return formatDouble(aFeatures.getValue().getStat(aStat, null));
        });
        col.setComparator(NUMERICAL_ORDERING);
        aTable.getColumns().add(col);
    }

    public static <T> void addAttributeColumn(TableView<T> aTable, String aName, String aStat) {
        TableColumn<T, String> col = makeAttributeColumn(aName, aStat);
        aTable.getColumns().add(col);
    }

    public static <T> TableColumn<T, String> makeAttributeColumn(String aName, String aStat) {
        TableColumn<T, String> col = new TableColumn<>(aName);
        col.setCellValueFactory(aFeatures -> {
            Object obj = aFeatures.getValue();
            String[] bits = aStat.split("\\.");

            for (String bit : bits) {
                String methodName = "get" + Character.toUpperCase(bit.charAt(0)) + bit.substring(1);

                boolean found = false;
                for (Method method : obj.getClass().getMethods()) {
                    if (method.getName().equals(methodName)) {
                        try {
                            obj = method.invoke(obj, new Object[method.getParameterCount()]);
                            found = true;
                            break;
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if (!found) {
                    throw new RuntimeException("Couldn't find property: " + aStat);
                }
            }

            if (obj instanceof Number)
                return formatDouble(((Number) obj).doubleValue());
            return new ReadOnlyStringWrapper(obj.toString());
        });
        col.setComparator(NUMERICAL_ORDERING);
        return col;
    }

    public static ReadOnlyStringWrapper formatDouble(double aValue) {
        if (0 == aValue)
            return new ReadOnlyStringWrapper("-");
        return new ReadOnlyStringWrapper(DF2.format(aValue));
    }
}
