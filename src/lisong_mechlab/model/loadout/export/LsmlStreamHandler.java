package lisong_mechlab.model.loadout.export;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class LsmlStreamHandler extends URLStreamHandler{

   @Override
   protected URLConnection openConnection(URL anUrl) throws IOException{
      return new LsmlURLConnection(anUrl);
   }

}
