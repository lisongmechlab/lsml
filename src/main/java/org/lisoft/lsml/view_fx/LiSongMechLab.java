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

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.lisoft.lsml.application.UpdateChecker;
import org.lisoft.lsml.application.components.*;
import org.lisoft.lsml.application.modules.GraphicalMechlabModule;
import org.lisoft.lsml.messages.*;
import org.lisoft.lsml.messages.ApplicationMessage.Type;
import org.lisoft.lsml.messages.NotificationMessage.Severity;
import org.lisoft.lsml.model.NoSuchItemException;
import org.lisoft.lsml.model.database.*;
import org.lisoft.lsml.model.export.LsmlProtocolIPC;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.DecodingException;
import org.lisoft.lsml.util.EncodingException;
import org.lisoft.lsml.view_fx.controllers.SplashScreenController;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * This is the main application for the LSML JavaFX GUI.
 *
 * @author Li Song
 */
public class LiSongMechLab extends Application implements MessageReceiver {
    public static final String DEVELOP_VERSION = "(develop)";
    private static final javafx.util.Duration AUTO_SAVE_PERIOD = javafx.util.Duration.minutes(5);
    private static GraphicalCoreComponent coreComponent;
    private static Optional<Database> db;
    private static GraphicalApplicationComponent fxApplication;
    private Stage mainStage;

    /**
     * This is just a dirty workaround to manage to load the database when we're running unit tests.
     *
     * @return An {@link Optional} {@link Database}.
     */
    public static Optional<Database> getDatabase() {
        if (db == null) {
            if (coreComponent != null) {
                db = coreComponent.mwoDatabaseProvider().getDatabase();
            } else {
                db = DaggerHeadlessCoreComponent.create().mwoDatabaseProvider().getDatabase();
            }
        }
        return db;
    }

    public static void main(final String[] args) {
        // This must be the first thing we do.
        coreComponent = DaggerGraphicalCoreComponent.create();
        fxApplication = DaggerGraphicalApplicationComponent.builder().graphicalCoreComponent(coreComponent).build();

        Thread.setDefaultUncaughtExceptionHandler(coreComponent.uncaughtExceptionHandler());

        if (args.length > 0 && sendLoadoutToActiveInstance(args[0])) {
            return;
        }

        launch(args);
    }

    public static boolean safeCommand(final Node aOwner, final CommandStack aStack, final Command aCommand,
                                      final MessageDelivery aDelivery) {
        try {
            aStack.pushAndApply(aCommand);
        } catch (final EquipException e) {
            aDelivery.post(new NotificationMessage(Severity.ERROR, null, e.getMessage()));
            return false;
        } catch (final Exception e) {
            LiSongMechLab.showError(aOwner, e);
            return false;
        }
        return true;
    }

