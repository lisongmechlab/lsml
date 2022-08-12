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
package org.lisoft.lsml.application.components;

import dagger.Component;
import org.lisoft.lsml.application.modules.GraphicalCoreModule;
import org.lisoft.lsml.view_fx.GraphicalDatabaseProvider;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controllers.SplashScreenController;

import javax.inject.Singleton;

@Singleton
@Component(modules = GraphicalCoreModule.class)
public interface GraphicalCoreComponent extends CoreComponent {
    Settings settings();

    /**
     * This is technically not part of the core component but it has to be here because
     * {@link GraphicalDatabaseProvider} requires it to update the splash screen
     * with loading progress.
     * <p>
     * TODO: Put splash in a sub/dep-component and tie the lifetime to that component
     */
    SplashScreenController splash();
}
