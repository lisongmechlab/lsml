package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStats{
   @XStreamAsAttribute
   public String       name;
   @XStreamAsAttribute
   public String       id;
   public ItemStatsLoc Loc;
}
