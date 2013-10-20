package lisong_mechlab.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.swing.JOptionPane;

public class LsmlPreferences{
   public static final String      GARAGEFILE_KEY     = "garagefile";
   public static final String      GARAGEFILE_DEFAULT = "garage.xml";
   private static final File       propertiesFile;
   private static final Properties properties;

   static public String getString(String key){
      return properties.getProperty(key, "");
   }
   
   static public String getString(String key, String aDefault){
      return properties.getProperty(key, aDefault);
   }

   static public void setString(String key, String value){
      properties.setProperty(key, value);

      FileOutputStream outputStream = null;
      try{
         outputStream = new FileOutputStream(propertiesFile);
         properties.storeToXML(outputStream, "Written by LSML");
      }
      catch( FileNotFoundException e ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "Program settings file not found! :" + e);
      }
      catch( IOException e ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "Unspecified IO error while writing program settings file! :" + e);
      }
      finally{
         if( outputStream != null ){
            try{
               outputStream.close();
            }
            catch( IOException e ){
               JOptionPane.showMessageDialog(ProgramInit.lsml(), "Error closing program settings file! :" + e);
            }
         }
      }
   }

   static{
      if(System.getProperties().getProperty("os.name").toLowerCase().contains("win")){
         propertiesFile = new File(System.getenv("AppData") + "/lsml_settings.xml");
      }
      else{
         propertiesFile = new File(System.getProperty("user.home") + "/.lsml.xml");
      }
      
      properties = new Properties();
      if( propertiesFile.exists() ){
         FileInputStream inputStream = null;
         try{
            inputStream = new FileInputStream(propertiesFile);

            properties.loadFromXML(inputStream);
         }
         catch( FileNotFoundException e ){
            JOptionPane.showMessageDialog(ProgramInit.lsml(), "Program settings file not found! :" + e);
         }
         catch( InvalidPropertiesFormatException e ){
            JOptionPane.showMessageDialog(ProgramInit.lsml(), "Program settings file is corrupt! :" + e);
         }
         catch( IOException e ){
            JOptionPane.showMessageDialog(ProgramInit.lsml(), "Unspecified IO error while reading program settings file! :" + e);
         }
         finally{
            if( inputStream != null )
               try{
                  inputStream.close();
               }
               catch( IOException e ){
                  JOptionPane.showMessageDialog(ProgramInit.lsml(), "Error closing program settings file! :" + e);
               }
         }
      }
   }
}
