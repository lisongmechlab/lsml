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
package org.lisoft.lsml.view_fx.controllers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lisoft.lsml.view_fx.Settings;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Handles showing a splash screen on program startup.
 *
 * @author Li Song
 */
@Singleton
public class SplashScreenController extends AbstractFXStageController {
	public static final Duration MINIMUM_SPLASH_TIME = Duration.of(500, ChronoUnit.MILLIS);

	private static final javafx.util.Duration FADE_DURATION = javafx.util.Duration.seconds(1.2);

	@FXML
	private Label progressText;

	@FXML
	private Label progressSubText;

	@Inject
	public SplashScreenController(Settings aSettings) {
		super(aSettings, null);
	}

	public void close() {
		final FadeTransition fadeSplash = new FadeTransition(FADE_DURATION, root);
		fadeSplash.setFromValue(1.0);
		fadeSplash.setToValue(0.0);
		fadeSplash.setOnFinished(actionEvent -> getStage().hide());
		fadeSplash.play();
	}

	/**
	 * @param string
	 */
	public void setProgressText(String string) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> {
				setProgressText(string);
			});
		} else {
			progressText.setText(string);
		}
	}

	/**
	 * @return
	 */
	public StringProperty subProgressTextProperty() {
		return progressSubText.textProperty();
	}

	@Override
	protected void onLoad() {
		progressText.setText("Reading cached game data...");
		progressSubText.setText("...");
	}

	@Override
	protected void onShow(LSMLStage aStage) {
		aStage.centerOnScreen();
		aStage.setTitle("Loading Li Song Mechlab...");
	}
}
