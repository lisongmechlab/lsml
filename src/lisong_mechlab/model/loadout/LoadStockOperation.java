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
package lisong_mechlab.model.loadout;

import java.io.File;
import java.util.List;

import lisong_mechlab.converter.GameDataFile;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.SetArmorOperation;
import lisong_mechlab.model.upgrades.SetArtemisOperation;
import lisong_mechlab.model.upgrades.SetDHSOperation;
import lisong_mechlab.model.upgrades.SetEndoSteelOperation;
import lisong_mechlab.model.upgrades.SetFerroFibrousOperation;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.XmlReader;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This operation loads a 'mechs stock {@link Loadout}.
 * 
 * @author Emily Björk
 */
public class LoadStockOperation extends LoadoutOperation{
   public LoadStockOperation(Loadout aLoadout, MessageXBar anXBar) throws Exception{
      super(aLoadout, anXBar, "load stock");
      addOp(new StripOperation(loadout, xBar));

      File loadoutXml = new File("Game/Libs/MechLoadout/" + loadout.getChassi().getMwoName().toLowerCase() + ".xml");
      GameDataFile dataFile = new GameDataFile();
      XmlReader reader = new XmlReader(dataFile.openGameFile(loadoutXml));

      List<Element> maybeUpgrades = reader.getElementsByTagName("Upgrades");
      if( maybeUpgrades.size() == 1 ){
         Element stockUpgrades = maybeUpgrades.get(0);
         // TODO: We really should fix issue #75 to get rid of these hard coded constants.
         boolean stockDhs = reader.getElementByTagName("HeatSinks", stockUpgrades).getAttribute("Type").equals("Double");
         boolean stockFerro = reader.getElementByTagName("Armor", stockUpgrades).getAttribute("ItemID").equals("2801");
         boolean stockEndo = reader.getElementByTagName("Structure", stockUpgrades).getAttribute("ItemID").equals("3101");
         boolean stockArtemis = reader.getElementByTagName("Artemis", stockUpgrades).getAttribute("Equipped").equals("1");
         addOp(new SetEndoSteelOperation(xBar, loadout, stockEndo));
         addOp(new SetArtemisOperation(xBar, loadout, stockArtemis));
         addOp(new SetFerroFibrousOperation(xBar, loadout, stockFerro));
         addOp(new SetDHSOperation(xBar, loadout, stockDhs));

         // FIXME: Revisit this fix! The game files are broken.
         if( loadout.getChassi().getNameShort().equals("KTO-19") ){
            addOp(new SetFerroFibrousOperation(xBar, loadout, true));
         }
      }
      else{
         addOp(new SetEndoSteelOperation(xBar, loadout, false));
         addOp(new SetArtemisOperation(xBar, loadout, false));
         addOp(new SetFerroFibrousOperation(xBar, loadout, false));
         addOp(new SetDHSOperation(xBar, loadout, false));
      }

      for(Element component : reader.getElementsByTagName("component")){
         String componentName = component.getAttribute("Name");
         int componentArmor = Integer.parseInt(component.getAttribute("Armor"));

         Part partType = Part.fromMwoName(componentName);

         LoadoutPart part = loadout.getPart(partType);
         if( partType.isTwoSided() ){
            if( Part.isRear(componentName) )
               addOp(new SetArmorOperation(xBar, part, ArmorSide.BACK, componentArmor));
            else
               addOp(new SetArmorOperation(xBar, part, ArmorSide.FRONT, componentArmor));
         }
         else
            addOp(new SetArmorOperation(xBar, part, ArmorSide.ONLY, componentArmor));

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
