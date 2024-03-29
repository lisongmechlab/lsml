/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.view_fx.controllers.loadoutwindow;

import javafx.scene.layout.Region;
import javax.inject.Inject;
import javax.inject.Named;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.DynamicSlotDistributor;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.Settings;
import org.lisoft.lsml.view_fx.controllers.LoadoutWindowController;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.style.ItemToolTipFormatter;
import org.lisoft.mwo_data.mechs.Location;

/**
 * A factory for panels used in the {@link LoadoutWindowController} to make construction easier.
 *
 * @author Li Song
 */
public class LoadoutPaneFactory {
  private final CommandStack cmdStack;
  private final DynamicSlotDistributor distributor;
  private final LoadoutFactory loadoutFactory;
  private final LoadoutModelAdaptor model;
  private final Settings settings;
  private final ItemToolTipFormatter toolTipFormatter;
  private final MessageXBar xBar;

  @Inject
  public LoadoutPaneFactory(
      Settings aSettings,
      @Named("local") MessageXBar aXBar,
      @Named("local") CommandStack aCommandStack,
      LoadoutModelAdaptor aModel,
      DynamicSlotDistributor aDistributor,
      ItemToolTipFormatter aToolTipFormatter,
      LoadoutFactory aLoadoutFactory) {
    settings = aSettings;
    xBar = aXBar;
    cmdStack = aCommandStack;
    model = aModel;
    distributor = aDistributor;
    toolTipFormatter = aToolTipFormatter;
    loadoutFactory = aLoadoutFactory;
  }

  public Region component(Location aLocation) {
    return new ComponentPaneController(
            settings,
            xBar,
            cmdStack,
            model,
            aLocation,
            distributor,
            toolTipFormatter,
            loadoutFactory)
        .getView();
  }

  public Region modulePane() {
    return new ModulePaneController(
            xBar, cmdStack, model, settings.getBoolean(Settings.UI_PGI_COMPATIBILITY).getValue())
        .getView();
  }
}
