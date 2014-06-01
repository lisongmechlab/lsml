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

import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutOmniMech;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.OpRename;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentStandard;
import lisong_mechlab.model.upgrades.GuidanceUpgrade;
import lisong_mechlab.model.upgrades.OpSetArmorType;
import lisong_mechlab.model.upgrades.OpSetGuidanceType;
import lisong_mechlab.model.upgrades.OpSetHeatSinkType;
import lisong_mechlab.model.upgrades.OpSetStructureType;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This {@link Converter} is used to load {@link LoadoutStandard}s from xml. It is not used for {@link LoadoutOmniMech}
 * s.
 * 
 * @author Li Song
 */
public class LoadoutConverter implements Converter{

   private final MessageXBar    xBar;
   private final OperationStack stack = new OperationStack(0);

   public LoadoutConverter(MessageXBar aXBar){
      xBar = aXBar;
   }

   @Override
   public boolean canConvert(Class aClass){
      return LoadoutStandard.class.isAssignableFrom(aClass) || LoadoutOmniMech.class.isAssignableFrom(aClass);
   }

   @Override
   public void marshal(Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext){
      LoadoutBase<?> loadout = (LoadoutBase<?>)anObject;

      // Common attributes and nodes
      aWriter.addAttribute("version", "2");
      aWriter.addAttribute("name", loadout.getName());
      aWriter.addAttribute("chassis", loadout.getChassis().getNameShort());

      aWriter.startNode("efficiencies");
      aContext.convertAnother(loadout.getEfficiencies());
      aWriter.endNode();

      for(ConfiguredComponentBase part : loadout.getComponents()){
         aWriter.startNode("component");
         aContext.convertAnother(part);
         aWriter.endNode();
      }

      // Specific to LoadoutStandard
      if( loadout instanceof LoadoutStandard ){
         aWriter.startNode("upgrades");
         aContext.convertAnother(loadout.getUpgrades());
         aWriter.endNode();
      }
      else if( loadout instanceof LoadoutOmniMech ){
         aWriter.addAttribute("artemis", Integer.toString(loadout.getUpgrades().getGuidance().getMwoId()));
      }
      else{
         throw new IllegalArgumentException("Unsupported loadout type: " + loadout.getClass());
      }

   }

