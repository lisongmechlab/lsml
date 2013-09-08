package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import lisong_mechlab.model.helpers.MockLoadoutContainer;
import lisong_mechlab.model.item.Engine;

import org.junit.Test;

/**
 * Test suite for {@link HeatDissipation}.
 * 
 * @author Emily Björk
 */
public class HeatDissipationTest{
   private final MockLoadoutContainer mlc = new MockLoadoutContainer();
   private final HeatDissipation      cut = new HeatDissipation(mlc.loadout);

   /**
    * The heat dissipation of a 'mech is dependent on the heat sink types. > For single heat sinks it is simply the
    * number of heat sinks multiplied by any modifier from the efficiencies. > For double heat sinks it each engine
    * internal heat sink counts as 0.2 and any other heat sinks count as 0.14.
    */
   @Test
   public void testCalculate(){
      final boolean doubleHs = true;
      final double dissipationFactor = 1.3;
      final int externalHs = 5;
      final int internalHs = 9;
      final double internalHsDissipation = 0.2;
      final double externalHsDissipation = 0.14;

      Engine engine = mock(Engine.class);
      when(engine.getNumInternalHeatsinks()).thenReturn(internalHs);
      when(mlc.efficiencies.getHeatDissipationModifier()).thenReturn(dissipationFactor);
      when(mlc.upgrades.hasDoubleHeatSinks()).thenReturn(doubleHs);
      when(mlc.loadout.getEngine()).thenReturn(engine);
      when(mlc.loadout.getHeatsinksCount()).thenReturn(externalHs + internalHs);

      double expectedDissipation = (internalHs * internalHsDissipation + externalHs * externalHsDissipation) * dissipationFactor;
      assertEquals(expectedDissipation, cut.calculate(), Math.ulp(expectedDissipation) * 4);
   }
}
