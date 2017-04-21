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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.lisoft.lsml.application.DataComponent;
import org.lisoft.lsml.messages.ApplicationMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.NotificationMessage;
import org.lisoft.lsml.messages.NotificationMessage.Severity;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.database.Database;
import org.lisoft.lsml.model.database.EnvironmentDB;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.database.StockLoadoutDB;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.export.LsmlProtocolIPC;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.view_fx.controllers.SplashScreenController;
import org.lisoft.lsml.view_fx.controls.LsmlAlert;
import org.lisoft.lsml.view_headless.DaggerHeadlessDataComponent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * This is the main application for the LSML JavaFX GUI.
 *
 * FIXME: Dependency Inject stuff
 *
 * @author Li Song
 */
public class LiSongMechLab extends Application implements MessageReceiver {
    public static final String DEVELOP_VERSION = "(develop)";

    private static FXApplicationComponent fxApplication;

    private static DataComponent dataComponent;

    /**
     * This is just a dirty work around to manage to load the database when we're running unit tests.
     *
     * @return An {@link Optional} {@link Database}.
     */
    public static Optional<Database> getDatabase() {
        return getDataComponent().mwoDatabaseProvider().getDatabase();
    }

    public static DataComponent getDataComponent() {
        if (dataComponent == null) {
            dataComponent = DaggerHeadlessDataComponent.create();
        }
        return dataComponent;
    }

    public static FXApplicationComponent getFXApplication() {
        return fxApplication;
    }

    public static void main(final String[] args) {
        // This must be the first thing we do.
        dataComponent = DaggerFXDataComponent.create();
        fxApplication = DaggerFXApplicationComponent.builder().dataComponent(dataComponent).build();

        Thread.setDefaultUncaughtExceptionHandler(fxApplication.uncaughtExceptionHandler());

        if (args.length > 0 && sendLoadoutToActiveInstance(args[0])) {
            return;
        }

        launch(args);
    }

    public static boolean safeCommand(final Node aOwner, final CommandStack aStack, final Command aCommand,
            final MessageDelivery aDelivery) {
        try {
            aStack.pushAndApply(aCommand);
        }
        catch (final EquipException e) {
            aDelivery.post(new NotificationMessage(Severity.ERROR, null, e.getMessage()));
            return false;
        }
        catch (final Exception e) {
            LiSongMechLab.showError(aOwner, e);
            return false;
        }
        return true;
    }

    public static void showError(final Node aOwner, final Exception aException) {
        if (Platform.isFxApplicationThread()) {
            final LsmlAlert alert = new LsmlAlert(aOwner, AlertType.ERROR, aException.getMessage(), ButtonType.CLOSE);
            alert.showAndWait();
        }
        else {
            Platform.runLater(() -> showError(aOwner, aException));
        }
    }

    private static boolean sendLoadoutToActiveInstance(String aLsmlLink) {
        final Settings settings = fxApplication.settings();

        int port = settings.getInteger(Settings.CORE_IPC_PORT).getValue().intValue();
        if (port < LsmlProtocolIPC.MIN_PORT) {
            port = LsmlProtocolIPC.DEFAULT_PORT;
        }
        return LsmlProtocolIPC.sendLoadout(aLsmlLink, port);
    }

    private Stage mainStage;

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof ApplicationMessage) {
            final ApplicationMessage msg = (ApplicationMessage) aMsg;
            final Loadout loadout = msg.getLoadout();
            final Node origin = msg.getOrigin();

            switch (msg.getType()) {
                case OPEN_LOADOUT:
                    // Must be ran later, otherwise MessageXBar will emit a "attach
                    // from post" error.
                    Platform.runLater(() -> {
                        fxApplication.mechlabComponent(new FXMechlabModule(loadout)).mechlabWindow()
                                .createStage(mainStage);
                    });
                    break;
                case SHARE_LSML:
                    fxApplication.linkPresenter().show("LSML Export Complete",
                            "The loadout " + loadout.getName() + " has been encoded to a LSML link.",
                            dataComponent.loadoutCoder().encodeHTTPTrampoline(loadout), origin);
                    break;
                case SHARE_SMURFY:
                    try {
                        final String url = fxApplication.smurfyImportExport().sendLoadout(loadout);
                        fxApplication.linkPresenter().show("Smurfy Export Complete",
                                "The loadout " + loadout.getName() + " has been uploaded to smurfy.", url, origin);
                    }
                    catch (final IOException e) {
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
        final SplashScreenController splash = fxApplication.splash();
        mainStage = splash.createStage(null);

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
                foregroundLoad();
            }
            finally {
                // Keep splash up until we're done.
                splash.close();
            }
            aEvent.consume();
        });

        backgroundLoadingTask.setOnFailed(aEvent -> {
            splash.close();
            fxApplication.uncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                    backgroundLoadingTask.getException());
            aEvent.consume();
            System.exit(0);
        });

        // FIXME: Do I need to join this sucker somewhere?
        new Thread(backgroundLoadingTask).start();
    }

    @Override
    public void stop() {
        fxApplication.garage().exitSave();
        fxApplication.ipc().ifPresent(ipc -> ipc.close());
    }

    private boolean backgroundLoad() {
        fxApplication.osIntegration().setup();
        fxApplication.updateChecker().ifPresent(x -> x.run());

        if (!dataComponent.mwoDatabaseProvider().getDatabase().isPresent()) {
            return false;
        }

        // Hack, force static initialisation to run until we get around to
        // fixing our database design.
        ItemDB.lookup("C.A.S.E.");
        StockLoadoutDB.lookup(ChassisDB.lookup("JR7-D"));
        EnvironmentDB.lookupAll();
        UpgradeDB.lookup(3003);

        fxApplication.ipc().ifPresent(ipc -> ipc.startServer());
        return true;
    }

    private boolean foregroundLoad() {
        final GlobalGarage garage = fxApplication.garage();
        if (!garage.loadLastOrNew(fxApplication.splash().getView())) {
            return false;
        }

        fxApplication.messageXBar().attach(this);

        fxApplication.mainWindow().createStage(null);

        // final List<String> params = getParameters().getUnnamed();
        // for (final String param : params) {
        // openLoadout(ApplicationModel.model.xBar, param,
        // mainStage.getScene());
        // }

        return true;
    }

}
