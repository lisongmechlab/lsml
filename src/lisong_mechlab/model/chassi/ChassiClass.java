package lisong_mechlab.model.chassi;

public enum ChassiClass{
   LIGHT(1.4), MEDIUM(1.3), HEAVY(1.2), ASSAULT(1.2);

   private final static double TONNAGE_EPSILON = Math.ulp(100) * 5.0;

   ChassiClass(double aMultiplier){
      multiplier = aMultiplier;
   }

   public static ChassiClass fromMaxTons(double tons){

      if( tons < 40 - TONNAGE_EPSILON ){
         return ChassiClass.LIGHT;
      }
      else if( tons < 60 - TONNAGE_EPSILON ){
         return ChassiClass.MEDIUM;
      }
      else if( tons < 80 - TONNAGE_EPSILON ){
         return ChassiClass.HEAVY;
      }
      else{
         return ChassiClass.ASSAULT;
      }
   }

   public double EngineMultiplier(){
      return multiplier;
   }

   private final double multiplier;
}
