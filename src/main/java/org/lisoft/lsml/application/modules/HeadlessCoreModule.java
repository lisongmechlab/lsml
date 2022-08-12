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
package org.lisoft.lsml.application.modules;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import org.lisoft.lsml.application.ConsoleErrorReporter;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.database.DatabaseProvider;
import org.lisoft.lsml.model.database.HeadlessDatabaseProvider;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.view_fx.LiSongMechLab;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.util.Base64;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * This Dagger 2 {@link Module} provides the necessary data dependencies specialised for headless applications
 * (UnitTests).
 *
 * @author Li Song
 */
@Module
public abstract class HeadlessCoreModule {

    @Singleton
    @Binds
    public abstract LoadoutFactory provideLoadoutFactory(DefaultLoadoutFactory aLoadoutFactory);

    @Singleton
    @Binds
    abstract ErrorReporter provideErrorReporter(ConsoleErrorReporter aErrorReporter);

    @Singleton
    @Binds
    abstract DatabaseProvider provideMwoDatabaseProvider(HeadlessDatabaseProvider aHeadlessProvider);

    @Provides
    static Base64.Decoder provideBase64Decoder() {
        return Base64.getDecoder();
    }

    @Provides
    static Base64.Encoder provideBase64Encoder() {
        return Base64.getEncoder();
    }

    @Singleton
    @Binds
    abstract UncaughtExceptionHandler provideUncaughtExceptionHandler(ConsoleErrorReporter aConsoleErrorReporter);

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
}
