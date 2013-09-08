package lisong_mechlab.model.loadout.converters;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ItemConverter implements Converter{

   @Override
   public boolean canConvert(Class aClass){
      return Item.class.isAssignableFrom(aClass);
   }

   @Override
   public void marshal(Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext){
      Item item = (Item)anObject;
      int mwoIdx = item.getMwoIdx();
      aWriter.setValue(Integer.valueOf(mwoIdx).toString());
   }

   @Override
   public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext){
      int mwoidx = Integer.parseInt(aReader.getValue());
      return ItemDB.lookup(mwoidx);
   }

}
