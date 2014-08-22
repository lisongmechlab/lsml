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
package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link OpRename}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class OpRenameTest{

   @Mock
   private MessageXBar xBar;

   /**
    * We can rename {@link LoadoutStandard}s.
    */
   @Test
   public void testApply(){
      // Setup
      LoadoutStandard loadout = new LoadoutStandard((ChassisStandard)ChassisDB.lookup("HBK-4J"));
      assertEquals("HBK-4J", loadout.getName());

      // Execute
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new OpRename(loadout, xBar, "Test"));

      // Verify
      assertEquals("Test", loadout.getName());
      assertEquals("Test (HBK-4J)", loadout.toString());
      verify(xBar).post(new LoadoutMessage(loadout, LoadoutMessage.Type.RENAME));
   }

   /**
    * A <code>null</code> xbar doesn't cause an error.
    */
   @Test
   public void testApply_nullXbar(){
      // Setup
      LoadoutStandard loadout = new LoadoutStandard((ChassisStandard)ChassisDB.lookup("HBK-4J"));
      assertEquals("HBK-4J", loadout.getName());

      // Execute
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new OpRename(loadout, null, "Test"));

      // Verify
      assertEquals("Test", loadout.getName());
      assertEquals("Test (HBK-4J)", loadout.toString());
   }

}
