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

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.lisoft.lsml.util.DecodingException;

/**
 * The new export/import API uses base64 encoding which is actually not even close to RFC 4648, as expected by PGI. For
 * this reason we have to create a new encoder/decoder class
 *
 * @author Emily Björk
 */
public class BasePGICoder {
    final private static char encoderTable[] = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';',
            '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o' };
    final private static Map<Character, Integer> decoderTable;
    static {
        final Map<Character, Integer> decoder = new HashMap<>(encoderTable.length * 2);
        for (int i = 0; i < encoderTable.length; i++) {
            decoder.put(encoderTable[i], i);
        }
        decoderTable = Collections.unmodifiableMap(decoder);
    }

    @Inject
    public BasePGICoder() {
        // Nop
    }

    /**
     * Encodes the value provided using between <code>[minChars, maxChars]</code> characters.
     *
     * @param aValue
     *            The value to encode.
     * @param aStringBuilder
     *            A {@link StringBuilder} to append the encoded string to.
     * @param minChars
     *            The minimum number of characters to output.
     * @param maxChars
     *            The maximum number of characters to output.
     */
    public void append(int aValue, StringBuilder aStringBuilder, int minChars, int maxChars) {
        int remainder = aValue;
        int encoded = 0;
        for (int i = 0; i < minChars; i++) {
            aStringBuilder.append(encoderTable[remainder & 0x3F]);
            remainder >>= 6;
        encoded++;
        }

        while (remainder > 0 && encoded < maxChars) {
            aStringBuilder.append(encoderTable[remainder & 0x3F]);
            remainder >>= 6;
        encoded++;
        }
    }

    /**
     * Encodes the value provided using between <code>[minChars, maxChars]</code> characters.
     *
     * @param aValue
     *            The value to encode.
     * @param aStringBuilder
     *            A {@link StringBuilder} to append the encoded string to.
     * @param aNumChars
     *            A exact number of characters to write to the output.
     */
    public void append(int aValue, StringBuilder aStringBuilder, int aNumChars) {
        append(aValue, aStringBuilder, aNumChars, aNumChars);
    }

    /**
     * Parses up to <code>maxChars</code> or until a non parseable character is encountered. Then returns, leaving the
     * input stream pointing at the next unparsed character (may be the unparseable character).
     *
     * @param aReader
     *            A {@link Reader} to get characters from.
     * @param maxChars
     *            The maximal number of chars to read.
     * @return A integer with the decoded value (may be jibberish if <code>maxChars</code> it too big).
     * @throws IOException
     *             If EOS is encountered prematurely or another IO error occurs.
     */
    public int parseAvailable(Reader aReader, int maxChars) throws IOException {
        int value = 0;
        for (int i = 0; i < maxChars; i++) {
            aReader.mark(1);
            final char ch = (char) aReader.read();
            final Integer bits = decoderTable.get(ch);
            if (null == bits) {
                aReader.reset();
                break;
            }
            value |= bits.intValue() << (6 * i);
        }
        return value;
    }

    /**
     * Parses exactly <code>maxChars</code> characters and decodes them into an int.
     *
     * @param aReader
     *            The {@link Reader} to get characters from.
     * @param maxChars
     *            The maximal number of chars to read.
     * @return A integer with the decoded value (may be jibberish if <code>maxChars</code> it too big).
     * @throws DecodingException
     *             If a character that is not part of the alphabet is encountered.
     * @throws IOException
     *             If EOS is encountered prematurely or another IO error occurs.
     */
    public int parseExactly(Reader aReader, int maxChars) throws DecodingException, IOException {
        int value = 0;
        for (int i = 0; i < maxChars; i++) {
            final char ch = (char) aReader.read();
            final Integer bits = decoderTable.get(ch);
            if (null == bits) {
                throw new DecodingException("Unexpected character: " + ch);
            }
            value |= bits.intValue() << (6 * i);
        }
        return value;
    }
}