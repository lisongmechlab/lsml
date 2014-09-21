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
package lisong_mechlab.view.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.swing.JOptionPane;

import lisong_mechlab.util.OS;
import lisong_mechlab.util.OS.WindowsVersion;
import lisong_mechlab.view.ProgramInit;

/**
 * Handles storing and loading of preferences.
 * 
 * @author Emily Björk
 */
public class PreferenceStore {

	public static final String		GAMEDIRECTORY_KEY	= "gamedir";
	public static final String		GARAGEFILE_KEY		= "garagefile";
	public static final String		GARAGEFILE_DEFAULT	= "garage.xml";
	private static final File		propertiesFile;
	private static final Properties	properties;
	public static final String		GAME_DATA_CACHE		= "gameDataCache";
	public static final String		USEBUNDLED_DATA		= "gameDataBundled";

	static public String getString(String key) {
		return properties.getProperty(key, "");
	}

	static public String getString(String key, String aDefault) {
		return properties.getProperty(key, aDefault);
	}

	static public void setString(String key, String value) {
		properties.setProperty(key, value);

		try (FileOutputStream outputStream = new FileOutputStream(propertiesFile)) {
			properties.storeToXML(outputStream, "Written by LSML");
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(ProgramInit.lsml(), "Program settings file not found! :" + e);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(ProgramInit.lsml(), "IO error while writing program settings file! :" + e);
		}
	}

	static {
		if (OS.isWindowsOrNewer(WindowsVersion.WinOld)) {
			propertiesFile = new File(System.getenv("AppData") + "/lsml_settings.xml");
		} else {
			propertiesFile = new File(System.getProperty("user.home") + "/.lsml.xml");
		}

		properties = new Properties();
		if (propertiesFile.exists()) {
			try (FileInputStream inputStream = new FileInputStream(propertiesFile)) {
				properties.loadFromXML(inputStream);
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(ProgramInit.lsml(), "Program settings file not found! :" + e);
			} catch (InvalidPropertiesFormatException e) {
				JOptionPane.showMessageDialog(ProgramInit.lsml(), "Program settings file is corrupt! :" + e);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(ProgramInit.lsml(),
						"Unspecified IO error while reading program settings file! :" + e);
			}
		}
	}
}
