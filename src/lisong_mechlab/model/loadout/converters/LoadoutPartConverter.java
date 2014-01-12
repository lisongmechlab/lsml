/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */  
//@formatter:on
package lisong_mechlab.model.loadout.converters;

import javax.swing.JOptionPane;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.LoadoutPart;
import lisong_mechlab.view.ProgramInit;

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
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "The loadout: " + loadout.getName()
                                                           + " is corrupt. Continuing to load as much as possible.");
      }

      while( aReader.hasMoreChildren() ){
         aReader.moveDown();
         if( "item".equals(aReader.getNodeName()) ){
            try{
               loadoutPart.addItem((Item)aContext.convertAnother(null, Item.class), false);
            }
            catch( IllegalArgumentException exception ){
               JOptionPane.showMessageDialog(ProgramInit.lsml(), "The loadout: " + loadout.getName()
                                                                 + " is corrupt. Continuing to load as much as possible.");
            }
         }
         aReader.moveUp();
      }
      return null; // We address directly into the given loadout, this is a trap.
   }

}
