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
package lisong_mechlab.model.chassi;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test suite for {@link MaxMovementProfile}.
 * 
 * @author Emily Björk
 */
public class MaxMovementProfileTest{

   @Test
   public void testGetMovementArchetype(){
      MovementProfile base = Mockito.mock(MovementProfile.class);
      MovementProfile arm1 = Mockito.mock(MovementProfile.class);
      MovementProfile arm2 = Mockito.mock(MovementProfile.class);
      MovementProfile leg1 = Mockito.mock(MovementProfile.class);
      MovementProfile leg2 = Mockito.mock(MovementProfile.class);

      List<MovementProfile> arm = new ArrayList<>();
      List<MovementProfile> leg = new ArrayList<>();
      List<List<MovementProfile>> groups = new ArrayList<>();
      groups.add(arm);
      groups.add(leg);
      arm.add(arm1);
      arm.add(arm2);
      leg.add(leg1);
      leg.add(leg2);

      MaxMovementProfile cut = new MaxMovementProfile(base, groups);

      Mockito.when(base.getMovementArchetype()).thenReturn(MovementArchetype.Small);
      Mockito.when(arm1.getMovementArchetype()).thenReturn(MovementArchetype.Large);
      Mockito.when(arm2.getMovementArchetype()).thenReturn(MovementArchetype.Large);
      Mockito.when(leg1.getMovementArchetype()).thenReturn(MovementArchetype.Large);
      Mockito.when(leg2.getMovementArchetype()).thenReturn(MovementArchetype.Large);

      assertEquals(MovementArchetype.Small, cut.getMovementArchetype());
   }

   @Test
   public void testGetMaxMovementSpeed(){
      MovementProfile base = Mockito.mock(MovementProfile.class);
      MovementProfile arm1 = Mockito.mock(MovementProfile.class);
      MovementProfile arm2 = Mockito.mock(MovementProfile.class);
      MovementProfile leg1 = Mockito.mock(MovementProfile.class);
      MovementProfile leg2 = Mockito.mock(MovementProfile.class);

      List<MovementProfile> arm = new ArrayList<>();
      List<MovementProfile> leg = new ArrayList<>();
      List<MovementProfile> torso = new ArrayList<>(); // Empty groups should be handled correctly
      List<List<MovementProfile>> groups = new ArrayList<>();
      groups.add(arm);
      groups.add(leg);
      groups.add(torso);
      arm.add(arm1);
      arm.add(arm2);
      leg.add(leg1);
      leg.add(leg2);

      MaxMovementProfile cut = new MaxMovementProfile(base, groups);

      Mockito.when(base.getMaxMovementSpeed()).thenReturn(3.0);
      Mockito.when(arm1.getMaxMovementSpeed()).thenReturn(-0.1);
      Mockito.when(arm2.getMaxMovementSpeed()).thenReturn(0.2);
      Mockito.when(leg1.getMaxMovementSpeed()).thenReturn(0.4);
      Mockito.when(leg2.getMaxMovementSpeed()).thenReturn(0.3);

      assertEquals(3.6, cut.getMaxMovementSpeed(), 0.0);
   }
}
