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
package org.lisoft.lsml.application.modules;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.datacache.DataCache;
import org.lisoft.lsml.model.datacache.gamedata.GameVFS;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.export.LsmlProtocolIPC;
import org.lisoft.lsml.model.loadout.LoadoutBuilder.ErrorReportingCallback;
import org.lisoft.lsml.model.modifiers.AffectsWeaponPredicate;
import org.lisoft.lsml.view_fx.DefaultExceptionHandler;
import org.lisoft.lsml.view_fx.DefaultLoadoutErrorReporter;
import org.lisoft.lsml.view_fx.DialogErrorReporter;
import org.lisoft.lsml.view_fx.DialogLinkPresenter;
import org.lisoft.lsml.view_fx.ErrorReporter;
import org.lisoft.lsml.view_fx.LinkPresenter;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.UpdateChecker.UpdateCallback;
import org.lisoft.lsml.view_fx.controllers.SplashScreenController;
import org.lisoft.lsml.view_fx.style.FilteredModifierFormatter;

import dagger.Module;
import dagger.Provides;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

/**
 * @author Emily
 *
 */
@Module
public class JavaFXUIModule {

	@Singleton
	@Provides
	static ErrorReporter provideErrorReporter() {
		return new DialogErrorReporter();
	}

	@Singleton
	@Provides
	static ErrorReportingCallback provideErrorReportingCallback() {
		return new DefaultLoadoutErrorReporter();
	}

	@Singleton
	@Provides
	static Optional<LsmlProtocolIPC> provideIPC(Settings aSettings, @Named("global") MessageXBar aXBar,
			Base64LoadoutCoder aCoder, ErrorReporter aErrorReporter) {
		final Property<Integer> portSetting = aSettings.getInteger(Settings.CORE_IPC_PORT);
		if (portSetting.getValue().intValue() < LsmlProtocolIPC.MIN_PORT) {
			final Alert notice = new Alert(AlertType.INFORMATION);
			notice.setTitle("Invalid port defined in settings");
			notice.setHeaderText("Port number will be reset to: " + LsmlProtocolIPC.DEFAULT_PORT);
			notice.setContentText("The port specified in the settings is: " + portSetting.getValue()
					+ " which is less than 1024. All ports lower than 1024 are reserved for administrator/root use.");
			portSetting.setValue(LsmlProtocolIPC.DEFAULT_PORT);
			notice.showAndWait();
		}

		final SecureRandom rng = new SecureRandom();

		int quietRetries = 2; // Quietly retry twice before prompting the user.
		while (true) {
			try {
				// FIXME: Solve this mess somehow
				final LsmlProtocolIPC ipc = new LsmlProtocolIPC(portSetting.getValue(), aXBar, aCoder, aErrorReporter);
				return Optional.of(ipc);
			} catch (final IOException e) {
				if (quietRetries-- > 0) {
					portSetting.setValue(LsmlProtocolIPC.randomPort(rng));
				} else {
					final Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Unable to open local socket!");
					alert.setHeaderText("LSML was unable to open a local socket on port: " + portSetting.getValue());
					alert.setContentText(
							"LSML uses a local socket connection to implement IPC necessary for opening of LSML links. "
									+ "You can try again with a new (random) port or disable LSML links for this session.");

					// FIXME: Set style

					final ButtonType tryAgain = new ButtonType("Try again");
					final ButtonType disableLinks = new ButtonType("Disable links");

					alert.getButtonTypes().setAll(disableLinks, tryAgain);
					final ButtonType pressedButton = alert.showAndWait().orElse(disableLinks);

					if (pressedButton == tryAgain) {
						portSetting.setValue(LsmlProtocolIPC.randomPort(rng));
					} else {
						return Optional.empty();
					}
				}
			}
		}
	}

	@Provides
	static LinkPresenter provideLinkPresenter(ErrorReporter aErrorReporter) {
		return new DialogLinkPresenter(aErrorReporter);
	}

