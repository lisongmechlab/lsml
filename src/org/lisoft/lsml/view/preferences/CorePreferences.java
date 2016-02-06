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
package org.lisoft.lsml.view.preferences;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.ButtonModel;
import javax.swing.JToggleButton;

/**
 * Allows easy access to core preferences.
 * 
 * @author Li Song
 *
 */
public class CorePreferences {
    private static final String     UPDATE_ACCEPT_BETA             = "updateAcceptBeta";
    private static final String     UPDATE_CHECK_FOR_UPDATES       = "updateCheckEnabled";
    private static final String     USEBUNDLED_DATA                = "gameDataBundled";
    private static final String     GAMEDIRECTORY_KEY              = "gamedir";
    private static final String     GAME_DATA_CACHE                = "gameDataCache";
    private static final String     UPDATE_LAST_CHECK              = "updateLastCheck";

    public static final ButtonModel USE_BUNDLED_DATA_MODEL         = new JToggleButton.ToggleButtonModel() {
                                                                       @Override
                                                                       public boolean isSelected() {
                                                                           return getUseBundledData();
                                                                       }

                                                                       @Override
                                                                       public void setSelected(boolean aValue) {
                                                                           setUseBundledData(aValue);
                                                                       }
                                                                   };

    public static final ButtonModel UPDATE_ACCEPT_BETA_MODEL       = new JToggleButton.ToggleButtonModel() {
                                                                       @Override
                                                                       public boolean isSelected() {
                                                                           return getCheckForUpdates()
                                                                                   && getAcceptBeta();
                                                                       }

                                                                       @Override
                                                                       public boolean isEnabled() {
                                                                           return getCheckForUpdates();
                                                                       }

                                                                       @Override
                                                                       public void setSelected(boolean aValue) {
                                                                           setAcceptBeta(aValue);
                                                                       }
                                                                   };

    public static final ButtonModel UPDATE_CHECK_FOR_UPDATES_MODEL = new JToggleButton.ToggleButtonModel() {
                                                                       @Override
                                                                       public boolean isSelected() {
                                                                           return CorePreferences.getCheckForUpdates();
                                                                       }

                                                                       @Override
                                                                       public void setSelected(boolean aValue) {
                                                                           CorePreferences.setCheckForUpdates(aValue);
                                                                       }
                                                                   };

    static public boolean getAcceptBeta() {
        String s = PreferenceStore.getString(UPDATE_ACCEPT_BETA, "false");
        return Boolean.parseBoolean(s);
    }

    static public void setAcceptBeta(boolean aValue) {
        PreferenceStore.setString(UPDATE_ACCEPT_BETA, Boolean.toString(aValue));
    }

    static public boolean getCheckForUpdates() {
        String s = PreferenceStore.getString(UPDATE_CHECK_FOR_UPDATES, "true");
        return Boolean.parseBoolean(s);
    }

    static public void setCheckForUpdates(boolean aValue) {
        PreferenceStore.setString(UPDATE_CHECK_FOR_UPDATES, Boolean.toString(aValue));
    }

    static public boolean getUseBundledData() {
        String s = PreferenceStore.getString(USEBUNDLED_DATA, "false");
        return Boolean.parseBoolean(s);
    }

    static public void setUseBundledData(boolean aValue) {
        PreferenceStore.setString(USEBUNDLED_DATA, Boolean.toString(aValue));
    }

    static public String getGameDirectory() {
        return PreferenceStore.getString(GAMEDIRECTORY_KEY);
    }

    static public void setGameDirectory(String aValue) {
        PreferenceStore.setString(GAMEDIRECTORY_KEY, aValue);
    }

    static public String getGameDataCache() {
        return PreferenceStore.getString(GAME_DATA_CACHE);
    }

    static public void setGameDataCache(String aValue) {
        PreferenceStore.setString(GAME_DATA_CACHE, aValue);
    }

    static public Date getLastUpdateCheck() {
        DateFormat df = DateFormat.getInstance();
        try {
            return df.parse(PreferenceStore.getString(UPDATE_LAST_CHECK));
        }
        catch (ParseException e) {
            // No-op
        }
        return new GregorianCalendar(2000, 1, 1).getTime();
    }

    static public void setLastUpdateCheck(Date aValue) {
        DateFormat df = DateFormat.getInstance();
        PreferenceStore.setString(UPDATE_LAST_CHECK, df.format(aValue));
    }

}
