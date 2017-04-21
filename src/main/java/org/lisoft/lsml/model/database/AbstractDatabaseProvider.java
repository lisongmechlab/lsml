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

import java.io.InputStream;
import java.util.Optional;

import org.lisoft.lsml.application.ErrorReporter;

/**
 * This ABC provides some common functionality for the different {@link DatabaseProvider}s.
 *
 *
 * @author Emily Björk
 */
public abstract class AbstractDatabaseProvider implements DatabaseProvider {
    private final ErrorReporter errorReporter;
    private final String currentVersion;

    protected AbstractDatabaseProvider(String aVersion, ErrorReporter aErrorReporter) {
        currentVersion = aVersion;
        errorReporter = aErrorReporter;
    }

    protected Optional<Database> getBundled() {
        try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("database.xml")) {
            // Let this throw as this is fatal.
            final Database database = (Database) Database.makeDatabaseXStream().fromXML(is);
            if (!database.getVersion().equals(currentVersion)) {
                return Optional.empty();
            }
            return Optional.of(database);
        }
        catch (final Throwable t) {
            errorReporter.error("Failed to load bundled database",
                    "Most likely due to Li forgetting to update the bundled database... Mea culpa.", t);
            return Optional.empty();
        }
    }
}
