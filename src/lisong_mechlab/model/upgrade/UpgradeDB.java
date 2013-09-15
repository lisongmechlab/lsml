package lisong_mechlab.model.upgrade;

import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.model.mwo_parsing.ItemStatsXml;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsUpgradeType;

/**
 * A database class that holds all the {@link Upgrade}s parsed from the game files.
 * 
 * @author Emily Björk
 */
public class UpgradeDB{
   private static Map<Integer, Upgrade> id2upgrade;

   public static void initialize(){
      ItemStatsXml xml = ItemStatsXml.stats;
      id2upgrade = new TreeMap<Integer, Upgrade>();

      for(ItemStatsUpgradeType upgradeType : xml.UpgradeTypeList){
         UpgradeType type = UpgradeType.fromMwo(upgradeType.UpgradeTypeStats.type);
         switch( type ){
            case ARMOR:
               addUpgrade(new ArmorUpgrade(upgradeType));
               break;
            case GUIDANCE:
               addUpgrade(new GuidanceUpgrade(upgradeType));
               break;
            case HEATSINKS:
               addUpgrade(new HeatsinkUpgrade(upgradeType));
               break;
            case STRUCTURE:
               addUpgrade(new StructureUpgrade(upgradeType));
               break;
         }
      }
   }

   /**
    * Looks up an {@link Upgrade} by its MW:O ID.
    * 
    * @param aMwoId
    *           The ID to look up.
    * @return The {@link Upgrade} for the sought for ID.
    * @throws IllegalArgumentException
    *            Thrown if the ID is not a valid upgrade ID.
    */
   public static Upgrade lookup(int aMwoId) throws IllegalArgumentException{
      Upgrade ans = id2upgrade.get(aMwoId);
      if( null == ans ){
         throw new IllegalArgumentException("The ID: " + aMwoId + " is not a valid MWO upgrade ID!");
      }
      return ans;
   }

   private static void addUpgrade(Upgrade anUpgrade){
      id2upgrade.put(anUpgrade.getMwoId(), anUpgrade);
   }
}
