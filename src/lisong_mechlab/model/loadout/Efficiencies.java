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
package lisong_mechlab.model.loadout;

import lisong_mechlab.model.loadout.Efficiencies.Message.Type;
import lisong_mechlab.util.MessageXBar;

/**
 * Handles efficiencies for a mech.
 * 
 * @author Emily Björk
 */
public class Efficiencies{

   public static class Message implements MessageXBar.Message{
      @Override
      public boolean equals(Object obj){
         if( obj instanceof Message ){
            Message other = (Message)obj;
            return efficiencies == other.efficiencies && type == other.type;
         }
         return false;
      }

      public Message(Efficiencies aEfficiencies, Type aType){
         efficiencies = aEfficiencies;
         type = aType;
      }

      enum Type{
         Changed
      }

      private final Efficiencies efficiencies;
      public final Type          type;

      @Override
      public boolean isForMe(Loadout aLoadout){
         return aLoadout.getEfficiencies() == efficiencies;
      }
   }

   // Elite
   private boolean                     speedTweak;

   // Basic
   private boolean                     coolRun;
   private boolean                     heatContainment;

   // Meta
   private boolean                     doubleBasics;

   private transient final MessageXBar xBar;

   private boolean                     fastfire;

   /**
    * Creates a new efficiencies object.
    * 
    * @param aXBar
    *           The {@link MessageXBar} on which messages about changes to the efficiencies are to be sent.
    */
   public Efficiencies(MessageXBar aXBar){
      xBar = aXBar;
   }

   /**
    * @return <code>true</code> if speed tweak is enabled (10% faster movement speed).
    */
   public boolean hasSpeedTweak(){
      return speedTweak;
   }

   /**
    * @return <code>true</code> if cool run is enabled (7.5% more heat dissipation).
    */
   public boolean hasCoolRun(){
      return coolRun;
   }

   /**
    * @return <code>true</code> if heat containment is enabled (10% more heat capactity).
    */
   public boolean hasHeatContainment(){
      return heatContainment;
   }

   /**
    * @return <code>true</code> if all elite skills are unlocked. Effectiveness of cool run and heat containment is
    *         doubled.
    */
   public boolean hasDoubleBasics(){
      return doubleBasics;
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + (coolRun ? 1231 : 1237);
      result = prime * result + (doubleBasics ? 1231 : 1237);
      result = prime * result + (heatContainment ? 1231 : 1237);
      result = prime * result + (speedTweak ? 1231 : 1237);
      return result;
   }

   @Override
   public boolean equals(Object obj){
      if( this == obj )
         return true;
      if( obj == null )
         return false;
      if( !(obj instanceof Efficiencies) )
         return false;
      Efficiencies other = (Efficiencies)obj;
      if( coolRun != other.coolRun )
         return false;
      if( doubleBasics != other.doubleBasics )
         return false;
      if( heatContainment != other.heatContainment )
         return false;
      if( speedTweak != other.speedTweak )
         return false;
      return true;
   }

   /**
    * Sets speed tweak status.
    * 
    * @param aSpeedTweak
    *           The value to set.
    */
   public void setSpeedTweak(boolean aSpeedTweak){
      if( aSpeedTweak != speedTweak ){
         speedTweak = aSpeedTweak;
         xBar.post(new Message(this, Type.Changed));
      }
   }

   /**
    * Sets cool run status.
    * 
    * @param aCoolRun
    *           The value to set.
    */
   public void setCoolRun(boolean aCoolRun){
      if( aCoolRun != coolRun ){
         coolRun = aCoolRun;
         xBar.post(new Message(this, Type.Changed));
      }
   }

   /**
    * Sets heat containment status.
    * 
    * @param aHeatContainment
    *           The value to set.
    */
   public void setHeatContainment(boolean aHeatContainment){
      if( aHeatContainment != heatContainment ){
         heatContainment = aHeatContainment;
         xBar.post(new Message(this, Type.Changed));
      }
   }

   /**
    * Sets double basics.
    * 
    * @param aDoubleBasics
    *           The value to set.
    */
   public void setDoubleBasics(boolean aDoubleBasics){
      if( aDoubleBasics != doubleBasics ){
         doubleBasics = aDoubleBasics;
         xBar.post(new Message(this, Type.Changed));
      }
   }

   /**
    * @return The modifier that should be applied to a 'mechs heat capacity with the current efficiencies.
    */
   public double getHeatCapacityModifier(){
      if( heatContainment ){
         if( doubleBasics )
            return 1.2;
         return 1.1;
      }
      return 1.0;
   }

   /**
    * @return The modifier that should be applied to a 'mechs heat dissipation with the current efficiencies.
    */
   public double getHeatDissipationModifier(){
      if( coolRun ){
         if( doubleBasics )
            return 1.15;
         return 1.075;
      }
      return 1.0;
   }

   /**
    * @return The modifier that should be applied to a 'mechs top speed with the current efficiencies.
    */
   public double getSpeedModifier(){
      if( speedTweak )
         return 1.1;
      return 1.0;
   }

   /**
    * @return <code>true</code> if the fast fire efficiency is enabled.
    */
   public boolean hasFastFire(){
      return fastfire;
   }

   /**
    * @return The modifier to be applied to weapon recycle times given the current status of the fast fire efficiency.
    */
   public double getWeaponCycleTimeModifier(){
      if( fastfire )
         return 0.95;
      return 1.0;
   }

   /**
    * Sets the status of the fast fire efficiency.
    * 
    * @param aFastFire
    *           The new status of the fast fire efficiency.
    */
   public void setFastFire(boolean aFastFire){
      if( aFastFire != fastfire ){
         fastfire = aFastFire;
         xBar.post(new Message(this, Type.Changed));
      }
   }
}
