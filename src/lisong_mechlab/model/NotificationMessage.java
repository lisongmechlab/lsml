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
package lisong_mechlab.model;

import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.util.MessageXBar.Message;

/**
 * This class carries notifications from the model to the UI.
 * 
 * @author Li Song
 */
public class NotificationMessage implements Message{
   public enum Severity{
      NOTICE, WARNING, ERROR
   }

   private final LoadoutBase<?, ?> loadout;
   public final String             message;
   public final Severity           severity;

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( obj == null )
         return false;
      if( !(obj instanceof NotificationMessage) )
         return false;
      NotificationMessage other = (NotificationMessage)obj;
      if( loadout == null ){
         if( other.loadout != null )
            return false;
      }
      else if( !loadout.equals(other.loadout) )
         return false;
      if( message == null ){
         if( other.message != null )
            return false;
      }
      else if( !message.equals(other.message) )
         return false;
      if( severity != other.severity )
         return false;
      return true;
   }

   /**
    * Creates a new {@link NotificationMessage}.
    * 
    * @param aSeverity
    *           The {@link Severity} of the message.
    * @param aLoadout
    *           The {@link LoadoutStandard} the message is for.
    * @param aMessage
    *           The human readable message.
    */
   public NotificationMessage(Severity aSeverity, LoadoutBase<?, ?> aLoadout, String aMessage){
      loadout = aLoadout;
      severity = aSeverity;
      message = aMessage;
   }

   @Override
   public boolean isForMe(LoadoutBase<?,?> aLoadout){
      return loadout == aLoadout;
   }

   @Override
   public boolean affectsHeatOrDamage(){
      return false;
   }
}
