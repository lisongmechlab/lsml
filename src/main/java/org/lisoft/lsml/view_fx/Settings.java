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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;

import org.lisoft.lsml.model.export.LsmlProtocolIPC;
import org.lisoft.lsml.util.OS;
import org.lisoft.lsml.util.OS.WindowsVersion;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * This class contains all global preferences/settings.
 *
 * @author Li Song
 */
public class Settings {
    public final static String UI_SHOW_TOOL_TIP_QUIRKED = "ui_showToolTipQuirked";
    public final static String UI_SMART_PLACE = "ui_useSmartPlace";
    public final static String UI_MECH_VARIANTS = "ui_showMechVariants";
    public final static String UI_COMPACT_LAYOUT = "ui_useCompactLayout";
    public static final String UI_USE_SMALL_MECH_LIST = "ui_useSmallMechList";
    public static final String UI_SHOW_STRUCTURE_ARMOR_QUIRKS = "ui_showStructureArmorQuirks";

    public final static String CORE_IPC_PORT = "core_ipcPort";
    public final static String CORE_GAME_DIRECTORY = "core_gameInstallDir";
    public final static String CORE_GARAGE_FILE = "core_garageFile";
    public final static String CORE_CHECK_FOR_UPDATES = "core_checkForUpdates";
    public final static String CORE_ACCEPT_BETA_UPDATES = "core_acceptBetaUpdates";
    public final static String CORE_LAST_UPDATE_CHECK = "core_lastUpdateCheck";
    public static final String CORE_FORCE_BUNDLED_DATA = "core_forceBundledData";
    public static final String CORE_DATA_CACHE = "core_dataCache";
    public static final String SMURFY_REMEMBER = "core_smurfyRemember";
    public static final String SMURFY_APIKEY = "core_smurfyApiKey";

    public static final String UPGRADES_DHS = "upgrades_defaultDHS";
    public static final String UPGRADES_ES = "upgrades_defaultES";
    public static final String UPGRADES_FF = "upgrades_defaultFF";
    public static final String UPGRADES_ARTEMIS = "upgrades_defaultArtemis";

    public static final String EFFICIENCIES_ALL = "efficiencies_defaultAll";
    public static final String MAX_ARMOUR = "armour_defaultMax";
    public static final String ARMOUR_RATIO = "armour_defaultRatio";

    private static Settings instance = null;

    public static File getDefaultSettingsFile() {
        if (OS.isWindowsOrNewer(WindowsVersion.WIN_OLD)) {
            return new File(System.getenv("AppData") + "/LiSoft/LSML/settings.xml");
        }
        return new File(System.getProperty("user.home") + "/.lisoft/lsml/settings.xml");
    }

    // TODO: Replace this ugly singleton getter with a DI framework in 2.1+
    public static Settings getSettings() {
        if (null == instance) {
            try {
                instance = new Settings();
            }
            catch (final Exception e) {
                LiSongMechLab.showError(null, e);
            }
        }
        return instance;
    }

    /**
     * Remove the file used in versions < 2.0.0
     */
    private static void removeOldSettingsFile() {
        File file;
        if (OS.isWindowsOrNewer(WindowsVersion.WIN_OLD)) {
            file = new File(System.getenv("AppData") + "/lsml_settings.xml");
        }
        else {
            file = new File(System.getProperty("user.home") + "/.lsml.xml");
        }
        if (file.exists()) {
            file.delete();
        }
    }

    private final File propertiesFile = getDefaultSettingsFile();

    private final Properties properties = new Properties();

    private final Map<String, Property<?>> propertiesMap = new HashMap<>();

    public Settings() throws InvalidPropertiesFormatException, IOException {
        removeOldSettingsFile();

        if (propertiesFile.exists() && propertiesFile.isFile()) {
            try (FileInputStream inputStream = new FileInputStream(propertiesFile);
                    BufferedInputStream bis = new BufferedInputStream(inputStream);) {
                properties.loadFromXML(bis);
            }
        }

        setupDefaults();
    }

    public Property<Boolean> getBoolean(String aProperty) {
        return getProperty(aProperty, Boolean.class);
    }

    public Property<Integer> getInteger(String aProperty) {
        return getProperty(aProperty, Integer.class);
    }

    public Property<Long> getLong(String aProperty) {
        return getProperty(aProperty, Long.class);
    }

