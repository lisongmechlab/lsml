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
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.upgrades.SetArtemisOperation;
import lisong_mechlab.model.upgrades.SetDHSOperation;
import lisong_mechlab.model.upgrades.SetEndoSteelOperation;
import lisong_mechlab.model.upgrades.SetFerroFibrousOperation;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class LoadoutConverter implements Converter{

   private final MessageXBar xBar;

   public LoadoutConverter(MessageXBar anXBar){
      xBar = anXBar;
   }

   @Override
   public boolean canConvert(Class aClass){
      return Loadout.class.isAssignableFrom(aClass);
   }

   @Override
   public void marshal(Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext){
      Loadout loadout = (Loadout)anObject;

      aWriter.addAttribute("name", loadout.getName());
      aWriter.addAttribute("chassi", loadout.getChassi().getNameShort());

      aWriter.startNode("upgrades");
      aContext.convertAnother(loadout.getUpgrades());
      aWriter.endNode();

      aWriter.startNode("efficiencies");
      aContext.convertAnother(loadout.getEfficiencies());
      aWriter.endNode();

      for(LoadoutPart part : loadout.getPartLoadOuts()){
         aWriter.startNode("component");
         aContext.convertAnother(part);
         aWriter.endNode();
      }
   }

   @Override
   public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext){
      String chassiVariation = aReader.getAttribute("chassi");
      String name = aReader.getAttribute("name");
      Chassi chassi = ChassiDB.lookup(chassiVariation);

      OperationStack stack = new OperationStack(0);

      Loadout loadout = new Loadout(chassi, xBar);
      loadout.rename(name);

      while( aReader.hasMoreChildren() ){
         aReader.moveDown();
         if( "upgrades".equals(aReader.getNodeName()) ){
            Upgrades upgrades = (Upgrades)aContext.convertAnother(loadout, Upgrades.class);
            stack.pushAndApply(new SetArtemisOperation(xBar, loadout, upgrades.hasArtemis()));
            stack.pushAndApply(new SetDHSOperation(xBar, loadout, upgrades.hasDoubleHeatSinks()));
            stack.pushAndApply(new SetEndoSteelOperation(xBar, loadout, upgrades.hasEndoSteel()));
            stack.pushAndApply(new SetFerroFibrousOperation(xBar, loadout, upgrades.hasFerroFibrous()));
         }
         else if( "efficiencies".equals(aReader.getNodeName()) ){
            Efficiencies eff = (Efficiencies)aContext.convertAnother(loadout, Efficiencies.class);
            loadout.getEfficiencies().setCoolRun(eff.hasCoolRun());
            loadout.getEfficiencies().setDoubleBasics(eff.hasDoubleBasics());
            loadout.getEfficiencies().setHeatContainment(eff.hasHeatContainment());
            loadout.getEfficiencies().setSpeedTweak(eff.hasSpeedTweak());
         }
         else if( "component".equals(aReader.getNodeName()) ){
            aContext.convertAnother(loadout, LoadoutPart.class, new LoadoutPartConverter(xBar, loadout));
         }
         aReader.moveUp();
      }

      return loadout;
   }

}
