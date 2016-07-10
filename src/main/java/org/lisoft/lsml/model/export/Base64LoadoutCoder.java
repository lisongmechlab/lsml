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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Locale;

import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutBuilder.ErrorReportingCallback;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.Base64;
import org.lisoft.lsml.util.DecodingException;
import org.lisoft.lsml.util.EncodingException;

/**
 * This class handles conversions of {@link LoadoutStandard}s to and from Base64 strings. It will correctly determine
 * which format the string is in and choose the right combination of decoders.
 *
 * @author Emily Björk
 */
public class Base64LoadoutCoder {
    private static final String LSML_PROTOCOL = "lsml://";
    private static final String LSML_TRAMPOLINE = "http://t.li-soft.org/?l=";
    private final transient LoadoutCoderV1 coderV1;
    private final transient LoadoutCoderV2 coderV2;
    private final transient LoadoutCoderV3 coderV3;
    private final transient LoadoutCoder preferredEncoder;
    private final transient Base64 base64;

    public Base64LoadoutCoder(Base64 aBase64, LoadoutCoderV1 aCoderV1, LoadoutCoderV2 aCoderV2,
            LoadoutCoderV3 aCoderV3) {
        base64 = aBase64;
        coderV1 = aCoderV1;
        coderV2 = aCoderV2;
        coderV3 = aCoderV3;
        preferredEncoder = coderV3;
    }

    /**
     * Provided for convenience for unit tests, don't use in new code.
     *
     * @param aErrorReportingCallback
     *            An {@link ErrorReportingCallback} to use.
     */
    @Deprecated
    public Base64LoadoutCoder(ErrorReportingCallback aErrorReportingCallback) {
        this(new Base64(), new LoadoutCoderV1(), new LoadoutCoderV2(), new LoadoutCoderV3(aErrorReportingCallback));
    }

    /**
     * Will encode a given {@link Loadout} into a HTTP trampoline LSML protocol {@link String}.
     *
     * @param aLoadout
     *            The {@link Loadout} to encode.
     * @return A HTTP URI as a {@link String} with a Base64 encoding of the {@link LoadoutStandard}.
     */
    public String encodeHTTPTrampoline(Loadout aLoadout) {
        try {
            return LSML_TRAMPOLINE
                    + URLEncoder.encode(String.valueOf(base64.encode(preferredEncoder.encode(aLoadout))), "UTF-8");
        }
        catch (final Exception e) {
            // This is a programmer error, the loadout code produced shall always be base64, 7-bit ASCII.
            throw new RuntimeException("Unable to encode loadout!", e);
        }
    }

    /**
     * Will encode a given {@link Loadout} into a LSML protocol {@link String}.
     *
     * @param aLoadout
     *            The {@link Loadout} to encode.
     * @return A {@link String} with a Base64 encoding of the {@link LoadoutStandard}.
     */
    public String encodeLSML(Loadout aLoadout) {
        try {
            return LSML_PROTOCOL + String.valueOf(base64.encode(preferredEncoder.encode(aLoadout)));
        }
        catch (final EncodingException e) {
            // This is a programmer error, the loadout code produced shall always be base64, 7-bit ASCII.
            throw new RuntimeException("Unable to encode loadout!", e);
        }
    }

    /**
     * Parses a Base64 {@link String} into a {@link LoadoutStandard}.
     *
     * @param aUrl
     *            The string to parse.
     * @return A new {@link LoadoutStandard} object.
     * @throws Exception
     *             if the argument was malformed.
     */
    public Loadout parse(String aUrl) throws Exception {
        String url = aUrl.trim();
        final String urlLowerCase = url.toLowerCase(Locale.ENGLISH);
        if (urlLowerCase.startsWith(LSML_PROTOCOL)) {
            url = url.substring(LSML_PROTOCOL.length());
        }
        else if (urlLowerCase.startsWith(LSML_TRAMPOLINE)) {
            url = URLDecoder.decode(url.substring(LSML_TRAMPOLINE.length()), "UTF-8");
        }

        // Remove trailing slashes
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        final byte[] bitStream = base64.decode(url.toCharArray());

        if (coderV1.canDecode(bitStream)) {
            return coderV1.decode(bitStream);
        }
        else if (coderV2.canDecode(bitStream)) {
            return coderV2.decode(bitStream);
        }
        else if (coderV3.canDecode(bitStream)) {
            return coderV3.decode(bitStream);
        }
        else {
            throw new DecodingException("No suitable decoder found to decode [" + aUrl + "] with!");
        }
    }
}
