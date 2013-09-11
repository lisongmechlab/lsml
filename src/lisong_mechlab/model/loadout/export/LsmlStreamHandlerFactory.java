package lisong_mechlab.model.loadout.export;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * A default factory of Stream Handlers for the LSML application.
 * 
 * @author Li Song
 */
public class LsmlStreamHandlerFactory implements URLStreamHandlerFactory{

   @Override
   public URLStreamHandler createURLStreamHandler(String aProtocolName){
      if( aProtocolName.toLowerCase().equals("lsml") ){
         return new LsmlStreamHandler();
      }
      return null;
   }

}
