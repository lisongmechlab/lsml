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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SmurfyImportExportTest {

    private static final int KEY_CHARS = 40;

    @Test
    public void testIsValidApiKey() {
        final String chars = "0123456789abcdefABCDEF";
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < KEY_CHARS; ++i) {
            sb.append(chars.charAt(i % chars.length()));
        }
        assertTrue(SmurfyImportExport.isValidApiKey(sb.toString()));
    }

    /**
     * API key must only contain hexadecimal numbers.
     */
    @Test
    public void testIsValidApiKeyInvalidChars() {
        final String chars = "012345x6789abcdqefABCDEF";
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < KEY_CHARS; ++i) {
            sb.append(chars.charAt(i % chars.length()));
        }
        assertFalse(SmurfyImportExport.isValidApiKey(sb.toString()));
    }

    /**
     * API key must be 40 chars long
     */
    @Test
    public void testIsValidApiKeyTooLong() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < KEY_CHARS + 1; ++i) {
            sb.append('a' + i % 3);
        }
        assertFalse(SmurfyImportExport.isValidApiKey(sb.toString()));
    }

    /**
     * API key must be 40 chars long
     */
    @Test
    public void testIsValidApiKeyTooShort() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < KEY_CHARS - 1; ++i) {
            sb.append('a' + i % 3);
        }
        assertFalse(SmurfyImportExport.isValidApiKey(sb.toString()));
    }
}
