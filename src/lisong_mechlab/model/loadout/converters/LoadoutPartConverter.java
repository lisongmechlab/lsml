/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.export.CompatibilityHelper;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.SetArmorOperation;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.ProgramInit;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class LoadoutPartConverter implements Converter{

   private final Loadout     loadout;
   private final MessageXBar xBar;

   public LoadoutPartConverter(MessageXBar anXBar, Loadout aLoadout){
      loadout = aLoadout;
      xBar = anXBar;
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

      OperationStack operationStack = new OperationStack(0);

      Part partType = Part.valueOf(aReader.getAttribute("part"));
      LoadoutPart loadoutPart = loadout.getPart(partType);

      try{
         if( partType.isTwoSided() ){
            String[] armors = aReader.getAttribute("armor").split("/");
            if( armors.length == 2 ){
               operationStack.pushAndApply(new SetArmorOperation(xBar, loadoutPart, ArmorSide.FRONT, Integer.parseInt(armors[0])));
               operationStack.pushAndApply(new SetArmorOperation(xBar, loadoutPart, ArmorSide.BACK, Integer.parseInt(armors[1])));
            }
         }
         else{
            operationStack.pushAndApply(new SetArmorOperation(xBar, loadoutPart, ArmorSide.ONLY, Integer.parseInt(aReader.getAttribute("armor"))));
         }
      }
      catch( IllegalArgumentException exception ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "The loadout: " + loadout.getName()
                                                           + " is corrupt. Continuing to load as much as possible.");
      }

      List<Item> later = new ArrayList<>();

      while( aReader.hasMoreChildren() ){
         aReader.moveDown();
         if( "item".equals(aReader.getNodeName()) ){
            try{
               Item item = (Item)aContext.convertAnother(null, Item.class);
               item = CompatibilityHelper.fixArtemis(item, loadout.getUpgrades().getGuidance());
               if( item instanceof HeatSink ){
                  later.add(item);
               }
               else{
                  operationStack.pushAndApply(new AddItemOperation(xBar, loadoutPart, item));
               }
            }
            catch( IllegalArgumentException exception ){
               JOptionPane.showMessageDialog(ProgramInit.lsml(), "The loadout: " + loadout.getName()
                                                                 + " is corrupt. Continuing to load as much as possible.");
            }
         }
         aReader.moveUp();
      }

      try{
         for(Item item : later){
            operationStack.pushAndApply(new AddItemOperation(xBar, loadoutPart, item));
         }
      }
      catch( IllegalArgumentException exception ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "The loadout: " + loadout.getName()
                                                           + " is corrupt. Continuing to load as much as possible.");
      }

      return null; // We address directly into the given loadout, this is a trap.
   }

}
