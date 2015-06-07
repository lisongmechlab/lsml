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
package org.lisoft.lsml.view.preferences;

import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.message.Message;
import org.lisoft.lsml.util.message.MessageXBar;

/**
 * This class contains preferences related to the UI behavior.
 * 
 * @author Emily Björk
 */
public class UiPreferences {
    /**
     * This message is sent over the {@link MessageXBar} when a UI preference has been changed.
     * 
     * @author Emily Björk
     */
    public class PreferencesMessage implements Message {
        public final String attribute;

        PreferencesMessage(String aAttribute) {
            attribute = aAttribute;
        }

        @Override
        public boolean isForMe(LoadoutBase<?> aLoadout) {
            return false;
        }

        @Override
        public boolean affectsHeatOrDamage() {
            return false;
        }
    }

    public static final String          UI_USE_SMARTPLACE     = "uiUseSmartPlace";
    public static final String          UI_COMPACT_MODE       = "uiCompactMode";
    public static final String          UI_HIDE_SPECIAL_MECHS = "uiHideSpecialMechs";
    private final transient MessageXBar xBar;

    public UiPreferences(MessageXBar aXBar) {
        xBar = aXBar;
    }

    public void setCompactMode(boolean aValue) {
        PreferenceStore.setString(UI_COMPACT_MODE, Boolean.toString(aValue));
        if (xBar != null)
            xBar.post(new PreferencesMessage(UI_COMPACT_MODE));
    }

    public void setUseSmartPlace(boolean aValue) {
        PreferenceStore.setString(UI_USE_SMARTPLACE, Boolean.toString(aValue));
        if (xBar != null)
            xBar.post(new PreferencesMessage(UI_USE_SMARTPLACE));
    }

    public void setHideSpecialMechs(boolean aValue) {
        PreferenceStore.setString(UI_HIDE_SPECIAL_MECHS, Boolean.toString(aValue));
        if (xBar != null)
            xBar.post(new PreferencesMessage(UI_HIDE_SPECIAL_MECHS));
    }

    /**
     * @return <code>true</code> if the user has opted to use smart place.
     */
    public boolean getUseSmartPlace() {
        return Boolean.parseBoolean(PreferenceStore.getString(UI_USE_SMARTPLACE, "false"));
    }

    public boolean getCompactMode() {
        return Boolean.parseBoolean(PreferenceStore.getString(UI_COMPACT_MODE, "false"));
    }

    public boolean getHideSpecialMechs() {
        return Boolean.parseBoolean(PreferenceStore.getString(UI_HIDE_SPECIAL_MECHS, "true"));
    }
}
