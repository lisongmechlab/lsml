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
import org.lisoft.lsml.view_fx.loadout.component.ComponentItemsList;
import org.lisoft.lsml.view_fx.loadout.component.ItemView;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * This class is responsible for rendering items on the components.
 * 
 * @author Li Song
 */
public class ComponentItemsCell extends ItemView.Cell<Item> {
    private final ConfiguredComponentBase component;
    private final LoadoutBase<?>          loadout;
    private final CommandStack            stack;
    private final MessageDelivery         messageDelivery;
    private boolean                       engineChangeInProgress;

    public ComponentItemsCell(ItemView<Item> aItemView, ConfiguredComponentBase aComponent, LoadoutBase<?> aLoadout,
            CommandStack aStack, MessageDelivery aMessageDelivery) {
        super(aItemView);
        component = aComponent;
        loadout = aLoadout;
        messageDelivery = aMessageDelivery;
        stack = aStack;
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
            setText("EMPTY");
            setGraphic(null);
            setRowSpan(1);
            pseudoClassStateChanged(StyleManager.CSS_PC_FIXED, false);
        }
        else {
            setRowSpan(aItem.getNumCriticalSlots());
            ComponentItemsList list = (ComponentItemsList) getListView().getItems();
            boolean isFixed = list.isFixed(getIndex());

            if (aItem instanceof Engine) {
                final Engine engine = (Engine) aItem;
                final int engineHS = component.getEngineHeatSinks();
                final int engineHSMax = component.getEngineHeatSinksMax();
                final Label nameLabel = new Label(aItem.getShortName());
                final Label hsLabel = new Label("Heat Sinks: " + engineHS + "/" + engineHSMax);
                hsLabel.setAlignment(Pos.BASELINE_CENTER);
                StyleManager.changeItemStyle(nameLabel, engine);
                StyleManager.changeItemStyle(hsLabel, engine);
                nameLabel.pseudoClassStateChanged(StyleManager.CSS_PC_FIXED, isFixed);
                hsLabel.pseudoClassStateChanged(StyleManager.CSS_PC_FIXED, isFixed);

                final boolean omnimech = loadout instanceof LoadoutOmniMech;

                final CheckBox checkbox = new CheckBox("XL");
                final ComboBox<Integer> rating = new ComboBox<>();
                checkbox.setDisable(omnimech);
                checkbox.setSelected(engine.getType() == EngineType.XL);
                rating.setDisable(omnimech);
                rating.getSelectionModel().select(Integer.valueOf(engine.getRating()));
                HBox.setHgrow(rating, Priority.ALWAYS);

                if (!omnimech) {
                    ChassisStandard chassis = (ChassisStandard) loadout.getChassis();
                    ObservableList<Integer> items = rating.getItems();
                    for (int r = chassis.getEngineMin(); r <= chassis.getEngineMax(); r += 5) {
                        items.add(r);
                    }

                    rating.getSelectionModel().selectedItemProperty().addListener((aObservable, aOld, aNew) -> {
                        if (!engineChangeInProgress && !changeEngine(checkbox, rating)) {
                            engineChangeInProgress = true;
                            rating.getSelectionModel().select(aOld);
                            engineChangeInProgress = false;
                        }
                    });

                    checkbox.selectedProperty().addListener((aObservable, aOld, aNew) -> {
                        if (!engineChangeInProgress && !changeEngine(checkbox, rating)) {
                            engineChangeInProgress = true;
                            checkbox.setSelected(aOld);
                            engineChangeInProgress = false;
                        }
                    });
                }

                HBox upgradeBox = new HBox();
                upgradeBox.setAlignment(Pos.BASELINE_CENTER);
                upgradeBox.getStyleClass().add(StyleManager.CSS_CLASS_COMPONENT_ENGINE);
                upgradeBox.getChildren().add(rating);
                upgradeBox.getChildren().add(checkbox);

                Region region = new Region();
                VBox.setVgrow(region, Priority.ALWAYS);

                VBox box = new VBox();
                box.getStyleClass().add(StyleManager.CSS_CLASS_COMPONENT_ENGINE);
                box.getChildren().add(nameLabel);
                box.getChildren().add(region);
                box.getChildren().add(upgradeBox);
                box.getChildren().add(hsLabel);

                setGraphic(box);
                setText(null);
            }
            else {
                setGraphic(null);
                setText(aItem.getShortName());
            }

            pseudoClassStateChanged(StyleManager.CSS_PC_FIXED, isFixed);
        }
        StyleManager.changeItemStyle(this, aItem);
    }
}