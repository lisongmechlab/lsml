package lisong_mechlab.model.loadout.export;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class LsmlURLConnection extends URLConnection{
   String base64data;
   
   protected LsmlURLConnection(URL anUrl){
      super(anUrl);

      base64data = anUrl.getPath();
   }

   @Override
   public void connect() throws IOException{
      // No-Op
   }

   @Override
   public InputStream getInputStream() throws IOException{
      return new ByteArrayInputStream(base64data.getBytes("UTF-8"));
   }
}