	@Singleton
	@Named("mainwindowFilterFormatter")
	@Provides
	static FilteredModifierFormatter provideMainWindowModifierFilterFormatter(AffectsWeaponPredicate aPredicate) {
		return new FilteredModifierFormatter(aPredicate);
	}

	@Singleton
	@Provides
	static Optional<DataCache> provideMwoDatabase(Settings aSettings, SplashScreenController aSplashScreen,
			ErrorReporter aErrorReporter, @Named("version") String aVersion) {
		// This method is executed in a background task so that the splash can
		// display while we're doing
		// work. Unfortunately we also need to display dialogs to the FX
		// application thread so there will
		// be some back and forth here.

		aSplashScreen.setProgressText("Searching for game install...");
		while (!GameVFS.areDataFilesAvailable(aSettings)) {
			final ButtonType useBundled = new ButtonType("Use bundled data");
			final ButtonType browse = new ButtonType("Browse...");
			final ButtonType autoDetect = new ButtonType("Auto detect");
			final ButtonType exit = new ButtonType("Close LSML", ButtonData.CANCEL_CLOSE);

			runInAppThreadAndWait(() -> showDataSourceSelectionDialog(aSettings, useBundled, browse, autoDetect, exit))
					.ifPresent(aAction -> {
						if (aAction == autoDetect) {
							final Callback<Path, Boolean> confirmationCallback = (aPath) -> runInAppThreadAndWait(
									() -> showConfirmGameDirDialog(aPath));

							if (!GameVFS.autoDetectGameInstall(aSettings, aSplashScreen.subProgressTextProperty(),
									confirmationCallback)) {
								runInAppThreadAndWait(() -> {
									final Alert failed = new Alert(AlertType.ERROR,
											"Auto detection failed, no game install detected");
									failed.showAndWait();
									return null;
								});
							}
						} else if (aAction == exit) {
							System.exit(0);
						} else {
							throw new IllegalArgumentException("Unknown action: " + aAction);
						}
					});
		}

		// FIXME: This mess...
		aSplashScreen.setProgressText("Parsing game data...");
		final PrintWriter writer = new PrintWriter(System.out);
		final DataCache instance;
		try {
			instance = DataCache.getInstance(aSettings, writer, aVersion);
		} catch (final IOException e) {
			aErrorReporter.error("Unable to load game data", "An error occurred while loading the data cache", e);
			return Optional.empty();
		}
		writer.flush();

		switch (DataCache.getStatus()) {
		case BUILT_IN:
			break;
		case PARSE_FAILED:
			runInAppThreadAndWait(() -> {
				final Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Couldn't read game files...");
				alert.setHeaderText("LSML was unable to parse the game data files.");
				alert.setContentText("This usually happens when PGI has changed the structure of the data files "
						+ "in a patch. Please look for an updated version of LSML at www.li-soft.org."
						+ " In the meanwhile LSML will continue to function with the data from the last"
						+ " successfully parsed patch.");
				alert.showAndWait();
				return null;
			});
			break;
		default:
			break;
		}
		return Optional.of(instance);
	}

	@Singleton
	@Provides
	static UncaughtExceptionHandler provideUncaughtExceptionHandler() {
		return new DefaultExceptionHandler();
	}

	@Singleton
	@Provides
	static UpdateCallback provideUpdateCallback(ErrorReporter aErrorReporter) {
		return (aReleaseData) -> {
			if (aReleaseData != null) {
				Platform.runLater(() -> {
					// FIXME: Set style for this alert
					final Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Update available!");
					alert.setHeaderText("A new version of LSML is available: " + aReleaseData.name);
					alert.setContentText("For more information about whats new, see the download page.");
					final ButtonType download = new ButtonType("Download");
					final ButtonType later = new ButtonType("Remind me again in 3 days");
					alert.getButtonTypes().setAll(later, download);
					alert.showAndWait().ifPresent(aButton -> {
						if (aButton == download) {
							try {
								Desktop.getDesktop().browse(new URI(aReleaseData.html_url));
							} catch (final Exception e) {
								aErrorReporter.error("Cannot open link",
										"Unable to open the link in the system default browser, please open the link manually.",
										e);
							}
						}
					});
				});
			}

		};
	}

