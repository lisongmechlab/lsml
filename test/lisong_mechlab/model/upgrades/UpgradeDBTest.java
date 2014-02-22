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
package lisong_mechlab.model.upgrades;

import static org.junit.Assert.assertSame;

import org.junit.Test;

public class UpgradeDBTest{

   @Test(expected = IllegalArgumentException.class)
   public void testLookup_BadId(){
      UpgradeDB.lookup(0);
   }

   @Test
   public void testLookup_alternativeIdLookup(){
      assertSame(UpgradeDB.lookup(2810), UpgradeDB.lookup(2800)); // Standard Armor
      assertSame(UpgradeDB.lookup(2811), UpgradeDB.lookup(2801)); // Ferro-Fibrous Armor

      assertSame(UpgradeDB.lookup(3003), UpgradeDB.lookup(3000)); // SHS
      assertSame(UpgradeDB.lookup(3002), UpgradeDB.lookup(3001)); // DHS

      assertSame(UpgradeDB.lookup(3050), UpgradeDB.lookup(9001)); // Artemis
   }
}
