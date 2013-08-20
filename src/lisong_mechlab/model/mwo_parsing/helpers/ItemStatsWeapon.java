package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsWeapon extends ItemStats{

   public static class WeaponStatsTag{
      @XStreamAsAttribute
      public double speed;
      @XStreamAsAttribute
      public double volleydelay;
      @XStreamAsAttribute
      public double duration;
      @XStreamAsAttribute
      public double tons;
      @XStreamAsAttribute
      public double maxRange;
      @XStreamAsAttribute
      public double longRange;
      @XStreamAsAttribute
      public double minRange;
      @XStreamAsAttribute
      public int    ammoPerShot;
      @XStreamAsAttribute
      public String ammoType;
      @XStreamAsAttribute
      public double cooldown;
      @XStreamAsAttribute
      public double heat;
      @XStreamAsAttribute
      public double impulse;
      @XStreamAsAttribute
      public double heatdamage;
      @XStreamAsAttribute
      public double damage;
      @XStreamAsAttribute
      public int    numFiring;
      @XStreamAsAttribute
      public String projectileclass;
      @XStreamAsAttribute
      public int    type;
      @XStreamAsAttribute
      public int    slots;
      @XStreamAsAttribute
      public int    Health;
      @XStreamAsAttribute
      public String artemisAmmoType;
   }

   public WeaponStatsTag WeaponStats;
}