	@Deprecated
	private static <T> T runInAppThreadAndWait(Callable<T> aRunnable) {
		final Task<T> task = new Task<T>() {
			@Override
			protected T call() throws Exception {
				return aRunnable.call();
			}
		};
		Platform.runLater(task);
		try {
			return task.get();
		} catch (InterruptedException | ExecutionException e) {
			// Programmer error
			throw new RuntimeException(e);
		}
	}

	@Deprecated // Replace with a "GameDataFinder" class
	private static Boolean showConfirmGameDirDialog(Path aPath) {
		final Alert confirm = new Alert(AlertType.CONFIRMATION);
		confirm.setHeaderText("Is this your primary MWO installation?");
		confirm.setContentText(aPath.toString());
		final Optional<ButtonType> answer = confirm.showAndWait();
		return answer.isPresent() && answer.get() == ButtonType.OK;
	}

	/**
	 * Explains to the user that LSML needs data files, one way or another and
	 * prompts the user to either browse for game files, automatically detect
	 * the game installation, use bundled data or exit LSML.
	 *
	 * This function may change the state of the application settings as a
	 * result of user input.
	 *
	 * @param useBundled
	 *            {@link ButtonType} to use for showing the user the option to
	 *            use bundled data.
	 * @param browse
	 *            The {@link ButtonType} to use for showing the user the option
	 *            to browse for the game install,.
	 * @param autoDetect
	 *            The {@link ButtonType} to use for showing the user the option
	 *            of auto detecting the game install.
	 * @param exit
	 *            The {@link ButtonType} to use for showing the user the option
	 *            of exiting LSML.
	 * @return An {@link Optional} containing either <code>autoDetect</code>
	 *         (which must be ran in a background thread) or <code>exit</code>
	 *         if the user selected those actions otherwise the {@link Optional}
	 *         is empty.
	 */
	@Deprecated
	private static Optional<ButtonType> showDataSourceSelectionDialog(Settings aSettings, final ButtonType useBundled,
			final ButtonType browse, final ButtonType autoDetect, final ButtonType exit) {
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Detecting game files...");
		alert.setHeaderText("LSML needs access to game files.");
		alert.setContentText(
				"Normally LSML will parse your game install to find the latest 'Mech and weapon stats automatically."
						+ " To do this LSML needs to know where your game install is, you can choose to browse for it or let"
						+ " LSML auto-detect it for you. If you don't have a game install you can use the bundled data "
						+ "(can be changed from settings page).");

		alert.getButtonTypes().setAll(autoDetect, browse, useBundled, exit);

		final Optional<ButtonType> action = alert.showAndWait();
		if (action.isPresent()) {
			final ButtonType aButton = action.get();
			if (aButton == useBundled) {
				aSettings.getBoolean(Settings.CORE_FORCE_BUNDLED_DATA).setValue(Boolean.TRUE);
			} else if (aButton == autoDetect) {
				return Optional.of(autoDetect);
			} else if (aButton == browse) {
				final DirectoryChooser chooser = new DirectoryChooser();
				chooser.setTitle("Browse for MWO installation directory...");
				final File dir = chooser.showDialog(null);
				if (!GameVFS.isValidGameDirectory(dir)) {
					final Alert error = new Alert(AlertType.ERROR);
					error.setContentText("That directory is not a valid MWO installation.");
					error.showAndWait();
				} else {
					final Property<String> installDir = aSettings.getString(Settings.CORE_GAME_DIRECTORY);
					installDir.setValue(dir.getAbsolutePath().toString());
				}
			} else if (aButton == exit) {
				return Optional.of(exit);
			}
		}
		return Optional.empty();
	}

}
