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
import lisong_mechlab.model.loadout.LoadoutOmniMech;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentOmniMech;
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
      stream.alias("loadout", LoadoutBase.class);
      stream.alias("loadout", LoadoutStandard.class);
      stream.alias("loadout", LoadoutOmniMech.class);
      stream.registerConverter(new Converter(){
         @Override
         public boolean canConvert(Class aClass){
            return LoadoutBase.class.isAssignableFrom(aClass);
         }

         @Override
         public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext){
            return null; // Implement this if we need to import from smurfy.
         }

         private <T> void writeCData(HierarchicalStreamWriter aWriter, String aName, T aVal){
            writeValueTag(aWriter, aName, "<![CDATA[" + aVal + "]]>");
         }

         private <T> void writeValueTag(HierarchicalStreamWriter aWriter, String aName, T aVal){
            aWriter.startNode(aName);
            aWriter.setValue(aVal.toString());
            aWriter.endNode();
         }

         @Override
         public void marshal(Object aObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext){

            LoadoutBase<?> loadoutBase = (LoadoutBase<?>)aObject;
            LoadoutOmniMech loadoutOmniMech = (loadoutBase instanceof LoadoutOmniMech) ? (LoadoutOmniMech)loadoutBase : null;

            writeCData(aWriter, "id", loadoutBase.getName());
            writeValueTag(aWriter, "mech_id", loadoutBase.getChassis().getMwoId());
            writeValueTag(aWriter, "valid", true);

            aWriter.startNode("configuration");
            {
               for(Location location : new Location[] {Location.Head, Location.LeftTorso, Location.CenterTorso, Location.RightTorso,
                     Location.LeftLeg, Location.RightLeg, Location.RightArm, Location.LeftArm}){
                  ConfiguredComponentBase part = loadoutBase.getComponent(location);
                  aWriter.startNode("component");

                  writeCData(aWriter, "name", part.getInternalComponent().getLocation().toMwoName());
                  if( part.getInternalComponent().getLocation().isTwoSided() ){
                     writeValueTag(aWriter, "armor", part.getArmor(ArmorSide.FRONT));
                  }
                  else{
                     writeValueTag(aWriter, "armor", part.getArmorTotal());
                  }

                  if( loadoutOmniMech != null ){
                     ConfiguredComponentOmniMech componentOmniMech = loadoutOmniMech.getComponent(location);

                     boolean actuatorsStarted = false;
                     for(Item togglable : componentOmniMech.getOmniPod().getToggleableItems()){
                        if( !actuatorsStarted ){
                           aWriter.startNode("actuators");
                           actuatorsStarted = true;
                        }

                        aWriter.startNode("actuator");
                        writeValueTag(aWriter, "id", togglable.getMwoId());
                        writeValueTag(aWriter, "enabled", Boolean.valueOf(componentOmniMech.getToggleState(togglable)));
                        aWriter.endNode();
                     }

                     if( actuatorsStarted )
                        aWriter.endNode();

                     if( location != Location.CenterTorso ){
                        writeValueTag(aWriter, "omni_pod", componentOmniMech.getOmniPod().getMwoId());
                     }
                  }

                  boolean itemsStarted = false;
                  for(Item item : part.getItemsEquipped()){
                     if( item instanceof Internal )
                        continue;

                     if( !itemsStarted ){
                        aWriter.startNode("items");
                        itemsStarted = true;
                     }

                     aWriter.startNode("item");

                     writeCData(aWriter, "id", item.getMwoId());
                     writeCData(aWriter, "type", item instanceof Weapon ? "weapon" : item instanceof Ammunition ? "ammo" : "module");
                     writeCData(aWriter, "name", item.getName());
                     aWriter.endNode();
                  }

                  if( itemsStarted )
                     aWriter.endNode();

                  aWriter.endNode();

               }
               for(Location type : new Location[] {Location.LeftTorso, Location.CenterTorso, Location.RightTorso}){
                  ConfiguredComponentBase part = loadoutBase.getComponent(type);
                  aWriter.startNode("component");
                  writeCData(aWriter, "name", part.getInternalComponent().getLocation().toMwoRearName());
                  writeValueTag(aWriter, "armor", part.getArmor(ArmorSide.BACK));
                  aWriter.endNode();
               }
            }
            aWriter.endNode();

            aWriter.startNode("upgrades");
            {
               Upgrades upgrades = loadoutBase.getUpgrades();
               Upgrade ups[] = new Upgrade[] {upgrades.getArmor(), upgrades.getStructure(), upgrades.getHeatSink(), upgrades.getGuidance()};
               for(int i = 0; i < ups.length; ++i){
                  Upgrade up = ups[i];
                  aWriter.startNode("upgrade");
                  writeCData(aWriter, "id", up.getMwoId());
                  writeCData(aWriter, "type", up.getType().toSmurfy());
                  writeCData(aWriter, "name", up.getName());
                  aWriter.endNode();
               }
            }
            aWriter.endNode();
         }
      });
      return stream;
   }
}
