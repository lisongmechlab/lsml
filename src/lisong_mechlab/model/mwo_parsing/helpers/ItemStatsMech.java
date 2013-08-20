package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsMech{
   @XStreamAsAttribute
   public String mdf;
   @XStreamAsAttribute
   public String name;
   @XStreamAsAttribute
   public int    id;

   public ItemStatsLoc Loc;
}
