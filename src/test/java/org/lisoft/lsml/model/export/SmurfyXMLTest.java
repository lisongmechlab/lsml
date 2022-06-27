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

import org.junit.Test;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.TestHelpers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test suite for {@link SmurfyXML}.
 *
 * @author Li Song
 */
public class SmurfyXMLTest {

    @Test
    public final void testToXmlClan() throws Exception {
        referenceTest("lsml://rgCwDhAIEBISCBAIEA4P6zHOZYy8rm2ZmaFXPVy9rmWaudrmGZmyxno2", "smurfy_kfxdstock.xml");
    }

    @Test
    public final void testToXmlIS() throws Exception {
        referenceTest("lsml://rgARREAUUhJeHEAUUkTne6/ep3rMhjZ5PGcsMNEXVOQww3HhhS2RYYbDGw==", "smurfy_as7ddcstock.xml");
    }

    private void referenceTest(String aLSMLLink, String aResource) throws Exception {
        final Loadout loadout = TestHelpers.parse(aLSMLLink);
        loadout.setName("stock");

        final String xml = SmurfyXML.toXml(loadout);
        final String[] lines = xml.split("\n");

        try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(aResource);
             InputStreamReader isr = new InputStreamReader(is); BufferedReader br = new BufferedReader(isr)) {

            for (final String line : lines) {
                final String expected = br.readLine();
                if (expected == null) {
                    fail("Unexpected end of file!");
                    return; // Make eclipse understand that this is the end of this function.
                }

                final String lineTrim = line.replaceAll("^\\s*", "").replaceAll("\\s*$", "");
                final String expectedTrim = expected.replaceAll("^\\s*", "").replaceAll("\\s*$", "");

                assertEquals(expectedTrim, lineTrim);
            }
        }
    }

}
