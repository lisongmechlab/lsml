/*
 * @formatter:off
 * Li Song Mech Lab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
package lisong_mechlab.view.preferences;

import java.util.Map;

/**
 * This class manages everything to do with settings fonts for the application.
 * 
 * @author Emily Björk
 */
public class FontPreferences{
   public enum FontSize{
      VerySmall(0.6), Small(0.8), Normal(1.0), Large(1.2), VeryLarge(1.4);

      private FontSize(double aFactor){
         factor = aFactor;
      }

      private double factor;

      public double getSizeFactor(){
         return factor;
      }
   }

   public static final String FONTSIZE_KEY = "fontSize";
   private FontSize           fontSize;

   public FontPreferences(){
      fontSize = FontSize.valueOf(PreferenceStore.getString(FONTSIZE_KEY, FontSize.Normal.name()));
      updateFonts();
   }

   /**
    * Sets the fonts for the application. This must be called before the main frame has opened.
    */
   public void updateFonts(){
      for(Map.Entry<Object, Object> entry : javax.swing.UIManager.getDefaults().entrySet()){
         Object key = entry.getKey();
         Object value = javax.swing.UIManager.get(key);
         if( value != null && value instanceof javax.swing.plaf.FontUIResource ){
            javax.swing.plaf.FontUIResource fr = (javax.swing.plaf.FontUIResource)value;
            javax.swing.plaf.FontUIResource f = new javax.swing.plaf.FontUIResource(fr.getFamily(), fr.getStyle(),
                                                                                    (int)(fr.getSize() * fontSize.getSizeFactor()));
            javax.swing.UIManager.put(key, f);
         }
      }
   }

   /**
    * Stores the font size into the settings file but doesn't update the font in the application.
    * 
    * @param aFontSize
    *           The {@link FontSize} to use.
    */
   public void setFontSize(FontSize aFontSize){
      fontSize = aFontSize;
      PreferenceStore.setString(FONTSIZE_KEY, fontSize.name());
   }

   /**
    * @return The currently stored {@link FontSize}. Which may be different from the currently displayed fontsize.
    */
   public FontSize getFontSize(){
      return fontSize;
   }
}
