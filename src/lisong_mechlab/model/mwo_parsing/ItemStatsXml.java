package lisong_mechlab.model.mwo_parsing;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import lisong_mechlab.converter.GameDataFile;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsMech;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsModule;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This class models the format of ItemStats.xml from the game data files to facilitate easy parsing.
 * 
 * @author Li Song
 */
@XStreamAlias("ItemStats")
public class ItemStatsXml{
   public List<ItemStatsMech>     MechList;
   public List<ItemStatsWeapon>   WeaponList;
   public List<ItemStatsModule> ModuleList;

   public final static ItemStatsXml            stats;

   private ItemStatsXml(){
   }

   private static ItemStatsXml fromXml(InputStream is){
      XStream xstream = new XStream(new StaxDriver(new NoNameCoder())){
         @Override
         protected MapperWrapper wrapMapper(MapperWrapper next){
            return new MapperWrapper(next){
               @Override
               public boolean shouldSerializeMember(Class definedIn, String fieldName){
                  if( definedIn == Object.class ){
                     return false;
                  }
                  return super.shouldSerializeMember(definedIn, fieldName);
               }
            };
         }
      };
      xstream.autodetectAnnotations(true);
      xstream.alias("ItemStats", ItemStatsXml.class);
      xstream.alias("Mech", ItemStatsMech.class);
      xstream.alias("Weapon", ItemStatsWeapon.class);
      xstream.alias("Module", ItemStatsModule.class);
      
      // Fixes for broken XML from PGI
      xstream.aliasAttribute("Ctype", "CType");
      
      return (ItemStatsXml)xstream.fromXML(is);
   }

   static{
      try{
         stats = ItemStatsXml.fromXml(new GameDataFile().openGameFile(GameDataFile.ITEM_STATS_XML));
      }
      catch( IOException e ){
         throw new RuntimeException(e);
      }
   }
}
