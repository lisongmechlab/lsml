package lisong_mechlab.model.loadout.converters;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiDB;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ChassiConverter implements Converter{

   @Override
   public boolean canConvert(Class aClass){
      return Chassi.class == aClass;
   }

   @Override
   public void marshal(Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext){
      Chassi chassi = (Chassi)anObject;
      aWriter.setValue(chassi.getNameShort());
   }

   @Override
   public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext){
      String variation = aReader.getValue();
      return ChassiDB.lookup(variation);
   }

}
