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
package org.lisoft.lsml.application.modules;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.module.ModuleDescriptor.Version;
import java.util.Base64;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.DatabaseProvider;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.view_fx.DialogErrorReporter;
import org.lisoft.lsml.view_fx.GraphicalDatabaseProvider;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.Settings;

/**
 * This Dagger 2 {@link Module} provides the necessary data dependencies specialised for the JavaFX
 * GUI application.
 *
 * @author Li Song
 */
@Module
public abstract class GraphicalCoreModule {

  @Singleton
  @Binds
  public abstract LoadoutFactory provideLoadoutFactory(DefaultLoadoutFactory aLoadoutFactory);

  @Provides
  @Singleton
  static Settings provideSettings(ErrorReporter aErrorReporter) {
    return new Settings(aErrorReporter);
  }

  @Singleton
  @Binds
  abstract ErrorReporter provideErrorReporter(DialogErrorReporter aErrorReporter);

  @Singleton
  @Binds
  abstract DatabaseProvider provideDatabaseProvider(GraphicalDatabaseProvider aFxProvider);

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
  abstract UncaughtExceptionHandler provideUncaughtExceptionHandler(
      DialogErrorReporter aDialogErrorReporter);

  @Provides
  @Named("version")
  static String provideVersionNumber() {
    java.lang.Module applicationModule = LiSongMechLab.class.getModule();
    Optional<Version> optionalVersion = applicationModule.getDescriptor().version();
    return optionalVersion.orElseGet(() -> Version.parse(LiSongMechLab.DEVELOP_VERSION)).toString();
  }
}
