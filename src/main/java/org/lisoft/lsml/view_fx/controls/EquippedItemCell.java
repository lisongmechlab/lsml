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

import static org.lisoft.lsml.view_fx.LiSongMechLab.safeCommand;

import java.util.Objects;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.lisoft.lsml.command.*;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.lsml.model.loadout.*;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.style.ItemToolTipFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxControlUtils;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.equipment.*;
import org.lisoft.mwo_data.mechs.ChassisStandard;

/**
 * This class is responsible for rendering items on the components.
 *
 * @author Li Song
 */
public class EquippedItemCell extends FixedRowsListView.FixedListCell<Item> {

  private static final Engine PROTO_ENGINE =
      new Engine(null, null, null, 0, 0, 0, 0, null, null, 0, null, 0, 0, 0, 0);

  private final ConfiguredComponent component;
  private final ContextMenu contextMenu = new ContextMenu();
  private final VBox engineBox = new VBox();
  private final Label engineHsLabel = new Label();
  private final Label engineLabel = new Label();
  private final ComboBox<Integer> engineRating = new ComboBox<>();
  private final ComboBox<Engine.EngineType> engineType = new ComboBox<>();
  private final Label label = new Label();
  private final Loadout loadout;
  private final LoadoutFactory loadoutFactory;
  private final MenuItem menuAddAmmo = new MenuItem("Add 1 ton of ammo");
  private final MenuItem menuAddEngineHS = new MenuItem("Add engine HS");
  private final MenuItem menuAddHalfAmmo = new MenuItem("Add ½ ton of ammo");
  private final MenuItem menuFillWithAmmo = new MenuItem("Fill 'Mech with ammo");
  private final MenuItem menuRemove = new MenuItem();
  private final MenuItem menuRemoveAll = new MenuItem();
  private final MenuItem menuRemoveAmmo = new MenuItem("Remove all ammo");
  private final MenuItem menuRemoveEngineHS = new MenuItem("Remove engine HS");
  private final MessageDelivery msgd;
  private final boolean pgiMode;
  private final SeparatorMenuItem separator = new SeparatorMenuItem();
  private final CommandStack stack;
  private final StackPane stackPane = new StackPane(label);
  private boolean engineChangeInProgress;

  public EquippedItemCell(
      final FixedRowsListView<Item> aItemView,
      final ConfiguredComponent aComponent,
      final Loadout aLoadout,
      final CommandStack aStack,
      final MessageDelivery aMessageDelivery,
      ItemToolTipFormatter aToolTipFormatter,
      boolean aPgiMode,
      LoadoutFactory aLoadoutFactory,
      Settings aSettings) {
    super(aItemView);
    component = aComponent;
    loadout = aLoadout;
    loadoutFactory = aLoadoutFactory;
    msgd = aMessageDelivery;
    stack = aStack;
    pgiMode = aPgiMode;

    menuRemove.setOnAction(e -> onRemoveItem());
    menuRemoveAll.setOnAction(e -> onRemoveAll());
    menuAddAmmo.setOnAction(e -> onAddAmmo());
    menuFillWithAmmo.setOnAction(e -> onFillWithAmmo());
    menuAddHalfAmmo.setOnAction(e -> onAddAmmoHalf());
    menuRemoveAmmo.setOnAction(e -> onRemoveAmmo());
    menuAddEngineHS.setOnAction(e -> onAddEngineHS());
    menuRemoveEngineHS.setOnAction(e -> onRemoveEngineHs());

    setOnMouseEntered(e -> onMouseEntered(aToolTipFormatter));

    label.getStyleClass().clear();
    label.getStyleClass().addAll(getStyleClass());
    label.setPadding(Insets.EMPTY);
    label.setStyle("-fx-background-color: none;");
    stackPane.getStyleClass().clear();
    stackPane.setPadding(Insets.EMPTY);
    stackPane.setMinWidth(0);
    stackPane.setPrefWidth(1);
    stackPane.setStyle("-fx-alignment: top-left;");

    Pane engineUpgradeBox;
    if (aSettings.getBoolean(Settings.UI_COMPACT_LAYOUT).getValue()) {
      final VBox box = new VBox();
      box.setAlignment(Pos.BASELINE_CENTER);
      StyleManager.addClass(box, StyleManager.CLASS_DEFAULT_SPACING);
      box.getChildren().setAll(engineRating, engineType);
      engineUpgradeBox = box;
    } else {
      final HBox box = new HBox();
      box.setAlignment(Pos.BASELINE_CENTER);
      StyleManager.addClass(box, StyleManager.CLASS_DEFAULT_SPACING);
      box.getChildren().setAll(engineRating, engineType);
      engineUpgradeBox = box;
    }

    final Region engineSpacer = new Region();
    VBox.setVgrow(engineSpacer, Priority.ALWAYS);

    engineHsLabel.setAlignment(Pos.BASELINE_CENTER);
    StyleManager.changeStyle(engineLabel, PROTO_ENGINE);
    StyleManager.changeStyle(engineHsLabel, PROTO_ENGINE);
    StyleManager.addClass(engineBox, StyleManager.CLASS_DEFAULT_SPACING);
    engineBox.getChildren().setAll(engineLabel, engineSpacer, engineUpgradeBox, engineHsLabel);

    engineRating.setStyle("-fx-pref-width: 4em;");
    engineRating
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (aObservable, aOld, aNew) -> {
              if (!engineChangeInProgress && changeEngine(engineType, engineRating).isEmpty()) {
                engineChangeInProgress = true;
                engineRating.getSelectionModel().select(aOld);
                engineChangeInProgress = false;
              }
            });

