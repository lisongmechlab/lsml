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
package lisong_mechlab.model.loadout;

import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.OpStripComponent;
import lisong_mechlab.model.upgrades.OpSetArmorType;
import lisong_mechlab.model.upgrades.OpSetGuidanceType;
import lisong_mechlab.model.upgrades.OpSetHeatSinkType;
import lisong_mechlab.model.upgrades.OpSetStructureType;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.message.MessageDelivery;

/**
 * This operation removes all armor, upgrades and items from a {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class OpStripLoadout extends CompositeOperation {
    protected final LoadoutBase<?> loadout;

    public OpStripLoadout(LoadoutBase<?> aLoadout, MessageDelivery aMessageDelivery) {
        super("strip mech", aMessageDelivery);
        loadout = aLoadout;
    }

    @Override
    public void buildOperation() {
        for (ConfiguredComponentBase component : loadout.getComponents()) {
            addOp(new OpStripComponent(messageBuffer, loadout, component));
        }

        if (loadout instanceof LoadoutStandard) {
            LoadoutStandard loadoutStandard = (LoadoutStandard) loadout;
            addOp(new OpSetStructureType(messageBuffer, loadoutStandard, UpgradeDB.STANDARD_STRUCTURE));
            addOp(new OpSetGuidanceType(messageBuffer, loadoutStandard, UpgradeDB.STANDARD_GUIDANCE));
            addOp(new OpSetArmorType(messageBuffer, loadoutStandard, UpgradeDB.STANDARD_ARMOR));
            addOp(new OpSetHeatSinkType(messageBuffer, loadoutStandard, UpgradeDB.STANDARD_HEATSINKS));
        }
    }
}
