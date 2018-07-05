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

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.lisoft.lsml.util.DecodingException;

public class BasePGICoderTest {

    @Test
    public void testEncodeDecodeUniqueAlphabet() throws DecodingException, IOException {
        final BasePGICoder cut = new BasePGICoder();

        for (int i = 0; i < 64; ++i) {
            final StringBuilder output = new StringBuilder();
            cut.append(i, output, 1);
            final StringReader input = new StringReader(output.toString());
            final int value = cut.parse(input, 1);
            assertEquals(i, value);
        }
    }

    @Test
    public void testEncodeDecode() throws DecodingException, IOException {
        final BasePGICoder cut = new BasePGICoder();

        final StringBuilder output = new StringBuilder();
        for (int i = 0; i < (64 * 64); ++i) {
            cut.append(i, output, 2);
        }

        final StringReader input = new StringReader(output.toString());
        for (int i = 0; i < (64 * 64); ++i) {
            final int value = cut.parse(input, 2);
            assertEquals(i, value);
        }
    }

    @Test
    public void testEncodeMWO() {
        final BasePGICoder cut = new BasePGICoder();
        final StringBuilder output = new StringBuilder();
        cut.append(30416, output, 3);
        assertEquals("@K7", output.toString());
    }
}
