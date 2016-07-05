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
package org.lisoft.lsml.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * A test suite for {@link Huffman2}. In addition to the tests from {@link Huffman1Test}, {@link Huffman2} shall have
 * well defined code words for symbols.
 *
 * @author Li Song
 */
@SuppressWarnings("javadoc")
public class Huffman2Test extends Huffman1Test {

    /**
     * Shall produce the correct output for the example given
     * <a href="https://github.com/lisongmechlab/lsml/issues/317"> here</a>.
     * <p>
     * <code>e = 2, b = 4, a = 1, c = 2, d = 1, STOP = 0</code> shall produce:
     * <code>c = 00, e = 01, b = 11, d = 100, a = 1011, STOP = 1010</code>
     *
     * @throws EncodingException
     */
    @Test
    public void testGithubExample() throws EncodingException {

        final Map<Character, Integer> aSymbolFrequencyTable = new HashMap<>();

        aSymbolFrequencyTable.put('e', 2);
        aSymbolFrequencyTable.put('b', 4);
        aSymbolFrequencyTable.put('a', 1);
        aSymbolFrequencyTable.put('c', 2);
        aSymbolFrequencyTable.put('d', 1);

        final Huffman2<Character> cut = new Huffman2<>(aSymbolFrequencyTable, null);

        final byte[] e = cut.encode(Arrays.asList(Character.valueOf('e')));
        final byte[] b = cut.encode(Arrays.asList(Character.valueOf('b')));
        final byte[] a = cut.encode(Arrays.asList(Character.valueOf('a')));
        final byte[] c = cut.encode(Arrays.asList(Character.valueOf('c')));
        final byte[] d = cut.encode(Arrays.asList(Character.valueOf('d')));

        assertEquals(1, e.length);
        assertEquals((byte) 0x68, e[0] & ~0x3); // 0b0110 10xx = 0x68 (with xx=00)

        assertEquals(1, b.length);
        assertEquals((byte) 0xe8, b[0] & ~0x3); // 0b1110 10xx = 0xe8 (with xx=00)

        assertEquals(1, a.length);
        assertEquals((byte) 0xba, a[0]); // 0b1011 1010 = 0xba

        assertEquals(1, c.length);
        assertEquals((byte) 0x28, c[0] & ~0x3); // 0b0010 10xx = 0x28 (with xx=00)

        assertEquals(1, d.length);
        assertEquals((byte) 0x94, d[0] & ~0x1); // 0b1001 010x = 0x94 (with x=0)
    }
}
