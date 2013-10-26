package lisong_mechlab.model.mwo_parsing.helpers;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class HardPointWeaponSlot{
   public static class Attachment{
      @XStreamAsAttribute
      public String AName;
      
      @XStreamAsAttribute
      public String search;
   }
   
   @XStreamImplicit(itemFieldName = "Attachment")
   public List<Attachment> attachments;
}
