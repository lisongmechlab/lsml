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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.OperationStack;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link OpAddToGarage}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class OpAddToGarageTest {
    @Mock
    private MechGarage     garage;
    private OperationStack opStack = new OperationStack(0);

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
