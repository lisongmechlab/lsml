package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link CoolingRatio}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class CoolingRatioTest{
   @Mock
   private HeatDissipation dissipation;
   @Mock
   private HeatGeneration  heatGeneration;
   @InjectMocks
   private CoolingRatio    cut;

   @Test
   public void testCalculate() throws Exception{
      double heat = 10;
      double cooling = 5;
      when(heatGeneration.calculate()).thenReturn(heat);
      when(dissipation.calculate()).thenReturn(cooling);
      assertEquals(cooling / heat, cut.calculate(), 0);
   }

   @Test
   public void testCalculate_noHeat() throws Exception{
      double heat = 0;
      double cooling = 5;

      when(heatGeneration.calculate()).thenReturn(heat);
      when(dissipation.calculate()).thenReturn(cooling);
      assertEquals(1.0, cut.calculate(), 0);
   }
}
