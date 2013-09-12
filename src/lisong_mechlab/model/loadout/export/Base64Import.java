package lisong_mechlab.model.loadout.export;

import java.io.IOException;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.Base64;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.view.LSML;

/**
 * This class handles exporting of {@link Loadout}s to base64 strings. It will correctly determine which format the
 * string is in and choose the right combination of decoders.
 * 
 * @author Li Song
 */
public class Base64Import{
   private static final transient LoadoutCoderV1 coderV1 = new LoadoutCoderV1(LSML.getInstance().getXBar());
   private static final transient Base64         base64  = new Base64();

   public static Loadout parse(String url) throws DecodingException, IOException{

      if( url.toLowerCase().contains("lsml://") ){
         url = url.substring(7);
      }
      if( url.endsWith("/") )
         url = url.substring(0, url.length() - 1);
      Loadout loadout = coderV1.decode(base64.decode(url.toCharArray()));
      return loadout;
   }

   public static String encode(Loadout aLoadout){
      try{
         return "lsml://" + new String(base64.encode(coderV1.encode(aLoadout)));
      }
      catch( Exception e ){
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return "Export failed!";
   }

}
