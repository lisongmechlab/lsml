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

import org.lisoft.lsml.command.CmdAutoAddItem;
import org.lisoft.lsml.command.CmdChangeEngine;
import org.lisoft.lsml.command.CmdRemoveItem;
import org.lisoft.lsml.command.CmdRemoveMatching;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.controls.FixedRowsListView;
import org.lisoft.lsml.view_fx.loadout.component.EquippedItemsList;
import org.lisoft.lsml.view_fx.style.ItemToolTipFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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

    private final static Engine       PROTO_ENGINE         = new Engine(null, null, null, 0, 0, 0, 0, null, null, 0,
            null, 0, 0, 0);

    private final ConfiguredComponent component;
    private final Loadout             loadout;
    private final CommandStack        stack;
    private final MessageDelivery     messageDelivery;
    private boolean                   engineChangeInProgress;

    private final Label               label                = new Label();
    private final StackPane           stackPane            = new StackPane(label);
    private final Label               engineLabel          = new Label();
    private final Label               engineHsLabel        = new Label();
    private final CheckBox            engineXlCheckBox     = new CheckBox("XL");
    private final ComboBox<Integer>   engineRatingCheckBox = new ComboBox<>();
    private final VBox                engineBox            = new VBox();

    private final MenuItem            menuRemove           = new MenuItem();
    private final MenuItem            menuRemoveAll        = new MenuItem();
    private final MenuItem            menuAddAmmo          = new MenuItem("Add 1 ton of ammo");
    private final MenuItem            menuAddHalfAmmo      = new MenuItem("Add ½ ton of ammo");
    private final MenuItem            menuRemoveAmmo       = new MenuItem("Remove all ammo");

    private final ContextMenu         contextMenu          = new ContextMenu();

    private final SeparatorMenuItem   separator            = new SeparatorMenuItem();

    public EquippedItemCell(final FixedRowsListView<Item> aItemView, final ConfiguredComponent aComponent,
            final Loadout aLoadout, final CommandStack aStack, final MessageDelivery aMessageDelivery,
            ItemToolTipFormatter aToolTipFormatter) {
        super(aItemView);
        component = aComponent;
        loadout = aLoadout;
        messageDelivery = aMessageDelivery;
        stack = aStack;

        menuRemove.setOnAction(e -> LiSongMechLab.safeCommand(this, aStack,
                new CmdRemoveItem(messageDelivery, loadout, component, getItem())));

        menuRemoveAll.setOnAction(e -> {
            final Item item = getItem();
            LiSongMechLab.safeCommand(this, aStack,
                    new CmdRemoveMatching("remove all " + item.getName(), messageDelivery, loadout, i -> i == item));
        });

        menuAddAmmo.setOnAction(e -> {
            final Item item = getItem();
            if (item instanceof AmmoWeapon) {
                final AmmoWeapon ammoWeapon = (AmmoWeapon) item;
                final Ammunition ammo = (Ammunition) ItemDB.lookup(ammoWeapon.getAmmoType());
                LiSongMechLab.safeCommand(this, stack, new CmdAutoAddItem(loadout, messageDelivery, ammo));
            }
        });

        menuAddHalfAmmo.setOnAction(e -> {
            final Item item = getItem();
            if (item instanceof AmmoWeapon) {
                final AmmoWeapon ammoWeapon = (AmmoWeapon) item;
                final Ammunition ammoHalf = (Ammunition) ItemDB.lookup(ammoWeapon.getAmmoType() + "half");
                LiSongMechLab.safeCommand(this, stack, new CmdAutoAddItem(loadout, messageDelivery, ammoHalf));
            }
        });

        menuRemoveAmmo.setOnAction(e -> {
            final Item item = getItem();
            if (item instanceof AmmoWeapon) {
                final AmmoWeapon ammoWeapon = (AmmoWeapon) item;
                final Ammunition ammo = (Ammunition) ItemDB.lookup(ammoWeapon.getAmmoType());
                final Ammunition ammoHalf = (Ammunition) ItemDB.lookup(ammoWeapon.getAmmoType() + "half");
                LiSongMechLab.safeCommand(this, stack, new CmdRemoveMatching("remove ammo", messageDelivery, loadout,
                        aItem -> aItem == ammo || aItem == ammoHalf));
            }
        });

        setOnMouseEntered(e -> {
            Item item = getItem();
            if (null != item) {
                setTooltip(aToolTipFormatter.format(item, component, loadout.getModifiers()));
                getTooltip().setAutoHide(false);
                // FIXME: Set timeout to infinite once we're on JavaFX9, see:
                // https://bugs.openjdk.java.net/browse/JDK-8090477
            }
            else {
                setTooltip(null);
            }
        });

        label.getStyleClass().clear();
        label.getStyleClass().addAll(getStyleClass());
        label.setPadding(Insets.EMPTY);
        label.setStyle("-fx-background-color: none;");
        stackPane.getStyleClass().clear();
        stackPane.setPadding(Insets.EMPTY);
        stackPane.setMinWidth(0);
        stackPane.setPrefWidth(1);
        stackPane.setStyle("-fx-alignment: top-left;");

        final HBox engineUpgradeBox = new HBox();
        engineUpgradeBox.setAlignment(Pos.BASELINE_CENTER);
        engineUpgradeBox.getStyleClass().add(StyleManager.CSS_CLASS_COMPONENT_ENGINE);
        engineUpgradeBox.getChildren().setAll(engineRatingCheckBox, engineXlCheckBox);

        final Region engineSpacer = new Region();
        VBox.setVgrow(engineSpacer, Priority.ALWAYS);

        engineHsLabel.setAlignment(Pos.BASELINE_CENTER);
        StyleManager.changeStyle(engineLabel, PROTO_ENGINE);
        StyleManager.changeStyle(engineHsLabel, PROTO_ENGINE);
        engineBox.getStyleClass().add(StyleManager.CSS_CLASS_COMPONENT_ENGINE);
        engineBox.getChildren().setAll(engineLabel, engineSpacer, engineUpgradeBox, engineHsLabel);

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

    protected boolean changeEngine(final CheckBox aXLCheckBox, final ComboBox<Integer> aRatingComboBox) {
        final LoadoutStandard loadoutStd = (LoadoutStandard) loadout;
        final EngineType type = aXLCheckBox.isSelected() ? EngineType.XL : EngineType.STD;
        final int rating = aRatingComboBox.getSelectionModel().getSelectedItem().intValue();
        final Engine engine = ItemDB.getEngine(rating, type, loadoutStd.getChassis().getFaction());

        return LiSongMechLab.safeCommand(this, stack, new CmdChangeEngine(messageDelivery, loadoutStd, engine));
    }

    @Override
    protected void updateItem(final Item aItem, final boolean aEmpty) {
        super.updateItem(aItem, aEmpty);
        if (null == aItem) {
            label.setText("EMPTY");
            setGraphic(stackPane);
            setRowSpan(1);
            setDisable(false);
            setContextMenu(null);
        }
        else {
            setRowSpan(aItem.getNumCriticalSlots());
            final EquippedItemsList list = (EquippedItemsList) getListView().getItems();
            final boolean isFixed = list.isFixed(getIndex());

            updateContextMenu(aItem, isFixed);

            if (aItem instanceof Engine) {
                final VBox box = makeEngineGraphic((Engine) aItem);
                setGraphic(box);
            }
            else {
                label.setText(aItem.getShortName());
                setGraphic(stackPane);
            }

            setDisable(isFixed);
        }
        StyleManager.changeStyle(this, aItem);
        StyleManager.changeStyle(label, aItem);
    }

    private void updateContextMenu(final Item aItem, boolean aIsFixed) {
        if (aIsFixed || aItem instanceof Internal) {
            setContextMenu(null);
        }
        else {
            menuRemove.setText("Remove " + aItem.getName());
            menuRemoveAll.setText("Remove all " + aItem.getName());

            if (aItem instanceof AmmoWeapon) {
                final AmmoWeapon ammoWeapon = (AmmoWeapon) aItem;
                final Ammunition ammo = (Ammunition) ItemDB.lookup(ammoWeapon.getAmmoType());
                final Ammunition ammoHalf = (Ammunition) ItemDB.lookup(ammoWeapon.getAmmoType() + "half");

                menuAddAmmo.setDisable(EquipResult.SUCCESS != loadout.canEquipDirectly(ammo));
                menuAddHalfAmmo.setDisable(EquipResult.SUCCESS != loadout.canEquipDirectly(ammoHalf));

                contextMenu.getItems().setAll(menuRemove, menuRemoveAll, menuRemoveAmmo, separator, menuAddAmmo,
                        menuAddHalfAmmo);
            }
            else {
                contextMenu.getItems().setAll(menuRemove, menuRemoveAll);
            }
            setContextMenu(contextMenu);
        }
    }

    private VBox makeEngineGraphic(final Engine engine) {
        engineChangeInProgress = true;
        final int engineHS = component.getEngineHeatSinks();
        final int engineHSMax = component.getEngineHeatSinksMax();
        final boolean omnimech = loadout instanceof LoadoutOmniMech;

        if (!omnimech) {
            final ChassisStandard chassis = (ChassisStandard) loadout.getChassis();
            final ObservableList<Integer> items = engineRatingCheckBox.getItems();
            items.clear();
            for (int r = chassis.getEngineMin(); r <= chassis.getEngineMax(); r += 5) {
                items.add(r);
            }
        }

        engineLabel.setText(engine.getShortName());
        engineHsLabel.setText("Heat Sinks: " + engineHS + "/" + engineHSMax);
        engineXlCheckBox.setSelected(engine.getType() == EngineType.XL);
        engineRatingCheckBox.getSelectionModel().select(Integer.valueOf(engine.getRating()));
        engineChangeInProgress = false;
        return engineBox;
    }
}