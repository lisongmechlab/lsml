package lisong_mechlab.model.mwo_parsing;

import java.io.InputStream;
import java.util.List;

import lisong_mechlab.model.mwo_parsing.helpers.MdfComponent;
import lisong_mechlab.model.mwo_parsing.helpers.MdfInternal;
import lisong_mechlab.model.mwo_parsing.helpers.MdfMech;
import lisong_mechlab.model.mwo_parsing.helpers.MdfMovementTuning;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class MechDefinition{
   @XStreamAsAttribute
   public String HardpointPath;  
   public MdfMech Mech;
   public List<MdfComponent> ComponentList;
   
   public MdfMovementTuning MovementTuningConfiguration;
   
   public static MechDefinition fromXml(InputStream is){
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
      xstream.alias("MechDefinition", MechDefinition.class);
      xstream.alias("Mech", MdfMech.class);
      xstream.alias("Component", MdfComponent.class);
      xstream.alias("Internal", MdfInternal.class);
      xstream.alias("MovementTuningConfiguration", MdfMovementTuning.class);
      return (MechDefinition)xstream.fromXML(is);
   }  
   
//   public static void main(String[] arg) throws IOException{
//      GameDataFile dataFile = new GameDataFile();
//      MechDefinition mechDef = MechDefinition.fromXml(dataFile.openGameFile(new File(GameDataFile.MDF_ROOT, "jenner/jr7-d.mdf")));
//      System.out.println(mechDef);
//   }
}
