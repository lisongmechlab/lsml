package lisong_mechlab.model.loadout;

import org.junit.Test;

import lisong_mechlab.model.helpers.MockLoadoutContainer;

/**
 * Tests the {@link ArtemisHandler} class.
 * 
 * @author Li Song
 */
public class ArtemisHandlerTest{
   private final MockLoadoutContainer mlc = new MockLoadoutContainer();
   @SuppressWarnings("unused")
   private final ArtemisHandler       cut = new ArtemisHandler(mlc.loadout);
   
   @Test
   public void testCanApplyUpgrade(){
      // TODO: Finish what I started...
   }
}
