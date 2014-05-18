package lisong_mechlab.model.loadout;

import static org.junit.Assert.*;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.loadout.component.ComponentBuilder.Factory;
import lisong_mechlab.util.MessageXBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class LoadoutBaseTest{
   protected abstract LoadoutBase<?, ?> makeDefaultCUT();

   @Test
   public final void testToString() throws Exception{
      LoadoutBase<?, ?> cut = makeDefaultCUT();
      String name = "mamboyeeya";
      cut.rename(name);

      assertEquals(name + " (" + cut.getChassis().getNameShort() + ")", cut.toString());
   }

   @Test
   public final void testGetAllItems() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetArmor() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetEfficiencies() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetMass() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetFreeMass() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetChassis() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetName() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetNumCriticalSlotsFree() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetNumCriticalSlotsUsed() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetComponent() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetComponents() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetUpgrades() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetHardpointsCount() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetHeatsinksCount() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetJumpJetCount() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetJumpJetType() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public final void testGetMovementProfile() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

}
