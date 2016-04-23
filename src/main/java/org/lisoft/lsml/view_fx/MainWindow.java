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
package org.lisoft.lsml.view_fx;

import java.util.Optional;

import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.export.BatchImportExporter;
import org.lisoft.lsml.model.export.LsmlLinkProtocol;
import org.lisoft.lsml.model.export.SmurfyImportExport;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.style.WindowState;
import org.lisoft.lsml.view_fx.util.FxBindingUtils;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * This class is the controller for the main window.
 *
 * @author Li Song
 */
public class MainWindow extends StackPane implements MessageReceiver {
    private final WindowState windowState;
    @FXML
    private StackPane block_content;
    private final CommandStack cmdStack = new CommandStack(100);
    private final ObjectBinding<Faction> factionFilter;
    @FXML
    private CheckBox filterClan;
    @FXML
    private CheckBox filterIS;
    @FXML
    private ListView<Loadout> loadout_pills;
    @FXML
    private TreeView<GaragePath<Loadout>> loadout_tree;
    @FXML
    private Toggle nav_chassis;
    @FXML
    private Toggle nav_dropships;
    @FXML
    private ToggleGroup nav_group;
    @FXML
    private Toggle nav_imexport;
    @FXML
    private Toggle nav_loadouts;
    @FXML
    private Toggle nav_settings;
    @FXML
    private Toggle nav_weapons;
    private final BorderPane page_chassis;
    @FXML
    private Pane page_dropships;
    private final BorderPane page_imexport;
    @FXML
    private BorderPane page_loadouts;
    @FXML
    private ScrollPane page_settings;
    @FXML
    private ScrollPane page_weapons;
    private final Settings settings = Settings.getSettings();
    private final MessageXBar xBar = new MessageXBar();
    @FXML
    private BorderPane base;

    private final GlobalGarage globalGarage = GlobalGarage.instance;

    public MainWindow(Stage aStage, Base64LoadoutCoder aCoder) {
        FxControlUtils.loadFxmlControl(this);
        xBar.attach(this);

        factionFilter = FxBindingUtils.createFactionBinding(filterClan.selectedProperty(), filterIS.selectedProperty());

        page_chassis = new ChassisPage(factionFilter, xBar);
        // FIXME: These really should be constructed through DI
        final BatchImportExporter importer = new BatchImportExporter(aCoder, LsmlLinkProtocol.LSML,
                DefaultLoadoutErrorReporter.instance);
        final SmurfyImportExport smurfyImportExport = new SmurfyImportExport(aCoder,
                DefaultLoadoutErrorReporter.instance);
        page_imexport = new ImportExportPage(xBar, importer, smurfyImportExport, cmdStack);
        setupNavigationBar();
        setupLoadoutPage();
        page_weapons.setContent(new WeaponsPage(factionFilter));

        windowState = new WindowState(aStage, this);

        final Property<String> garageFile = settings.getProperty(Settings.CORE_GARAGE_FILE, String.class);
        garageFile.addListener((aObs, aOld, aNew) -> {
            Platform.runLater(() -> {
                setupLoadoutPage();
            });
        });
    }

    @FXML
    public void addGarageFolder() {
        final TreeItem<GaragePath<Loadout>> selectedItem = loadout_tree.getSelectionModel().getSelectedItem();
        if (null == selectedItem) {
            final GaragePath<Loadout> root = loadout_tree.getRoot().getValue();
            GlobalGarage.addFolder(root, this, cmdStack, xBar);
        }
        else {
            GaragePath<Loadout> item = selectedItem.getValue();
            if (item.isLeaf()) {
                item = item.getParent();
            }

            GlobalGarage.addFolder(item, this, cmdStack, xBar);
        }
    }

    @FXML
    public void garageTreeKeyRelease(KeyEvent aEvent) {
        if (aEvent.getCode() == KeyCode.DELETE) {
            removeSelectedGarageFolder();
            aEvent.consume();
        }
    }

    public WindowState getWindowState() {
        return windowState;
    }

    /**
     * @return The global {@link MessageXBar}.
     */
    public MessageXBar getXBar() {
        return xBar;
    }

    @FXML
    public void loadoutPillKeyRelease(KeyEvent aEvent) {
        if (aEvent.getCode() == KeyCode.DELETE) {
            deleteSelectedLoadout();
            aEvent.consume();
        }
    }

