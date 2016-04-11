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

import java.util.Optional;

import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.PilotModuleDB;
import org.lisoft.lsml.model.item.Equipment;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.view_fx.controls.FixedRowsListView;
import org.lisoft.lsml.view_fx.loadout.component.ComponentPane;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.paint.Color;

/**
 * This class contains helpers for dealing with dragging items.
 * 
 * @author Li Song
 *
 */
public class EquipmentDragUtils {
    private static final DataFormat EQ_DF = new DataFormat("lsml_equipment.custom");

    public static void doDrag(Dragboard aDragboard, Equipment aItem) {
        // Pack the data
        ClipboardContent cc = new ClipboardContent();
        cc.put(EQ_DF, Integer.valueOf(aItem.getMwoId()));
        aDragboard.setContent(cc);

        // Create an off-screen scene and add a label representing our item.
        Label label = new Label(aItem.getName());
        label.getStyleClass().add(StyleManager.CLASS_EQUIPPED);
        StyleManager.changeStyle(label, aItem);
        if (aItem instanceof Item) {
            label.setPrefHeight(FixedRowsListView.DEFAULT_HEIGHT * ((Item) aItem).getSlots());
        }
        else {
            label.setPrefHeight(FixedRowsListView.DEFAULT_HEIGHT);
        }
        label.setPrefWidth(ComponentPane.ITEM_WIDTH);
        Scene scene = new Scene(label);
        scene.getStylesheets().setAll(FxControlUtils.getLoadoutStyleSheet());

        // Take a snapshot of the scene using transparent as the background fill
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        aDragboard.setDragView(label.snapshot(sp, null));
    }

    public static <T extends Equipment> Optional<T> unpackDrag(Dragboard aDragboard, Class<T> aClass) {
        if (aDragboard.hasContent(EQ_DF)) {
            int itemId = ((Integer) aDragboard.getContent(EQ_DF)).intValue();

            try {
                Item item = ItemDB.lookup(itemId);
                if (aClass.isAssignableFrom(item.getClass())) {
                    return Optional.of(aClass.cast(item));
                }
            }
            catch (Throwable t) {
                // Wasn't an item, maybe it's a Module?
                try {
                    PilotModule module = PilotModuleDB.lookup(itemId);
                    if (aClass.isAssignableFrom(module.getClass())) {
                        return Optional.of(aClass.cast(module));
                    }
                }
                catch (Throwable tt) {
                    // Dafuq? Could be some weird interoperability issue,
                    // just silently ignore it. A failed drag is such a drag.
                }
            }
        }
        return Optional.empty();
    }

}
