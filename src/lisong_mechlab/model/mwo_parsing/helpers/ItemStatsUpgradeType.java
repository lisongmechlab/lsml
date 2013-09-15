package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsUpgradeType extends ItemStats{

   public class UpgradeTypeStatsTag{
      @XStreamAsAttribute
      public int    type;
      @XStreamAsAttribute
      public int    slots;
      @XStreamAsAttribute
      public double pointMultiplier;
      @XStreamAsAttribute
      public int    associatedItem;

   }

   public UpgradeTypeStatsTag UpgradeTypeStats;

}
