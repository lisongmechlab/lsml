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

import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;

import java.util.List;
import java.util.Optional;

/**
 * This class encapsulates helper functions to deal with dragging loadouts.
 *
 * @author Li Song
 */
public class GarageDirectoryDragUtils {
    private static final DataFormat GARAGE_DATA_FORMAT = new DataFormat("lsml_garage.custom");

    public static void doDrag(Dragboard aDragboard, List<String> aTreePaths) {
        // Pack the data
        ClipboardContent cc = new ClipboardContent();
        cc.put(GARAGE_DATA_FORMAT, aTreePaths);
        aDragboard.setContent(cc);
    }

    public static boolean isDrag(Dragboard aDragboard) {
        return aDragboard.hasContent(GARAGE_DATA_FORMAT);
    }

    @SuppressWarnings("unchecked")
    public static Optional<List<String>> unpackDrag(Dragboard aDragboard) {
        if (aDragboard.hasContent(GARAGE_DATA_FORMAT)) {
            return Optional.of((List<String>) aDragboard.getContent(GARAGE_DATA_FORMAT));
        }
        return Optional.empty();
    }
}
