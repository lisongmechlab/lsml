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

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.StructureUpgrade;
import lisong_mechlab.model.upgrades.Upgrades;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * A test suite for {@link ChassisOmniMech}.
 * 
 * @author Li Song
 */
public class ChassisOmniMechTest extends ChassisBaseTest{
   private Engine              engine;
   private StructureUpgrade    structureType;
   private ArmorUpgrade        armorType;
   private HeatSinkUpgrade     heatSinkType;
   private ComponentOmniMech[] components;

   @Override
   @Before
   public void setup(){
      super.setup();

      engine = Mockito.mock(Engine.class);
      Mockito.when(engine.getFaction()).thenReturn(true);
      Mockito.when(engine.getHardpointType()).thenReturn(HardPointType.NONE);
      Mockito.when(engine.getRating()).thenReturn(250);
      Mockito.when(engine.getType()).thenReturn(EngineType.XL);
      Mockito.when(engine.isCompatible(Matchers.any(Upgrades.class))).thenReturn(true);

      structureType = Mockito.mock(StructureUpgrade.class);
      armorType = Mockito.mock(ArmorUpgrade.class);
      heatSinkType = Mockito.mock(HeatSinkUpgrade.class);

      components = new ComponentOmniMech[Location.values().length];
      for(Location location : Location.values()){
         components[location.ordinal()] = Mockito.mock(ComponentOmniMech.class);
         Mockito.when(components[location.ordinal()].isAllowed(Matchers.any(Item.class))).thenReturn(true);
      }

      Mockito.when(components[Location.Head.ordinal()].getArmorMax()).thenReturn(18);
      Mockito.when(components[Location.LeftArm.ordinal()].getArmorMax()).thenReturn(48);
      Mockito.when(components[Location.LeftTorso.ordinal()].getArmorMax()).thenReturn(64);
      Mockito.when(components[Location.LeftLeg.ordinal()].getArmorMax()).thenReturn(64);
      Mockito.when(components[Location.CenterTorso.ordinal()].getArmorMax()).thenReturn(92);
      Mockito.when(components[Location.RightArm.ordinal()].getArmorMax()).thenReturn(48);
      Mockito.when(components[Location.RightTorso.ordinal()].getArmorMax()).thenReturn(64);
      Mockito.when(components[Location.RightLeg.ordinal()].getArmorMax()).thenReturn(64);

      Mockito.when(components[Location.Head.ordinal()].getDynamicArmorSlots()).thenReturn(1);
      Mockito.when(components[Location.CenterTorso.ordinal()].getDynamicArmorSlots()).thenReturn(1);
      Mockito.when(components[Location.LeftTorso.ordinal()].getDynamicArmorSlots()).thenReturn(3);
      Mockito.when(components[Location.RightArm.ordinal()].getDynamicArmorSlots()).thenReturn(2);

      Mockito.when(components[Location.LeftLeg.ordinal()].getDynamicStructureSlots()).thenReturn(1);
      Mockito.when(components[Location.RightTorso.ordinal()].getDynamicStructureSlots()).thenReturn(1);
      Mockito.when(components[Location.LeftArm.ordinal()].getDynamicStructureSlots()).thenReturn(3);
      Mockito.when(components[Location.RightLeg.ordinal()].getDynamicStructureSlots()).thenReturn(2);

      Mockito.when(structureType.getExtraSlots()).thenReturn(7);
      Mockito.when(armorType.getExtraSlots()).thenReturn(7);
   }

   @Override
   protected ChassisOmniMech makeDefaultCUT(){
      return new ChassisOmniMech(mwoID, mwoName, series, name, shortName, maxTons, variant, baseVariant, movementProfile, isClan, components, engine,
                                 structureType, armorType, heatSinkType);
   }

   @Test(expected = IllegalArgumentException.class)
   public final void testCtor_BadDynStructure(){
      Mockito.when(components[Location.Head.ordinal()].getDynamicStructureSlots()).thenReturn(13);
      new ChassisOmniMech(mwoID, mwoName, series, name, shortName, maxTons, variant, baseVariant, movementProfile, isClan, components, engine,
                          structureType, armorType, heatSinkType);
   }

