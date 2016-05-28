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

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.export.BatchImportExporter;
import org.lisoft.lsml.model.export.LsmlLinkProtocol;
import org.lisoft.lsml.model.export.LsmlProtocolIPC;
import org.lisoft.lsml.model.export.SmurfyImportExport;
import org.lisoft.lsml.model.loadout.LoadoutBuilder.ErrorReportingCallback;
import org.lisoft.lsml.util.CommandStack;

/**
 * This class contains the model for the general application.
 *
 * Must only be accessed through JavaFX Application thread!
 *
 * @author Li Song
 */
public class ApplicationModel {
    public final static ApplicationModel model = new ApplicationModel();

    public final CommandStack cmdStack = new CommandStack(100);
    public final MessageXBar xBar = new MessageXBar();
    public final Settings settings = Settings.getSettings();
    public final GlobalGarage globalGarage = GlobalGarage.instance;
    public final ErrorReportingCallback errorReporter = DefaultLoadoutErrorReporter.instance;
    public final Base64LoadoutCoder coder = new Base64LoadoutCoder(errorReporter);
    public final BatchImportExporter importer = new BatchImportExporter(coder, LsmlLinkProtocol.LSML, errorReporter);
    public final SmurfyImportExport smurfyImportExport = new SmurfyImportExport(coder, errorReporter);
    public LsmlProtocolIPC ipc;
}
