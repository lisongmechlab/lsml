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
package lisong_mechlab.model.loadout;

import java.io.File;
import java.util.List;

import lisong_mechlab.converter.GameDataFile;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Chassis;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.SetArmorOperation;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.GuidanceUpgrade;
import lisong_mechlab.model.upgrades.HeatsinkUpgrade;
import lisong_mechlab.model.upgrades.SetArmorTypeOperation;
import lisong_mechlab.model.upgrades.SetGuidanceTypeOperation;
import lisong_mechlab.model.upgrades.SetHeatSinkTypeOperation;
import lisong_mechlab.model.upgrades.SetStructureTypeOperation;
import lisong_mechlab.model.upgrades.StructureUpgrade;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.XmlReader;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This operation loads a 'mechs stock {@link Loadout}.
 * 
 * @author Li Song
 */
public class LoadStockOperation extends LoadoutOperation{
   public LoadStockOperation(Chassis aChassiVariation, Loadout aLoadout, MessageXBar anXBar) throws Exception{
      super(aLoadout, anXBar, "load stock");
      addOp(new StripOperation(loadout, xBar));

      File loadoutXml = new File("Game/Libs/MechLoadout/" + aChassiVariation.getMwoName().toLowerCase() + ".xml");
      GameDataFile dataFile = new GameDataFile();
      XmlReader reader = new XmlReader(dataFile.openGameFile(loadoutXml));

      List<Element> maybeUpgrades = reader.getElementsByTagName("Upgrades");
      if( maybeUpgrades.size() == 1 ){
         Element stockUpgrades = maybeUpgrades.get(0);

         int armorId = Integer.parseInt(reader.getElementByTagName("Armor", stockUpgrades).getAttribute("ItemID"));
         int structureId = Integer.parseInt(reader.getElementByTagName("Structure", stockUpgrades).getAttribute("ItemID"));
         int heatsinkId = reader.getElementByTagName("HeatSinks", stockUpgrades).getAttribute("Type").equals("Double") ? 3002 : 3003;
         int guidanceId = reader.getElementByTagName("Artemis", stockUpgrades).getAttribute("Equipped").equals("1") ? 3050 : 3051;
        
         addOp(new SetStructureTypeOperation(xBar, loadout, (StructureUpgrade)UpgradeDB.lookup(structureId)));
         addOp(new SetGuidanceTypeOperation(xBar, loadout, (GuidanceUpgrade)UpgradeDB.lookup(guidanceId)));
         addOp(new SetArmorTypeOperation(xBar, loadout, (ArmorUpgrade)UpgradeDB.lookup(armorId)));
         addOp(new SetHeatSinkTypeOperation(xBar, loadout, (HeatsinkUpgrade)UpgradeDB.lookup(heatsinkId)));
      }
      else{
         addOp(new SetStructureTypeOperation(xBar, loadout, UpgradeDB.STANDARD_STRUCTURE));
         addOp(new SetGuidanceTypeOperation(xBar, loadout, UpgradeDB.STANDARD_GUIDANCE));
         addOp(new SetArmorTypeOperation(xBar, loadout, UpgradeDB.STANDARD_ARMOR));
         addOp(new SetHeatSinkTypeOperation(xBar, loadout, UpgradeDB.STANDARD_HEATSINKS));
      }

      for(Element component : reader.getElementsByTagName("component")){
         String componentName = component.getAttribute("Name");
         int componentArmor = Integer.parseInt(component.getAttribute("Armor"));

         Part partType = Part.fromMwoName(componentName);

         LoadoutPart part = loadout.getPart(partType);
         if( partType.isTwoSided() ){
            if( Part.isRear(componentName) )
               addOp(new SetArmorOperation(xBar, part, ArmorSide.BACK, componentArmor, true));
            else
               addOp(new SetArmorOperation(xBar, part, ArmorSide.FRONT, componentArmor, true));
         }
         else
            addOp(new SetArmorOperation(xBar, part, ArmorSide.ONLY, componentArmor, true));

         Node child = component.getFirstChild();
         while( null != child ){
            if( child.getNodeType() == Node.ELEMENT_NODE ){
               Item item = ItemDB.lookup(Integer.parseInt(((Element)child).getAttribute("ItemID")));
               addOp(new AddItemOperation(xBar, part, item));
            }
            child = child.getNextSibling();
         }
      }
   }
}
