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
import lisong_mechlab.util.MessageXBar;

/**
 * Handles efficiencies for a mech.
 * 
 * @author Li Song
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
      public boolean isForMe(LoadoutBase<?> aLoadout){
         return aLoadout.getEfficiencies() == efficiencies;
      }

      @Override
      public boolean affectsHeatOrDamage(){
         return true;
      }
   }

   // Elite
   private boolean speedTweak;

   // Basic
   private boolean coolRun;
   private boolean heatContainment;
   private boolean anchorTurn;

   // Meta
   private boolean doubleBasics;

   private boolean fastfire;

   /**
    * Creates a new efficiencies object.
    */
   public Efficiencies(){}

   public Efficiencies(Efficiencies aEfficiencies){
      speedTweak = aEfficiencies.speedTweak;
      coolRun = aEfficiencies.coolRun;
      heatContainment = aEfficiencies.coolRun;
      anchorTurn = aEfficiencies.anchorTurn;
      doubleBasics = aEfficiencies.doubleBasics;
      fastfire = aEfficiencies.fastfire;
   }

   /**
    * @return <code>true</code> if speed tweak is enabled (10% faster movement speed).
    */
   public boolean hasSpeedTweak(){
      return speedTweak;
   }

   /**
    * @return <code>true</code> if anchor turn is enabled (10% faster turn speed).
    */
   public boolean hasAnchorTurn(){
      return anchorTurn;
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
    * @param xBar
    *           {@link MessageXBar} to signal changes on.
    */
   public void setSpeedTweak(boolean aSpeedTweak, MessageXBar xBar){
      if( aSpeedTweak != speedTweak ){
         speedTweak = aSpeedTweak;
         if( xBar != null )
            xBar.post(new Message(this, Message.Type.Changed));
      }
   }

   /**
    * Sets anchor turn status.
    * 
    * @param aAnchorTurn
    *           The value to set.
    * @param xBar
    *           {@link MessageXBar} to signal changes on.
    *           
    */
   public void setAnchorTurn(boolean aAnchorTurn, MessageXBar xBar){
      if( aAnchorTurn != anchorTurn ){
         anchorTurn = aAnchorTurn;
         if( xBar != null )
            xBar.post(new Message(this, Message.Type.Changed));
      }
   }

   /**
    * Sets cool run status.
    * 
    * @param aCoolRun
    *           The value to set.
    * @param xBar
    *           {@link MessageXBar} to signal changes on.
    */
   public void setCoolRun(boolean aCoolRun, MessageXBar xBar){
      if( aCoolRun != coolRun ){
         coolRun = aCoolRun;
         if( xBar != null )
            xBar.post(new Message(this, Message.Type.Changed));
      }
   }

   /**
    * Sets heat containment status.
    * 
    * @param aHeatContainment
    *           The value to set.
    * @param xBar
    *           {@link MessageXBar} to signal changes on.
    */
   public void setHeatContainment(boolean aHeatContainment, MessageXBar xBar){
      if( aHeatContainment != heatContainment ){
         heatContainment = aHeatContainment;
         if( xBar != null )
            xBar.post(new Message(this, Message.Type.Changed));
      }
   }

   /**
    * Sets double basics.
    * 
    * @param aDoubleBasics
    *           The value to set.
    * @param xBar
    *           {@link MessageXBar} to signal changes on.
    */
   public void setDoubleBasics(boolean aDoubleBasics, MessageXBar xBar){
      if( aDoubleBasics != doubleBasics ){
         doubleBasics = aDoubleBasics;
         if( xBar != null )
            xBar.post(new Message(this, Message.Type.Changed));
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
    * @return The modifier that should be applied to a 'mechs turn speed with the current efficiencies.
    */
   public double getTurnSpeedModifier(){
      if( anchorTurn ){
         if( doubleBasics ){
            return 1.2;
         }
         return 1.1;
      }
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
    * @param xBar
    *           {@link MessageXBar} to signal changes on.
    */
   public void setFastFire(boolean aFastFire, MessageXBar xBar){
      if( aFastFire != fastfire ){
         fastfire = aFastFire;
         if( xBar != null )
            xBar.post(new Message(this, Message.Type.Changed));
      }
   }
}
