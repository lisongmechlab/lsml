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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.chassi.OmniPodDB;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutOmniMech;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentOmniMech;
import lisong_mechlab.model.loadout.component.ConfiguredComponentStandard;
import lisong_mechlab.model.loadout.component.OpAddItem;
import lisong_mechlab.model.loadout.component.OpSetArmor;
import lisong_mechlab.model.loadout.export.CompatibilityHelper;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.ProgramInit;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ConfiguredComponentConverter implements Converter{
   private final OperationStack operationStack = new OperationStack(0);
   private final LoadoutBase<?> loadout;
   private final MessageXBar    xBar;

   public ConfiguredComponentConverter(MessageXBar anXBar, LoadoutBase<?> aLoadoutBase){
      loadout = aLoadoutBase;
      xBar = anXBar;
   }

   @Override
   public boolean canConvert(Class aClass){
      return ConfiguredComponentStandard.class.isAssignableFrom(aClass) || ConfiguredComponentOmniMech.class.isAssignableFrom(aClass);
   }

   @Override
   public void marshal(Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext){
      ConfiguredComponentBase part = (ConfiguredComponentBase)anObject;

      aWriter.addAttribute("version", "2");
      aWriter.addAttribute("location", part.getInternalComponent().getLocation().toString());
      aWriter.addAttribute("autoarmor", Boolean.toString(part.allowAutomaticArmor()));

      if( part instanceof ConfiguredComponentOmniMech ){
         if( !((ConfiguredComponentOmniMech)part).getInternalComponent().hasFixedOmniPod() ){
            aWriter.addAttribute("omnipod", Integer.toString(((ConfiguredComponentOmniMech)part).getOmniPod().getMwoId()));
         }
      }

      if( part.getInternalComponent().getLocation().isTwoSided() ){
         aWriter.addAttribute("armor", part.getArmor(ArmorSide.FRONT) + "/" + part.getArmor(ArmorSide.BACK));
      }
      else{
         aWriter.addAttribute("armor", Integer.toString(part.getArmor(ArmorSide.ONLY)));
      }

      for(Item item : part.getItemsEquipped()){
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
      String version = aReader.getAttribute("version");
      if( version == null || version.isEmpty() || version.equals("1") ){
         parseV1(aReader, aContext);
      }
      else if( version.equals("2") ){
         parseV2(aReader, aContext);
      }
      return null; // We address directly into the given loadout, this is a trap.
   }

   private void parseV2(HierarchicalStreamReader aReader, UnmarshallingContext aContext){
      Location partType = Location.valueOf(aReader.getAttribute("location"));
      boolean autoArmor = Boolean.parseBoolean(aReader.getAttribute("autoarmor"));
      ConfiguredComponentBase loadoutPart = loadout.getComponent(partType);

      if( loadout instanceof LoadoutOmniMech ){
         LoadoutOmniMech omniMech = ((LoadoutOmniMech)loadout);
         if( !omniMech.getComponent(partType).getInternalComponent().hasFixedOmniPod() ){
            OmniPod omnipod = OmniPodDB.lookup(Integer.parseInt(aReader.getAttribute("omnipod")));
            omniMech.getComponent(partType).setOmniPod(omnipod);
         }
      }

      try{
         if( partType.isTwoSided() ){
            String[] armors = aReader.getAttribute("armor").split("/");
            if( armors.length == 2 ){
               operationStack.pushAndApply(new OpSetArmor(xBar, loadout, loadoutPart, ArmorSide.FRONT, Integer.parseInt(armors[0]), !autoArmor));
               operationStack.pushAndApply(new OpSetArmor(xBar, loadout, loadoutPart, ArmorSide.BACK, Integer.parseInt(armors[1]), !autoArmor));
            }
         }
         else{
            operationStack.pushAndApply(new OpSetArmor(xBar, loadout, loadoutPart, ArmorSide.ONLY, Integer.parseInt(aReader.getAttribute("armor")),
                                                       !autoArmor));
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
               if( item instanceof HeatSink ){
                  later.add(item);
               }
               else{
                  operationStack.pushAndApply(new OpAddItem(xBar, loadout, loadoutPart, item));
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
            operationStack.pushAndApply(new OpAddItem(xBar, loadout, loadoutPart, item));
         }
      }
      catch( IllegalArgumentException exception ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "The loadout: " + loadout.getName()
                                                           + " is corrupt. Continuing to load as much as possible.");
      }
   }

   private void parseV1(HierarchicalStreamReader aReader, UnmarshallingContext aContext){
      Location partType = Location.valueOf(aReader.getAttribute("part"));
      ConfiguredComponentBase loadoutPart = loadout.getComponent(partType);

      String autoArmorString = aReader.getAttribute("autoarmor");
      boolean autoArmor = false;
      if( autoArmorString != null ){
         autoArmor = Boolean.parseBoolean(autoArmorString);
      }

      try{
         if( partType.isTwoSided() ){
            String[] armors = aReader.getAttribute("armor").split("/");
            if( armors.length == 2 ){
               operationStack.pushAndApply(new OpSetArmor(xBar, loadout, loadoutPart, ArmorSide.FRONT, Integer.parseInt(armors[0]), !autoArmor));
               operationStack.pushAndApply(new OpSetArmor(xBar, loadout, loadoutPart, ArmorSide.BACK, Integer.parseInt(armors[1]), !autoArmor));
            }
         }
         else{
            operationStack.pushAndApply(new OpSetArmor(xBar, loadout, loadoutPart, ArmorSide.ONLY, Integer.parseInt(aReader.getAttribute("armor")),
                                                       !autoArmor));
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
                  operationStack.pushAndApply(new OpAddItem(xBar, loadout, loadoutPart, item));
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
            operationStack.pushAndApply(new OpAddItem(xBar, loadout, loadoutPart, item));
         }
      }
      catch( IllegalArgumentException exception ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "The loadout: " + loadout.getName()
                                                           + " is corrupt. Continuing to load as much as possible.");
      }
   }
}
