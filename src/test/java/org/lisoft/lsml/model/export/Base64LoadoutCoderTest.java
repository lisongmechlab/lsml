/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.model.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import org.junit.Test;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.util.DecodingException;
import org.lisoft.lsml.util.EncodingException;

/**
 * Test suite for {@link Base64LoadoutCoder}.
 *
 * @author Li Song
 */
public class Base64LoadoutCoderTest {
  private static final Charset UTF8 = StandardCharsets.UTF_8;
  private final Decoder base64Decoder = mock(Decoder.class);
  private final Encoder base64Encoder = mock(Encoder.class);
  private final LoadoutCoderV2 coderV2 = mock(LoadoutCoderV2.class);
  private final LoadoutCoderV3 coderV3 = mock(LoadoutCoderV3.class);
  private final LoadoutCoderV4 coderV4 = mock(LoadoutCoderV4.class);
  private final Base64LoadoutCoder cut =
      new Base64LoadoutCoder(base64Encoder, base64Decoder, coderV2, coderV3, coderV4);
  private final Loadout loadout = mock(Loadout.class);

  @Test
  public void testEncodeHTTPTrampoline() throws EncodingException {
    // Setup
    final byte[] encodedRaw = "data".getBytes(UTF8);
    final String encodedBase64 = "base/64=";
    when(coderV4.encode(loadout)).thenReturn(encodedRaw);
    when(base64Encoder.encodeToString(encodedRaw)).thenReturn(encodedBase64);

    // Execute
    final String ans = cut.encodeHTTPTrampoline(loadout);

    // Verify
    assertEquals("http://t.li-soft.org/?l=base%2F64%3D", ans);
  }

  @Test(expected = RuntimeException.class)
  public void testEncodeHTTPTrampolineEncodingError() throws EncodingException {
    // Setup
    when(coderV3.encode(loadout)).thenThrow(new EncodingException("Couldn't encode loadout!"));

    cut.encodeHTTPTrampoline(loadout);
  }

  @Test
  public void testEncodeLSML() throws EncodingException {
    // Setup
    final byte[] encodedRaw = "data".getBytes(UTF8);
    final String encodedBase64 = "base/64=";
    when(coderV4.encode(loadout)).thenReturn(encodedRaw);
    when(base64Encoder.encodeToString(encodedRaw)).thenReturn(encodedBase64);

    // Execute
    final String ans = cut.encodeLSML(loadout);

    // Verify
    assertEquals("lsml://" + encodedBase64, ans);
  }

  @Test(expected = RuntimeException.class)
  public void testEncodeLSMLEncodingError() throws EncodingException {
    // Setup
    when(coderV4.encode(loadout)).thenThrow(new EncodingException("Couldn't encode loadout!"));

    cut.encodeLSML(loadout);
  }

  @Test
  public void testParseHTTPV2() throws Exception {
    final String data = "base/64=";
    final byte[] bitStream = "rawdata".getBytes(UTF8);
    final String url = "http://t.li-soft.org/?l=" + URLEncoder.encode(data, "UTF-8");
    final LoadoutStandard loadoutStd = mock(LoadoutStandard.class);

    verifyParseV2(data, bitStream, url, loadoutStd);
  }

  @Test
  public void testParseHTTPV3() throws Exception {
    final String data = "base/64=";
    final byte[] bitStream = "rawdata".getBytes(UTF8);
    final String url = "http://t.li-soft.org/?l=" + URLEncoder.encode(data, "UTF-8");

    verifyParseV3(data, bitStream, url);
  }

  @Test
  public void testParseHTTPV4() throws Exception {
    final String data = "base/64=";
    final byte[] bitStream = "rawdata".getBytes(UTF8);
    final String url = "http://t.li-soft.org/?l=" + URLEncoder.encode(data, "UTF-8");

    verifyParseV4(data, bitStream, url);
  }

  @Test(expected = DecodingException.class)
  public void testParseLSMLRubbish() throws Exception {
    final String data = "base/64=";
    final byte[] bitStream = "rawdata".getBytes(UTF8);
    final String url = "lsml://" + data;
    when(base64Decoder.decode(data)).thenReturn(bitStream);
    // All decoders return false on "canDecode"

    cut.parse(url);
  }

