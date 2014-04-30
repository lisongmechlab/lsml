/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */  
//@formatter:on
package lisong_mechlab.model.loadout.export;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.RenameOperation;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.LSML;

/**
 * This class handles data exchange with smurfy's website.
 * 
 * @author Li Song
 */
public class SmurfyImportExport{
   public final static String             CREATE_API_KEY_URL = "https://mwo.smurfy-net.de/change-password";
   private final String                   apiKey;
   private final URL                      userMechbayUrl;
   private final Base64LoadoutCoder       coder;
   private final static String            API_VALID_CHARS    = "0123456789abcdefABCDEF";
   private final static int               API_NUM_CHARS      = 40;
   private final transient OperationStack stack              = new OperationStack(0);
   private final SSLSocketFactory         sslSocketFactory;

   /**
    * @param aApiKey
    *           The API key to import or export for.
    * @param aCoder
    *           A {@link Base64LoadoutCoder} to use for encoding and decoding {@link Loadout}s.
    */
   public SmurfyImportExport(String aApiKey, Base64LoadoutCoder aCoder){
      if( aApiKey != null )
         apiKey = aApiKey.toLowerCase(); // It's case sensitive
      else
         apiKey = null;

      try{
         userMechbayUrl = new URL("https://mwo.smurfy-net.de/api/data/user/mechbay.xml");
         InputStream keyStoreStream = SmurfyImportExport.class.getResourceAsStream("/resources/lsml.jks");
         KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
         keyStore.load(keyStoreStream, "lsmllsml".toCharArray());

         TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
         tmf.init(keyStore);
         SSLContext ctx = SSLContext.getInstance("TLS");
         ctx.init(null, tmf.getTrustManagers(), null);
         sslSocketFactory = ctx.getSocketFactory();
      }
      catch( Exception e ){
         // Any exception thrown here is a bug, promote MalformedURLException to RuntimeException.
         throw new RuntimeException(e);
      }
      coder = aCoder;
   }

   /**
    * Checks if the given API key is a valid key.
    * 
    * @param aApiKey
    *           The API key to test.
    * @return <code>true</code> if the key is a valid key, false otherwise.
    */
   public static boolean isValidApiKey(String aApiKey){
      if( aApiKey.length() != API_NUM_CHARS )
         return false;
      int offset = 0;
      int len = aApiKey.length();
      while( offset < len ){
         int c = aApiKey.codePointAt(offset);
         offset += Character.charCount(c);

         if( -1 == API_VALID_CHARS.indexOf(c) ){
            return false;
         }
      }
      return true;
   }

   @SuppressWarnings("resource")
   // The resource is auto-closed with new try-resource statement
   public List<Loadout> listMechBay() throws DecodingException, IOException{
      List<Loadout> ans = new ArrayList<>();

      HttpURLConnection connection = connect(userMechbayUrl);
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Accept", "text/html;charset=UTF-8");
      connection.setRequestProperty("Accept-Charset", "UTF-8");
      connection.setRequestProperty("Authorization", "APIKEY " + apiKey);

      try( BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())) ){
         String line;
         Pattern namePattern = Pattern.compile("\\s*<name>.*CDATA\\[([^\\]]+).*");
         Pattern lsmlPattern = Pattern.compile("\\s*<lsml>.*CDATA\\[lsml://([^\\]]+).*");

         String name = null;
         while( null != (line = in.readLine()) ){
            Matcher nameMatcher = namePattern.matcher(line);
            Matcher lsmlMatcher = lsmlPattern.matcher(line);

            if( nameMatcher.matches() && name == null ){
               name = nameMatcher.group(1);
            }

            if( lsmlMatcher.matches() ){
               if( name == null )
                  throw new IOException("Found lsml without name!");
               String lsml = lsmlMatcher.group(1);
               Loadout loadout = coder.parse(lsml);
               stack.pushAndApply(new RenameOperation(loadout, null, name));
               ans.add(loadout);
               name = null;
            }
         }
      }
      return ans;
   }

   @SuppressWarnings("resource")
   // It is closed!
   public String sendLoadout(Loadout aLoadout) throws IOException{
      int mechId = aLoadout.getChassi().getMwoId();
      URL loadoutUploadUrlXml = new URL("https://mwo.smurfy-net.de/api/data/mechs/" + mechId + "/loadouts.xml");

      String data = SmurfyXML.toXml(aLoadout);
      byte[] rawData = data.getBytes(StandardCharsets.UTF_8);

      HttpURLConnection connection = connect(loadoutUploadUrlXml);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("User-Agent", "LSML/" + LSML.VERSION_STRING);
      connection.setRequestProperty("Accept-Charset", "UTF-8");
      connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
      connection.setRequestProperty("Content-Length", String.valueOf(rawData.length));

      connection.setDoOutput(true);

      try( OutputStream wr = connection.getOutputStream() ){
         wr.write(rawData);
      }

      try( BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream())) ){
         Pattern pattern = Pattern.compile(".*mwo.smurfy-net.de/api/data/mechs/" + mechId + "/loadouts/([^.]{40})\\..*");
         for(String line = rd.readLine(); line != null; line = rd.readLine()){
            Matcher m = pattern.matcher(line);
            if( m.matches() ){
               String sha = m.group(1);
               if( sha != null && sha.length() == 40 ){
                  return "http://mwo.smurfy-net.de/mechlab#i=" + mechId + "&l=" + sha;
               }
            }
         }
      }
      throw new IOException("Unable to determine uploaded URL... oops!");
   }

   private HttpURLConnection connect(URL aUrl) throws IOException{
      HttpsURLConnection connection = (HttpsURLConnection)aUrl.openConnection();
      connection.setSSLSocketFactory(sslSocketFactory);
      return connection;
   }
}
