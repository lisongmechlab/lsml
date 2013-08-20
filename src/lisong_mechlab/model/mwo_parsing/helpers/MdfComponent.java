package lisong_mechlab.model.mwo_parsing.helpers;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class MdfComponent{
   public class Hardpoint{
      @XStreamAsAttribute
      public int ID;
      @XStreamAsAttribute
      public int Type;
      @XStreamAsAttribute
      public int Slots;
   }
   
   @XStreamAsAttribute
   public String Name;
   @XStreamAsAttribute
   public int Slots;
   @XStreamAsAttribute
   public double HP;
   @XStreamAsAttribute
   public int CanEquipECM;
   
   @XStreamImplicit(itemFieldName="Internal")
   public List<MdfInternal> internals;
   
   @XStreamImplicit(itemFieldName="Hardpoint")
   public List<Hardpoint> hardpoints;
}
