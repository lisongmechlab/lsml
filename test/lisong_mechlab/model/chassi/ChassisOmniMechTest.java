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
import static org.junit.Assert.assertSame;

import java.util.Arrays;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.StructureUpgrade;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * A test suite for {@link ChassisOmniMech}.
 * 
 * @author Li Song
 */
public class ChassisOmniMechTest extends ChassisBaseTest{
   private Engine           engine;
   private StructureUpgrade structureType;
   private ArmorUpgrade     armorType;
   private HeatSinkUpgrade  heatSinkType;
   private OmniPod          centerTorso;
   private int[]            dynArmor;
   private int[]            dynStructure;

   @Override
   @Before
   public void setup(){
      super.setup();

      engine = Mockito.mock(Engine.class);
      structureType = Mockito.mock(StructureUpgrade.class);
      armorType = Mockito.mock(ArmorUpgrade.class);
      heatSinkType = Mockito.mock(HeatSinkUpgrade.class);
      centerTorso = Mockito.mock(OmniPod.class);

      int dynArmorSlots = 0;
      int dynStructureSlots = 0;
      dynArmor = new int[Location.values().length];
      dynStructure = new int[Location.values().length];

      dynArmor[Location.Head.ordinal()] = 1;
      dynArmor[Location.CenterTorso.ordinal()] = 1;
      dynArmor[Location.LeftTorso.ordinal()] = 3;
      dynArmor[Location.RightArm.ordinal()] = 2;

      dynStructure[Location.LeftLeg.ordinal()] = 1;
      dynStructure[Location.RightTorso.ordinal()] = 1;
      dynStructure[Location.LeftArm.ordinal()] = 3;
      dynStructure[Location.LeftLeg.ordinal()] = 2;

      for(Location location : Location.values()){
         dynArmorSlots += dynArmor[location.ordinal()];
         dynStructureSlots += dynStructure[location.ordinal()];
      }

      Mockito.when(structureType.getExtraSlots()).thenReturn(dynStructureSlots);
      Mockito.when(armorType.getExtraSlots()).thenReturn(dynArmorSlots);

   }

   @Override
   protected ChassisOmniMech makeDefaultCUT(){
      return new ChassisOmniMech(mwoID, mwoName, series, name, shortName, maxTons, variant, baseVariant, movementProfile, isClan, engine,
                                 structureType, armorType, heatSinkType, centerTorso, dynStructure, dynArmor);
   }

   @Test(expected = IllegalArgumentException.class)
   public final void testCtor_BadDynStructure(){
      int[] struct = Arrays.copyOf(dynStructure, dynStructure.length);
      struct[Location.LeftTorso.ordinal()] = 12;
      new ChassisOmniMech(mwoID, mwoName, series, name, shortName, maxTons, variant, baseVariant, movementProfile, isClan, engine, structureType,
                          armorType, heatSinkType, centerTorso, struct, dynArmor);
   }

   @Test(expected = IllegalArgumentException.class)
   public final void testCtor_BadDynArmor(){
      int[] armor = Arrays.copyOf(dynArmor, dynArmor.length);
      armor[Location.LeftTorso.ordinal()] = 12;
      new ChassisOmniMech(mwoID, mwoName, series, name, shortName, maxTons, variant, baseVariant, movementProfile, isClan, engine, structureType,
                          armorType, heatSinkType, centerTorso, dynStructure, armor);
   }

   @Test
   public final void testGetArmorMax(){
      ChassisOmniMech chassisOmniMech = (ChassisOmniMech)ChassisDB.lookup("TIMBERWOLF PRIME");
      assertEquals(462, chassisOmniMech.getArmorMax());
   }

   @Test
   public final void testGetEngine(){
      assertSame(engine, makeDefaultCUT().getEngine());
   }

   @Test
   public final void testGetStructureType(){
      assertSame(structureType, makeDefaultCUT().getStructureType());
   }

   @Test
   public final void testGetArmorType(){
      assertSame(armorType, makeDefaultCUT().getArmorType());
   }

   @Test
   public final void testGetHeatSinkType(){
      assertSame(heatSinkType, makeDefaultCUT().getHeatSinkType());
   }

   @Test
   public final void testGetDynamicArmorSlots(){
      ChassisOmniMech cut = makeDefaultCUT();
      for(Location location : Location.values()){
         assertEquals(dynArmor[location.ordinal()], cut.getDynamicArmorSlots(location));
      }
   }

   @Test
   public final void testGetDynamicStructureSlots(){
      ChassisOmniMech cut = makeDefaultCUT();
      for(Location location : Location.values()){
         assertEquals(dynStructure[location.ordinal()], cut.getDynamicStructureSlots(location));
      }
   }
}
