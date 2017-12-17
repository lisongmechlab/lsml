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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.DecodingException;
import org.lisoft.lsml.util.EncodingException;

/**
 * This class handles conversions of {@link LoadoutStandard}s to and from Base64 strings. It will correctly determine
 * which format the string is in and choose the right combination of decoders.
 *
 * @author Li Song
 */
@Singleton
public class Base64LoadoutCoder {
    private static final String LSML_PROTOCOL = "lsml://";
    private static final String LSML_TRAMPOLINE = "http://t.li-soft.org/?l=";
    private final transient LoadoutCoderV2 coderV2;
    private final transient LoadoutCoderV3 coderV3;
    private final transient LoadoutCoderV4 coderV4;
    private final transient LoadoutCoder preferredEncoder;
    private final transient Encoder base64Encoder;
    private final transient Decoder base64Decoder;

    @Inject
    public Base64LoadoutCoder(Encoder aBase64Encoder, Decoder aBase64Decoder, LoadoutCoderV2 aCoderV2,
            LoadoutCoderV3 aCoderV3, LoadoutCoderV4 aCoderV4) {
        base64Encoder = aBase64Encoder;
        base64Decoder = aBase64Decoder;
        coderV2 = aCoderV2;
        coderV3 = aCoderV3;
        coderV4 = aCoderV4;
        preferredEncoder = coderV4;
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
            final String data = base64Encoder.encodeToString(preferredEncoder.encode(aLoadout));
            return LSML_TRAMPOLINE + URLEncoder.encode(data, "UTF-8");
        }
        catch (final Exception e) {
            // This is a programmer error, the loadout code produced shall
            // always be base64, 7-bit ASCII.
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
            return LSML_PROTOCOL + base64Encoder.encodeToString(preferredEncoder.encode(aLoadout));
        }
        catch (final EncodingException e) {
            // This is a programmer error, the loadout code produced shall
            // always be base64, 7-bit ASCII.
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

        final byte[] bitStream = base64Decoder.decode(url);

        if (coderV2.canDecode(bitStream)) {
            return coderV2.decode(bitStream);
        }
        else if (coderV3.canDecode(bitStream)) {
            return coderV3.decode(bitStream);
        }
        else if (coderV4.canDecode(bitStream)) {
            return coderV4.decode(bitStream);
        }
        else {
            throw new DecodingException("No suitable decoder found to decode [" + aUrl + "] with!");
        }
    }
}
