package lisong_mechlab.model.chassi;

public enum HardpointType{
   ENERGY("E"), BALLISTIC("B"), MISSILE("M"), AMS("AMS"), NONE(""), ECM("ECM");

   private HardpointType(String aShortName){
      shortName = aShortName;
   }

   public String shortName(){
      return shortName;
   }

   public static HardpointType fromMwoType(int type){
      switch( type ){
         case 1:
            return HardpointType.ENERGY;
         case 4:
            return HardpointType.AMS;
         case 0:
            return HardpointType.BALLISTIC;
         case 2:
            return HardpointType.MISSILE;
         default:
            throw new RuntimeException("Unknown hardpoint type!");
      }
   }

   private final String shortName;
}
