/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.NamedObject;
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

import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * This class is the controller for the main window.
 * 
 * @author Emily Björk
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
    private BorderPane page_chassis;
    @FXML
    private Pane page_dropships;
    private BorderPane page_imexport;
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

    public MainWindow(Stage aStage, Base64LoadoutCoder aCoder) {
        FxControlUtils.loadFxmlControl(this);
        xBar.attach(this);

        factionFilter = FxBindingUtils.createFactionBinding(filterClan.selectedProperty(), filterIS.selectedProperty());

        page_chassis = new ChassisPage(factionFilter, xBar);
        // FIXME: These really should be constructed through DI
        BatchImportExporter importer = new BatchImportExporter(aCoder, LsmlLinkProtocol.LSML,
                DefaultLoadoutErrorReporter.instance);
        SmurfyImportExport smurfyImportExport = new SmurfyImportExport(aCoder, DefaultLoadoutErrorReporter.instance);
        page_imexport = new ImportExportPage(xBar, importer, smurfyImportExport, cmdStack);
        setupNavigationBar();
        setupLoadoutPage();
        page_weapons.setContent(new WeaponsPage(factionFilter));

        windowState = new WindowState(aStage, this);
    }

    public WindowState getWindowState() {
        return windowState;
    }

    @FXML
    public void addGarageFolder() {
        TreeItem<GaragePath<Loadout>> selectedItem = loadout_tree.getSelectionModel().getSelectedItem();
        if (null == selectedItem) {
            GaragePath<Loadout> root = loadout_tree.getRoot().getValue();
            GlobalGarage.addFolder(root, this, cmdStack, xBar);
        }
        else {
            GaragePath<Loadout> item = selectedItem.getValue();
            if (item.isLeaf())
                item = item.getParent();

            GlobalGarage.addFolder(item, this, cmdStack, xBar);
        }
    }

    @FXML
    public void removeSelectedGarageFolder() {
        TreeItem<GaragePath<Loadout>> selectedItem = loadout_tree.getSelectionModel().getSelectedItem();
        if (null == selectedItem)
            return;

        GaragePath<Loadout> item = selectedItem.getValue();
        if (null == item)
            return;

        if (item.isLeaf())
            return;

        GlobalGarage.remove(item, this, cmdStack, xBar);
    }

    /**
     * Selects a new garage file and creates an empty garage. Sets the {@link Settings#CORE_GARAGE_FILE} property to the
     * new file. If successful, the {@link Settings#CORE_GARAGE_FILE} property is updated.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    @FXML
    public void newGarage() throws FileNotFoundException, IOException {
        globalGarage.newGarage(getScene().getWindow());
    }

    @FXML
    public void openNewMechOverlay() {
        NewMechPane newMechPane = new NewMechPane(() -> {
            getChildren().remove(1);
            base.setDisable(false);
        }, xBar, settings);
        StyleManager.makeOverlay(newMechPane);
        getChildren().add(newMechPane);
        base.setDisable(true);
    }

    /**
     * Opens a garage after confirming save with the user. If a new garage is loaded the
     * {@link Settings#CORE_GARAGE_FILE} property will be updated.
     * 
     * @throws IOException
     */
    @FXML
    public void openGarage() throws IOException {
        globalGarage.openGarage(getScene().getWindow());
    }

    @FXML
    public void saveGarage() throws IOException {
        globalGarage.saveGarage();
    }

    /**
     * Will save the current garage as a new file. If successful, the {@link Settings#CORE_GARAGE_FILE} property is
     * updated.
     * 
     * @return <code>true</code> if the garage was written to a file, <code>false</code> otherwise.
     * @throws IOException
     */
    @FXML
    public boolean saveGarageAs() throws IOException {
        return globalGarage.saveGarageAs(getScene().getWindow());
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

    private void updateAllLoadoutPills(Optional<GaragePath<Loadout>> aNew) {
        if (aNew.isPresent()) {
            GaragePath<Loadout> path = aNew.get();
            loadout_pills.setItems(FXCollections.observableArrayList(path.getTopDirectory().getValues()));
        }
        else {
            loadout_pills.getItems().clear();
        }
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

    /**
     * @return The global {@link MessageXBar}.
     */
    public MessageXBar getXBar() {
        return xBar;
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof GarageMessage) {
            GarageMessage msg = (GarageMessage) aMsg;

            TreeItem<GaragePath<Loadout>> selected = loadout_tree.getSelectionModel().getSelectedItem();
            boolean isCurrentDir = selected != null && msg.garageDir.isPresent()
                    && selected.getValue().getTopDirectory() == msg.garageDir.get();

            ObservableList<Loadout> items = loadout_pills.getItems();
            switch (msg.type) {
                case ADDED:
                    if (isCurrentDir) {
                        NamedObject names = msg.value.get();
                        if (names instanceof Loadout) {
                            Loadout loadout = (Loadout) names;
                            items.add(loadout);
                        }
                    }
                    break;
                case REMOVED:
                    if (isCurrentDir) {
                        NamedObject names = msg.value.get();
                        if (names instanceof Loadout) {
                            Loadout loadout = (Loadout) names;
                            items.remove(loadout);
                        }
                    }
                    break;
                case RENAMED:
                    if (selected != null) {
                        msg.value.ifPresent(aNamed -> {
                            if (aNamed instanceof Loadout) {
                                Loadout loadout = (Loadout) aNamed;
                                int idx = items.indexOf(loadout);
                                if (idx >= 0) {
                                    items.set(idx, loadout);
                                }
                            }
                        });
                    }
                    break;
                default:
                    break;
            }

        }

    }
}
