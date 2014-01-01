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

package lisong_mechlab.model.environment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite for {@link EnvironmentDB}
 * 
 * @author Emily Björk
 */
public class EnvironmentDBTest{

   static EnvironmentDB cut = new EnvironmentDB();

   @BeforeClass
   public static void setupOnce() throws IOException{
      cut.initialize();
   }

   /**
    * {@link EnvironmentDB#lookup(String)} shall return an {@link Environment} with matching name if found in the DB.
    */
   @Test
   public void testLookup(){
      Environment caustic = cut.lookup("caustic valley");

      assertEquals(0.3, caustic.getHeat(), 0.0);
      assertEquals("CAUSTIC VALLEY", caustic.getName());
   }

   /**
    * {@link EnvironmentDB#lookup(String)} shall return null if the map was not found.
    */
   @Test
   public void testLookupNull(){
      assertNull(cut.lookup("Mumbo jumbo therma"));
   }

   /**
    * {@link EnvironmentDB#lookupAll()} shall return all maps in the game.
    */
   @Test
   public void testLookupAll(){
      List<Environment> environments = cut.lookupAll();

      assertEquals(13, environments.size()); // To date 13 known maps.
   }
}