  @Test
  public void testParseLSMLV2() throws Exception {
    final String data = "base/64=";
    final byte[] bitStream = "rawdata".getBytes(UTF8);
    final String url = "lsml://" + data;
    final LoadoutStandard loadoutStd = mock(LoadoutStandard.class);

    verifyParseV2(data, bitStream, url, loadoutStd);
  }

  @Test
  public void testParseLSMLV3() throws Exception {
    final String data = "base/64=";
    final byte[] bitStream = "rawdata".getBytes(UTF8);
    final String url = "lsml://" + data;

    verifyParseV3(data, bitStream, url);
  }

  @Test
  public void testParseLSMLV4() throws Exception {
    final String data = "base/64=";
    final byte[] bitStream = "rawdata".getBytes(UTF8);
    final String url = "lsml://" + data;

    verifyParseV4(data, bitStream, url);
  }

  @Test(expected = DecodingException.class)
  public void testParseRawRubbishInValidBase64() throws Exception {
    final String data = "12base/64=";
    final byte[] bitStream = "rawdata".getBytes(UTF8);
    when(base64Decoder.decode(data)).thenReturn(bitStream);
    // All decoders return false on "canDecode"

    cut.parse(data);
  }

  @Test(expected = DecodingException.class)
  public void testParseRawRubbishValidBase64() throws Exception {
    final String data = "1234base/64=";
    final byte[] bitStream = "rawdata".getBytes(UTF8);
    when(base64Decoder.decode(data)).thenReturn(bitStream);
    // All decoders return false on "canDecode"

    cut.parse(data);
  }

  /**
   * Passing in a string without a protocol identifier is assumed to be LSML format with protocol
   * identifier stripped. Test for V2 links.
   */
  @Test
  public void testParseRawV2() throws Exception {
    final String data = "base/64=";
    final byte[] bitStream = "rawdata".getBytes(UTF8);
    final String url = data;
    final LoadoutStandard loadoutStd = mock(LoadoutStandard.class);

    verifyParseV2(data, bitStream, url, loadoutStd);
  }

  /** Trailing slashes shall be stripped. */
  @Test
  public void testParseRawV2TrailingSlash() throws Exception {
    final String data = "base/64=";
    final byte[] bitStream = "rawdata".getBytes(UTF8);
    final String url = data + "//";
    final LoadoutStandard loadoutStd = mock(LoadoutStandard.class);
    verifyParseV2(data, bitStream, url, loadoutStd);
  }

  /**
   * Passing in a string without a protocol identifier is assumed to be LSML format with protocol
   * identifier stripped. Test for V3 links.
   */
  @Test
  public void testParseRawV3() throws Exception {
    final String data = "base/64=";
    final byte[] bitStream = "rawdata".getBytes(UTF8);
    final String url = data;

    verifyParseV3(data, bitStream, url);
  }

  @Test
  public void testParseRawV4() throws Exception {
    final String data = "base/64=";
    final byte[] bitStream = "rawdata".getBytes(UTF8);
    final String url = data;

    verifyParseV4(data, bitStream, url);
  }

  private void verifyParseV2(
      final String base64Data,
      final byte[] bitStream,
      final String url,
      final LoadoutStandard loadoutStd)
      throws Exception {
    when(base64Decoder.decode(base64Data)).thenReturn(bitStream);
    when(coderV2.canDecode(bitStream)).thenReturn(true);
    when(coderV2.decode(bitStream)).thenReturn(loadoutStd);

    final Loadout ans = cut.parse(url);

    assertSame(loadoutStd, ans);
  }

  private void verifyParseV3(final String base64Data, final byte[] bitStream, final String url)
      throws Exception {
    when(base64Decoder.decode(base64Data)).thenReturn(bitStream);
    when(coderV3.canDecode(bitStream)).thenReturn(true);
    when(coderV3.decode(bitStream)).thenReturn(loadout);

    final Loadout ans = cut.parse(url);

    assertSame(loadout, ans);
  }

  private void verifyParseV4(final String base64Data, final byte[] bitStream, final String url)
      throws Exception {
    when(base64Decoder.decode(base64Data)).thenReturn(bitStream);
    when(coderV4.canDecode(bitStream)).thenReturn(true);
    when(coderV4.decode(bitStream)).thenReturn(loadout);

    final Loadout ans = cut.parse(url);

    assertSame(loadout, ans);
  }
}
