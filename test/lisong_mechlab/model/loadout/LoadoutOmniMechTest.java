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
package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.MovementArchetype;
import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.chassi.Quirks;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.loadout.component.ConfiguredComponentOmniMech;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.Upgrades;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test suite for {@link LoadoutOmniMech}.
 * 
 * @author Emily Björk
 */
public class LoadoutOmniMechTest extends LoadoutBaseTest{
   class ComponentFactory implements ComponentBuilder.Factory<ConfiguredComponentOmniMech>{
      @Override
      public ConfiguredComponentOmniMech[] cloneComponents(LoadoutBase<ConfiguredComponentOmniMech> aLoadout){
         return (ConfiguredComponentOmniMech[])components;
      }

      @Override
      public ConfiguredComponentOmniMech[] defaultComponents(ChassisBase aChassis){
         return (ConfiguredComponentOmniMech[])components;
      }
   }

   protected OmniPod[]     pods = new OmniPod[Location.values().length];

   protected Engine        engine;
   private ChassisOmniMech chassisOmni;
   private MovementProfile quirkBase;

   @Override
   @Before
   public void setup(){
      super.setup();
      chassisOmni = Mockito.mock(ChassisOmniMech.class);
      chassis = chassisOmni;
      engine = Mockito.mock(Engine.class);
      quirkBase = Mockito.mock(MovementProfile.class);

      components = new ConfiguredComponentOmniMech[Location.values().length];
      for(Location location : Location.values()){
         pods[location.ordinal()] = Mockito.mock(OmniPod.class);
         components[location.ordinal()] = Mockito.mock(ConfiguredComponentOmniMech.class);

         Mockito.when(getComponent(location).getOmniPod()).thenReturn(pods[location.ordinal()]);
      }
   }

   @Override
   protected LoadoutBase<?> makeDefaultCUT(){
      Mockito.when(chassis.getName()).thenReturn(chassisName);
      Mockito.when(chassis.getNameShort()).thenReturn(chassisShortName);
      Mockito.when(chassis.getMassMax()).thenReturn(mass);
      Mockito.when(chassis.getCriticalSlotsTotal()).thenReturn(slots);
      Mockito.when(chassisOmni.getArmorType()).thenReturn(armor);
      Mockito.when(chassisOmni.getStructureType()).thenReturn(structure);
      Mockito.when(chassisOmni.getHeatSinkType()).thenReturn(heatSinks);
      Mockito.when(chassisOmni.getEngine()).thenReturn(engine);
      Mockito.when(chassisOmni.getMovementProfileBase()).thenReturn(quirkBase);
      return new LoadoutOmniMech(new ComponentFactory(), (ChassisOmniMech)chassis, xBar);
   }

   @Test
   public final void testGetEngine() throws Exception{
      assertSame(engine, makeDefaultCUT().getEngine());
   }

   @Test
   public final void testGetUpgrades() throws Exception{
      assertEquals(Upgrades.class, makeDefaultCUT().getUpgrades().getClass());

      assertSame(armor, makeDefaultCUT().getUpgrades().getArmor());
      assertSame(structure, makeDefaultCUT().getUpgrades().getStructure());
      assertSame(heatSinks, makeDefaultCUT().getUpgrades().getHeatSink());
      assertSame(UpgradeDB.STANDARD_GUIDANCE, makeDefaultCUT().getUpgrades().getGuidance());
   }

   @Test
   public final void testGetJumpJetsMax() throws Exception{
      Mockito.when(pods[3].getJumpJetsMax()).thenReturn(2);
      Mockito.when(pods[6].getJumpJetsMax()).thenReturn(3);
      Mockito.when(pods[7].getJumpJetsMax()).thenReturn(5);

      assertEquals(10, makeDefaultCUT().getJumpJetsMax());
   }

   @Test
   public final void testGetNumCriticalSlotsUsedFree() throws Exception{
      Mockito.when(structure.getExtraSlots()).thenReturn(7);
      Mockito.when(armor.getExtraSlots()).thenReturn(7);

      Mockito.when(getComponent(Location.LeftArm).getSlotsUsed()).thenReturn(5);
      Mockito.when(getComponent(Location.RightLeg).getSlotsUsed()).thenReturn(3);

      assertEquals(8, makeDefaultCUT().getNumCriticalSlotsUsed());
      assertEquals(slots - 8, makeDefaultCUT().getNumCriticalSlotsFree());
   }

   @Test
   public final void testGetMovementProfile_() throws Exception{
      Quirks quirkEmpty = Mockito.mock(Quirks.class);
      Quirks quirk1 = Mockito.mock(Quirks.class);
      Quirks quirk2 = Mockito.mock(Quirks.class);

      for(Location location : Location.values()){
         if( location.ordinal() >= 2 )
            Mockito.when(pods[location.ordinal()].getQuirks()).thenReturn(quirkEmpty);
      }
      Mockito.when(pods[0].getQuirks()).thenReturn(quirk1);
      Mockito.when(pods[1].getQuirks()).thenReturn(quirk2);

      Mockito.when(quirkBase.getMovementArchetype()).thenReturn(MovementArchetype.Huge);
      Mockito.when(quirkBase.getArmYawMax()).thenReturn(14.0);
      Mockito.when(quirk1.extraArmYawMax(14.0)).thenReturn(4.0);
      Mockito.when(quirk2.extraArmYawMax(14.0)).thenReturn(-1.0);

      assertEquals(17.0, makeDefaultCUT().getMovementProfile().getArmYawMax(), 0.0);
      assertSame(MovementArchetype.Huge, makeDefaultCUT().getMovementProfile().getMovementArchetype());
   }

   private ConfiguredComponentOmniMech getComponent(Location aLocation){
      return (ConfiguredComponentOmniMech)components[aLocation.ordinal()];
   }
}