    engineType.setStyle("-fx-pref-width: 4em;");
    engineType
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (aObservable, aOld, aNew) -> {
              if (!engineChangeInProgress) {
                final Optional<Engine> engine = changeEngine(engineType, engineRating);
                engineChangeInProgress = true;
                if (engine.isPresent()) {
                  regenerateEngineRatingDropDown(engine.get());
                } else {
                  engineType.getSelectionModel().select(aOld);
                }
                engineChangeInProgress = false;
              }
            });

    generateEngineTypeDropDown();

    HBox.setHgrow(engineRating, Priority.ALWAYS);
    setAlignment(Pos.TOP_LEFT);
  }

  protected Optional<Engine> changeEngine(
      final ComboBox<Engine.EngineType> aTypeComboBox, final ComboBox<Integer> aRatingComboBox) {
    final Integer selectedRating = aRatingComboBox.getSelectionModel().getSelectedItem();
    final Engine.EngineType type = aTypeComboBox.getSelectionModel().getSelectedItem();
    final Engine currentEngine = loadout.getEngine();

    Objects.requireNonNull(selectedRating, "engine rating dropdown has no selected item");

    if (currentEngine != null
        && currentEngine.getType() == type
        && currentEngine.getRating() == selectedRating) {
      return Optional.empty();
    }

    final LoadoutStandard loadoutStd = (LoadoutStandard) loadout;
    final int rating = type.clampRating(selectedRating);
    final Engine engine;
    try {
      engine = ItemDB.getEngine(rating, type, loadoutStd.getChassis().getFaction());
    } catch (final NoSuchItemException e) {
      throw new RuntimeException(e);
    }
    if (safeCommand(this, stack, new CmdChangeEngine(msgd, loadoutStd, engine), msgd)) {
      return Optional.of(engine);
    }
    return Optional.empty();
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
    } else {
      setRowSpan(aItem.getSlots());
      final EquippedItemsList list = (EquippedItemsList) getListView().getItems();
      final boolean isFixed = list.isFixed(getIndex());

      updateContextMenu(aItem, isFixed);

      if (aItem instanceof Engine) {
        final VBox box = makeEngineGraphic((Engine) aItem);
        setGraphic(box);
      } else {
        label.setText(aItem.getShortName());
        setGraphic(stackPane);
      }

      setDisable(isFixed);
    }

    getStyleClass().remove(StyleManager.CLASS_EQUIPPED);
    StyleManager.changeStyle(this, aItem);
    StyleManager.changeStyle(label, aItem);
    getStyleClass().add(StyleManager.CLASS_EQUIPPED);
  }

  private void addRatingIfEngineExists(
      final ObservableList<Integer> aList,
      final int aRating,
      final Engine.EngineType aType,
      final Faction aFaction) {
    Objects.requireNonNull(aList);
    Objects.requireNonNull(aType);
    Objects.requireNonNull(aFaction);

    try {
      ItemDB.getEngine(aRating, aType, aFaction);
      aList.add(aRating);
    } catch (final NoSuchItemException ex) {
      // Eat NoSuchItemException it can occur if rating is not available for an engine.
    }
  }

  private void generateEngineTypeDropDown() {
    if (loadout.getChassis() instanceof final ChassisStandard chassis) {
      final ObservableList<Engine.EngineType> items = engineType.getItems();
      items.clear();

      // Always show all of the available engine types, even if one of the current rating is
      // unavailable.
      // If we get a request to switch to an invalid combination (e.g. XL 60), we'll clamp it to the
      // minimum.
      items.add(Engine.EngineType.STD);
      if (chassis.getFaction().isCompatible(Faction.INNERSPHERE)) {
        items.add(Engine.EngineType.LE);
      }
      items.add(Engine.EngineType.XL);
    }
  }

  private <T> Optional<T> itemOfType(Class<T> aClass) {
    final Item item = getItem();
    if (aClass.isAssignableFrom(item.getClass())) {
      final T t = aClass.cast(item);
      return Optional.of(t);
    }
    return Optional.empty();
  }

  private VBox makeEngineGraphic(final Engine aEngine) {
    Objects.requireNonNull(aEngine);

    engineChangeInProgress = true;
    final int engineHS = component.getEngineHeatSinks();
    final int engineHSMax = component.getEngineHeatSinksMax();
    engineLabel.setText(aEngine.getShortName());
    engineHsLabel.setText("Heat Sinks: " + engineHS + "/" + engineHSMax);
    engineHsLabel.setOnMouseClicked(
        aEvent -> {
          if (FxControlUtils.isDoubleClick(aEvent) && engineHS > 0) {
            final HeatSink hs = loadout.getUpgrades().getHeatSink().getHeatSinkType();
            safeCommand(this, stack, new CmdRemoveItem(msgd, loadout, component, hs), msgd);
            aEvent.consume();
          }
        });

    engineType.getSelectionModel().select(aEngine.getType());
    regenerateEngineRatingDropDown(aEngine);
    engineChangeInProgress = false;
    return engineBox;
  }

  private void onAddAmmo() {
    itemOfType(AmmoWeapon.class)
        .ifPresent(
            ammoWeapon ->
                safeCommand(
                    this,
                    stack,
                    new CmdAutoAddItem(loadout, msgd, ammoWeapon.getAmmoType(), loadoutFactory),
                    msgd));
  }

  private void onAddAmmoHalf() {
    itemOfType(AmmoWeapon.class)
        .ifPresent(
            ammoWeapon ->
                safeCommand(
                    this,
                    stack,
                    new CmdAutoAddItem(loadout, msgd, ammoWeapon.getAmmoHalfType(), loadoutFactory),
                    msgd));
  }

  private void onAddEngineHS() {
    if (component.getEngineHeatSinks() < component.getEngineHeatSinksMax()) {
      final HeatSink hs = loadout.getUpgrades().getHeatSink().getHeatSinkType();
      safeCommand(this, stack, new CmdAddItem(msgd, loadout, component, hs), msgd);
    }
  }

  private void onFillWithAmmo() {
    itemOfType(AmmoWeapon.class)
        .ifPresent(
            ammoWeapon ->
                safeCommand(
                    this,
                    stack,
                    new CmdFillWithItem(
                        msgd,
                        loadout,
                        ammoWeapon.getAmmoType(),
                        ammoWeapon.getAmmoHalfType(),
                        loadoutFactory),
                    msgd));
  }

  private void onMouseEntered(ItemToolTipFormatter aToolTipFormatter) {
    final Item item = getItem();
    if (null != item) {
      setTooltip(aToolTipFormatter.format(item, component, loadout.getAllModifiers()));
      getTooltip().setAutoHide(false);
      // FIXME: Set timeout to infinite once we're on JavaFX9, see:
      // https://bugs.openjdk.java.net/browse/JDK-8090477
    } else {
      setTooltip(null);
    }
  }

  private void onRemoveAll() {
    final Item item = getItem();
    safeCommand(
        this,
        stack,
        new CmdRemoveMatching("remove all " + item.getName(), msgd, loadout, i -> i == item),
        msgd);
  }

  private void onRemoveAmmo() {
    itemOfType(AmmoWeapon.class)
        .ifPresent(
            ammoWeapon -> {
              final Ammunition ammo = ammoWeapon.getAmmoType();
              final Ammunition ammoHalf = ammoWeapon.getAmmoHalfType();
              safeCommand(
                  this,
                  stack,
                  new CmdRemoveMatching(
                      "remove ammo", msgd, loadout, aItem -> aItem == ammo || aItem == ammoHalf),
                  msgd);
            });
  }

  private void onRemoveEngineHs() {
    if (component.getEngineHeatSinks() > 0) {
      final HeatSink hs = loadout.getUpgrades().getHeatSink().getHeatSinkType();
      safeCommand(this, stack, new CmdRemoveItem(msgd, loadout, component, hs), msgd);
    }
  }

  private void onRemoveItem() {
    safeCommand(this, stack, new CmdRemoveItem(msgd, loadout, component, getItem()), msgd);
  }

  private void regenerateEngineRatingDropDown(final Engine aSelectedEngine) {
    Objects.requireNonNull(aSelectedEngine);

    if (loadout.getChassis() instanceof final ChassisStandard chassis) {

      final Integer selectedRating = aSelectedEngine.getRating();
      final Engine.EngineType selectedType = aSelectedEngine.getType();

      final ObservableList<Integer> items = engineRating.getItems();
      items.clear();

      if (pgiMode) {
        for (int r = chassis.getEngineMin(); r <= chassis.getEngineMax(); r += 5) {
          addRatingIfEngineExists(items, r, selectedType, chassis.getFaction());
        }
      } else {
        for (int r = chassis.getEngineMax(); r >= chassis.getEngineMin(); r -= 5) {
          addRatingIfEngineExists(items, r, selectedType, chassis.getFaction());
        }
      }

      engineRating.getSelectionModel().select(selectedRating);
    }
  }

  private void updateContextMenu(final Item aItem, boolean aIsFixed) {
    if (aIsFixed || aItem instanceof Internal) {
      setContextMenu(null);
    } else {
      menuRemove.setText("Remove " + aItem.getName());
      menuRemoveAll.setText("Remove all " + aItem.getName());

      if (aItem instanceof final AmmoWeapon ammoWeapon) {
        if (!ammoWeapon.hasBuiltInAmmo()) {
          menuAddAmmo.setDisable(
              EquipResult.SUCCESS != loadout.canEquipDirectly(ammoWeapon.getAmmoType()));
          menuAddHalfAmmo.setDisable(
              EquipResult.SUCCESS != loadout.canEquipDirectly(ammoWeapon.getAmmoHalfType()));
          contextMenu
              .getItems()
              .setAll(
                  menuRemove,
                  menuRemoveAll,
                  menuRemoveAmmo,
                  separator,
                  menuAddAmmo,
                  menuAddHalfAmmo,
                  menuFillWithAmmo);
        } else {
          contextMenu.getItems().setAll(menuRemove, menuRemoveAll);
        }
      } else if (aItem instanceof Engine) {
        final HeatSink hs = loadout.getUpgrades().getHeatSink().getHeatSinkType();

        menuAddEngineHS.setDisable(EquipResult.SUCCESS != loadout.canEquipDirectly(hs));
        menuRemoveEngineHS.setDisable(component.getEngineHeatSinks() == 0);

        contextMenu.getItems().setAll(menuRemove, separator, menuAddEngineHS, menuRemoveEngineHS);
      } else {
        contextMenu.getItems().setAll(menuRemove, menuRemoveAll);
      }
      setContextMenu(contextMenu);
    }
  }
}
