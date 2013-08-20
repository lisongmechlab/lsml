package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class MdfInternal{
   @XStreamAsAttribute
   public int    Slots;
   @XStreamAsAttribute
   public String Name;
   @XStreamAsAttribute
   public String Desc;
}
