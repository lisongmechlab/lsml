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
package org.lisoft.lsml.application.components;

import dagger.Component;
import javax.inject.Named;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.DatabaseProvider;
import org.lisoft.lsml.model.export.MWOCoder;
import org.lisoft.lsml.model.loadout.LoadoutFactory;

/**
 * This {@link Component} defines all the necessary providers for any application, tests, GUI or
 * CLI.
 *
 * @author Li Song
 */
public interface CoreComponent {

  ErrorReporter errorReporter();

  LoadoutFactory loadoutFactory();

  DatabaseProvider mwoDatabaseProvider();

  MWOCoder mwoLoadoutCoder();

  Thread.UncaughtExceptionHandler uncaughtExceptionHandler();

  @Named("version")
  String versionNumber();
}
