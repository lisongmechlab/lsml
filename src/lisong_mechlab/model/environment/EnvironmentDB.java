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

package lisong_mechlab.model.environment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lisong_mechlab.model.DataCache;

/**
 * This class parses all the environments to memory from the game files.
 * 
 * @author Li Song
 */
public class EnvironmentDB {
    private static List<Environment> environments = new ArrayList<>();

    /**
     * Looks up an {@link Environment} by name.
     * 
     * @param aString
     *            The name of the {@link Environment} to look for.
     * @return The {@link Environment} which's name matches <code>aString</code> or null if no {@link Environment}
     *         matched.
     */
    public static Environment lookup(String aString) {
        for (Environment environment : environments) {
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

    /**
     * A decision has been made to rely on static initializers for *DB classes. The motivation is that all items are
     * immutable, and this is the only way that allows providing global item constans such as ItemDB.AMS.
     */
    static {
        DataCache dataCache;
        try {
            dataCache = DataCache.getInstance();
        }
        catch (IOException e) {
            throw new RuntimeException(e); // Promote to unchecked. This is a critical failure.
        }

        environments = dataCache.getEnvironments();
    }
}
