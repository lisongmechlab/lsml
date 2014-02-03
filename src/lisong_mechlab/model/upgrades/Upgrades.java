/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
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
package lisong_mechlab.model.upgrades;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.MessageXBar;

public class Upgrades{
   private boolean artemis;
   private boolean ferroFibrous;
   private boolean endoSteel;
   private boolean dhs;

   public static class Message implements MessageXBar.Message{
      public final ChangeMsg msg;
      private final Upgrades source;

      public enum ChangeMsg{
         GUIDANCE, STRUCTURE, ARMOR, HEATSINKS
      }

      @Override
      public boolean equals(Object obj){
         if( obj instanceof Message ){
            Message other = (Message)obj;
            return msg == other.msg && source == other.source;
         }
         return false;
      }

      Message(ChangeMsg aChangeMsg, Upgrades anUpgrades){
         msg = aChangeMsg;
         source = anUpgrades;
      }

      @Override
      public boolean isForMe(Loadout aLoadout){
         return aLoadout.getUpgrades() == source;
      }
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + (artemis ? 1231 : 1237);
      result = prime * result + (dhs ? 1231 : 1237);
      result = prime * result + (endoSteel ? 1231 : 1237);
      result = prime * result + (ferroFibrous ? 1231 : 1237);
      return result;
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( obj == null )
         return false;
      if( !(obj instanceof Upgrades) )
         return false;
      Upgrades other = (Upgrades)obj;
      if( artemis != other.artemis )
         return false;
      if( dhs != other.dhs )
         return false;
      if( endoSteel != other.endoSteel )
         return false;
      if( ferroFibrous != other.ferroFibrous )
         return false;
      return true;
   }

   public boolean hasArtemis(){
      return artemis;
   }

   public boolean hasDoubleHeatSinks(){
      return dhs;
   }

   public boolean hasEndoSteel(){
      return endoSteel;
   }

   public boolean hasFerroFibrous(){
      return ferroFibrous;
   }

   void setArtemis(boolean anArtemis){
      artemis = anArtemis;
   }

   void setEndo(boolean anEndo){
      endoSteel = anEndo;
   }

   void setFerro(boolean aFerro){
      ferroFibrous = aFerro;
   }

   void setDHS(boolean aDHS){
      dhs = aDHS;
   }
}
