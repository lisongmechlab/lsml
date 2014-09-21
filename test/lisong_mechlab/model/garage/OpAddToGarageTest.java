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
package lisong_mechlab.model.garage;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.util.OperationStack;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link OpAddToGarage}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class OpAddToGarageTest {
	@Mock
	private MechGarage		garage;
	private OperationStack	opStack	= new OperationStack(0);

	/**
	 * Adding the same {@link LoadoutStandard} twice is an error and shall throw an {@link IllegalArgumentException}.
	 * 
	 * @throws Exception
	 *             Shouldn't be thrown.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testAddLoadoutTwice() throws Exception {
		// Setup
		LoadoutStandard loadout = Mockito.mock(LoadoutStandard.class);
		List<LoadoutBase<?>> loadouts = new ArrayList<>();
		loadouts.add(loadout);
		Mockito.when(garage.getMechs()).thenReturn(loadouts);

		opStack.pushAndApply(new OpAddToGarage(garage, loadout));
	}
}
