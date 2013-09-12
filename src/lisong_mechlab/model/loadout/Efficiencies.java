package lisong_mechlab.model.loadout;

import lisong_mechlab.model.loadout.Efficiencies.Message.Type;
import lisong_mechlab.util.MessageXBar;

public class Efficiencies{
   // TODO: Add module slots

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

      public final Efficiencies efficiencies;
      public final Type         type;
   }

   // Elite
   private boolean                     speedTweak;

   // Basic
   private boolean                     coolRun;
   private boolean                     heatContainment;

   // Meta
   private boolean                     doubleBasics;

   private transient final MessageXBar xBar;

   public Efficiencies(MessageXBar aXBar){
      xBar = aXBar;
   }

   public boolean hasSpeedTweak(){
      return speedTweak;
   }

   public boolean hasCoolRun(){
      return coolRun;
   }

   public boolean hasHeatContainment(){
      return heatContainment;
   }

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

   public void setSpeedTweak(boolean aSpeedTweak){
      if( aSpeedTweak != speedTweak ){
         speedTweak = aSpeedTweak;
         xBar.post(new Message(this, Type.Changed));
      }
   }

   public void setCoolRun(boolean aCoolRun){
      if( aCoolRun != coolRun ){
         coolRun = aCoolRun;
         xBar.post(new Message(this, Type.Changed));
      }
   }

   public void setHeatContainment(boolean aHeatContainment){
      if( aHeatContainment != heatContainment ){
         heatContainment = aHeatContainment;
         xBar.post(new Message(this, Type.Changed));
      }
   }

   public void setDoubleBasics(boolean aDoubleBasics){
      if( aDoubleBasics != doubleBasics ){
         doubleBasics = aDoubleBasics;
         xBar.post(new Message(this, Type.Changed));
      }
   }

   public double getHeatCapacityModifier(){
      if( heatContainment ){
         if( doubleBasics )
            return 1.2;
         return 1.1;
      }
      return 1.0;
   }

   public double getHeatDissipationModifier(){
      if( coolRun ){
         if( doubleBasics )
            return 1 + 0.075 * 2;
         return 1.075;
      }
      return 1.0;
   }

   public double getSpeedModifier(){
      if( speedTweak )
         return 1.1;
      return 1.0;
   }
}
