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

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;

/**
 * Abstract base class that provides common functionality for controllers in LSML.
 *
 * @author Li Song
 */
public abstract class AbstractFXController {

    protected Region root;

    public AbstractFXController() {
        loadView();
    }

    public Region getView() {
        if (null == root) {
            loadView();
        }
        return root;
    }

    private String determineFxmlFileFromThis() {
        return ("view/" + getClass().getSimpleName() + ".fxml").replace("Controller", "");
    }

    private void loadView() {
        final String fxmlFile = determineFxmlFileFromThis();
        final URL fxmlResource = ClassLoader.getSystemClassLoader().getResource(fxmlFile);
        if (null == fxmlResource) {
            throw new IllegalArgumentException("Unable to load FXML file: " + fxmlFile);
        }
        final FXMLLoader fxmlLoader = new FXMLLoader(fxmlResource);
        fxmlLoader.setControllerFactory((aClass) -> this);
        try {
            root = fxmlLoader.load();
        }
        catch (final IOException e) {
            // Failure to load XML is a program error and cannot be recovered
            // from, promote to unchecked.
            throw new RuntimeException(e);
        }
    }

}
