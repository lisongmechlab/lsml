/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.view_fx;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javafx.beans.property.*;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.export.LsmlProtocolIPC;
import org.lisoft.lsml.mwo_data.equipment.UpgradeDB;
import org.lisoft.lsml.util.OS;
import org.lisoft.lsml.util.OS.WindowsVersion;

/**
 * This class contains all global preferences/settings.
 *
 * @author Li Song
 */
public class Settings {
  public static final String ARMOUR_RATIO = "armour_defaultRatio";
  public static final String CORE_ACCEPT_BETA_UPDATES = "core_acceptBetaUpdates";
  public static final String CORE_CHECK_FOR_UPDATES = "core_checkForUpdates";
  public static final String CORE_DATABASE = "core_database";
  public static final String CORE_FORCE_BUNDLED_DATA = "core_forceBundledData";
  public static final String CORE_GAME_DIRECTORY = "core_gameInstallDir";
  public static final String CORE_GARAGE_FILE = "core_garageFile";
  public static final String CORE_IPC_PORT = "core_ipcPort";
  public static final String CORE_LAST_UPDATE_CHECK = "core_lastUpdateCheck";
  public static final String MAX_ARMOUR = "armour_defaultMax";
  public static final String UI_COMPACT_LAYOUT = "ui_useCompactLayout";
  public static final String UI_MECH_VARIANTS = "ui_showMechVariants";
  public static final String UI_PGI_COMPATIBILITY = "ui_pgiCompatibility";
  public static final String UI_SHOW_STRUCTURE_ARMOR_QUIRKS = "ui_showStructureArmorQuirks";
  public static final String UI_SHOW_TOOL_TIP_QUIRKED = "ui_showToolTipQuirked";
  public static final String UI_SMART_PLACE = "ui_useSmartPlace";
  public static final String UI_USE_SMALL_MECH_LIST = "ui_useSmallMechList";
  public static final String UPGRADES_DEFAULT_ARTEMIS = "upgrades_defaultArtemis";
  public static final String UPGRADES_DEFAULT_CLAN_ARMOUR = "upgrades_defaultClanArmour";
  public static final String UPGRADES_DEFAULT_CLAN_HEAT_SINKS = "upgrades_defaultClanHeatsinks";
  public static final String UPGRADES_DEFAULT_CLAN_STRUCTURE = "upgrades_defaultClanStructure";
  public static final String UPGRADES_DEFAULT_IS_ARMOUR = "upgrades_defaultIsArmour";
  public static final String UPGRADES_DEFAULT_IS_HEAT_SINKS = "upgrades_defaultIsHeatsinks";
  public static final String UPGRADES_DEFAULT_IS_STRUCTURE = "upgrades_defaultIsStructure";
  private final ErrorReporter errorReporter;
  private final Properties properties = new Properties();
  private final File propertiesFile = getDefaultSettingsFile();
  private final Map<String, Property<?>> propertiesMap = new HashMap<>();

