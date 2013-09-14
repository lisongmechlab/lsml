package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import lisong_mechlab.model.helpers.MockLoadoutContainer;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;

import org.junit.Test;

/**
 * A test suite for {@link JumpDistance}
 * 
 * @author Emily Björk
 */
public class JumpDistanceTest{
   private final MockLoadoutContainer mlc = new MockLoadoutContainer();
   private final JumpDistance         cut = new JumpDistance(mlc.loadout);

   /**
    * The jump jet definitions in the ItemStats.xml list forces in kilo newton. When weights are taken as tons, the
    * calculations can be done without compensating for factor 1000 (surprise, how convenient!). F = m*a h = a*t^2/2 =
    * F*t*t/(2*m) TODO: Does not take into account the impulse yet!
    */
   @Test
   public void testCalculate(){
      final int mass = 30;
      final int num_jj = 3;
      final JumpJet jj = (JumpJet)ItemDB.lookup("JUMP JETS - CLASS I");

      final double t = jj.getDuration();
      final double F = jj.getForce();
      final double h = F * t * t / (2 * mass) * num_jj;

      when(mlc.chassi.getMassMax()).thenReturn(mass);
      when(mlc.loadout.getJumpJetType()).thenReturn(jj);
      when(mlc.loadout.getJumpJetCount()).thenReturn(num_jj);

      assertEquals(h, cut.calculate(), 0.5);
   }

   /**
    * A mech with zero jump jets shall have a jump distance of 0m.
    */
   @Test
   public void testCalculate_noJJ(){
      when(mlc.loadout.getJumpJetType()).thenReturn(null);
      when(mlc.loadout.getJumpJetCount()).thenReturn(0);
      assertEquals(0.0, cut.calculate(), 0.0);
   }
}
