package lisong_mechlab.model.loadout.metrics;

import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.Loadout;

/**
 * A metric that calculates how high the mech can jump.
 * 
 * @author Li Song
 */
public class JumpDistance implements Metric{
   private final Loadout loadout;

   public JumpDistance(final Loadout aLoadout){
      loadout = aLoadout;
   }

   @Override
   public double calculate(){
      JumpJet jj = loadout.getJumpJetType();
      if( jj == null )
         return 0;
      return loadout.getJumpJetCount() * jj.getForce() * jj.getDuration() * jj.getDuration() / (2 * loadout.getChassi().getMassMax());
   }
}
