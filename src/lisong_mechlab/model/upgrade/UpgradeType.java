package lisong_mechlab.model.upgrade;

public enum UpgradeType{
   ARMOR(0), STRUCTURE(1), HEATSINKS(2), GUIDANCE(3);

   private final int mwoType;

   private UpgradeType(int aMwoType){
      mwoType = aMwoType;
   }

   public static UpgradeType fromMwo(int aMwoType){
      if( aMwoType == ARMOR.mwoType ){
         return ARMOR;
      }
      else if( aMwoType == STRUCTURE.mwoType ){
         return STRUCTURE;
      }
      else if( aMwoType == HEATSINKS.mwoType ){
         return HEATSINKS;
      }
      else if( aMwoType == GUIDANCE.mwoType ){
         return GUIDANCE;
      }
      else{
         throw new IllegalArgumentException("Invalid upgrade type! " + aMwoType);
      }
   }
}