   @Test(expected = IllegalArgumentException.class)
   public final void testCtor_BadDynArmor(){
      Mockito.when(components[Location.Head.ordinal()].getDynamicArmorSlots()).thenReturn(13);
      new ChassisOmniMech(mwoID, mwoName, series, name, shortName, maxTons, variant, baseVariant, movementProfile, isClan, components, engine,
                          structureType, armorType, heatSinkType);
   }

   @Test
   public final void testGetArmorMax(){
      assertEquals(462, makeDefaultCUT().getArmorMax());
   }

   @Test
   public final void testGetEngine(){
      assertSame(engine, makeDefaultCUT().getEngine());
   }
   
   /**
    * {@link ChassisOmniMech#getMassStripped()} shall return the mass of the chassis with all non fixed items and armor removed.
    */
   @Test
   public final void testGetFixedHeatSinks(){
      ChassisOmniMech cut = makeDefaultCUT();

      List<Item> fixed1 = new ArrayList<>();
      List<Item> fixed2 = new ArrayList<>();
      List<Item> fixed3 = new ArrayList<>();

      Item item1 = Mockito.mock(Item.class);
      HeatSink item2 = Mockito.mock(HeatSink.class);
      HeatSink item3 = Mockito.mock(HeatSink.class);

      Mockito.when(item1.getMass()).thenReturn(1.0);
      Mockito.when(item2.getMass()).thenReturn(2.0);
      Mockito.when(item3.getMass()).thenReturn(3.0);

      fixed1.add(item1);
      fixed1.add(item2);
      fixed2.add(item2);
      fixed2.add(item2);
      fixed3.add(item3);

      Mockito.when(components[2].getFixedItems()).thenReturn(fixed1);
      Mockito.when(components[3].getFixedItems()).thenReturn(fixed2);
      Mockito.when(components[5].getFixedItems()).thenReturn(fixed3);

      assertEquals(4, cut.getFixedHeatSinks());
   }
   
   /**
    * {@link ChassisOmniMech#getMassStripped()} shall return the mass of the chassis with all non fixed items and armor removed.
    */
   @Test
   public final void testGetMassStripped(){
      ChassisOmniMech cut = makeDefaultCUT();

      List<Item> fixed1 = new ArrayList<>();
      List<Item> fixed2 = new ArrayList<>();
      List<Item> fixed3 = new ArrayList<>();

      Item item1 = Mockito.mock(Item.class);
      Item item2 = Mockito.mock(Item.class);
      Item item3 = Mockito.mock(Item.class);

      Mockito.when(item1.getMass()).thenReturn(1.0);
      Mockito.when(item2.getMass()).thenReturn(2.0);
      Mockito.when(item3.getMass()).thenReturn(3.0);

      fixed1.add(item1);
      fixed1.add(item2);
      fixed2.add(item2);
      fixed2.add(item2);
      fixed3.add(item3);

      Mockito.when(components[2].getFixedItems()).thenReturn(fixed1);
      Mockito.when(components[3].getFixedItems()).thenReturn(fixed2);
      Mockito.when(components[5].getFixedItems()).thenReturn(fixed3);

      Mockito.when(structureType.getStructureMass(cut)).thenReturn(3.0);

      double expected = 1 * 1 + 3 * 2 + 1 * 3 + 3;

      assertEquals(expected, cut.getMassStripped(), 0.0);

      Mockito.verify(armorType, Mockito.never()).getArmorMass(Matchers.anyInt());
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
   public final void testIsAllowed_Engine(){
      assertFalse(makeDefaultCUT().isAllowed(engine));
   }

   @Test
   public final void testIsAllowed_NoComponentSupport(){
      Item item = Mockito.mock(Item.class);
      Mockito.when(item.getHardpointType()).thenReturn(HardPointType.NONE);
      Mockito.when(item.getFaction()).thenReturn(true);
      Mockito.when(item.isCompatible(Matchers.any(Upgrades.class))).thenReturn(true);

      ChassisOmniMech cut = makeDefaultCUT();
      assertTrue(cut.isAllowed(item)); // Item in it self is allowed

      // But no component supports it.
      for(Location location : Location.values()){
         Mockito.when(components[location.ordinal()].isAllowed(item)).thenReturn(false);
      }
      assertFalse(cut.isAllowed(item));
   }

   @Test
   public final void testIsAllowed_CASE(){
      Item item = ItemDB.CASE;

      ChassisOmniMech cut = makeDefaultCUT();
      assertFalse(cut.isAllowed(item));
   }
}
