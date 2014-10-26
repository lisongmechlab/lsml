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

import static org.mockito.Mockito.verify;
import lisong_mechlab.util.message.MessageXBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link OpRename}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class OpRenameTest {

    @Mock
    private MessageXBar xBar;

    /**
     * We can rename {@link LoadoutStandard}s.
     */
    @Test
    public void testApply() {
        LoadoutBase<?> loadout = Mockito.mock(LoadoutBase.class);

        // Execute
        new OpRename(loadout, xBar, "Test").apply();

        // Verify
        verify(loadout).rename("Test");
        verify(xBar).post(new LoadoutMessage(loadout, LoadoutMessage.Type.RENAME));
    }

    /**
     * A <code>null</code> xbar doesn't cause an error.
     */
    @Test
    public void testApply_nullXbar() {
        LoadoutBase<?> loadout = Mockito.mock(LoadoutBase.class);

        // Execute
        new OpRename(loadout, xBar, "Test").apply();

        // Verify
        verify(loadout).rename("Test");
    }

}
