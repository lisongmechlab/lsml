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
package org.lisoft.lsml.model.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Base64.Decoder;

import org.junit.Test;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.util.DecodingException;

/**
 * Test suite for {@link LoadoutCoderV3}.
 *
 * @author Li Song
 */
@SuppressWarnings("javadoc")
public class LoadoutCoderV3Test {
    private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();
    private final ErrorReporter errorReporter = mock(ErrorReporter.class);
    private final LoadoutCoderV3 cut = new LoadoutCoderV3(errorReporter, loadoutFactory);

    // TODO test error reporting to the callback!

    // TODO test handling of items that don't exist!

    /**
     * Even if heat sinks are encoded before the engine for CT, the heat sinks shall properly appear as engine heat
     * sinks.
     */
    @Test
    public void testDecodeHeatsinksBeforeEngine() throws DecodingException {
        final Decoder base64 = java.util.Base64.getDecoder();
        final Loadout l = cut
                .decode(base64.decode("rgARREYOMRJoFEYOMUTne6/upzrLydT6fsxT6z64t7j1VaIokEgkCbPp9PlsxT65OQ5Zsg=="));

        assertTrue(l.getFreeMass() < 0.005);
        assertEquals(3, l.getComponent(Location.CenterTorso).getEngineHeatSinks());
    }

    /**
     * Test that we can decode a loadout that contains old pilot modules after they were removed from the game.
     */
    @Test
    public void testDecodeWithPilotModule() throws Exception {
        final Decoder base64 = java.util.Base64.getDecoder();
        cut.decode(base64.decode("rgCzAAAAAAAAAAAAAAAA6zHWZdZdZdZdZdZdSpVd3KlSq66untdjKlSq62uoy6y6y6y6y6y6lSr+2f6M"));
        verifyZeroInteractions(errorReporter);
    }

    /**
     * Even if heat sinks are encoded before the engine for CT, the heat sinks shall properly appear as engine heat
     * sinks.
     */
    @Test
    public void testIssue481() throws DecodingException {
        final Decoder base64 = java.util.Base64.getDecoder();
        final Loadout l = cut.decode(base64.decode("rgEoHCQILBIsDCQILBwD6yzxWKqd5EX4qp3yndbTw4jSVTvdO/Yl"));

        assertTrue(l.getMass() > 44.8);
    }
}