    public static void showError(final Node aOwner, final Exception aException) {
        if (Platform.isFxApplicationThread()) {
            final LsmlAlert alert = new LsmlAlert(aOwner, AlertType.ERROR, aException.getMessage(), ButtonType.CLOSE);
            alert.showAndWait();
        } else {
            Platform.runLater(() -> showError(aOwner, aException));
        }
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof ApplicationMessage) {
            final ApplicationMessage msg = (ApplicationMessage) aMsg;
            final Loadout loadout = msg.getLoadout();
            final Node origin = msg.getOrigin();

            switch (msg.getType()) {
                case OPEN_LOADOUT:
                    // Must be run later, otherwise MessageXBar will emit an "attach from post" error.
                    Platform.runLater(
                        () -> fxApplication.mechlabComponent(new GraphicalMechlabModule(loadout)).mechlabWindow()
                                           .createStage(mainStage, coreComponent.settings()));
                    break;
                case SHARE_MWO:
                    try {
                        fxApplication.linkPresenter().show("MWO Export Complete", "The loadout " + loadout.getName() +
                                                                                  " has been encoded to a MWO Export string.",
                                                           coreComponent.mwoLoadoutCoder().encode(loadout), origin);
                    } catch (final EncodingException e) {
                        LiSongMechLab.showError(origin, e);
                    }
                    break;
                case SHARE_LSML:
                    fxApplication.linkPresenter().show("LSML Export Complete", "The loadout " + loadout.getName() +
                                                                               " has been encoded to a LSML link.",
                                                       coreComponent.loadoutCoder().encodeHTTPTrampoline(loadout),
                                                       origin);
                    break;
                case SHARE_SMURFY:
                    try {
                        final String url = coreComponent.smurfyImportExport().sendLoadout(loadout);
                        fxApplication.linkPresenter().show("Smurfy Export Complete",
                                                           "The loadout " + loadout.getName() +
                                                           " has been uploaded to Smurfy.", url, origin);
                    } catch (final IOException e) {
                        LiSongMechLab.showError(origin, e);
                    }
                    break;
                case CLOSE_OVERLAY: // Fall through
                default:
                    break;
            }
        }
    }

    @Override
    public void start(final Stage aStage) throws Exception {
        aStage.close(); // We won't use the primary stage, get rid of it.

        // Throw up the splash ASAP
        final SplashScreenController splash = coreComponent.splash();
        mainStage = splash.createStage(null, coreComponent.settings());

        // Splash won't display until we return from start(), so we use a
        // background thread to do the loading after we returned.
        // XXX: Why are we not using Platform.invokeLater() ?
        final Task<Boolean> backgroundLoadingTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                final Instant startTime = Instant.now();
                final boolean success = backgroundLoad();
                final Instant endTime = Instant.now();
                final Duration loadDuration = Duration.between(startTime, endTime);
                final Duration sleepDuration = SplashScreenController.MINIMUM_SPLASH_TIME.minus(loadDuration);
                if (!sleepDuration.isNegative() && !sleepDuration.isZero()) {
                    Thread.sleep(sleepDuration.toMillis());
                }
                return success;
            }
        };

        backgroundLoadingTask.setOnSucceeded(aEvent -> {
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

        backgroundLoadingTask.setOnFailed(aEvent -> {
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
        fxApplication.ipc().ifPresent(LsmlProtocolIPC::close);
    }

    private static boolean sendLoadoutToActiveInstance(String aLSMLLink) {
        final Settings settings = coreComponent.settings();

        int port = settings.getInteger(Settings.CORE_IPC_PORT).getValue();
        if (port < LsmlProtocolIPC.MIN_PORT) {
            port = LsmlProtocolIPC.DEFAULT_PORT;
        }
        return LsmlProtocolIPC.sendLoadout(aLSMLLink, port);
    }

    private boolean backgroundLoad() throws NoSuchItemException {
        fxApplication.osIntegration().setup();
        fxApplication.updateChecker().ifPresent(UpdateChecker::run);

        if (!coreComponent.mwoDatabaseProvider().getDatabase().isPresent()) {
            return false;
        }

        initDB();

        fxApplication.ipc().ifPresent(LsmlProtocolIPC::startServer);
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
        if (!garage.openLastGarage(splashRoot) && !garage.createOrOpen(splashRoot.getScene().getWindow())) {
            return false;
        }

        fxApplication.messageXBar().attach(this);
        fxApplication.mainWindow().createStage(splashRoot.getScene().getWindow(), coreComponent.settings());

        final Parent origin = mainStage.getScene().getRoot();
        final List<String> params = getParameters().getUnnamed();
        for (final String param : params) {
            try {
                final Loadout loadout = coreComponent.loadoutCoder().parse(param);
                fxApplication.messageXBar().post(new ApplicationMessage(loadout, Type.OPEN_LOADOUT, origin));
            } catch (final Exception e) {
                showError(origin, new DecodingException("Parse error on loadout passed on command line", e));
            }
        }

        enablePeriodicAutoSave(garage);
        return true;
    }

    private void initDB() throws NoSuchItemException {
        // Hack: force static initialisation to run until we get around to
        // fixing our database design.
        ItemDB.lookup("C.A.S.E.");
        StockLoadoutDB.lookup(ChassisDB.lookup("JR7-D"));
        //noinspection ResultOfMethodCallIgnored
        EnvironmentDB.lookupAll();
        UpgradeDB.lookup(3003);
    }

}
