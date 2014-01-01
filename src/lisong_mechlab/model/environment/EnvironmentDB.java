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

package lisong_mechlab.model.environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lisong_mechlab.converter.GameDataFile;
import lisong_mechlab.model.mwo_parsing.Localization;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This class parses all the environments to memory from the game files.
 * 
 * @author Emily Björk
 */
public class EnvironmentDB{
   class Mission{
      class Entity{
         class EntityProperties{
            @XStreamAsAttribute
            double temperature;
         }

         EntityProperties Properties;
         @XStreamAsAttribute
         String           EntityClass;
      }

      List<Entity> Objects;
   }

   private List<Environment> environments = new ArrayList<>();

   public void initialize() throws IOException{
      environments.clear();
      GameDataFile dataFile = new GameDataFile();
      File[] levels = dataFile.listGameDir(new File("Game/Levels"));
      if( levels == null )
         throw new IOException("Couldn't find environments!");

      for(File file : levels){
         // Skip the tutorials and mechlab
         if( file.getName().toLowerCase().contains("tutorial") || file.getName().toLowerCase().contains("mechlab") ){
            continue;
         }

         String uiTag = "ui_" + file.getName();
         String uiName = Localization.key2string(uiTag);

         XStream xstream = new XStream(new StaxDriver(new NoNameCoder())){
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next){
               return new MapperWrapper(next){
                  @Override
                  public boolean shouldSerializeMember(Class definedIn, String fieldName){
                     if( definedIn == Object.class ){
                        return false;
                     }
                     return super.shouldSerializeMember(definedIn, fieldName);
                  }
               };
            }
         };
         xstream.autodetectAnnotations(true);
         xstream.alias("Mission", EnvironmentDB.Mission.class);
         xstream.alias("Entity", EnvironmentDB.Mission.Entity.class);
         xstream.alias("Object", EnvironmentDB.Mission.Entity.class);
         xstream.alias("Properties", EnvironmentDB.Mission.Entity.EntityProperties.class);
         // xstream.aliasAttribute("Properties", "properties");

         Mission mission = (Mission)xstream.fromXML(dataFile.openGameFile(new File(file, "mission_mission0.xml")));

         boolean found = false;
         for(Mission.Entity entity : mission.Objects){
            if( entity.EntityClass != null && entity.EntityClass.toLowerCase().equals("worldparameters") ){
               environments.add(new Environment(uiName, entity.Properties.temperature));
               found = true;
               break;
            }
         }
         if( !found ){
            throw new IOException("Unable to find temperature for environment: [" + uiName + "]!");
         }
      }
   }

   /**
    * Looks up an {@link Environment} by name.
    * 
    * @param aString
    *           The name of the {@link Environment} to look for.
    * @return The {@link Environment} which's name matches <code>aString</code> or null if no {@link Environment}
    *         matched.
    */
   public Environment lookup(String aString){
      for(Environment environment : environments){
         if( environment.getName().toLowerCase().equals(aString.toLowerCase()) ){
            return environment;
         }
      }
      return null;
   }

   /**
    * @return A list of all {@link Environment}s loaded.
    */
   public List<Environment> lookupAll(){
      return Collections.unmodifiableList(environments);
   }
}
