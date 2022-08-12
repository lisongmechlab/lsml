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
import org.lisoft.lsml.application.ApplicationSingleton;
import org.lisoft.lsml.application.LinkPresenter;
import org.lisoft.lsml.application.OSIntegration;
import org.lisoft.lsml.application.UpdateChecker;
import org.lisoft.lsml.application.modules.GraphicalApplicationModule;
import org.lisoft.lsml.application.modules.GraphicalMechlabModule;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.export.LsmlProtocolIPC;
import org.lisoft.lsml.view_fx.GlobalGarage;
import org.lisoft.lsml.view_fx.controllers.MainWindowController;

import javax.inject.Named;
import java.util.Optional;

/**
 * This dagger Component provides the services necessary for the main application.
 *
 * @author Li Song
 */
@ApplicationSingleton
@Component(dependencies = GraphicalCoreComponent.class, modules = GraphicalApplicationModule.class)
public interface GraphicalApplicationComponent {
    GlobalGarage garage();

    Optional<LsmlProtocolIPC> ipc();

    LinkPresenter linkPresenter();

    MainWindowController mainWindow();

    GraphicalMechlabComponent mechlabComponent(GraphicalMechlabModule aMechlabModule);

    @Named("global")
    MessageXBar messageXBar();

    OSIntegration osIntegration();


    Optional<UpdateChecker> updateChecker();
}
