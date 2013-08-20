package lisong_mechlab.view;

import java.util.prefs.Preferences;

public class LsmlPreferences{
   public static final String GARAGEFILE_KEY = "garagefile";
   public static final String GARAGEFILE_DEFAULT = "garage.xml";
   static private Preferences prefs = Preferences.userRoot().node(LsmlPreferences.class.getName());

   static public String getString(String key){
      return prefs.get(key, "");
   }

   static public void setString(String key, String value){
      prefs.put(key, value);
   }
}
