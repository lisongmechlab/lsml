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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64.Decoder;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.mwo_data.ChassisDB;
import org.lisoft.mwo_data.mechs.Chassis;
import org.lisoft.mwo_data.mechs.ChassisClass;

/**
 * Test suite for {@link LoadoutCoderV4}.
 *
 * @author Li Song
 */
public class LoadoutCoderV4Test {

  private final ErrorReporter errorReporter = mock(ErrorReporter.class);
  private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();
  private final LoadoutCoderV4 cut = new LoadoutCoderV4(errorReporter, loadoutFactory);

  /**
   * This test verifies that previously encoded loadouts can still be decoded. In other words that
   * the encoded strings are stable between versions. The stock loadouts that are compared against
   * can change upstream so we allow a certain number of comparison failures.
   */
  @Test
  public void testDecoderStability() throws Exception {
    int failures = 0;
    int notEqual = 0;
    try (InputStream is =
            ClassLoader.getSystemClassLoader().getResourceAsStream("lsmlv4stock.txt");
        Scanner sc = new Scanner(is, StandardCharsets.UTF_8)) {
      final Decoder base64 = java.util.Base64.getDecoder();

      // [JENNER JR7-D(F)]=lsml://rQAD5AgQCAwOFAYQCAwIuipmzMO3aIExIyk9jt2DMA==
      while (sc.hasNextLine()) {
        final String line = sc.nextLine();
        final Pattern pat = Pattern.compile("\\[([^\\]]*)\\]\\s*=\\s*lsml://(\\S*).*");
        final Matcher m = pat.matcher(line);
        m.matches();
        final Chassis chassis = ChassisDB.lookup(m.group(1));
        final String lsml = m.group(2);

        final Loadout reference = loadoutFactory.produceStock(chassis);
        final Loadout decoded;
        try {
          decoded = cut.decode(base64.decode(lsml));
        } catch (final Throwable t) {
          failures++;
          continue;
        }
        // Name is not encoded
        decoded.setName(reference.getName());

        // Verify
        if (!reference.equals(decoded)) {
          notEqual++;
        }
      }
    }
    assertTrue(failures <= 0);
    assertTrue(notEqual <= 10);
  }

  /** The coder shall be able to decode all stock 'Mechs. */
  @Test
  public void testEncodeAllStock() throws Exception {
    final List<Chassis> chassii = new ArrayList<>(ChassisDB.lookup(ChassisClass.LIGHT));
    chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
    chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
    chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));

    for (final Chassis chassis : chassii) {

      Loadout loadout;
      try {
        loadout = loadoutFactory.produceStock(chassis);
      } catch (final Throwable e) {
        // Ignore loadouts that cannot be loaded due to errors in data files.
        continue;
      }
      final byte[] result = cut.encode(loadout);
      final Loadout decoded = cut.decode(result);

      // Name is not encoded
      decoded.setName(loadout.getName());

      // Verify
      assertEquals(loadout, decoded);
    }
  }
}
