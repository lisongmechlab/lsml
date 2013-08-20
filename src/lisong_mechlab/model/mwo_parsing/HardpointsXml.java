package lisong_mechlab.model.mwo_parsing;

import java.io.InputStream;
import java.util.List;

import lisong_mechlab.model.mwo_parsing.helpers.HardPointInfo;
import lisong_mechlab.model.mwo_parsing.helpers.HardPointWeaponSlot;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class HardpointsXml{
   @XStreamImplicit(itemFieldName = "Hardpoint")
   public List<HardPointInfo> hardpoints;

   public static HardpointsXml fromXml(InputStream is){
      XStream xstream = new XStream(new StaxDriver()){
         @Override
         protected MapperWrapper wrapMapper(MapperWrapper next){
            return new MapperWrapper(next){
               @Override
               public boolean shouldSerializeMember(@SuppressWarnings("rawtypes") Class definedIn, String fieldName){
                  if( definedIn == Object.class ){
                     return false;
                  }
                  return super.shouldSerializeMember(definedIn, fieldName);
               }
            };
         }
      };
      xstream.autodetectAnnotations(true);
      xstream.alias("Hardpoints", HardpointsXml.class);
      xstream.alias("HardPoint", HardPointInfo.class);
      xstream.alias("WeaponSlot", HardPointWeaponSlot.class);
      return (HardpointsXml)xstream.fromXML(is);
   }

   public int slotsForId(int aID){
      for(HardPointInfo hardPointInfo : hardpoints){
         if(hardPointInfo.id == aID){
            return hardPointInfo.weaponslots.size();
         }
      }
      throw new RuntimeException("Problem reading hardpoint info!");
   }

//   public static void main(String[] arg) throws IOException{
//      GameDataFile dataFile = new GameDataFile();
//      HardpointsXml mechDef = HardpointsXml.fromXml(dataFile.openGameFile(new File(GameDataFile.MDF_ROOT, "jenner/jenner-hardpoints.xml")));
//      System.out.println(mechDef);
//   }
}
