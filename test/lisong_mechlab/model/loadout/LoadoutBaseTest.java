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
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link LoadoutBase}
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class LoadoutBaseTest{
   protected abstract LoadoutBase<?> makeDefaultCUT();

   protected ConfiguredComponentBase[] components;

   class ComponentFactory implements ComponentBuilder.Factory<ConfiguredComponentBase>{
      @Override
      public ConfiguredComponentBase[] cloneComponents(LoadoutBase<ConfiguredComponentBase> aLoadout){
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public ConfiguredComponentBase[] defaultComponents(ChassisBase aChassis){
         // TODO Auto-generated method stub
         return null;
      }

   }

   @Test
   public final void testToString() throws Exception{
      LoadoutBase<?> cut = makeDefaultCUT();
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
