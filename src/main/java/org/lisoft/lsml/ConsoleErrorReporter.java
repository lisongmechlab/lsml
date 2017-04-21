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
package org.lisoft.lsml;

import java.util.List;

import javax.inject.Inject;

import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view_fx.ErrorReporter;

import javafx.stage.Window;

/**
 * Reports errors to {@link System#err}.
 *
 * @author Emily Björk
 */
public class ConsoleErrorReporter implements ErrorReporter {
    @Inject
    public ConsoleErrorReporter() {
        // NOP
    }

    @Override
    public void error(Window aOwner, Loadout aLoadout, List<Throwable> aErrors) {
        System.err.println("Error processing loadout: " + aLoadout.toString());
        for (final Throwable t : aErrors) {
            t.printStackTrace();
        }
    }

    @Override
    public void error(Window aOwner, String aTitle, String aMessage, Throwable aThrowable) {
        System.err.println(aTitle);
        System.err.println(aMessage);
        aThrowable.printStackTrace();
    }

    @Override
    public void fatal(Window aOwner, String aTitle, String aMessage, Throwable aThrowable) {
        System.err.println(aTitle);
        System.err.println(aMessage);
        aThrowable.printStackTrace();
        System.exit(0);
    }

}
