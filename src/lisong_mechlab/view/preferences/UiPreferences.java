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

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.MessageXBar;

/**
 * This class contains preferences related to the UI behavior.
 * 
 * @author Li Song
 */
public class UiPreferences{
   /**
    * This message is sent over the {@link MessageXBar} when a UI preference has been changed.
    * 
    * @author Li Song
    */
   public class Message implements MessageXBar.Message{
      public final String attribute;

      Message(String aAttribute){
         attribute = aAttribute;
      }

      @Override
      public boolean isForMe(Loadout aLoadout){
         return false;
      }

      @Override
      public boolean affectsHeatOrDamage(){
         return false;
      }
   }

   public static final String          UI_USE_SMARTPLACE     = "uiUseSmartPlace";
   public static final String          UI_COMPACT_MODE       = "uiCompactMode";
   public static final String          UI_HIDE_SPECIAL_MECHS = "uiHideSpecialMechs";
   private final transient MessageXBar xBar;

   public UiPreferences(MessageXBar aXBar){
      xBar = aXBar;
   }

   public void setCompactMode(boolean aValue){
      PreferenceStore.setString(UI_COMPACT_MODE, Boolean.toString(aValue));
      if( xBar != null )
         xBar.post(new Message(UI_COMPACT_MODE));
   }

   public void setUseSmartPlace(boolean aValue){
      PreferenceStore.setString(UI_USE_SMARTPLACE, Boolean.toString(aValue));
      if( xBar != null )
         xBar.post(new Message(UI_USE_SMARTPLACE));
   }

   public void setHideSpecialMechs(boolean aValue){
      PreferenceStore.setString(UI_HIDE_SPECIAL_MECHS, Boolean.toString(aValue));
      if( xBar != null )
         xBar.post(new Message(UI_HIDE_SPECIAL_MECHS));
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

   public boolean getHideSpecialMechs(){
      return Boolean.parseBoolean(PreferenceStore.getString(UI_HIDE_SPECIAL_MECHS, "true"));
   }
}
