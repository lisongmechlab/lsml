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
package org.lisoft.lsml.view_fx.controls;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.lisoft.lsml.command.CmdAddModule;
import org.lisoft.lsml.command.CmdAutoAddItem;
import org.lisoft.lsml.command.CmdFillWithItem;
import org.lisoft.lsml.command.CmdRemoveMatching;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.item.Consumable;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.MwoObject;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.EquipmentCategory;
import org.lisoft.lsml.view_fx.util.EquipmentDragUtils;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import java.util.Optional;

import static org.lisoft.lsml.view_fx.LiSongMechLab.safeCommand;

/**
 * Fixes styles for equipment rendering in the loadout window.
 *
 * @author Li Song
 */
public class EquipmentTableRow extends TreeTableRow<Object> {
    private final MenuItem autoEquip;
    private final Loadout loadout;

    public EquipmentTableRow(Loadout aLoadout, CommandStack aStack, MessageDelivery aMsgDelivery,
                             LoadoutFactory aLoadoutFactory, Settings aSettings) {
        loadout = aLoadout;
        setOnDragDetected(aEvent -> {
            getValueAsItem().ifPresent(aItem -> {
                final Dragboard db = startDragAndDrop(TransferMode.COPY);
                EquipmentDragUtils.doDrag(db, aItem);
                aEvent.consume();
            });

            getValueAsPilotModule().ifPresent(aModule -> {
                final Dragboard db = startDragAndDrop(TransferMode.COPY);
                EquipmentDragUtils.doDrag(db, aModule);
                aEvent.consume();
            });
        });

        setOnMouseClicked(aEvent -> {
            if (FxControlUtils.isDoubleClick(aEvent)) {
                getValueAsItem().ifPresent(aItem -> safeCommand(this, aStack,
                                                                new CmdAutoAddItem(loadout, aMsgDelivery, aItem,
                                                                                   aLoadoutFactory), aMsgDelivery));
                getValueAsPilotModule().ifPresent(
                    aModule -> safeCommand(this, aStack, new CmdAddModule(aMsgDelivery, loadout, aModule),
                                           aMsgDelivery));
            }
            aEvent.consume();
        });

        autoEquip = new MenuItem("Auto equip");
        autoEquip.setOnAction(e -> getValueAsItem().ifPresent(
            aItem -> safeCommand(this, aStack, new CmdAutoAddItem(loadout, aMsgDelivery, aItem, aLoadoutFactory),
                                 aMsgDelivery)));

        final MenuItem removeAll = new MenuItem("Remove all");
        removeAll.setOnAction(e -> getValueAsItem().ifPresent(aItem -> safeCommand(this, aStack, new CmdRemoveMatching(
            "remove all " + aItem.getName(), aMsgDelivery, loadout, i -> i == aItem), aMsgDelivery)));

        final MenuItem fillMech = new MenuItem("Fill 'Mech");
        fillMech.setOnAction(e -> getValueAsItem().ifPresent(
            aItem -> safeCommand(this, aStack, new CmdFillWithItem(aMsgDelivery, loadout, aItem, aLoadoutFactory),
                                 aMsgDelivery)));

        final CheckMenuItem showModifier = new CheckMenuItem("Tool tips with quirks");
        showModifier.selectedProperty().bindBidirectional(aSettings.getBoolean(Settings.UI_SHOW_TOOL_TIP_QUIRKED));

        setContextMenu(new ContextMenu(autoEquip, fillMech, removeAll, showModifier));

    }

    @Override
    protected void updateItem(Object aObject, boolean aEmpty) {
        super.updateItem(aObject, aEmpty);

        if (aObject instanceof Item) {
            final Item item = (Item) aObject;

            StyleManager.changeListStyle(this, EquipmentCategory.classify(item));

            if (EquipResult.SUCCESS == loadout.canEquipDirectly(item)) {
                // Directly equipable
                pseudoClassStateChanged(StyleManager.PC_UNEQUIPPABLE, false);
                pseudoClassStateChanged(StyleManager.PC_SMARTPLACEABLE, false);
                autoEquip.setDisable(false);
            } else if (!loadout.getCandidateLocationsForItem(item).isEmpty()) {
                // Might be smart placeable
                pseudoClassStateChanged(StyleManager.PC_UNEQUIPPABLE, false);
                pseudoClassStateChanged(StyleManager.PC_SMARTPLACEABLE, true);
                autoEquip.setDisable(false);
            } else {
                pseudoClassStateChanged(StyleManager.PC_UNEQUIPPABLE, true);
                pseudoClassStateChanged(StyleManager.PC_SMARTPLACEABLE, false);
                autoEquip.setDisable(true);
            }
        } else if (aObject instanceof Consumable) {
            final Consumable pilotModule = (Consumable) aObject;

            final boolean equipable = loadout.canAddModule(pilotModule) == EquipResult.SUCCESS;
            pseudoClassStateChanged(StyleManager.PC_UNEQUIPPABLE, !equipable);
            final EquipmentCategory category = EquipmentCategory.classify((MwoObject) aObject);
            StyleManager.changeListStyle(this, category);
        } else {
            final EquipmentCategory category;
            if (aObject instanceof EquipmentCategory) {
                category = (EquipmentCategory) aObject;
            } else {
                category = null;
            }
            StyleManager.changeStyle(this, category);
            pseudoClassStateChanged(StyleManager.PC_UNEQUIPPABLE, false);
            pseudoClassStateChanged(StyleManager.PC_SMARTPLACEABLE, false);
        }
    }

    private Optional<Item> getValueAsItem() {
        final Object object = getItem();
        if (!(object instanceof Item)) {
            return Optional.empty();
        }
        return Optional.of((Item) object);
    }

    private Optional<Consumable> getValueAsPilotModule() {
        final Object object = getItem();
        if (!(object instanceof Consumable)) {
            return Optional.empty();
        }
        return Optional.of((Consumable) object);
    }
}
