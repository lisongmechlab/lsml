package lisong_mechlab.model.upgrade;

import lisong_mechlab.model.mwo_parsing.Localization;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsUpgradeType;

/**
 * Base class for all upgrades for 'mechs.
 * 
 * @author Li Song
 */
public class Upgrade{
   private final String name;
   private final int    mwoId;
   private final String description;

   public Upgrade(ItemStatsUpgradeType aUpgradeType){
      name = Localization.key2string(aUpgradeType.Loc.nameTag);
      description = Localization.key2string(aUpgradeType.Loc.descTag);
      mwoId = aUpgradeType.id;
   }

   /**
    * @return The localized name of the upgrade.
    */
   public String getName(){
      return name;
   }

   /**
    * @return The MW:O ID for the upgrade.
    */
   public int getMwoId(){
      return mwoId;
   }

   /**
    * @return The MW:O description of the upgrade.
    */
   public String getDescription(){
      return description;
   }
}
