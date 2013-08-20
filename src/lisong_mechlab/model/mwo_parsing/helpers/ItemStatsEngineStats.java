package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsEngineStats{
   @XStreamAsAttribute
   public int    slots;
   @XStreamAsAttribute
   public int    rating;
   @XStreamAsAttribute
   public double weight;
   @XStreamAsAttribute
   public int    type;
   @XStreamAsAttribute
   public int    heatsinks;
   @XStreamAsAttribute
   public int    health;
}
