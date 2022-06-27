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

import javafx.scene.control.TableView.ResizeFeatures;
import javafx.util.Callback;

/**
 * Implements a resize policy which by default sets the width of each column to be auto sized.
 * <p>
 * TODO: For now this also disables resizing of the columns. This should be fixed in the future, but it is fine for
 * right now.
 *
 * @author Li Song
 */
@SuppressWarnings("rawtypes")
public class SensibleTableColumnResizePolicy implements Callback<ResizeFeatures, Boolean> {
    @Override
    public Boolean call(ResizeFeatures aParam) {
        return true;
    }
}
