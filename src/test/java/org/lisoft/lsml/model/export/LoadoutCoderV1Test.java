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

import java.io.InputStream;
import java.util.Base64.Decoder;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests the {@link LoadoutCoderV1}
 *
 * @author Emily Björk
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class LoadoutCoderV1Test {
    private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();
    private final LoadoutCoderV1 cut = new LoadoutCoderV1(loadoutFactory);

    /**
     * The coder shall be able to decode all stock mechs.
     *
     * @throws Exception
     */
    @Test
    public void testAllStock() throws Exception {
        try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("lsmlv1stock.txt");
                Scanner sc = new Scanner(is);) {
            final Decoder base64 = java.util.Base64.getDecoder();

            // [JENNER JR7-D(F)]=lsml://rQAD5AgQCAwOFAYQCAwIuipmzMO3aIExIyk9jt2DMA==
            while (sc.hasNextLine()) {
                final String line = sc.nextLine();
                final Pattern pat = Pattern.compile("\\[([^\\]]*)\\]\\s*=\\s*lsml://(\\S*).*");
                final Matcher m = pat.matcher(line);
                m.matches();
                final Chassis chassi = ChassisDB.lookup(m.group(1));
                final String lsml = m.group(2);
                final Loadout reference = loadoutFactory.produceStock(chassi);
                final LoadoutStandard decoded = cut.decode(base64.decode(lsml));

                // Name is not encoded
                decoded.setName(reference.getName());

                // Verify
                assertEquals(reference, decoded);
            }
        }
    }

    /**
     * The coder shall handle the artemis change.
     *
     * @throws Exception
     */
    @Test
    public void testArtemis() throws Exception {

        final Decoder base64 = java.util.Base64.getDecoder();
        final String line = "[CENTURION CN9-D]=lsml://rJAAHSAaDCASJA4aDCAg9D7+/hCK32zHWw==";
        final Pattern pat = Pattern.compile("\\[([^\\]]*)\\]\\s*=\\s*lsml://(\\S*).*");
        final Matcher m = pat.matcher(line);
        m.matches();
        final Chassis chassi = ChassisDB.lookup(m.group(1));
        final String lsml = m.group(2);
        final Loadout reference = loadoutFactory.produceStock(chassi);

        // Execute
        final LoadoutStandard decoded = cut.decode(base64.decode(lsml));

        // Name is not encoded
        decoded.setName(reference.getName());

        // Verify
        assertEquals(reference, decoded);
    }

    /**
     * Even if heat sinks are encoded before the engine for CT, the heat sinks shall properly appear as engine heat
     * sinks.
     *
     * @throws Exception
     *
     */
    @Test
    public void testDecodeHeatsinksBeforeEngine() throws Exception {
        final Decoder base64 = java.util.Base64.getDecoder();
        final LoadoutStandard l = cut.decode(base64.decode("rN8AEURGDjESaBRGDjFEKtpaJ84vF9ZjGog+lp6en848eJk+cUr6qxY="));

        assertTrue(l.getFreeMass() < 0.005);
        assertEquals(3, l.getComponent(Location.CenterTorso).getEngineHeatSinks());
    }
}
