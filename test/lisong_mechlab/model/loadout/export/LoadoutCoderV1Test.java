package lisong_mechlab.model.loadout.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiClass;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.MessageXBar;

import org.junit.Test;

/**
 * Tests the {@link LoadoutCoderV1}
 * 
 * @author Li Song
 */
public class LoadoutCoderV1Test{

   private MessageXBar xBar = new MessageXBar();
   private LoadoutCoderV1 cut;

   public LoadoutCoderV1Test(){
      try{
         cut = new LoadoutCoderV1(xBar);
      }
      catch( Exception e ){
         cut = null;
         fail();
      }
   }

   /**
    * The coder shall be able to encode and decode all stock mechs.
    * 
    * @throws Exception
    */
   @Test
   public void testAllStock() throws Exception{
      List<Chassi> chassii = new ArrayList<>(ChassiDB.lookup(ChassiClass.LIGHT));
      chassii.addAll(ChassiDB.lookup(ChassiClass.MEDIUM));
      chassii.addAll(ChassiDB.lookup(ChassiClass.HEAVY));
      chassii.addAll(ChassiDB.lookup(ChassiClass.ASSAULT));

      MessageXBar anXBar = new MessageXBar();
      for(Chassi chassi : chassii){
         Loadout loadout = new Loadout(chassi, anXBar);
         loadout.loadStock();

         byte[] result = cut.encode(loadout);
         Loadout decoded = cut.decode(result);

         // Name is not encoded
         decoded.rename(loadout.getName());

         // Verify
         assertEquals(loadout, decoded);
      }
   }
}