  /**
   * Produces a new settings object that is read from the default settings file if it exists. If it
   * doesn't exist, default settings are loaded, if it exists but cannot be read, it is renamed and
   * a new settings object will be generated on next run, if renaming fails we throw runtime
   * exception.
   *
   * @param aErrorReporter Where to report errors from settings.
   */
  public Settings(ErrorReporter aErrorReporter) {
    errorReporter = aErrorReporter;
    if (propertiesFile.exists() && propertiesFile.isFile()) {
      try (FileInputStream inputStream = new FileInputStream(propertiesFile);
          BufferedInputStream bis = new BufferedInputStream(inputStream)) {
        properties.loadFromXML(bis);
      } catch (final Throwable e) {
        // We couldn't read the settings file, rename the old file so that a fresh one will be
        // generated on next run and the user doesn't repeatedly get the same error.
        final File backup =
            new File(propertiesFile.getParentFile(), propertiesFile.getName() + "_broken");
        String sb =
            "LSML was unable to parse the settings file stored at: "
                + propertiesFile.getAbsolutePath()
                + System.lineSeparator()
                + "LSML will move the old settings file to: "
                + backup.getAbsolutePath()
                + " and create a new default settings and proceed.";
        errorReporter.error("Unable to read settings file", sb, e);

        try {
          Files.move(propertiesFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (final Throwable t) {
          throw new RuntimeException(
              "LSML was unable to create a backup of the broken settings file and is therefore unable to start.",
              t);
        }
      }
    }
    setupDefaults();
  }

  public static File getDefaultSettingsFile() {
    if (OS.isWindowsOrNewer(WindowsVersion.WIN_OLD)) {
      return new File(System.getenv("AppData") + "/LiSoft/LSML/settings.xml");
    }
    return new File(System.getProperty("user.home") + "/.lisoft/lsml/settings.xml");
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
    prop.addListener(
        (aObs, aOld, aNew) -> {
          properties.setProperty(aKey, Boolean.toString(aNew));
          persist();
        });
    propertiesMap.put(aKey, prop);
  }

  private void addInteger(final String aKey, final int aDefaultValue) {
    final String value = properties.getProperty(aKey, Integer.toString(aDefaultValue));
    final SimpleIntegerProperty prop = new SimpleIntegerProperty(aDefaultValue);
    prop.set(Integer.parseInt(value));
    prop.addListener(
        (aObs, aOld, aNew) -> {
          properties.setProperty(aKey, Integer.toString(aNew.intValue()));
          persist();
        });
    propertiesMap.put(aKey, prop);
  }

  private void addLong(final String aKey, final long aDefaultValue) {
    final String value = properties.getProperty(aKey, Long.toString(aDefaultValue));
    final SimpleLongProperty prop = new SimpleLongProperty(aDefaultValue);
    prop.set(Long.parseLong(value));
    prop.addListener(
        (aObs, aOld, aNew) -> {
          properties.setProperty(aKey, Long.toString(aNew.longValue()));
          persist();
        });
    propertiesMap.put(aKey, prop);
  }

  private void addString(final String aKey, final String aDefaultValue) {
    final String value = properties.getProperty(aKey, aDefaultValue);
    final SimpleStringProperty prop = new SimpleStringProperty(aDefaultValue);
    prop.set(value);
    prop.addListener(
        (aObs, aOld, aNew) -> {
          properties.setProperty(aKey, aNew);
          persist();
        });
    propertiesMap.put(aKey, prop);
  }

  private void persist() {
    try {
      if (!propertiesFile.exists()) {
        // Create the directories so the stores will succeed.
        File propertiesDir = propertiesFile.getParentFile();
        if (!propertiesDir.isDirectory() && !propertiesDir.mkdirs()) {
          throw new IOException(
              "Unable to create directory for settings file: " + propertiesDir.getAbsolutePath());
        }
      } else if (propertiesFile.isDirectory()) {
        if (!propertiesFile.delete()) {
          throw new IOException(
              "Unable to delete directory with same path as settings file: "
                  + propertiesFile.getAbsolutePath());
        }
      }

      try (FileOutputStream outputStream = new FileOutputStream(propertiesFile);
          BufferedOutputStream bos = new BufferedOutputStream(outputStream)) {
        properties.storeToXML(bos, "Written by LSML");
      }
    } catch (final Exception e) {
      errorReporter.error(
          "Couldn't save settings",
          "An error occurred during writing of: " + propertiesFile.getAbsolutePath(),
          e);
    }
  }

  private void setupDefaults() {
    addBoolean(UI_SHOW_TOOL_TIP_QUIRKED, true);
    addBoolean(UI_SMART_PLACE, true);
    addBoolean(UI_MECH_VARIANTS, true);
    addBoolean(UI_COMPACT_LAYOUT, false);
    addBoolean(UI_USE_SMALL_MECH_LIST, true);
    addBoolean(UI_SHOW_STRUCTURE_ARMOR_QUIRKS, false);
    addBoolean(UI_PGI_COMPATIBILITY, false);

    addBoolean(CORE_CHECK_FOR_UPDATES, true);
    addBoolean(CORE_ACCEPT_BETA_UPDATES, true);
    addBoolean(CORE_FORCE_BUNDLED_DATA, false);
    addString(CORE_GARAGE_FILE, "");
    addString(CORE_GAME_DIRECTORY, "");
    addString(
        CORE_DATABASE, new File(propertiesFile.getParentFile(), "database.xml").getAbsolutePath());
    addInteger(CORE_IPC_PORT, LsmlProtocolIPC.DEFAULT_PORT);
    addLong(CORE_LAST_UPDATE_CHECK, 0);

    addInteger(UPGRADES_DEFAULT_CLAN_ARMOUR, UpgradeDB.CLAN_STD_ARMOUR_ID);
    addInteger(UPGRADES_DEFAULT_CLAN_STRUCTURE, UpgradeDB.CLAN_ES_STRUCTURE_ID);
    addInteger(UPGRADES_DEFAULT_CLAN_HEAT_SINKS, UpgradeDB.CLAN_DHS_ID);
    addInteger(UPGRADES_DEFAULT_IS_ARMOUR, UpgradeDB.IS_STD_ARMOUR_ID);
    addInteger(UPGRADES_DEFAULT_IS_STRUCTURE, UpgradeDB.IS_ES_STRUCTURE_ID);
    addInteger(UPGRADES_DEFAULT_IS_HEAT_SINKS, UpgradeDB.IS_DHS_ID);
    addBoolean(UPGRADES_DEFAULT_ARTEMIS, false);

    addBoolean(MAX_ARMOUR, true);
    addInteger(ARMOUR_RATIO, 10); // 10:1 ratio
  }
}
