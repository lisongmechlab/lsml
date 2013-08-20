package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class MdfMovementTuning{
   @XStreamAsAttribute
   public String MovementArchetype = "Huge";
   @XStreamAsAttribute
   public double MaxMovementSpeed;
   @XStreamAsAttribute
   public double TorsoTurnSpeedYaw;
   @XStreamAsAttribute
   public double TorsoTurnSpeedPitch;
   @XStreamAsAttribute
   public double ArmTurnSpeedYaw;
   @XStreamAsAttribute
   public double ArmTurnSpeedPitch;
   @XStreamAsAttribute
   public double MaxTorsoAngleYaw;
   @XStreamAsAttribute
   public double MaxTorsoAnglePitch;
   @XStreamAsAttribute
   public double MaxArmRotationYaw;
   @XStreamAsAttribute
   public double MaxArmRotationPitch;

}
