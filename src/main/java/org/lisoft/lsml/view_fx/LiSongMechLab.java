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

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.application.UpdateChecker;
import org.lisoft.lsml.application.components.*;
import org.lisoft.lsml.application.modules.GraphicalMechlabModule;
import org.lisoft.lsml.messages.*;
import org.lisoft.lsml.messages.NotificationMessage.Severity;
import org.lisoft.lsml.model.*;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.view_fx.controllers.SplashScreenController;
import org.lisoft.mwo_data.Database;
import org.lisoft.mwo_data.equipment.NoSuchItemException;

import java.awt.*;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * This is the main application for the LSML JavaFX GUI.
 *
 * @author Li Song
 */
public class LiSongMechLab extends Application implements MessageReceiver {
  public static final String DEVELOP_VERSION = "0.0.0";
  private static final javafx.util.Duration AUTO_SAVE_PERIOD = javafx.util.Duration.minutes(5);
  private static GraphicalCoreComponent coreComponent;
  private static Database db;
  private static GraphicalApplicationComponent fxApplication;
  private Stage mainStage;

  public static void openURLInBrowser(String aURL, ErrorReporter aErrorReporter) {
    try {
      Desktop.getDesktop().browse(new URI(aURL));
    } catch (final Exception e) {
      aErrorReporter.error(
          "Couldn't open browser",
          "LSML was unable to open link in the default browser. Please open: "
              + aURL
              + " manually.",
          e);
    }
  }

  /**
   * This is just a dirty workaround to manage to load the database when we're running unit tests.
   *
   * @return An {@link Optional} {@link Database}.
   */
  public static Database getDatabase() {
    if (db == null) {
      db =
          Objects.requireNonNullElseGet(coreComponent, DaggerHeadlessCoreComponent::create)
              .mwoDatabaseProvider()
              .getDatabase();
    }
    return db;
  }

  public static void main(final String[] args) {
    // This must be the first thing we do.
    coreComponent = DaggerGraphicalCoreComponent.create();
    fxApplication =
        DaggerGraphicalApplicationComponent.builder().graphicalCoreComponent(coreComponent).build();

    Thread.setDefaultUncaughtExceptionHandler(coreComponent.uncaughtExceptionHandler());

    launch(args);
  }

  public static boolean safeCommand(
      final Node aOwner,
      final CommandStack aStack,
      final Command aCommand,
      final MessageDelivery aDelivery) {
    try {
      aStack.pushAndApply(aCommand);
    } catch (final EquipException e) {
      aDelivery.post(new NotificationMessage(Severity.ERROR, null, e.getMessage()));
      return false;
    } catch (final Exception e) {
      coreComponent
          .errorReporter()
          .error(
              aOwner,
              "Unexpected error",
              "Error while performing operation: " + aCommand.describe(),
              e);
      return false;
    }
    return true;
  }

  @Override
  public void receive(Message aMsg) {
    if (aMsg instanceof final ApplicationMessage msg) {
      final Loadout loadout = msg.getLoadout();
      final Node origin = msg.getOrigin();

      switch (msg.getType()) {
        case OPEN_LOADOUT:
          // Must be run later, otherwise MessageXBar will emit an "attach from post" error.
          Platform.runLater(
              () ->
                  fxApplication
                      .mechlabComponent(new GraphicalMechlabModule(loadout))
                      .mechlabWindow()
                      .createStage(mainStage, coreComponent.settings()));
          break;
        case SHARE_MWO:
          fxApplication
              .linkPresenter()
              .show(
                  "MWO Export Complete",
                  "The loadout " + loadout.getName() + " has been encoded to a MWO Export string.",
                  coreComponent.mwoLoadoutCoder().encode(loadout),
                  origin);
          break;
        case CLOSE_OVERLAY: // Fall through
        default:
          break;
      }
    }
  }

  @Override
  public void start(final Stage aStage) {
    aStage.close(); // We won't use the primary stage, get rid of it.

    // Throw up the splash ASAP
    final SplashScreenController splash = coreComponent.splash();
    mainStage = splash.createStage(null, coreComponent.settings());

    // Splash won't display until we return from start(), so we use a
    // background thread to do the loading after we returned.
    // XXX: Why are we not using Platform.invokeLater() ?
    final Task<Boolean> backgroundLoadingTask =
        new Task<Boolean>() {
          @Override
          protected Boolean call() throws Exception {
            final Instant startTime = Instant.now();
            final boolean success = backgroundLoad();
            final Instant endTime = Instant.now();
            final Duration loadDuration = Duration.between(startTime, endTime);
            final Duration sleepDuration =
                SplashScreenController.MINIMUM_SPLASH_TIME.minus(loadDuration);
            if (!sleepDuration.isNegative() && !sleepDuration.isZero()) {
              Thread.sleep(sleepDuration.toMillis());
            }
            return success;
          }
        };

    backgroundLoadingTask.setOnSucceeded(
        aEvent -> {
          // This is executed on JavaFX Application Thread
          try {
            if (!foregroundLoad()) {
              System.exit(0);
            }
          } finally {
            // Keep splash up until we're done.
            splash.close();
          }
          aEvent.consume();
        });

    backgroundLoadingTask.setOnFailed(
        aEvent -> {
          splash.close();
          final Throwable exception = backgroundLoadingTask.getException();
          if (null != exception) {
            throw new RuntimeException("Error during startup!", exception);
          }
          aEvent.consume();
        });

    // FIXME: Do I need to join this sucker somewhere?
    new Thread(backgroundLoadingTask).start();
  }

  @Override
  public void stop() {
    fxApplication.garage().exitSave(null);
  }

   private boolean backgroundLoad() throws NoSuchItemException {
    fxApplication.osIntegration().setup();
    fxApplication.updateChecker().ifPresent(UpdateChecker::run);

    coreComponent.mwoDatabaseProvider().getDatabase();

    initDB();
    return true;
  }

  private void enablePeriodicAutoSave(GlobalGarage garage) {
    Timeline autoSaveTimer = new Timeline(new KeyFrame(AUTO_SAVE_PERIOD, e -> garage.autoSave()));
    autoSaveTimer.setCycleCount(Animation.INDEFINITE);
    autoSaveTimer.play();
  }

  private boolean foregroundLoad() {
    final GlobalGarage garage = fxApplication.garage();
    final Region splashRoot = coreComponent.splash().getView();
    if (!garage.openLastGarage(splashRoot)
        && !garage.createOrOpen(splashRoot.getScene().getWindow())) {
      return false;
    }

    fxApplication.messageXBar().attach(this);
    fxApplication
        .mainWindow()
        .createStage(splashRoot.getScene().getWindow(), coreComponent.settings());
    enablePeriodicAutoSave(garage);
    return true;
  }

  private void initDB() throws NoSuchItemException {
    // Hack: force static initialization to run until we get around to
    // fixing our database design.
    ItemDB.lookup("C.A.S.E.");
    StockLoadoutDB.lookup(ChassisDB.lookup("JR7-D"));
    //noinspection ResultOfMethodCallIgnored
    EnvironmentDB.lookupAll();
    UpgradeDB.lookup(3003);
  }
}
