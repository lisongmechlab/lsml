package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsModuleStats{
   @XStreamAsAttribute
   public int            slots;
   @XStreamAsAttribute
   public double         tons;
   @XStreamAsAttribute
   public double         health;
}
