package lisong_mechlab.model.upgrade;

import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.model.mwo_parsing.ItemStatsXml;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsUpgradeType;

/**
 * A database class that holds all the {@link Upgrade}s parsed from the game files.
 * 
 * @author Li Song
 */
public class UpgradeDB{
   public static final ArmorUpgrade     STANDARD_ARMOR;
   public static final StructureUpgrade STANDARD_STRUCTURE;
   public static final GuidanceUpgrade  STANDARD_GUIDANCE;
   public static final HeatsinkUpgrade  STANDARD_HEATSINNKS;
   private static Map<Integer, Upgrade> id2upgrade;

   public static void initialize(){
      ItemStatsXml xml = ItemStatsXml.stats;
      id2upgrade = new TreeMap<Integer, Upgrade>();

      for(ItemStatsUpgradeType upgradeType : xml.UpgradeTypeList){
         UpgradeType type = UpgradeType.fromMwo(upgradeType.UpgradeTypeStats.type);
         switch( type ){
            case ARMOR:
               addUpgrade(new ArmorUpgrade(upgradeType), upgradeType);
               break;
            case GUIDANCE:
               addUpgrade(new GuidanceUpgrade(upgradeType), upgradeType);
               break;
            case HEATSINKS:
               addUpgrade(new HeatsinkUpgrade(upgradeType), upgradeType);
               break;
            case STRUCTURE:
               addUpgrade(new StructureUpgrade(upgradeType), upgradeType);
               break;
         }
      }
   }

   static{
      initialize();
      STANDARD_ARMOR = (ArmorUpgrade)lookup(2810);
      STANDARD_STRUCTURE = (StructureUpgrade)lookup(3100);
      STANDARD_GUIDANCE = (GuidanceUpgrade)lookup(3003);
      STANDARD_HEATSINNKS = (HeatsinkUpgrade)lookup(3051);
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

   private static void addUpgrade(Upgrade anUpgrade, ItemStatsUpgradeType anUpgradeType){
      id2upgrade.put(anUpgrade.getMwoId(), anUpgrade);
      if( anUpgradeType.UpgradeTypeStats.associatedItem > 0 )
         id2upgrade.put(anUpgradeType.UpgradeTypeStats.associatedItem, anUpgrade);
   }
}
