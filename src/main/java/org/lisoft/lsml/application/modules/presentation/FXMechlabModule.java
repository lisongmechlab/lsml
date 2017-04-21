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
package org.lisoft.lsml.application.modules.presentation;

import javax.inject.Named;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.loadout.Loadout;

import dagger.Module;
import dagger.Provides;

/**
 * This {@link Module} provides the necessary services for a mechlab window implemented through JavaFX.
 *
 * @author Li Song
 */
@Module
public class FXMechlabModule {
    private final MessageXBar xBar;
    private final Loadout loadout;

    /**
     * @param aLoadout
     *            The data that shall be injected into the services.
     */
    public FXMechlabModule(Loadout aLoadout) {
        loadout = aLoadout;
        xBar = new MessageXBar();
    }

    @Provides
    Loadout provideLoadout() {
        return loadout;
    }

    @Provides
    @Named("local")
    MessageXBar provideMessageXBar() {
        return xBar;
    }
}
