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

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.lisoft.lsml.util.DecodingException;

/**
 * The new export/import API uses base64 encoding which is actually not even close to RFC 4648, as expected by PGI. For
 * this reason we have to create a new encoder/decoder class
 *
 * @author Li Song
 */
public class BasePGICoder {
    final private static char encoderTable[] = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';',
            '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '|', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o' };
    final private static Map<Character, Integer> decoderTable;
    static {
        final Map<Character, Integer> decoder = new HashMap<>(encoderTable.length * 2);
        for (int i = 0; i < encoderTable.length; i++) {
            decoder.put(encoderTable[i], i);
        }
        decoderTable = Collections.unmodifiableMap(decoder);
    }

    public void append(int aValue, StringBuilder aStringBuilder, int chars) {
        int remainder = aValue;
        for (int i = 0; i < chars; i++) {
            aStringBuilder.append(encoderTable[remainder & 0x3F]);
            remainder >>= 6;
        }
    }

    public int parse(Reader aReader, int maxChars) throws DecodingException, IOException {
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