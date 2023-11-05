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

import static org.junit.Assert.*;
import static org.lisoft.lsml.util.TestHelpers.parse;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.ChassisDB;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.mwo_data.mechs.Chassis;
import org.lisoft.mwo_data.mechs.ChassisClass;

public class MWOCoderTest {
  static final String EMPTY_LCT_1VP = "AY192000p00q00r00s00t00u00v00w000000";
  private final BasePGICoder baseCoder = new BasePGICoder();
  private final ErrorReporter errorReporter = mock(ErrorReporter.class);
  private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();
  private final MWOCoder cut = new MWOCoder(baseCoder, loadoutFactory, errorReporter);

  @Test
  public void testCanDecodeLegitLoadouts() {
    assertTrue(cut.canDecode(EMPTY_LCT_1VP));
    assertTrue(cut.canDecode("A?502:P0|Xb|Y?|Y?pF0|i^|Y?qF0|i^|Y?rH0sH0|]?tP0uP0vB0w<0:0:0"));
    assertTrue(
        cut.canDecode(
            "AX4D<2X0pT0TM7|lB|TR|TRqT0RM7|l<2rD0QM7|lBsD0UM7|hBtT0VM7uT0WM7v@0PM7w<08080"));
  }

  @Test
  public void testCanDecodeTooShort() {
    assertFalse(cut.canDecode(EMPTY_LCT_1VP.substring(0, EMPTY_LCT_1VP.length() - 1)));
  }

  @Test
  public void testCanDecodeWrongMagick() {
    assertFalse(cut.canDecode("B?502:P0|Xb|Y?|Y?pF0|i^|Y?qF0|i^|Y?rH0sH0|]?tP0uP0vB0w<0:0:0"));
  }

  /**
   * The coder shall be able to decode all stock 'Mechs.
   *
   * @throws Exception
   */
  @Test
  public void testEncodeAllStock() throws Exception {
    final List<Chassis> chassii = new ArrayList<>(ChassisDB.lookup(ChassisClass.LIGHT));
    chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
    chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
    chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));

    for (final Chassis chassis : chassii) {
      Loadout expected;
      try {
        expected = loadoutFactory.produceStock(chassis);
      } catch (final Throwable e) {
        // Ignore loadouts that cannot be loaded due to errors in data files.
        continue;
      }
      final String result = cut.encode(expected);
      final Loadout actual = cut.decode(result);

      // Name is not encoded
      actual.setName(expected.getName());

      // Verify
      assertEquals(expected, actual);
    }
  }
}
