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
package lisong_mechlab.model.item;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * A test suite for {@link Ammunition}.
 * 
 * @author Li Song
 */
public class AmmunitionTest {

	@Test
	public void testHalfTonAmmo() {
		Ammunition cut = (Ammunition) ItemDB.lookup(2233);

		assertEquals(0.5, cut.getMass(), 0.0);
		assertEquals(1, cut.getNumCriticalSlots());
		assertEquals(10.0, cut.getHealth(), 0.0);
	}

}
