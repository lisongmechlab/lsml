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
package org.lisoft.lsml.command;

import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPodDB;
import org.lisoft.lsml.model.item.ItemDB;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.StockLoadout;
import org.lisoft.lsml.model.loadout.StockLoadoutDB;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.upgrades.OpSetArmorType;
import org.lisoft.lsml.model.upgrades.OpSetGuidanceType;
import org.lisoft.lsml.model.upgrades.OpSetHeatSinkType;
import org.lisoft.lsml.model.upgrades.OpSetStructureType;
import org.lisoft.lsml.util.message.MessageDelivery;

/**
 * This operation loads a 'mechs stock {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class OpLoadStock extends OpLoadoutBase {
    private final ChassisBase chassiVariation;

    public OpLoadStock(ChassisBase aChassiVariation, LoadoutBase<?> aLoadout, MessageDelivery aMessageDelivery) {
        super(aLoadout, aMessageDelivery, "load stock");
        chassiVariation = aChassiVariation;
    }

    @Override
    public void buildOperation() {
        StockLoadout stockLoadout = StockLoadoutDB.lookup(chassiVariation);

        addOp(new OpStripLoadout(loadout, messageBuffer));

        if (loadout instanceof LoadoutStandard) {
            LoadoutStandard loadoutStandard = (LoadoutStandard) loadout;
            addOp(new OpSetStructureType(messageBuffer, loadoutStandard, stockLoadout.getStructureType()));
            addOp(new OpSetArmorType(messageBuffer, loadoutStandard, stockLoadout.getArmorType()));
            addOp(new OpSetHeatSinkType(messageBuffer, loadoutStandard, stockLoadout.getHeatSinkType()));
        }
        else if (loadout instanceof LoadoutOmniMech) {
            LoadoutOmniMech loadoutOmniMech = (LoadoutOmniMech) loadout;
            for (Location location : Location.values()) {
                addOp(new OpChangeOmniPod(messageBuffer, loadoutOmniMech, loadoutOmniMech.getComponent(location),
                        OmniPodDB.lookupOriginal(loadoutOmniMech.getChassis(), location)));
            }
        }
        addOp(new OpSetGuidanceType(messageBuffer, loadout, stockLoadout.getGuidanceType()));

        for (StockLoadout.StockComponent stockComponent : stockLoadout.getComponents()) {
            Location location = stockComponent.getPart();
            ConfiguredComponentBase configured = loadout.getComponent(location);

            if (location.isTwoSided()) {
                addOp(new OpSetArmor(messageBuffer, loadout, configured, ArmorSide.FRONT, 0, true));
                addOp(new OpSetArmor(messageBuffer, loadout, configured, ArmorSide.BACK, stockComponent.getArmorBack(),
                        true));
                addOp(new OpSetArmor(messageBuffer, loadout, configured, ArmorSide.FRONT,
                        stockComponent.getArmorFront(), true));
            }
            else {
                addOp(new OpSetArmor(messageBuffer, loadout, configured, ArmorSide.ONLY,
                        stockComponent.getArmorFront(), true));
            }

            for (Integer item : stockComponent.getItems()) {
                addOp(new OpAddItem(messageBuffer, loadout, configured, ItemDB.lookup(item)));
            }
        }
    }
}
