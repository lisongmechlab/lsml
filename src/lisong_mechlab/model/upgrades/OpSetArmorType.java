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
package lisong_mechlab.model.upgrades;

import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.upgrades.Upgrades.UpgradesMessage;
import lisong_mechlab.model.upgrades.Upgrades.UpgradesMessage.ChangeMsg;
import lisong_mechlab.util.OperationStack.Operation;
import lisong_mechlab.util.message.MessageDelivery;

/**
 * This {@link Operation} can change the armor type of a {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class OpSetArmorType extends OpUpgradeBase {
	private final ArmorUpgrade		oldValue;
	private final ArmorUpgrade		newValue;
	private final UpgradesMutable	upgrades;
	private final LoadoutStandard	loadout;

	/**
	 * Creates a {@link OpSetArmorType} that only affects a stand-alone {@link UpgradesMutable} object This is useful
	 * only for altering {@link UpgradesMutable} objects which are not attached to a {@link LoadoutStandard} in any way.
	 * 
	 * @param aUpgrades
	 *            The {@link UpgradesMutable} object to alter with this {@link Operation}.
	 * @param aArmorUpgrade
	 *            The new armor type when this upgrades has been applied.
	 */
	public OpSetArmorType(UpgradesMutable aUpgrades, ArmorUpgrade aArmorUpgrade) {
		super(null, aArmorUpgrade.getName());
		upgrades = aUpgrades;
		loadout = null;
		oldValue = upgrades.getArmor();
		newValue = aArmorUpgrade;
	}

	/**
	 * Creates a new {@link OpSetStructureType} that will change the armor type of a {@link LoadoutStandard}.
	 * 
	 * @param aMessageDelivery
	 *            A {@link MessageDelivery} to signal changes in internal structure on.
	 * @param aLoadout
	 *            The {@link LoadoutStandard} to alter.
	 * @param aArmorUpgrade
	 *            The new armor type this upgrades is applied.
	 */
	public OpSetArmorType(MessageDelivery aMessageDelivery, LoadoutStandard aLoadout, ArmorUpgrade aArmorUpgrade) {
		super(aMessageDelivery, aArmorUpgrade.getName());
		upgrades = aLoadout.getUpgrades();
		loadout = aLoadout;
		oldValue = upgrades.getArmor();
		newValue = aArmorUpgrade;
	}

	@Override
	protected void apply() {
		set(newValue);
	}

	@Override
	protected void undo() {
		set(oldValue);
	}

	protected void set(ArmorUpgrade aValue) {
		if (aValue != upgrades.getArmor()) {
			ArmorUpgrade old = upgrades.getArmor();
			upgrades.setArmor(aValue);

			try {
				verifyLoadoutInvariant(loadout);
			} catch (Exception e) {
				upgrades.setArmor(old);
				throw new IllegalArgumentException("Couldn't change armour type: ", e);
			}

			if (messageDelivery != null)
				messageDelivery.post(new UpgradesMessage(ChangeMsg.ARMOR, upgrades));
		}
	}
}
