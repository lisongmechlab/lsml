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
package lisong_mechlab.view.preferences;

/**
 * This class contains preferences related to the UI behavior.
 * 
 * @author Li Song
 */
public class UiPreferences{
   private static final String UI_USE_SMARTPLACE = "uiUseSmartPlace";
   private static final String UI_COMPACT_MODE = "uiCompactMode";
   
   public void setCompactMode(boolean aValue){
      PreferenceStore.setString(UI_COMPACT_MODE, Boolean.toString(aValue));
   }
   
   public void setUseSmartPlace(boolean aValue){
      PreferenceStore.setString(UI_USE_SMARTPLACE, Boolean.toString(aValue));
   }

   /**
    * @return <code>true</code> if the user has opted to use smart place.
    */
   public boolean getUseSmartPlace(){
      return Boolean.parseBoolean(PreferenceStore.getString(UI_USE_SMARTPLACE, "false"));
   }
   
   public boolean getCompactMode(){
      return Boolean.parseBoolean(PreferenceStore.getString(UI_COMPACT_MODE, "false"));
   }
}
