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
package org.lisoft.lsml.model.loadout.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.lisoft.lsml.command.CmdSetName;
import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.export.LoadoutCoderV3;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.Base64;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.util.DecodingException;

/**
 * Test suite for {@link LoadoutCoderV3}.
 * 
 * @author Emily Björk
 */
public class LoadoutCoderV3Test {

    private LoadoutCoderV3 cut = new LoadoutCoderV3();

    /**
     * The coder shall be able to decode all stock mechs.
     * 
     * @throws Exception
     */
    @Test
    public void testEncodeAllStock() throws Exception {
        List<ChassisBase> chassii = new ArrayList<>(ChassisDB.lookup(ChassisClass.LIGHT));
        chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
        chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
        chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));

        CommandStack stack = new CommandStack(0);

        for (ChassisBase chassis : chassii) {
            LoadoutBase<?> loadout = DefaultLoadoutFactory.instance.produceStock(chassis);
            byte[] result = cut.encode(loadout);
            LoadoutBase<?> decoded = cut.decode(result);

            // Name is not encoded
            stack.pushAndApply(new CmdSetName(decoded, null, loadout.getName()));

            // Verify
            assertEquals(loadout, decoded);
        }
    }

    /**
     * The coder shall be able to decode all stock mechs.
     * 
     * @throws Exception
     */
    @Test
    public void testDecodeAllStock() throws Exception {
        try (InputStream is = LoadoutCoderV3.class.getResourceAsStream("/resources/lsmlv3stock.txt");
                Scanner sc = new Scanner(is);) {
            Base64 base64 = new Base64();

            CommandStack stack = new CommandStack(0);

            // [JENNER JR7-D(F)]=lsml://rQAD5AgQCAwOFAYQCAwIuipmzMO3aIExIyk9jt2DMA==
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                Pattern pat = Pattern.compile("\\[([^\\]]*)\\]\\s*=\\s*lsml://(\\S*).*");
                Matcher m = pat.matcher(line);
                m.matches();
                ChassisBase chassis = ChassisDB.lookup(m.group(1));
                String lsml = m.group(2);

                LoadoutBase<?> reference = DefaultLoadoutFactory.instance.produceStock(chassis);

                LoadoutBase<?> decoded = cut.decode(base64.decode(lsml.toCharArray()));

                // Name is not encoded
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
     * @throws DecodingException
     */
    @Test
    public void testDecodeHeatsinksBeforeEngine() throws DecodingException {
        Base64 base64 = new Base64();
        LoadoutBase<?> l = cut.decode(base64
                .decode("rgARREYOMRJoFEYOMUTne6/upzrLydT6fsxT6z64t7j1VaIokEgkCbPp9PlsxT65OQ5Zsg==".toCharArray()));

        assertTrue(l.getFreeMass() < 0.005);
        assertEquals(3, l.getComponent(Location.CenterTorso).getEngineHeatSinks());
    }
}
