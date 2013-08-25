package lisong_mechlab.model.loadout;

import javax.swing.text.html.HTMLDocument.HTMLReader.SpecialAction;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsTest{
   @Mock
   private Chassi       chassi;
   @Mock
   private Efficiencies efficiencies;
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
    * The jump jet definitions in the ItemStats.xml list forces in kilonewton. When weights are taken as tons,
    * the calculations can be done without compensating for factor 1000 (surprise, how convenient!).
    *
    * F = m*a
    * h = a*t^2/2 = F*t*t/(2*m)
    * 
    *  TODO: Does not take into account the impulse yet!
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

   @Test
   public void testGetHeatCapacity() throws Exception{
      throw new RuntimeException("not yet implemented");
   }

   @Test
   public void testGetHeatDissapation() throws Exception{
      throw new RuntimeException("not yet implemented");
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
