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
package lisong_mechlab.model.loadout.export;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiClass;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.UndoStack;
import lisong_mechlab.util.MessageXBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests the {@link LoadoutCoderV1}
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadoutCoderV1Test{

   @Mock
   private MessageXBar    xBar;
   @Mock
   private UndoStack      undoStack;
   @InjectMocks
   private LoadoutCoderV1 cut;

   /**
    * The coder shall be able to encode and decode all stock mechs.
    * 
    * @throws Exception
    */
   @Test
   public void testAllStock() throws Exception{
      List<Chassi> chassii = new ArrayList<>(ChassiDB.lookup(ChassiClass.LIGHT));
      chassii.addAll(ChassiDB.lookup(ChassiClass.MEDIUM));
      chassii.addAll(ChassiDB.lookup(ChassiClass.HEAVY));
      chassii.addAll(ChassiDB.lookup(ChassiClass.ASSAULT));

      MessageXBar anXBar = new MessageXBar();
      for(Chassi chassi : chassii){
         Loadout loadout = new Loadout(chassi, anXBar, undoStack);
         loadout.loadStock();

         byte[] result = cut.encode(loadout);
         Loadout decoded = cut.decode(result);

         // Name is not encoded
         decoded.rename(loadout.getName());

         // Verify
         assertEquals(loadout, decoded);
      }
   }
}
