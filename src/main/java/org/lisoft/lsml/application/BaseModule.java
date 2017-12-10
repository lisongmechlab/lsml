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
package org.lisoft.lsml.application;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.inject.Named;
import javax.inject.Singleton;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.Settings;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * This {@link Module} provides basic functionality common among all
 * configurations.
 *
 * @author Li Song
 */
@Module
public abstract class BaseModule {

	@Provides
	static Decoder provideBase64Decoder() {
		return Base64.getDecoder();
	}

	@Provides
	static Encoder provideBase64Encoder() {
		return Base64.getEncoder();
	}

	@Provides
	@Named("global")
	@Singleton
	static MessageXBar provideMessageXBar() {
		return new MessageXBar();
	}

	@Provides
	@Singleton
	static Settings provideSettings(ErrorReporter aErrorReporter) {
		final Settings settings;
		try {
			settings = new Settings();
		} catch (final Throwable e) {
			final File settingsFile = Settings.getDefaultSettingsFile();
			if (settingsFile.exists()) {
				final File backup = new File(settingsFile.getParentFile(), settingsFile.getName() + "_broken");
				final StringBuilder sb = new StringBuilder();
				sb.append("LSML was unable to parse the settings file stored at: ");
				sb.append(settingsFile.getAbsolutePath());
				sb.append(System.lineSeparator());
				sb.append("LSML will move the old settings file to: ");
				sb.append(backup.getAbsolutePath());
				sb.append(" and create a new default settings and proceed.");
				aErrorReporter.error("Unable to read settings file", sb.toString(), e);

				if (!settingsFile.renameTo(backup)) {
					throw new RuntimeException(
							"LSML was unable to create a backup of the broken settings file and is therefore unable to start.");
				}
				return provideSettings(aErrorReporter);
			}
			throw new RuntimeException(
					"LSML cannot start without a settings file in location: " + settingsFile.getAbsolutePath());
		}
		return settings;
	}

	@Provides
	@Named("version")
	static String provideVersionNumber() {
		final Class<?> clazz = LiSongMechLab.class;
		final String className = clazz.getSimpleName() + ".class";
		final String classPath = clazz.getResource(className).toString();
		if (!classPath.startsWith("jar")) {
			// Class not from JAR
			return LiSongMechLab.DEVELOP_VERSION;
		}
		final String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
		try (InputStream stream = new URL(manifestPath).openStream()) {
			final Manifest manifest = new Manifest(new URL(manifestPath).openStream());
			final Attributes attr = manifest.getMainAttributes();
			return attr.getValue("Implementation-Version");
		} catch (final IOException e) {
			return LiSongMechLab.DEVELOP_VERSION;
		}
	}

	@Singleton
	@Binds
	public abstract LoadoutFactory provideLoadoutFactory(DefaultLoadoutFactory aLoadoutFactory);
}
