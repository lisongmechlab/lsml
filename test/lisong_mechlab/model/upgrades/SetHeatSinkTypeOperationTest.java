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
package lisong_mechlab.model.upgrades;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.OpStripLoadout;
import lisong_mechlab.model.loadout.export.Base64LoadoutCoder;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import org.junit.Test;
import org.mockito.Mockito;

public class SetHeatSinkTypeOperationTest{

   @Test
   public void testStripMech() throws DecodingException{
      MessageXBar xBar = Mockito.mock(MessageXBar.class);
      Base64LoadoutCoder coder = new Base64LoadoutCoder(xBar);
      Loadout loadout = coder.parse("lsml://rR4AEURNB1QScQtNB1REvqCEj9P37332SAXGzly5WoqI0fyo");
      Loadout loadoutOriginal = coder.parse("lsml://rR4AEURNB1QScQtNB1REvqCEj9P37332SAXGzly5WoqI0fyo");
      OperationStack stack = new OperationStack(1);

      stack.pushAndApply(new OpStripLoadout(loadout, xBar));

      assertEquals(loadout.getMass(), loadout.getChassis().getMassMax() * 0.1, 0.0);
      assertSame(UpgradeDB.STANDARD_ARMOR, loadout.getUpgrades().getArmor());
      assertSame(UpgradeDB.STANDARD_STRUCTURE, loadout.getUpgrades().getStructure());
      assertSame(UpgradeDB.STANDARD_GUIDANCE, loadout.getUpgrades().getGuidance());
      assertSame(UpgradeDB.STANDARD_HEATSINKS, loadout.getUpgrades().getHeatSink());

      stack.undo();

      assertEquals(loadoutOriginal, loadout);
   }
}
