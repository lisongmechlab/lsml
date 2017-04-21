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

package org.lisoft.lsml.model.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.view_fx.LiSongMechLab;

/**
 * This class parses all the environments to memory from the game files.
 *
 * @author Emily Björk
 */
public class EnvironmentDB {
    private static List<Environment> environments = new ArrayList<>();

    /**
     * A decision has been made to rely on static initializers for *DB classes. The motivation is that all items are
     * immutable, and this is the only way that allows providing global item constans such as ItemDB.AMS.
     */
    static {
        final Database database = LiSongMechLab.getDatabase()
                .orElseThrow(() -> new RuntimeException("Cannot run without database"));

        environments = new ArrayList<>(database.getEnvironments());
        environments.add(Environment.NEUTRAL);
    }

    /**
     * Looks up an {@link Environment} by name.
     *
     * @param aString
     *            The name of the {@link Environment} to look for.
     * @return The {@link Environment} which's name matches <code>aString</code> or null if no {@link Environment}
     *         matched.
     */
    public static Environment lookup(String aString) {
        for (final Environment environment : environments) {
            if (environment.getName().toLowerCase().equals(aString.toLowerCase())) {
                return environment;
            }
        }
        return null;
    }

    /**
     * @return A list of all {@link Environment}s loaded.
     */
    public static List<Environment> lookupAll() {
        return Collections.unmodifiableList(environments);
    }
}