   @Override
   public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext){
      String version = aReader.getAttribute("version");
      if( version == null || version.isEmpty() || version.equals("1") ){
         return parseV1(aReader, aContext);
      }
      else if( version.equals("2") ){
         return parseV2(aReader, aContext);
      }
      else{
         throw new RuntimeException("Unsupported loadout version: " + version);
      }
   }

   private LoadoutBase<?> parseV2(HierarchicalStreamReader aReader, UnmarshallingContext aContext){
      String name = aReader.getAttribute("name");
      ChassisBase chassi = ChassisDB.lookup(aReader.getAttribute("chassis"));
      LoadoutBase<?> loadoutBase;

      if( chassi instanceof ChassisStandard ){
         LoadoutStandard loadout = new LoadoutStandard((ChassisStandard)chassi, xBar);
         loadoutBase = loadout;
      }
      else if( chassi instanceof ChassisOmniMech ){
         LoadoutOmniMech loadout = new LoadoutOmniMech(ComponentBuilder.getOmniPodFactory(), (ChassisOmniMech)chassi, xBar);
         loadoutBase = loadout;

         GuidanceUpgrade artemis = (GuidanceUpgrade)UpgradeDB.lookup(Integer.parseInt(aReader.getAttribute("artemis")));
         stack.pushAndApply(new OpSetGuidanceType(xBar, loadout, artemis));
      }
      else{
         throw new RuntimeException("Unsupported chassis class: " + chassi.getClass());
      }

      stack.pushAndApply(new OpRename(loadoutBase, xBar, name));

      while( aReader.hasMoreChildren() ){
         aReader.moveDown();
         if( "upgrades".equals(aReader.getNodeName()) ){
            if( !(chassi instanceof ChassisStandard) ){
               aReader.moveUp(); // Shouldn't happen, skip past...
               continue;
            }
            LoadoutStandard loadout = (LoadoutStandard)loadoutBase;
            Upgrades upgrades = (Upgrades)aContext.convertAnother(loadout, Upgrades.class);
            stack.pushAndApply(new OpSetGuidanceType(xBar, loadout, upgrades.getGuidance()));
            stack.pushAndApply(new OpSetHeatSinkType(xBar, loadout, upgrades.getHeatSink()));
            stack.pushAndApply(new OpSetStructureType(xBar, loadout, upgrades.getStructure()));
            stack.pushAndApply(new OpSetArmorType(xBar, loadout, upgrades.getArmor()));
         }
         else if( "efficiencies".equals(aReader.getNodeName()) ){
            Efficiencies eff = (Efficiencies)aContext.convertAnother(loadoutBase, Efficiencies.class);
            loadoutBase.getEfficiencies().setCoolRun(eff.hasCoolRun());
            loadoutBase.getEfficiencies().setDoubleBasics(eff.hasDoubleBasics());
            loadoutBase.getEfficiencies().setHeatContainment(eff.hasHeatContainment());
            loadoutBase.getEfficiencies().setSpeedTweak(eff.hasSpeedTweak());
            loadoutBase.getEfficiencies().setAnchorTurn(eff.hasAnchorTurn());
            loadoutBase.getEfficiencies().setFastFire(eff.hasFastFire());
         }
         else if( "component".equals(aReader.getNodeName()) ){
            aContext.convertAnother(loadoutBase, ConfiguredComponentStandard.class, new ConfiguredComponentConverter(xBar, loadoutBase));
         }
         aReader.moveUp();
      }
      return loadoutBase;
   }

   private LoadoutBase<?> parseV1(HierarchicalStreamReader aReader, UnmarshallingContext aContext){
      String chassiVariation = aReader.getAttribute("chassi");
      String name = aReader.getAttribute("name");
      ChassisBase chassi = ChassisDB.lookup(chassiVariation);
      if( !(chassi instanceof ChassisStandard) )
         throw new RuntimeException("Error parsing loadout: " + name + " expected standard mech but found an omni mech chassis.");

      LoadoutStandard loadout = new LoadoutStandard((ChassisStandard)chassi, xBar);
      stack.pushAndApply(new OpRename(loadout, xBar, name));

      while( aReader.hasMoreChildren() ){
         aReader.moveDown();
         if( "upgrades".equals(aReader.getNodeName()) ){
            Upgrades upgrades = (Upgrades)aContext.convertAnother(loadout, Upgrades.class);
            stack.pushAndApply(new OpSetGuidanceType(xBar, loadout, upgrades.getGuidance()));
            stack.pushAndApply(new OpSetHeatSinkType(xBar, loadout, upgrades.getHeatSink()));
            stack.pushAndApply(new OpSetStructureType(xBar, loadout, upgrades.getStructure()));
            stack.pushAndApply(new OpSetArmorType(xBar, loadout, upgrades.getArmor()));
         }
         else if( "efficiencies".equals(aReader.getNodeName()) ){
            Efficiencies eff = (Efficiencies)aContext.convertAnother(loadout, Efficiencies.class);
            loadout.getEfficiencies().setCoolRun(eff.hasCoolRun());
            loadout.getEfficiencies().setDoubleBasics(eff.hasDoubleBasics());
            loadout.getEfficiencies().setHeatContainment(eff.hasHeatContainment());
            loadout.getEfficiencies().setSpeedTweak(eff.hasSpeedTweak());
         }
         else if( "component".equals(aReader.getNodeName()) ){
            aContext.convertAnother(loadout, ConfiguredComponentStandard.class, new ConfiguredComponentConverter(xBar, loadout));
         }
         aReader.moveUp();
      }
      return loadout;
   }

}
