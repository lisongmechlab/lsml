package lisong_mechlab.model.loadout.converters;

import javax.swing.JOptionPane;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.LoadoutPart;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class LoadoutPartConverter implements Converter{

   private final Loadout loadout;

   public LoadoutPartConverter(Loadout aLoadout){
      loadout = aLoadout;
   }

   @Override
   public boolean canConvert(Class aClass){
      return LoadoutPart.class.isAssignableFrom(aClass);
   }

   @Override
   public void marshal(Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext){
      LoadoutPart part = (LoadoutPart)anObject;

      aWriter.addAttribute("part", part.getInternalPart().getType().toString());

      if( part.getInternalPart().getType().isTwoSided() ){
         aWriter.addAttribute("armor", part.getArmor(ArmorSide.FRONT) + "/" + part.getArmor(ArmorSide.BACK));
      }
      else{
         aWriter.addAttribute("armor", Integer.toString(part.getArmor(ArmorSide.ONLY)));
      }

      /*
       * if( part.getNumEngineHeatsinksMax() > 0 ){ aWriter.addAttribute("engineheatsinks",
       * Integer.toString(part.getNumEngineHeatsinks())); }
       */

      for(Item item : part.getItems()){
         if( item instanceof Internal ){
            continue;
         }
         aWriter.startNode("item");
         aContext.convertAnother(item);
         aWriter.endNode();
      }

   }

   @Override
   public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext){

      Part partType = Part.valueOf(aReader.getAttribute("part"));
      LoadoutPart loadoutPart = loadout.getPart(partType);

      try{
         if( partType.isTwoSided() ){
            String[] armors = aReader.getAttribute("armor").split("/");
            if( armors.length == 2 ){
               loadoutPart.setArmor(ArmorSide.FRONT, Integer.parseInt(armors[0]));
               loadoutPart.setArmor(ArmorSide.BACK, Integer.parseInt(armors[1]));
            }
         }
         else{
            loadoutPart.setArmor(ArmorSide.ONLY, Integer.parseInt(aReader.getAttribute("armor")));
         }
      }
      catch( IllegalArgumentException exception ){
         JOptionPane.showMessageDialog(null, "The loadout: " + loadout.getName() + " is corrupt. Continuing to load as much as possible.");
      }
      
      while( aReader.hasMoreChildren() ){
         aReader.moveDown();
         if( "item".equals(aReader.getNodeName()) ){
            try{
               loadoutPart.addItem((Item)aContext.convertAnother(null, Item.class));
            }
            catch( IllegalArgumentException exception ){
               JOptionPane.showMessageDialog(null, "The loadout: " + loadout.getName() + " is corrupt. Continuing to load as much as possible.");
            }
         }
         aReader.moveUp();
      }
      return null; // We address directly into the given loadout, this is a trap.
   }

}