    @FXML
    public void openNewMechOverlay() {
        final NewMechPane newMechPane = new NewMechPane(() -> {
            getChildren().remove(1);
            base.setDisable(false);
        }, xBar, settings);
        StyleManager.makeOverlay(newMechPane);
        getChildren().add(newMechPane);
        base.setDisable(true);
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof GarageMessage) {
            final GarageMessage<?> msg = (GarageMessage<?>) aMsg;

            final TreeItem<GaragePath<Loadout>> selectedItem = loadout_tree.getSelectionModel().getSelectedItem();
            if (null != selectedItem) {
                msg.value.ifPresent(aValue -> {
                    if (aValue instanceof Loadout) {
                        updateAllLoadoutPills(Optional.of(selectedItem.getValue()));
                    }
                });
            }
        }
    }

    @FXML
    public void removeSelectedGarageFolder() {
        final TreeItem<GaragePath<Loadout>> selectedItem = loadout_tree.getSelectionModel().getSelectedItem();
        if (null == selectedItem) {
            return;
        }

        final GaragePath<Loadout> item = selectedItem.getValue();
        if (null == item) {
            return;
        }

        if (item.isLeaf()) {
            return;
        }

        GlobalGarage.remove(item, this, cmdStack, xBar);
    }

    @FXML
    public void windowClose() {
        windowState.windowClose();
    }

    @FXML
    public void windowIconify() {
        windowState.windowIconify();
    }

    @FXML
    public void windowMaximize() {
        windowState.windowMaximize();
    }

    /**
     * Deletes the currently selected loadout, if there is one. No-op otherwise.
     */
    private void deleteSelectedLoadout() {
        final TreeItem<GaragePath<Loadout>> parent = loadout_tree.getSelectionModel().getSelectedItem();
        final Loadout loadout = loadout_pills.getSelectionModel().getSelectedItem();
        if (parent != null && parent.getValue() != null && loadout != null) {
            final GaragePath<Loadout> parentPath = parent.getValue();
            final GaragePath<Loadout> path = new GaragePath<>(parentPath, loadout);
            GlobalGarage.remove(path, this, cmdStack, xBar);
        }
    }

    private void setupLoadoutPage() {
        FxControlUtils.setupGarageTree(loadout_tree, globalGarage.getGarage().getLoadoutRoot(), xBar, cmdStack, false);
        loadout_tree.getSelectionModel().selectedItemProperty().addListener((aObservable, aOld, aNew) -> {
            if (aNew != null) {
                updateAllLoadoutPills(Optional.ofNullable(aNew.getValue()));
            }
        });
        loadout_pills.setCellFactory(aView -> new LoadoutPillCell(xBar, cmdStack, loadout_tree, aView));
        loadout_pills.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void setupNavigationBar() {
        page_settings.setContent(new SettingsPage());

        nav_group.selectedToggleProperty().addListener((aObservable, aOld, aNew) -> {
            if (aNew == nav_loadouts) {
                block_content.getChildren().setAll(page_loadouts);
                page_loadouts.setVisible(true);
            }
            else if (aNew == nav_dropships) {
                block_content.getChildren().setAll(page_dropships);
                page_dropships.setVisible(true);
            }
            else if (aNew == nav_chassis) {
                block_content.getChildren().setAll(page_chassis);
                page_chassis.setVisible(true);
            }
            else if (aNew == nav_weapons) {
                block_content.getChildren().setAll(page_weapons);
                page_weapons.setVisible(true);
            }
            else if (aNew == nav_imexport) {
                block_content.getChildren().setAll(page_imexport);
                page_imexport.setVisible(true);
            }
            else if (aNew == nav_settings) {
                block_content.getChildren().setAll(page_settings);
                page_settings.setVisible(true);
            }
            else if (aNew == null) {
                aOld.setSelected(true);
            }
            else {
                throw new IllegalArgumentException("Unknown toggle value! " + aNew);
            }
        });
        nav_group.selectToggle(nav_loadouts);
    }

    private void updateAllLoadoutPills(Optional<GaragePath<Loadout>> aNew) {
        if (aNew.isPresent()) {
            final GaragePath<Loadout> path = aNew.get();
            loadout_pills.getItems().setAll(path.getTopDirectory().getValues());
        }
        else {
            loadout_pills.getItems().clear();
        }
    }
}
