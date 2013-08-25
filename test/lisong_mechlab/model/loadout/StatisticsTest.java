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

   @Test
   public void testGetTopSpeed_noengine() throws Exception{
      when(loadout.getEngine()).thenReturn(null);
      assertEquals(0, statistics.getTopSpeed(), 0.0);
   }

   @Test
   public void testGetTopSpeed() throws Exception{
      int rating = 300;
      double factor = 4;
      int tonnage = 30;
      for(double speedtweak : new double[] {1.0, 1.1}){
         when(loadout.getEngine()).thenReturn((Engine)ItemDB.lookup("STD ENGINE " + rating));
         when(loadout.getChassi()).thenReturn(chassi);
         when(loadout.getEfficiencies()).thenReturn(efficiencies);
         when(chassi.getSpeedFactor()).thenReturn(factor);
         when(chassi.getMassMax()).thenReturn(tonnage);
         when(efficiencies.hasSpeedTweak()).thenReturn(speedtweak > 1.0);
         when(efficiencies.getSpeedModifier()).thenReturn(speedtweak);

         double expected = rating * factor / tonnage * speedtweak;
         assertEquals(expected, statistics.getTopSpeed(), 0.0);
      }
   }

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
      boolean hasDoubleHs = true;
      double modifier = 1.3;
      double basecapacity = 30;
      int numHS = 5;
      int numInternalHs = 8;
      double internalHsCapacity = 2.0;
      double externalHsCapacity = 1.4;

      Engine engine = mock(Engine.class);
      when(engine.getNumInternalHeatsinks()).thenReturn(numInternalHs);
      when(efficiencies.getHeatCapacityModifier()).thenReturn(modifier);
      when(upgrades.hasDoubleHeatSinks()).thenReturn(hasDoubleHs);
      when(loadout.getUpgrades()).thenReturn(upgrades);
      when(loadout.getEfficiencies()).thenReturn(efficiencies);
      when(loadout.getEngine()).thenReturn(engine);
      when(loadout.getHeatsinksCount()).thenReturn(numHS + numInternalHs);

      double expectedCapacity = (basecapacity + numInternalHs * internalHsCapacity + numHS * externalHsCapacity) * modifier;
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
      boolean hasDoubleHs = true;
      double modifier = 1.3;
      int numHS = 5;
      int numInternalHs = 8;
      double internalHsDissipation = 0.2;
      double externalHsDissipation = 0.14;

      Engine engine = mock(Engine.class);
      when(engine.getNumInternalHeatsinks()).thenReturn(numInternalHs);
      when(efficiencies.getHeatDissipationModifier()).thenReturn(modifier);
      when(upgrades.hasDoubleHeatSinks()).thenReturn(hasDoubleHs);
      when(loadout.getUpgrades()).thenReturn(upgrades);
      when(loadout.getEfficiencies()).thenReturn(efficiencies);
      when(loadout.getEngine()).thenReturn(engine);
      when(loadout.getHeatsinksCount()).thenReturn(numHS + numInternalHs);

      double expectedDissipation = (numInternalHs * internalHsDissipation + numHS * externalHsDissipation) * modifier;
      assertEquals(expectedDissipation, statistics.getHeatDissipation(), Math.ulp(expectedDissipation)*4);
   }

   @Test
   public void testGetHeatGeneration() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testGetTimeToOverHeat() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testGetCoolingRatio() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

}
