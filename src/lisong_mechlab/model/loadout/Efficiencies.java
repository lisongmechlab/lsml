package lisong_mechlab.model.loadout;

import lisong_mechlab.model.loadout.Efficiencies.Message.Type;
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
}
