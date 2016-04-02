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
package org.lisoft.lsml.view_fx.loadout.equipment;

import java.util.Optional;

import org.lisoft.lsml.command.CmdAddModule;
import org.lisoft.lsml.command.CmdAutoAddItem;
import org.lisoft.lsml.command.CmdRemoveMatching;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.item.Equipment;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.EquipmentDragHelper;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;

/**
 * Fixes styles for equipment rendering in the loadout window.
 * 
 * @author Li Song
 *
 */
public class EquipmentTableRow extends TreeTableRow<Object> {
    private final Loadout  loadout;
    private final MenuItem autoEquip;
    private final Settings settings = Settings.getSettings();

    public EquipmentTableRow(Loadout aLoadout, CommandStack aStack, MessageDelivery aMessageDelivery) {
        loadout = aLoadout;
        setOnDragDetected(aEvent -> {
            getValueAsItem().ifPresent(aItem -> {
                Dragboard db = startDragAndDrop(TransferMode.COPY);
                EquipmentDragHelper.doDrag(db, aItem);
                aEvent.consume();
            });

            getValueAsPilotModule().ifPresent(aModule -> {
                Dragboard db = startDragAndDrop(TransferMode.COPY);
                EquipmentDragHelper.doDrag(db, aModule);
                aEvent.consume();
            });
        });

        setOnMouseClicked(aEvent -> {
            int clicks = aEvent.getClickCount();
            if (MouseButton.PRIMARY == aEvent.getButton() && 2 <= clicks && clicks % 2 == 0) {
                getValueAsItem().ifPresent(aItem -> {
                    LiSongMechLab.safeCommand(this, aStack, new CmdAutoAddItem(loadout, aMessageDelivery, aItem));
                });
                getValueAsPilotModule().ifPresent(aModule -> {
                    LiSongMechLab.safeCommand(this, aStack, new CmdAddModule(aMessageDelivery, loadout, aModule));
                });
            }
            aEvent.consume();
        });

        autoEquip = new MenuItem("Auto equip");
        autoEquip.setOnAction(e -> {
            getValueAsItem().ifPresent(aItem -> {
                LiSongMechLab.safeCommand(this, aStack, new CmdAutoAddItem(loadout, aMessageDelivery, aItem));
            });
        });

        MenuItem removeAll = new MenuItem("Remove all");
        removeAll.setOnAction(e -> {
            getValueAsItem().ifPresent(aItem -> {
                LiSongMechLab.safeCommand(this, aStack, new CmdRemoveMatching("remove all " + aItem.getName(),
                        aMessageDelivery, loadout, i -> i == aItem));
            });
        });

        CheckMenuItem showModifier = new CheckMenuItem("Tool tips with quirks");
        showModifier.selectedProperty()
                .bindBidirectional(settings.getProperty(Settings.UI_SHOW_TOOL_TIP_QUIRKED, Boolean.class));

        setContextMenu(new ContextMenu(autoEquip, removeAll, showModifier));

    }

    private Optional<Item> getValueAsItem() {
        Object object = getItem();
        if (!(object instanceof Item))
            return Optional.empty();
        return Optional.of((Item) object);
    }

    private Optional<PilotModule> getValueAsPilotModule() {
        Object object = getItem();
        if (!(object instanceof PilotModule))
            return Optional.empty();
        return Optional.of((PilotModule) object);
    }

    @Override
    protected void updateItem(Object aObject, boolean aEmpty) {
        super.updateItem(aObject, aEmpty);

        if (aObject instanceof Item) {
            Item item = (Item) aObject;

            StyleManager.changeListStyle(this, EquipmentCategory.classify(item));

            if (EquipResult.SUCCESS == loadout.canEquipDirectly(item)) {
                // Directly equippable
                pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, false);
                pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, false);
                autoEquip.setDisable(false);
            }
            else if (!loadout.getCandidateLocationsForItem(item).isEmpty()) {
                // Might be smart placeable
                pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, false);
                pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, true);
                autoEquip.setDisable(false);
            }
            else {
                pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, true);
                pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, false);
                autoEquip.setDisable(true);
            }
        }
        else if (aObject instanceof PilotModule) {
            PilotModule pilotModule = (PilotModule) aObject;

            boolean equippable = loadout.canAddModule(pilotModule) == EquipResult.SUCCESS;
            pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, !equippable);
            final EquipmentCategory category = EquipmentCategory.classify((Equipment) aObject);
            StyleManager.changeListStyle(this, category);
        }
        else {
            final EquipmentCategory category;
            if (aObject instanceof EquipmentCategory) {
                category = (EquipmentCategory) aObject;
            }
            else {
                category = null;
            }
            StyleManager.changeStyle(this, category);
            pseudoClassStateChanged(StyleManager.CSS_PC_UNEQUIPPABLE, false);
            pseudoClassStateChanged(StyleManager.CSS_PC_SMARTPLACEABLE, false);
        }
    }
}
