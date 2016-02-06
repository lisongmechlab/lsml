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
package org.lisoft.lsml.view_fx.util;

import java.util.List;
import java.util.Optional;

import org.lisoft.lsml.model.loadout.Loadout;

import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;

/**
 * This class provides helper functions for dealing with dragging {@link Loadout} objects.
 * 
 * @author Emily Björk
 */
public class LoadoutDragHelper {
    private static final DataFormat   LOADOUT_DATA_FORMAT = new DataFormat("lsml_loadout.custom");
    // We don't want to serialize/deserialize the loadouts so we instead store the currently dragged
    // loadout data here. This works as long as the user only has one pointing device.
    private static LoadoutDragData<?> onGoingDrag         = null;

    public static class LoadoutDragData<T> {
        final public Class<T>     valueClass;
        final public List<T>      loadouts;
        final public List<String> sourcePath;

        public LoadoutDragData(List<T> aLoadouts, List<String> aSourcePath, Class<T> aValueClass) {
            loadouts = aLoadouts;
            sourcePath = aSourcePath;
            valueClass = aValueClass;
        }
    }

    public static void doDrag(Dragboard aDragboard, LoadoutDragData<?> aLoadoutDragData) {
        // Pack the data
        ClipboardContent cc = new ClipboardContent();
        cc.put(LOADOUT_DATA_FORMAT, "PLACEHOLDER");
        onGoingDrag = aLoadoutDragData;
        aDragboard.setContent(cc);
    }

    public static <T> boolean isDrag(Dragboard aDragboard, Class<? extends T> aClass) {
        return aDragboard.hasContent(LOADOUT_DATA_FORMAT) && aClass.isAssignableFrom(onGoingDrag.valueClass);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<LoadoutDragData<T>> unpackDrag(Dragboard aDragboard, Class<? extends T> aClass) {
        if (isDrag(aDragboard, aClass)) {
            // return Optional.of((LoadoutDragData) aDragboard.getContent(LOADOUT_DATA_FORMAT));
            return Optional.of((LoadoutDragData<T>) onGoingDrag);
        }
        return Optional.empty();
    }
}