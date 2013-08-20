package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsLoc{
   @XStreamAsAttribute
   public String shortNameTag;
   @XStreamAsAttribute
   public int    iconTag;
   @XStreamAsAttribute
   public String descTag;
   @XStreamAsAttribute
   public String nameTag;
}