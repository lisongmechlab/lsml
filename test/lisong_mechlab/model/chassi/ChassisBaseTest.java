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
package lisong_mechlab.model.chassi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lisong_mechlab.model.item.Item;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * An abstract base class for testing {@link ChassisBase} derived objects.
 * 
 * @author Li Song
 */
@RunWith(JUnitParamsRunner.class)
public abstract class ChassisBaseTest{
   protected int             baseVariant = 12;
   protected boolean         isClan      = true;
   protected int             maxTons     = 75;
   protected MovementProfile movementProfile;
   protected int             mwoID       = 300;
   protected String          mwoName     = "tbw-p";
   protected String          name        = "Timber Wolf Primal";
   protected String          series      = "Timber Wolf";
   protected String          shortName   = "tbw primal";
   protected ChassisVariant  variant     = ChassisVariant.FOUNDER;

   protected abstract ChassisBase makeDefaultCUT();
   
   @Before
   public void setup(){
      movementProfile = Mockito.mock(MovementProfile.class);
   }

   @Test
   public final void testGetBaseVariantId() throws Exception{
      assertEquals(baseVariant, makeDefaultCUT().getBaseVariantId());
   }

   @Test
   public final void testGetChassiClass() throws Exception{
      assertEquals(ChassisClass.fromMaxTons(maxTons), makeDefaultCUT().getChassiClass());
   }

   @Test
   public final void testGetCriticalSlotsTotal() throws Exception{
      assertEquals(78, makeDefaultCUT().getCriticalSlotsTotal());
   }

   @Test
   public final void testGetMassMax() throws Exception{
      assertEquals(maxTons, makeDefaultCUT().getMassMax());
   }

   @Test
   public final void testGetMovementProfile() throws Exception{
      assertSame(movementProfile, makeDefaultCUT().getMovementProfile());
   }
   
   @Test
   public final void testGetMwoId() throws Exception{
      assertEquals(mwoID, makeDefaultCUT().getMwoId());
   }

   @Test
   public final void testGetMwoName() throws Exception{
      assertEquals(mwoName, makeDefaultCUT().getMwoName());
   }

   @Test
   public final void testGetName() throws Exception{
      assertEquals(name, makeDefaultCUT().getName());
   }

   @Test
   public final void testGetNameShort() throws Exception{
      assertEquals(shortName, makeDefaultCUT().getNameShort());
   }

   @Test
   public final void testGetSeriesName() throws Exception{
      assertEquals(series, makeDefaultCUT().getSeriesName());
   }

   @Test
   public final void testGetVariantType() throws Exception{
      assertEquals(variant, makeDefaultCUT().getVariantType());
   }

   @Parameters({"SDR-5K", "JR7-D", "CDA-2A"})
   @Test
   public void testGetVariantType_Negative(String aChassis){
      assertFalse(ChassisDB.lookup(aChassis).getVariantType().isVariation());
   }

   @Parameters({"SDR-5K(C)", "JR7-D(S)", "CDA-2A(C)"})
   @Test
   public void testGetVariantType_Positive(String aChassis){
      assertTrue(ChassisDB.lookup(aChassis).getVariantType().isVariation());
   }

   @Test
   public final void testIsAllowed() throws Exception{
      ChassisBase cut0 = makeDefaultCUT();
      Item clanItem = Mockito.mock(Item.class);
      Mockito.when(clanItem.isClan()).thenReturn(true);
      Item isItem = Mockito.mock(Item.class);
      Mockito.when(isItem.isClan()).thenReturn(false);

      if( cut0.isClan() ){
         assertTrue(cut0.isAllowed(clanItem));
         assertFalse(cut0.isAllowed(isItem));
      }
      else{
         assertFalse(cut0.isAllowed(clanItem));
         assertTrue(cut0.isAllowed(isItem));
      }
   }

   @Test
   public final void testIsClan() throws Exception{
      assertEquals(isClan, makeDefaultCUT().isClan());
   }

   @Test
   public void testIsHero(){
      ChassisBase ilya = ChassisDB.lookup("Ilya Muromets");
      assertEquals(ChassisVariant.HERO, ilya.getVariantType());

      ChassisBase ctf3d = ChassisDB.lookup("CTF-3D");
      assertEquals(ChassisVariant.NORMAL, ctf3d.getVariantType());
   }

   @Test
   public final void testIsSameSeries() throws Exception{
      ChassisBase cut0 = makeDefaultCUT();
      ChassisBase cut1 = makeDefaultCUT();

      series = "Other Series";
      ChassisBase cut2 = makeDefaultCUT();

      assertTrue(cut0.isSameSeries(cut0));
      assertTrue(cut0.isSameSeries(cut1));
      assertFalse(cut0.isSameSeries(cut2));
   }

   @Parameters({"HBK-4J, CTF-3D", "EMBER, Ilya Muromets"})
   @Test
   public void testIsSameSeries_Negative(String aChassiA, String aChassiB){
      assertFalse(ChassisDB.lookup(aChassiA).isSameSeries(ChassisDB.lookup(aChassiB)));
   }

   @Parameters({"HBK-4J, HBK-4P", "CTF-3D, Ilya Muromets"})
   @Test
   public void testIsSameSeries_Positive(String aChassisA, String aChassisB){
      assertTrue(ChassisDB.lookup(aChassisA).isSameSeries(ChassisDB.lookup(aChassisB)));
   }

   @Test
   public final void testToString() throws Exception{
      assertEquals(shortName, makeDefaultCUT().toString());
   }
}
