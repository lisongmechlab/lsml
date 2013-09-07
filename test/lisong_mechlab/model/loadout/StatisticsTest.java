package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsTest{
   @Mock
   private Chassi       chassi;
   @Mock
   private Efficiencies efficiencies;
   @Mock
   private Upgrades     upgrades;
   @Mock
   private Loadout      loadout;
   @InjectMocks
   private Statistics   statistics;

   /**
    * The jump jet definitions in the ItemStats.xml list forces in kilonewton. When weights are taken as tons, the
    * calculations can be done without compensating for factor 1000 (surprise, how convenient!). F = m*a h = a*t^2/2 =
    * F*t*t/(2*m) TODO: Does not take into account the impulse yet!
    * 
    * @throws Exception
    */
   @Test
   public void testGetJumpDistance() throws Exception{
      final int mass = 30;
      final int num_jj = 3;
      final JumpJet jj = (JumpJet)ItemDB.lookup("JUMP JETS - CLASS I");

      final double t = jj.getDuration();
      final double F = jj.getForce();
      final double h = F * t * t / (2 * mass) * num_jj;

      Chassi chassi = mock(Chassi.class);
      when(chassi.getMassMax()).thenReturn(mass);
      when(loadout.getChassi()).thenReturn(chassi);
      when(loadout.getJumpJetType()).thenReturn(jj);
      when(loadout.getJumpJetCount()).thenReturn(num_jj);

      assertEquals(h, statistics.getJumpDistance(), 0.5);
   }

   @Test
   public void testGetJumpDistance_noJJ() throws Exception{
      when(loadout.getJumpJetType()).thenReturn(null);
      when(loadout.getJumpJetCount()).thenReturn(0);
      assertEquals(0.0, statistics.getJumpDistance(), 0.0);
   }

   /**
    * Each 'mech has a base heat capacity of 30 heat. Each single heat sink adds 1 capacity. Each double heat sink adds
    * 1.4 capacity. Except for the ones counted as engine internal heat sinks which count as 2.0 (those in engine slots
    * count as 1.4 still). These values are also affected by the efficiency modifier.
    * 
    * @throws Exception
    */
   @Test
   public void testGetHeatCapacity() throws Exception{
      final boolean doubleHs = true;
      final double capacityFactor = 1.3;
      final double dissipationFactor = 1.3;
      final int externalHs = 5;
      final int internalHs = 9;
      double basecapacity = 30;
      double internalHsCapacity = 2.0;
      double externalHsCapacity = 1.4;
      setupMocksHeat(doubleHs, dissipationFactor, capacityFactor, externalHs, internalHs);

      double expectedCapacity = (basecapacity + internalHs * internalHsCapacity + externalHs * externalHsCapacity) * capacityFactor;
      assertEquals(expectedCapacity, statistics.getHeatCapacity(), 0.0);
   }

   /**
    * The heat dissipation of a 'mech is dependent on the heat sink types. > For single heat sinks it is simply the
    * number of heat sinks multiplied by any modifier from the efficiencies. > For double heat sinks it each engine
    * internal heat sink counts as 0.2 and any other heat sinks count as 0.14.
    * 
    * @throws Exception
    */
   @Test
   public void testGetHeatDissipation() throws Exception{
      final boolean doubleHs = true;
      final double capacityFactor = 1.3;
      final double dissipationFactor = 1.3;
      final int externalHs = 5;
      final int internalHs = 9;
      double internalHsDissipation = 0.2;
      double externalHsDissipation = 0.14;
      setupMocksHeat(doubleHs, dissipationFactor, capacityFactor, externalHs, internalHs);

      double expectedDissipation = (internalHs * internalHsDissipation + externalHs * externalHsDissipation) * dissipationFactor;
      assertEquals(expectedDissipation, statistics.getHeatDissipation(), Math.ulp(expectedDissipation) * 4);
   }

   @Test
   public void testGetTimeToOverHeat_15minutes() throws Exception{
      double heat = 10;
      double cooling = heat - 1;
      double capacity = 15 * 60;
      Statistics cut = mock(Statistics.class);
      when(cut.getHeatGeneration()).thenReturn(heat);
      when(cut.getHeatDissipation()).thenReturn(cooling);
      when(cut.getHeatCapacity()).thenReturn(capacity);
      when(cut.getTimeToOverHeat()).thenCallRealMethod();

      assertEquals(Double.POSITIVE_INFINITY, cut.getTimeToOverHeat(), 0);
   }

   @Test
   public void testGetTimeToOverHeat() throws Exception{
      double heat = 10;
      double cooling = 5;
      double capacity = 60;
      Statistics cut = mock(Statistics.class);
      when(cut.getHeatGeneration()).thenReturn(heat);
      when(cut.getHeatDissipation()).thenReturn(cooling);
      when(cut.getHeatCapacity()).thenReturn(capacity);
      when(cut.getTimeToOverHeat()).thenCallRealMethod();
      assertEquals(capacity / (heat - cooling), cut.getTimeToOverHeat(), 0);
   }

   @Test
   public void testGetCoolingRatio() throws Exception{
      double heat = 10;
      double cooling = 5;
      Statistics cut = mock(Statistics.class);
      when(cut.getHeatGeneration()).thenReturn(heat);
      when(cut.getHeatDissipation()).thenReturn(cooling);
      when(cut.getCoolingRatio()).thenCallRealMethod();
      assertEquals(cooling / heat, cut.getCoolingRatio(), 0);
   }

   @Test
   public void testGetCoolingRatio_noHeat() throws Exception{
      double heat = 0;
      double cooling = 5;
      Statistics cut = mock(Statistics.class);
      when(cut.getHeatGeneration()).thenReturn(heat);
      when(cut.getHeatDissipation()).thenReturn(cooling);
      when(cut.getCoolingRatio()).thenCallRealMethod();
      assertEquals(1.0, cut.getCoolingRatio(), 0);
   }

   private void setupMocksHeat(boolean hasDoubleHs, double dissipationMod, double capacityMod, int numHs, int numInternalHs){
      Engine engine = mock(Engine.class);
      when(engine.getNumInternalHeatsinks()).thenReturn(numInternalHs);
      when(efficiencies.getHeatCapacityModifier()).thenReturn(dissipationMod);
      when(efficiencies.getHeatDissipationModifier()).thenReturn(capacityMod);
      when(upgrades.hasDoubleHeatSinks()).thenReturn(hasDoubleHs);
      when(loadout.getUpgrades()).thenReturn(upgrades);
      when(loadout.getEfficiencies()).thenReturn(efficiencies);
      when(loadout.getEngine()).thenReturn(engine);
      when(loadout.getHeatsinksCount()).thenReturn(numHs + numInternalHs);
   }
}
