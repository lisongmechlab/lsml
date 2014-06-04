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
package lisong_mechlab.mwo_data;

import java.io.InputStream;
import java.util.List;

import lisong_mechlab.mwo_data.helpers.MdfComponent;
import lisong_mechlab.mwo_data.helpers.MdfInternal;
import lisong_mechlab.mwo_data.helpers.MdfMech;
import lisong_mechlab.mwo_data.helpers.MdfMovementTuning;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This class represents the XML content of the .mdf files.
 * 
 * @author Li Song
 */
public class MechDefinition{
   public MdfMech            Mech;
   public List<MdfComponent> ComponentList;

   public MdfMovementTuning  MovementTuningConfiguration;

   public static MechDefinition fromXml(InputStream is){
      XStream xstream = new XStream(new StaxDriver()){
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
      xstream.alias("MechDefinition", MechDefinition.class);
      xstream.alias("Mech", MdfMech.class);
      xstream.alias("Component", MdfComponent.class);
      xstream.alias("Internal", MdfInternal.class);
      xstream.alias("MovementTuningConfiguration", MdfMovementTuning.class);
      return (MechDefinition)xstream.fromXML(is);
   }
}
