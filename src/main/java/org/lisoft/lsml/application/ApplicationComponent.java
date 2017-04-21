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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Optional;

import javax.inject.Named;
import javax.inject.Singleton;

import org.lisoft.lsml.application.modules.CoreModule;
import org.lisoft.lsml.application.modules.JavaFXUIModule;
import org.lisoft.lsml.application.modules.MechlabModule;
import org.lisoft.lsml.application.modules.MechlabSubComponent;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.datacache.DataCache;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.export.LsmlProtocolIPC;
import org.lisoft.lsml.model.export.SmurfyImportExport;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.view_fx.GlobalGarage;
import org.lisoft.lsml.view_fx.LinkPresenter;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.UpdateChecker;
import org.lisoft.lsml.view_fx.controllers.MainWindowController;
import org.lisoft.lsml.view_fx.controllers.SplashScreenController;

import dagger.Component;

/**
 * This dagger Component provides the services necessary for the main
 * application.
 *
 * @author Li Song
 *
 */
@Singleton
@Component(modules = { CoreModule.class, JavaFXUIModule.class })
public interface ApplicationComponent {
	Base64LoadoutCoder coder();

	GlobalGarage garage();

	Optional<LsmlProtocolIPC> ipc();

	LinkPresenter linkPresenter();

	LoadoutFactory loadoutFactory();

	MainWindowController mainWindow();

	MechlabSubComponent mechlabComponent(MechlabModule aMechlabModule);

	@Named("global")
	MessageXBar messageXBar();

	// TODO: Put in some other component and without UI.
	Optional<DataCache> mwoDatabase();

	OSIntegration osIntegration();

	Settings settings();

	SmurfyImportExport smurfyImportExport();

	// TODO: Put splash in a sub/dep-component and tie the lifetime to that
	// component
	SplashScreenController splash();

	UncaughtExceptionHandler uncaughtExceptionHandler();

	// void inject(MainWindowController aMainWindowController);

	Optional<UpdateChecker> updateChecker();
}
