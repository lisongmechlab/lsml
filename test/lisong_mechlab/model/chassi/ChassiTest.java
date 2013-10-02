package lisong_mechlab.model.chassi;

import static org.junit.Assert.*;

import org.junit.Test;

public class ChassiTest{

   @Test
   public void testLoadHeroMech(){
      Chassi cut = ChassiDB.lookup("Ilya Muromets");

      assertEquals(140, cut.getEngineMin());
      assertEquals(340, cut.getEngineMax());

      assertEquals("ILYA MUROMETS", cut.getName());
      assertEquals("CTF-IM", cut.getNameShort());
      assertEquals(cut.getNameShort(), cut.toString());
      assertEquals("ctf-im", cut.getMwoName());

      assertEquals(7.0, cut.getInternalMass(), 0.0);
      assertEquals(70.0, cut.getMassMax(), 0.0);

      assertEquals(434, cut.getArmorMax());

      assertSame(ChassiClass.HEAVY, cut.getChassiClass());
      assertEquals(0, cut.getMaxJumpJets());
      assertEquals(false, cut.isEcmCapable());

      // Do a through test only on the Ilyas components
      {
         InternalPart pt = cut.getInternalPart(Part.Head);
         
         assertEquals(18, pt.getArmorMax());
         assertEquals(15.0, pt.getHitpoints(), 0.0);
         assertEquals(6, pt.getNumCriticalslots());
         assertEquals(0, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(0, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.Head, pt.getType());
         assertFalse(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         
         assertEquals(3, pt.getInternalItems().size());
      }

      {
         InternalPart pt = cut.getInternalPart(Part.RightArm);
         assertEquals(44, pt.getArmorMax());
         assertEquals(22.0, pt.getHitpoints(), 0.0);
         assertEquals(12, pt.getNumCriticalslots());
         assertEquals(1, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(1, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.RightArm, pt.getType());
         assertFalse(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(3, pt.getInternalItems().size());
      }

      {
         InternalPart pt = cut.getInternalPart(Part.LeftArm);        
         assertEquals(44, pt.getArmorMax());
         assertEquals(22.0, pt.getHitpoints(), 0.0);
         assertEquals(12, pt.getNumCriticalslots());
         assertEquals(0, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(1, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.LeftArm, pt.getType());
         assertFalse(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(4, pt.getInternalItems().size());
      }

      {
         InternalPart pt = cut.getInternalPart(Part.RightTorso);
         assertEquals(60, pt.getArmorMax());
         assertEquals(30.0, pt.getHitpoints(), 0.0);
         assertEquals(12, pt.getNumCriticalslots());
         assertEquals(1, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(1, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.RightTorso, pt.getType());
         assertTrue(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(0, pt.getInternalItems().size());
      }
      
      {
         InternalPart pt = cut.getInternalPart(Part.LeftTorso);
         assertEquals(60, pt.getArmorMax());
         assertEquals(30.0, pt.getHitpoints(), 0.0);
         assertEquals(12, pt.getNumCriticalslots());
         assertEquals(1, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(0, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(1, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.LeftTorso, pt.getType());
         assertTrue(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(0, pt.getInternalItems().size());
      }
      
      {
         InternalPart pt = cut.getInternalPart(Part.CenterTorso);
         assertEquals(88, pt.getArmorMax());
         assertEquals(44.0, pt.getHitpoints(), 0.0);
         assertEquals(12, pt.getNumCriticalslots());
         assertEquals(0, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(0, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.CenterTorso, pt.getType());
         assertTrue(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(1, pt.getInternalItems().size());
      }
      
      {
         InternalPart pt = cut.getInternalPart(Part.RightLeg);
         assertEquals(60, pt.getArmorMax());
         assertEquals(30.0, pt.getHitpoints(), 0.0);
         assertEquals(6, pt.getNumCriticalslots());
         assertEquals(0, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(0, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.RightLeg, pt.getType());
         assertFalse(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(4, pt.getInternalItems().size());
      }
      
      {
         InternalPart pt = cut.getInternalPart(Part.LeftLeg);
         assertEquals(60, pt.getArmorMax());
         assertEquals(30.0, pt.getHitpoints(), 0.0);
         assertEquals(6, pt.getNumCriticalslots());
         assertEquals(0, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(0, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.LeftLeg, pt.getType());
         assertFalse(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(4, pt.getInternalItems().size());
      }
   }

   @Test
   public void testLoadHasECM(){
      Chassi cut = ChassiDB.lookup("AS7-D-DC");

      assertEquals(200, cut.getEngineMin());
      assertEquals(360, cut.getEngineMax());

      assertEquals("ATLAS AS7-D-DC", cut.getName());
      assertEquals("AS7-D-DC", cut.getNameShort());
      assertEquals(cut.getNameShort(), cut.toString());
      assertEquals("as7-d-dc", cut.getMwoName());

      assertEquals(10.0, cut.getInternalMass(), 0.0);
      assertEquals(100.0, cut.getMassMax(), 0.0);

      assertEquals(614, cut.getArmorMax());

      assertSame(ChassiClass.ASSAULT, cut.getChassiClass());
      assertEquals(0, cut.getMaxJumpJets());
      assertEquals(true, cut.isEcmCapable());

      assertEquals(3, cut.getInternalPart(Part.Head).getInternalItems().size());

      assertEquals(4, cut.getInternalPart(Part.RightArm).getInternalItems().size());
      assertEquals(4, cut.getInternalPart(Part.LeftArm).getInternalItems().size());

      assertEquals(4, cut.getInternalPart(Part.RightLeg).getInternalItems().size());
      assertEquals(4, cut.getInternalPart(Part.LeftLeg).getInternalItems().size());

      assertEquals(1, cut.getInternalPart(Part.RightArm).getNumHardpoints(HardpointType.ENERGY));
      assertEquals(1, cut.getInternalPart(Part.LeftArm).getNumHardpoints(HardpointType.ENERGY));
      assertEquals(1, cut.getInternalPart(Part.LeftArm).getNumHardpoints(HardpointType.AMS));
      assertEquals(3, cut.getInternalPart(Part.LeftTorso).getNumHardpoints(HardpointType.MISSILE));
      assertEquals(2, cut.getInternalPart(Part.RightTorso).getNumHardpoints(HardpointType.BALLISTIC));
   }

   @Test
   public void testLoadHasJJ(){
      Chassi cut = ChassiDB.lookup("Jenner JR7-F");

      assertEquals(70, cut.getEngineMin()); // However no such engine exists :)
      assertEquals(300, cut.getEngineMax());

      assertEquals("JENNER JR7-F", cut.getName());
      assertEquals("JR7-F", cut.getNameShort());
      assertEquals(cut.getNameShort(), cut.toString());
      assertEquals("jr7-f", cut.getMwoName());

      assertEquals(3.5, cut.getInternalMass(), 0.0);
      assertEquals(35.0, cut.getMassMax(), 0.0);

      assertEquals(238, cut.getArmorMax());

      assertSame(ChassiClass.LIGHT, cut.getChassiClass());
      assertEquals(5, cut.getMaxJumpJets());
      assertEquals(false, cut.isEcmCapable());

      assertEquals(3, cut.getInternalPart(Part.Head).getInternalItems().size());

      assertEquals(2, cut.getInternalPart(Part.RightArm).getInternalItems().size());
      assertEquals(2, cut.getInternalPart(Part.LeftArm).getInternalItems().size());

      assertEquals(4, cut.getInternalPart(Part.RightLeg).getInternalItems().size());
      assertEquals(4, cut.getInternalPart(Part.LeftLeg).getInternalItems().size());

      assertEquals(3, cut.getInternalPart(Part.RightArm).getNumHardpoints(HardpointType.ENERGY));
      assertEquals(3, cut.getInternalPart(Part.LeftArm).getNumHardpoints(HardpointType.ENERGY));
      assertEquals(1, cut.getInternalPart(Part.LeftTorso).getNumHardpoints(HardpointType.AMS));
   }
}
