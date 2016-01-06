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
package org.lisoft.lsml.view_fx.loadout;

import java.io.IOException;

import org.lisoft.lsml.command.CmdSetArmorType;
import org.lisoft.lsml.command.CmdSetGuidanceType;
import org.lisoft.lsml.command.CmdSetHeatSinkType;
import org.lisoft.lsml.command.CmdSetStructureType;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.properties.LoadoutModelAdaptor;
import org.lisoft.lsml.view_fx.util.FxmlHelpers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;

/**
 * This control shows all the stats for a loadout in one convenient place.
 * 
 * @author Li Song
 */
public class UpgradesPane extends GridPane {
    @FXML
    private CheckBox upgradeArtemis;
    @FXML
    private CheckBox upgradeDoubleHeatSinks;
    @FXML
    private CheckBox upgradeEndoSteel;
    @FXML
    private CheckBox upgradeFerroFibrous;

    public UpgradesPane(MessageXBar aXBar, CommandStack aStack, LoadoutModelAdaptor aModel) throws IOException {
        FxmlHelpers.loadFxmlControl(this);

        Faction faction = aModel.loadout.getChassis().getFaction();

        FxmlHelpers.bindTogglable(upgradeArtemis, aModel.hasArtemis, aNewValue -> LiSongMechLab.safeCommand(this,
                aStack, new CmdSetGuidanceType(aXBar, aModel.loadout, UpgradeDB.getGuidance(faction, aNewValue))));

        if (!(aModel.loadout instanceof LoadoutStandard)) {
            Upgrades upgrades = aModel.loadout.getUpgrades();
            upgradeDoubleHeatSinks.setSelected(upgrades.getHeatSink().isDouble());
            upgradeEndoSteel.setSelected(upgrades.getStructure().getExtraSlots() != 0);
            upgradeFerroFibrous.setSelected(upgrades.getArmor().getExtraSlots() != 0);
            upgradeDoubleHeatSinks.setDisable(true);
            upgradeEndoSteel.setDisable(true);
            upgradeFerroFibrous.setDisable(true);
        }
        else {
            LoadoutStandard lstd = (LoadoutStandard) aModel.loadout;

            FxmlHelpers.bindTogglable(upgradeDoubleHeatSinks, aModel.hasDoubleHeatSinks,
                    aNewValue -> LiSongMechLab.safeCommand(this, aStack,
                            new CmdSetHeatSinkType(aXBar, lstd, UpgradeDB.getHeatSinks(faction, aNewValue))));

            FxmlHelpers.bindTogglable(upgradeEndoSteel, aModel.hasEndoSteel,
                    aNewValue -> LiSongMechLab.safeCommand(this, aStack,
                            new CmdSetStructureType(aXBar, lstd, UpgradeDB.getStructure(faction, aNewValue))));

            FxmlHelpers.bindTogglable(upgradeFerroFibrous, aModel.hasFerroFibrous,
                    aNewValue -> LiSongMechLab.safeCommand(this, aStack,
                            new CmdSetArmorType(aXBar, lstd, UpgradeDB.getArmor(faction, aNewValue))));
        }
    }
}
