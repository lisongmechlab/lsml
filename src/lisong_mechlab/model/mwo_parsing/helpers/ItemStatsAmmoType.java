package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsAmmoType extends ItemStats{
   public class AmmoTypeStats{
      @XStreamAsAttribute
      public String type;
      @XStreamAsAttribute
      public int health;
      @XStreamAsAttribute
      public int    shotsPerTon;
      @XStreamAsAttribute
      public double    internalDamage;
   }

   public AmmoTypeStats AmmoTypeStats;
}
