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
package org.lisoft.lsml.view_fx.controllers.mainwindow;

import java.util.Comparator;

import javax.inject.Inject;
import javax.inject.Named;

import org.lisoft.lsml.messages.GarageMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.garage.GaragePath;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.GlobalGarage;
import org.lisoft.lsml.view_fx.LoadoutPillCell;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controllers.AbstractFXController;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;

/**
 * This container will show the {@link Loadout}s stored in the currently open
 * garage.
 *
 * @author Li Song
 */
public class ViewLoadoutsPaneController extends AbstractFXController implements MessageReceiver {
	@FXML
	private ListView<Loadout> loadoutPills;
	@FXML
	private TreeView<GaragePath<Loadout>> loadoutTree;
	@FXML
	private Button redoButton;
	@FXML
	private Button undoButton;
	@FXML
	private Region listingTypeIcon;

	private final MessageXBar xBar;
	private final CommandStack commandStack;
	private final Settings settings;
	private final GlobalGarage globalGarage;
	private final LoadoutFactory loadoutFactory;

	/**
	 * Creates a new {@link ViewLoadoutsPaneController} that will show the
	 * garage contents.
	 *
	 * @param aSettings
	 *            a {@link Settings} object to configure this controller.
	 * @param aXBar
	 *            A {@link MessageXBar} to get world changes on.
	 * @param aCommandStack
	 *            A {@link CommandStack} for the stage.
	 * @param aGlobalGarage
	 *            The open garage manager.
	 * @param aLoadoutFactory
	 */
	@Inject
	public ViewLoadoutsPaneController(Settings aSettings, @Named("global") MessageXBar aXBar,
			CommandStack aCommandStack, GlobalGarage aGlobalGarage, LoadoutFactory aLoadoutFactory) {
		xBar = aXBar;
		commandStack = aCommandStack;
		settings = aSettings;
		globalGarage = aGlobalGarage;
		loadoutFactory = aLoadoutFactory;
		xBar.attach(this);

		refreshAll();

		final Property<String> garageFile = settings.getString(Settings.CORE_GARAGE_FILE);
		garageFile.addListener((aObs, aOld, aNew) -> {
			Platform.runLater(() -> {
				refreshAll();
			});
		});

		redoButton.disableProperty().bind(commandStack.nextRedoProperty().isNull());
		undoButton.disableProperty().bind(commandStack.nextUndoProperty().isNull());

		final Property<Boolean> smallList = settings.getBoolean(Settings.UI_USE_SMALL_MECH_LIST);
		smallList.addListener(this::updateListingIcon);
		updateListingIcon(smallList, null, smallList.getValue());

		loadoutTree.getSelectionModel().select(loadoutTree.getRoot());
	}

	@FXML
	public void addGarageFolder() {
		final TreeItem<GaragePath<Loadout>> selectedItem = loadoutTree.getSelectionModel().getSelectedItem();
		if (null == selectedItem) {
			final GaragePath<Loadout> garageRoot = loadoutTree.getRoot().getValue();
			GlobalGarage.addFolder(garageRoot, root, commandStack, xBar);
		} else {
			GaragePath<Loadout> item = selectedItem.getValue();
			if (item.isLeaf()) {
				item = item.getParent();
			}

			GlobalGarage.addFolder(item, root, commandStack, xBar);
		}
	}

	@FXML
	public void garageTreeKeyRelease(KeyEvent aEvent) {
		if (aEvent.getCode() == KeyCode.DELETE && loadoutTree.getEditingItem() == null) {
			removeSelectedGarageFolder();
			aEvent.consume();
		}
	}

	@FXML
	public void loadoutPillKeyRelease(KeyEvent aEvent) {
		if (loadoutPills.isFocused() && aEvent.getCode() == KeyCode.DELETE) {
			deleteSelectedLoadout();
			aEvent.consume();
		}
	}

	@Override
	public void receive(Message aMsg) {
		if (aMsg instanceof GarageMessage) {
			final GarageMessage<?> msg = (GarageMessage<?>) aMsg;

			final TreeItem<GaragePath<Loadout>> selectedDirectory = loadoutTree.getSelectionModel().getSelectedItem();
			if (null != selectedDirectory && msg.path.isLeaf()) {
				if (msg.path.getValue().get() instanceof Loadout) {
					updateAllLoadoutPills(selectedDirectory.getValue());
				}
			}
		}
	}

	@FXML
	public void redo() {
		commandStack.redo();
	}

	public void refreshAll() {
		FxControlUtils.setupGarageTree(loadoutTree, globalGarage.getGarage().getLoadoutRoot(), xBar, commandStack,
				false, Loadout.class);
		loadoutTree.getSelectionModel().selectedItemProperty().addListener((aObservable, aOld, aNew) -> {
			if (null != aNew) {
				updateAllLoadoutPills(aNew.getValue());
				globalGarage.setDefaultSaveToFolder(aNew.getValue());
			}
		});
		refreshPills();
	}

	@FXML
	public void removeSelectedGarageFolder() {
		final TreeItem<GaragePath<Loadout>> selectedItem = loadoutTree.getSelectionModel().getSelectedItem();
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

		GlobalGarage.remove(item, root, commandStack, xBar);
	}

	@FXML
	public void showLargeList() {
		settings.getBoolean(Settings.UI_USE_SMALL_MECH_LIST).setValue(false);
	}

	@FXML
	public void showSmallList() {
		settings.getBoolean(Settings.UI_USE_SMALL_MECH_LIST).setValue(true);
	}

	@FXML
	public void undo() {
		commandStack.undo();
	}

	/**
	 * Deletes the currently selected loadout, if there is one. No-op otherwise.
	 */
	private void deleteSelectedLoadout() {
		final TreeItem<GaragePath<Loadout>> parent = loadoutTree.getSelectionModel().getSelectedItem();
		final Loadout loadout = loadoutPills.getSelectionModel().getSelectedItem();
		if (parent != null && parent.getValue() != null && loadout != null) {
			final GaragePath<Loadout> parentPath = parent.getValue();
			final GaragePath<Loadout> path = new GaragePath<>(parentPath, loadout);
			GlobalGarage.remove(path, root, commandStack, xBar);
		}
	}

	private void refreshPills() {
		// FIXME: Inject the factory
		loadoutPills.setCellFactory(
				aView -> new LoadoutPillCell(settings, xBar, commandStack, loadoutTree, aView, loadoutFactory));
		loadoutPills.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

	private void updateAllLoadoutPills(GaragePath<Loadout> aNew) {
		loadoutPills.setItems(FXCollections.emptyObservableList());
		if (null != aNew) {
			final SortedList<Loadout> sorted = new SortedList<>(
					FXCollections.observableArrayList(aNew.getTopDirectory().getValues()),
					Comparator.comparing(aLoadout -> aLoadout.getName().toLowerCase()));
			loadoutPills.setItems(sorted);
		}
	}

	@SuppressWarnings("unused")
	private void updateListingIcon(ObservableValue<? extends Boolean> aObs, Boolean aOld, Boolean aNew) {
		if (null != aNew) {
			final ObservableList<String> styles = listingTypeIcon.getStyleClass();
			if (true == aNew) { // Small mechs
				if (styles.remove(StyleManager.ICON_LISTING_LARGE)) {
					styles.add(StyleManager.ICON_LISTING_SMALL);
					refreshPills();
				}
			} else {
				if (styles.remove(StyleManager.ICON_LISTING_SMALL)) {
					styles.add(StyleManager.ICON_LISTING_LARGE);
					refreshPills();
				}
			}
		}
	}
}
