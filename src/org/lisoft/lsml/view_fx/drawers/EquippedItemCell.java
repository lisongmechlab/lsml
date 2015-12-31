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
package org.lisoft.lsml.view_fx.drawers;

import org.lisoft.lsml.command.CmdChangeEngine;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controls.FixedRowsListView;
import org.lisoft.lsml.view_fx.loadout.component.EquippedItemsList;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * This class is responsible for rendering items on the components.
 * 
 * @author Li Song
 */
public class EquippedItemCell extends FixedRowsListView.FixedListCell<Item> {

    private final static Engine           PROTO_ENGINE         = new Engine(null, null, null, 0, 0, 0, 0, null, null, 0,
            null, 0, 0, 0);

    private final ConfiguredComponentBase component;
    private final LoadoutBase<?>          loadout;
    private final CommandStack            stack;
    private final MessageDelivery         messageDelivery;
    private boolean                       engineChangeInProgress;

    private final Label                   label                = new Label();
    private final StackPane               stackPane            = new StackPane(label);
    private Label                         engineLabel          = new Label();
    private Label                         engineHsLabel        = new Label();
    private CheckBox                      engineXlCheckBox     = new CheckBox("XL");
    private ComboBox<Integer>             engineRatingCheckBox = new ComboBox<>();
    private VBox                          engineBox            = new VBox();

    public EquippedItemCell(FixedRowsListView<Item> aItemView, ConfiguredComponentBase aComponent,
            LoadoutBase<?> aLoadout, CommandStack aStack, MessageDelivery aMessageDelivery) {
        super(aItemView);
        component = aComponent;
        loadout = aLoadout;
        messageDelivery = aMessageDelivery;
        stack = aStack;

        label.getStyleClass().clear();
        label.getStyleClass().addAll(getStyleClass());
        label.setPadding(Insets.EMPTY);
        label.setStyle("-fx-background-color: none;");
        stackPane.getStyleClass().clear();
        stackPane.setPadding(Insets.EMPTY);
        stackPane.setMinWidth(0);
        stackPane.setPrefWidth(1);
        stackPane.setStyle("-fx-alignment: top-left;");

        HBox engineUpgradeBox = new HBox();
        engineUpgradeBox.setAlignment(Pos.BASELINE_CENTER);
        engineUpgradeBox.getStyleClass().add(StyleManager.CSS_CLASS_COMPONENT_ENGINE);
        engineUpgradeBox.getChildren().add(engineRatingCheckBox);
        engineUpgradeBox.getChildren().add(engineXlCheckBox);

        Region engineSpacer = new Region();
        VBox.setVgrow(engineSpacer, Priority.ALWAYS);

        engineHsLabel.setAlignment(Pos.BASELINE_CENTER);
        StyleManager.changeStyle(engineLabel, PROTO_ENGINE);
        StyleManager.changeStyle(engineHsLabel, PROTO_ENGINE);
        engineBox.getStyleClass().add(StyleManager.CSS_CLASS_COMPONENT_ENGINE);
        engineBox.getChildren().add(engineLabel);
        engineBox.getChildren().add(engineSpacer);
        engineBox.getChildren().add(engineUpgradeBox);
        engineBox.getChildren().add(engineHsLabel);

        engineRatingCheckBox.getSelectionModel().selectedItemProperty().addListener((aObservable, aOld, aNew) -> {
            if (!engineChangeInProgress && !changeEngine(engineXlCheckBox, engineRatingCheckBox)) {
                engineChangeInProgress = true;
                engineRatingCheckBox.getSelectionModel().select(aOld);
                engineChangeInProgress = false;
            }
        });

        engineXlCheckBox.selectedProperty().addListener((aObservable, aOld, aNew) -> {
            if (!engineChangeInProgress && !changeEngine(engineXlCheckBox, engineRatingCheckBox)) {
                engineChangeInProgress = true;
                engineXlCheckBox.setSelected(aOld);
                engineChangeInProgress = false;
            }
        });

        HBox.setHgrow(engineRatingCheckBox, Priority.ALWAYS);
        setAlignment(Pos.TOP_LEFT);
        getStyleClass().add(StyleManager.CSS_CLASS_EQUIPPED);
    }

    protected boolean changeEngine(CheckBox aXLCheckBox, ComboBox<Integer> aRatingComboBox) {
        LoadoutStandard loadoutStd = (LoadoutStandard) loadout;
        EngineType type = aXLCheckBox.isSelected() ? EngineType.XL : EngineType.STD;
        int rating = aRatingComboBox.getSelectionModel().getSelectedItem().intValue();
        Engine engine = ItemDB.getEngine(rating, type, loadoutStd.getChassis().getFaction());

        try {
            stack.pushAndApply(new CmdChangeEngine(messageDelivery, loadoutStd, engine));
        }
        catch (Exception e) {
            LiSongMechLab.showError(e);
            return false;
        }
        return true;
    }

    @Override
    protected void updateItem(Item aItem, boolean aEmpty) {
        super.updateItem(aItem, aEmpty);
        if (null == aItem) {
            label.setText("EMPTY");
            setGraphic(stackPane);
            setRowSpan(1);
            pseudoClassStateChanged(StyleManager.CSS_PC_FIXED, false);
        }
        else {
            setRowSpan(aItem.getNumCriticalSlots());
            EquippedItemsList list = (EquippedItemsList) getListView().getItems();
            boolean isFixed = list.isFixed(getIndex());

            if (aItem instanceof Engine) {
                VBox box = makeEngineGraphic(isFixed, (Engine) aItem);
                setGraphic(box);
            }
            else {
                label.setText(aItem.getShortName());
                setGraphic(stackPane);
            }

            pseudoClassStateChanged(StyleManager.CSS_PC_FIXED, isFixed);
        }
        StyleManager.changeStyle(this, aItem);
        StyleManager.changeStyle(label, aItem);
    }

    private VBox makeEngineGraphic(boolean isFixed, final Engine engine) {
        engineChangeInProgress = true;
        final int engineHS = component.getEngineHeatSinks();
        final int engineHSMax = component.getEngineHeatSinksMax();
        final boolean omnimech = loadout instanceof LoadoutOmniMech;

        engineLabel.setText(engine.getShortName());
        engineLabel.pseudoClassStateChanged(StyleManager.CSS_PC_FIXED, isFixed);
        engineHsLabel.setText("Heat Sinks: " + engineHS + "/" + engineHSMax);
        engineHsLabel.pseudoClassStateChanged(StyleManager.CSS_PC_FIXED, isFixed);
        engineXlCheckBox.setDisable(omnimech);
        engineXlCheckBox.setSelected(engine.getType() == EngineType.XL);
        engineRatingCheckBox.setDisable(omnimech);
        engineRatingCheckBox.getSelectionModel().select(Integer.valueOf(engine.getRating()));

        if (!omnimech) {
            final ChassisStandard chassis = (ChassisStandard) loadout.getChassis();
            final ObservableList<Integer> items = engineRatingCheckBox.getItems();
            items.clear();
            for (int r = chassis.getEngineMin(); r <= chassis.getEngineMax(); r += 5) {
                items.add(r);
            }
        }
        engineChangeInProgress = false;
        return engineBox;
    }
}