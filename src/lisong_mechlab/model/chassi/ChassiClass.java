package lisong_mechlab.model.chassi;

public enum ChassiClass{
   LIGHT(1.4), MEDIUM(1.3), HEAVY(1.2), ASSAULT(1.2);

   ChassiClass(double aMultiplier){
      multiplier = aMultiplier;
   }

   public static ChassiClass fromMaxTons(double tons){
      if( tons <= 35 ){
         return ChassiClass.LIGHT;
      }
      else if( tons <= 55 ){
         return ChassiClass.MEDIUM;
      }
      else if( tons <= 70 ){
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
