package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStats{
   @XStreamAsAttribute
   public String       name;
   @XStreamAsAttribute
   public int          id;
   public ItemStatsLoc Loc;
}