    @SuppressWarnings("unchecked")
    public <E> Property<E> getProperty(String aProperty, Class<E> aClass) {
        final Property<?> property = propertiesMap.get(aProperty);
        if (null == property) {
            throw new IllegalArgumentException("No such property!");
        }
        if (!property.getValue().getClass().equals(aClass)) {
            throw new IllegalArgumentException("Wrong type for property!");
        }
        return (Property<E>) property;
    }

    public Property<String> getString(String aProperty) {
        return getProperty(aProperty, String.class);
    }

    private void addBoolean(final String aKey, final boolean aDefaultValue) {
        final String value = properties.getProperty(aKey, Boolean.toString(aDefaultValue));
        final SimpleBooleanProperty prop = new SimpleBooleanProperty(aDefaultValue);
        prop.set(Boolean.parseBoolean(value));
        prop.addListener((aObs, aOld, aNew) -> {
            properties.setProperty(aKey, Boolean.toString(aNew));
            persist();
        });
        propertiesMap.put(aKey, prop);
    }

    private void addInteger(final String aKey, final int aDefaultValue) {
        final String value = properties.getProperty(aKey, Integer.toString(aDefaultValue));
        final SimpleIntegerProperty prop = new SimpleIntegerProperty(aDefaultValue);
        prop.set(Integer.parseInt(value));
        prop.addListener((aObs, aOld, aNew) -> {
            properties.setProperty(aKey, Integer.toString(aNew.intValue()));
            persist();
        });
        propertiesMap.put(aKey, prop);
    }

    private void addLong(final String aKey, final long aDefaultValue) {
        final String value = properties.getProperty(aKey, Long.toString(aDefaultValue));
        final SimpleLongProperty prop = new SimpleLongProperty(aDefaultValue);
        prop.set(Long.parseLong(value));
        prop.addListener((aObs, aOld, aNew) -> {
            properties.setProperty(aKey, Long.toString(aNew.intValue()));
            persist();
        });
        propertiesMap.put(aKey, prop);
    }

    private void addString(final String aKey, final String aDefaultValue) {
        final String value = properties.getProperty(aKey, aDefaultValue);
        final SimpleStringProperty prop = new SimpleStringProperty(aDefaultValue);
        prop.set(value);
        prop.addListener((aObs, aOld, aNew) -> {
            properties.setProperty(aKey, aNew);
            persist();
        });
        propertiesMap.put(aKey, prop);
    }

    private void persist() {

        if (!propertiesFile.exists()) {
            // Create the directories so the stores will succeed.
            propertiesFile.getParentFile().mkdirs();
        }
        else if (propertiesFile.isDirectory()) {
            propertiesFile.delete();
        }

        try (FileOutputStream outputStream = new FileOutputStream(propertiesFile);
                BufferedOutputStream bos = new BufferedOutputStream(outputStream);) {
            properties.storeToXML(bos, "Written by LSML");
        }
        catch (final Exception e) {
            LiSongMechLab.showError(null, e);
        }
    }

    private void setupDefaults() {
        addBoolean(UI_SHOW_TOOL_TIP_QUIRKED, true);
        addBoolean(UI_SMART_PLACE, true);
        addBoolean(UI_MECH_VARIANTS, true);
        addBoolean(UI_COMPACT_LAYOUT, false);
        addBoolean(UI_USE_SMALL_MECH_LIST, true);
        addBoolean(UI_SHOW_STRUCTURE_ARMOR_QUIRKS, true);

        addBoolean(CORE_CHECK_FOR_UPDATES, true);
        addBoolean(CORE_ACCEPT_BETA_UPDATES, true);
        addBoolean(CORE_FORCE_BUNDLED_DATA, false);
        addString(CORE_GARAGE_FILE, "");
        addString(CORE_GAME_DIRECTORY, "");
        addString(CORE_DATA_CACHE, new File(propertiesFile.getParentFile(), "data_cache.xml").getAbsolutePath());
        addInteger(CORE_IPC_PORT, LsmlProtocolIPC.DEFAULT_PORT);
        addLong(CORE_LAST_UPDATE_CHECK, 0);
        addBoolean(SMURFY_REMEMBER, false);
        addString(SMURFY_APIKEY, "");

        addBoolean(UPGRADES_DHS, true);
        addBoolean(UPGRADES_ES, true);
        addBoolean(UPGRADES_FF, false);
        addBoolean(UPGRADES_ARTEMIS, false);

        addBoolean(EFFICIENCIES_ALL, true);
        addBoolean(MAX_ARMOUR, true);
        addInteger(ARMOUR_RATIO, 10); // 10:1 ratio
    }
}
