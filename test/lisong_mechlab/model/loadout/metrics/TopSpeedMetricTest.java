package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import lisong_mechlab.model.helpers.MockLoadoutContainer;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.ItemDB;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TopSpeedMetricTest{
   MockLoadoutContainer mlc = new MockLoadoutContainer();
   TopSpeedMetric statistics = new TopSpeedMetric(mlc.loadout);

   @Test
   public void testCalculate_noengine() throws Exception{
      when(mlc.loadout.getEngine()).thenReturn(null);
      assertEquals(0, statistics.calculate(), 0.0);
   }

   @Test
   public void testCalculate() throws Exception{
      int rating = 300;
      double factor = 4;
      int tonnage = 30;
      for(double speedtweak : new double[] {1.0, 1.1}){
         when(mlc.loadout.getEngine()).thenReturn((Engine)ItemDB.lookup("STD ENGINE " + rating));
         when(mlc.chassi.getSpeedFactor()).thenReturn(factor);
         when(mlc.chassi.getMassMax()).thenReturn(tonnage);
         when(mlc.efficiencies.hasSpeedTweak()).thenReturn(speedtweak > 1.0);
         when(mlc.efficiencies.getSpeedModifier()).thenReturn(speedtweak);

         double expected = rating * factor / tonnage * speedtweak;
         assertEquals(expected, statistics.calculate(), 0.0);
      }
   }
}
