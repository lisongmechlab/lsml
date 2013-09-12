package lisong_mechlab.model.loadout.converters;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.loadout.Efficiencies;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.LoadoutPart;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.util.MessageXBar;

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
      
      Loadout loadout = new Loadout(chassi, xBar);
      loadout.rename(name);
      
      while( aReader.hasMoreChildren() ){
         aReader.moveDown();
         if("upgrades".equals(aReader.getNodeName())){
            Upgrades upgrades = (Upgrades)aContext.convertAnother(loadout, Upgrades.class);
            loadout.getUpgrades().setArtemis(upgrades.hasArtemis());
            loadout.getUpgrades().setDoubleHeatSinks(upgrades.hasDoubleHeatSinks());
            loadout.getUpgrades().setEndoSteel(upgrades.hasEndoSteel());
            loadout.getUpgrades().setFerroFibrous(upgrades.hasFerroFibrous());
         }
         else if("efficiencies".equals(aReader.getNodeName())){
            Efficiencies eff = (Efficiencies)aContext.convertAnother(loadout, Efficiencies.class);
            loadout.getEfficiencies().setCoolRun(eff.hasCoolRun());
            loadout.getEfficiencies().setDoubleBasics(eff.hasDoubleBasics());
            loadout.getEfficiencies().setHeatContainment(eff.hasHeatContainment());
            loadout.getEfficiencies().setSpeedTweak(eff.hasSpeedTweak());
         }
         else if("component".equals(aReader.getNodeName())){
            aContext.convertAnother(loadout, LoadoutPart.class, new LoadoutPartConverter(loadout));
         }
         aReader.moveUp();
      }
      
      return loadout;
   }

}
