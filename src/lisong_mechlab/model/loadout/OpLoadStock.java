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
package lisong_mechlab.model.loadout;

import lisong_mechlab.model.StockLoadout;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.OmniPodDB;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.OpAddItem;
import lisong_mechlab.model.loadout.component.OpChangeOmniPod;
import lisong_mechlab.model.loadout.component.OpSetArmor;
import lisong_mechlab.model.upgrades.OpSetArmorType;
import lisong_mechlab.model.upgrades.OpSetGuidanceType;
import lisong_mechlab.model.upgrades.OpSetHeatSinkType;
import lisong_mechlab.model.upgrades.OpSetStructureType;
import lisong_mechlab.util.message.MessageDelivery;

/**
 * This operation loads a 'mechs stock {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class OpLoadStock extends OpLoadoutBase {
	private final ChassisBase	chassiVariation;

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
		} else if (loadout instanceof LoadoutOmniMech) {
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
				addOp(new OpSetArmor(messageBuffer, loadout, configured, ArmorSide.BACK, stockComponent.getArmorBack(), true));
				addOp(new OpSetArmor(messageBuffer, loadout, configured, ArmorSide.FRONT, stockComponent.getArmorFront(), true));
			} else {
				addOp(new OpSetArmor(messageBuffer, loadout, configured, ArmorSide.ONLY, stockComponent.getArmorFront(), true));
			}

			for (Integer item : stockComponent.getItems()) {
				addOp(new OpAddItem(messageBuffer, loadout, configured, ItemDB.lookup(item)));
			}
		}
	}
}
