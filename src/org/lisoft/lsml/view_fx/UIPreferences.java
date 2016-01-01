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

import org.lisoft.lsml.view.preferences.PreferenceStore;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * This class contains all the UI preferences that the GUI will use.
 * 
 * @author Emily Björk
 */
public class UIPreferences {
    private final static String          TOOL_TIP_SHOW_MODIFIED;
    private final static BooleanProperty toolTipShowModified;

    static public BooleanProperty toolTipShowModifiedValuesProperty() {
        return toolTipShowModified;
    }

    static public boolean getToolTipShowModifiedValues() {
        return toolTipShowModified.get();
    }

    static public void setToolTipShowModifiedValues(boolean aValue) {
        toolTipShowModified.set(aValue);
    }

    static {
        TOOL_TIP_SHOW_MODIFIED = "toolTipShowModifier";
        toolTipShowModified = new SimpleBooleanProperty();
        toolTipShowModified
                .set(Boolean.parseBoolean(PreferenceStore.getString(TOOL_TIP_SHOW_MODIFIED, Boolean.toString(true))));
        toolTipShowModified.addListener((aObs, aOld, aNew) -> {
            PreferenceStore.setString(TOOL_TIP_SHOW_MODIFIED, Boolean.toString(aNew));
        });
    }
}
