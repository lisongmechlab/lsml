package lisong_mechlab.model.loadout;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.loadout.Efficiencies.Message.Type;

public class Efficiencies{
   // TODO: Add module slots

   public static class Message implements MessageXBar.Message{
      @Override
      public boolean equals(Object obj){
         if( obj instanceof Message){
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
      };

      public final Efficiencies efficiencies;
      public final Type         type;
   }

   // Elite
   private boolean speedTweak;

   // Basic
   private boolean coolRun;
   private boolean heatContainment;

   // Meta
   private boolean doubleBasics;

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
            return 1 + 0.075 * 2;
         return 1.075;
      }
      return 1.0;
   }

   public double getHeatDissapationModifier(){
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
