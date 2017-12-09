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
package org.lisoft.lsml.command;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NoSuchItemException;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;

/**
 * Test suite for {@link CmdSetArmourType}.
 * 
 * @author Emily Björk
 */
public class CmdSetArmourTypeTest {
	private LoadoutFactory lf = new DefaultLoadoutFactory();
	private MessageDelivery msgs = mock(MessageDelivery.class);

	@Test(expected = EquipException.class)
	public void testStealthOnNonECM() throws NoSuchItemException, EquipException {
		LoadoutStandard l = (LoadoutStandard) lf.produceEmpty(ChassisDB.lookup("CTF-3D"));
		ArmourUpgrade stealth = (ArmourUpgrade) UpgradeDB.lookup(UpgradeDB.STEALTH_ARMOUR_ID);

		new CmdSetArmourType(msgs, l, stealth).apply();
	}

	@Test
	public void testStealthOnECM() throws NoSuchItemException, EquipException {
		LoadoutStandard l = (LoadoutStandard) lf.produceEmpty(ChassisDB.lookup("SDR-5D"));
		ArmourUpgrade stealth = (ArmourUpgrade) UpgradeDB.lookup(UpgradeDB.STEALTH_ARMOUR_ID);

		CmdSetArmourType cmd = new CmdSetArmourType(msgs, l, stealth);
		cmd.apply();
		
		assertEquals(6, l.getComponent(Location.RightArm).getSlotsFree());
		assertEquals(6, l.getComponent(Location.LeftArm).getSlotsFree());
		
		assertEquals(0, l.getComponent(Location.RightLeg).getSlotsFree());
        assertEquals(0, l.getComponent(Location.LeftLeg).getSlotsFree());
        
        assertEquals(10, l.getComponent(Location.RightTorso).getSlotsFree());
        assertEquals(10, l.getComponent(Location.LeftTorso).getSlotsFree());
	}
}
