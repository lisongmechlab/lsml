package lisong_mechlab.model.chassi;

/**
 * Represents a hard point on a chassis.
 * 
 * @author Emily Björk
 */
public class Hardpoint{
   final int           tubes;
   final HardpointType type;

   public Hardpoint(HardpointType aType){
      this(aType, 0);
   }

   public Hardpoint(HardpointType aType, int aNumTubes){
      if( aType == HardpointType.MISSILE ){
         throw new IllegalArgumentException("Missile hard points must have a positive, non-zero number of tubes");
      }
      type = aType;
      tubes = aNumTubes;
   }

   /**
    * @return The type of this hardpoint.
    */
   public HardpointType getType(){
      return type;
   }

   /**
    * @return The number of missile tubes this hardpoint has.
    */
   public int getNumMissileTubes(){
      return tubes;
   }
}
