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
package org.lisoft.lsml.model.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64.Decoder;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.util.DecodingException;
import org.lisoft.lsml.view_fx.ErrorReporter;

/**
 * Test suite for {@link LoadoutCoderV3}.
 *
 * @author Emily Björk
 */
@SuppressWarnings("javadoc")
public class LoadoutCoderV3Test {
    private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();
    private final ErrorReporter errorReporter = mock(ErrorReporter.class);
    private final LoadoutCoderV3 cut = new LoadoutCoderV3(errorReporter, loadoutFactory);

    // TODO test error reporting to the callback!

    /**
     * The coder shall be able to decode all stock mechs.
     *
     * @throws Exception
     */
    @Test
    public void testDecodeAllStock() throws Exception {
        try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("lsmlv3stock.txt");
                Scanner sc = new Scanner(is);) {
            final Decoder base64 = java.util.Base64.getDecoder();

            // [JENNER JR7-D(F)]=lsml://rQAD5AgQCAwOFAYQCAwIuipmzMO3aIExIyk9jt2DMA==
            while (sc.hasNextLine()) {
                final String line = sc.nextLine();
                final Pattern pat = Pattern.compile("\\[([^\\]]*)\\]\\s*=\\s*lsml://(\\S*).*");
                final Matcher m = pat.matcher(line);
                m.matches();
                final Chassis chassis = ChassisDB.lookup(m.group(1));
                final String lsml = m.group(2);

                final Loadout reference = loadoutFactory.produceStock(chassis);

                final Loadout decoded = cut.decode(base64.decode(lsml));

                // Name is not encoded
                decoded.setName(reference.getName());

                // Verify
                assertEquals(reference, decoded);
            }
        }
    }

    /**
     * Even if heat sinks are encoded before the engine for CT, the heat sinks shall properly appear as engine heat
     * sinks.
     *
     * @throws DecodingException
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
     * The coder shall be able to decode all stock mechs.
     *
     * @throws Exception
     */
    @Test
    public void testEncodeAllStock() throws Exception {
        final List<Chassis> chassii = new ArrayList<>(ChassisDB.lookup(ChassisClass.LIGHT));
        chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));

        for (final Chassis chassis : chassii) {

            Loadout loadout;
            try {
                loadout = loadoutFactory.produceStock(chassis);
            }
            catch (final Throwable e) {
                // Ignore loadouts that cannot be loaded due to errors in data files.
                continue;
            }
            final byte[] result = cut.encode(loadout);
            final Loadout decoded = cut.decode(result);

            // Name is not encoded
            decoded.setName(loadout.getName());

            // Verify
            assertEquals(loadout, decoded);
        }
    }

    /**
     * Even if heat sinks are encoded before the engine for CT, the heat sinks shall properly appear as engine heat
     * sinks.
     *
     * @throws DecodingException
     */
    @Test
    public void testIssue481() throws DecodingException {
        final Decoder base64 = java.util.Base64.getDecoder();
        final Loadout l = cut.decode(base64.decode("rgEoHCQILBIsDCQILBwD6yzxWKqd5EX4qp3yndbTw4jSVTvdO/Yl"));

        assertTrue(l.getMass() > 44.8);
    }
}
