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
package org.lisoft.lsml.model.loadout.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.lisoft.lsml.command.CmdSetName;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ChassisDB;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.export.LoadoutCoderV2;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.Base64;
import org.lisoft.lsml.util.CommandStack;

/**
 * A test suite for {@link LoadoutCoderV2}.
 * 
 * @author Li Song
 */
public class LoadoutCoderV2Test {
    private LoadoutCoderV2 cut = new LoadoutCoderV2();

    /**
     * The coder shall be able to decode all stock mechs.
     * 
     * @throws Exception
     */
    @Test
    public void testDecodeAllStock() throws Exception {
        try (InputStream is = LoadoutCoderV2.class.getResourceAsStream("/resources/lsmlv2stock.txt");
                Scanner sc = new Scanner(is);) {

            Base64 base64 = new Base64();

            // [JENNER JR7-D(F)]=lsml://rQAD5AgQCAwOFAYQCAwIuipmzMO3aIExIyk9jt2DMA==
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                Pattern pat = Pattern.compile("\\[([^\\]]*)\\]\\s*=\\s*lsml://(\\S*).*");
                Matcher m = pat.matcher(line);
                m.matches();
                ChassisBase chassi = ChassisDB.lookup(m.group(1));
                String lsml = m.group(2);
                LoadoutBase<?> reference = DefaultLoadoutFactory.instance.produceStock(chassi);
                LoadoutStandard decoded = cut.decode(base64.decode(lsml.toCharArray()));

                // Name is not encoded
                CommandStack stack = new CommandStack(0);
                stack.pushAndApply(new CmdSetName(decoded, null, reference.getName()));

                // Verify
                assertEquals(reference, decoded);
            }
        }
    }

    /**
     * Even if heat sinks are encoded before the engine for CT, the heat sinks shall properly appear as engine heat
     * sinks.
     * 
     * @throws Exception
     */
    @Test
    public void testDecodeHeatsinksBeforeEngine() throws Exception {
        Base64 base64 = new Base64();

        LoadoutStandard l = cut.decode(base64.decode("rR4AEURGDjESaBRGDjFEvqCEjP34S+noutuWC1ooocl776JfSNH8KQ=="
                .toCharArray()));

        assertTrue(l.getFreeMass() < 0.005);
        assertEquals(3, l.getComponent(Location.CenterTorso).getEngineHeatsinks());
    }
}
