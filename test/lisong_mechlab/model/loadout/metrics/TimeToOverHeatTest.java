package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * A test suite for {@link TimeToOverHeat}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class TimeToOverHeatTest{
   @Mock
   private HeatCapacity    capacity;
   @Mock
   private HeatDissipation dissipation;
   @Mock
   private HeatGeneration  generation;

   @InjectMocks
   private TimeToOverHeat  cut;

   /**
    * 15 minutes and above is rounded up to infinity. Matches are only 15 minutes :)
    */
   @Test
   public void testGetTimeToOverHeat_15minutes(){
      final double heat = 10;
      final double cooling = heat - 1;
      final double ccapacity = 15 * 60;
      when(generation.calculate()).thenReturn(heat);
      when(dissipation.calculate()).thenReturn(cooling);
      when(capacity.calculate()).thenReturn(ccapacity);

      assertEquals(Double.POSITIVE_INFINITY, cut.calculate(), 0);
   }

   /**
    * If a mech generates 10 heat per second and can dissipate 5, then the mech will over heat after the differential
    * has filled the heat capacity: capacity / (generation - dissipation)
    */
   @Test
   public void testGetTimeToOverHeat(){
      double heat = 10;
      double cooling = 5;
      double ccapacity = 60;
      when(generation.calculate()).thenReturn(heat);
      when(dissipation.calculate()).thenReturn(cooling);
      when(capacity.calculate()).thenReturn(ccapacity);
      assertEquals(ccapacity / (heat - cooling), cut.calculate(), 0);
   }
}
