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
package lisong_mechlab.model.loadout.export;

import java.io.StringWriter;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.upgrades.Upgrade;
import lisong_mechlab.model.upgrades.Upgrades;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * This class provides converters between {@link LoadoutStandard}s and Smurfy's XML.
 * 
 * @author Emily Björk
 */
public class SmurfyXML{
   /**
    * Will convert the given {@link LoadoutStandard} to Smurfy-compatible XML.
    * 
    * @param aLoadout
    *           The {@link LoadoutStandard} to convert.
    * @return A {@link String} with the XML (including embedded new lines).
    */
   static public String toXml(final LoadoutBase<?> aLoadout){
      StringWriter sw = new StringWriter();
      sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      stream().marshal(aLoadout, new PrettyPrintWriter(sw, new NoNameCoder()){
         @Override
         protected void writeText(QuickWriter writer, String text){
            writer.write(text);
         }
      });
      return sw.toString();
   }

   static private XStream stream(){
      XStream stream = new XStream(new StaxDriver(new NoNameCoder()));
      stream.setMode(XStream.NO_REFERENCES);
      stream.alias("loadout", LoadoutStandard.class);
      stream.registerConverter(new Converter(){
         @Override
         public boolean canConvert(Class aArg0){
            return LoadoutStandard.class.isAssignableFrom(aArg0);
         }

         @Override
         public Object unmarshal(HierarchicalStreamReader aArg0, UnmarshallingContext aArg1){
            return null; // TODO: If needed
         }

         private <T> void writeValue(HierarchicalStreamWriter aWriter, String aName, T aVal){
            aWriter.startNode(aName);
            aWriter.setValue("<![CDATA[" + aVal + "]]>");
            aWriter.endNode();
         }

         @Override
         public void marshal(Object aObject, HierarchicalStreamWriter writer, MarshallingContext context){

            LoadoutStandard loadout = (LoadoutStandard)aObject;
            writeValue(writer, "id", loadout.getName());
            writeValue(writer, "mech_id", loadout.getChassis().getMwoId());

            writer.startNode("configuration");
            {
               for(Location type : new Location[] {Location.Head, Location.LeftTorso, Location.CenterTorso, Location.RightTorso, Location.LeftLeg,
                     Location.RightLeg, Location.RightArm, Location.LeftArm}){
                  ConfiguredComponentBase part = loadout.getComponent(type);
                  writer.startNode("component");

                  writeValue(writer, "name", part.getInternalComponent().getLocation().toMwoName());
                  if( part.getInternalComponent().getLocation().isTwoSided() ){
                     writeValue(writer, "armor", part.getArmor(ArmorSide.FRONT));
                  }
                  else{
                     writeValue(writer, "armor", part.getArmorTotal());
                  }

                  writer.startNode("items");
                  for(Item item : part.getItemsAll()){
                     if( item instanceof Internal )
                        continue;
                     writer.startNode("item");

                     writeValue(writer, "id", item.getMwoId());
                     writeValue(writer, "type", item instanceof Weapon ? "weapon" : item instanceof Ammunition ? "ammo" : "module");
                     writeValue(writer, "name", item.getName());
                     writer.endNode();
                  }
                  writer.endNode();

                  writer.endNode();

               }
               for(Location type : new Location[] {Location.LeftTorso, Location.CenterTorso, Location.RightTorso}){
                  ConfiguredComponentBase part = loadout.getComponent(type);
                  writer.startNode("component");
                  writeValue(writer, "name", part.getInternalComponent().getLocation().toMwoRearName());
                  writeValue(writer, "armor", part.getArmor(ArmorSide.BACK));
                  writer.endNode();
               }
            }
            writer.endNode();

            writer.startNode("upgrades");
            {
               Upgrades upgrades = loadout.getUpgrades();
               Upgrade ups[] = new Upgrade[] {upgrades.getArmor(), upgrades.getStructure(), upgrades.getHeatSink(), upgrades.getGuidance()};
               for(int i = 0; i < ups.length; ++i){
                  Upgrade up = ups[i];
                  writer.startNode("upgrade");
                  writer.startNode("id");
                  writer.setValue("" + up.getMwoId());
                  writer.endNode();
                  writeValue(writer, "type", i);
                  writeValue(writer, "name", up.getName());
                  writer.endNode();
               }
            }
            writer.endNode();
         }
      });
      return stream;
   }
}
