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

import lisong_mechlab.model.chassi.HardPoint;
import lisong_mechlab.model.chassi.HardPointType;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class HardPointConverter implements Converter{

   @Override
   public boolean canConvert(Class aClass){
      return HardPoint.class.isAssignableFrom(aClass);
   }

   @Override
   public void marshal(Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext){
      HardPoint hp = (HardPoint)anObject;

      aWriter.addAttribute("type", hp.getType().toString());
      if( hp.getNumMissileTubes() > 0 ){
         aWriter.addAttribute("tubes", Integer.toString(hp.getNumMissileTubes()));
      }
      if( hp.hasMissileBayDoor() != false ){
         aWriter.addAttribute("bayDoor", Boolean.toString(hp.hasMissileBayDoor()));
      }
   }

   @Override
   public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext){
      HardPointType type = HardPointType.valueOf(aReader.getAttribute("type"));
      int numTubes = 0;
      boolean hasDoors = false;

      String tubes = aReader.getAttribute("tubes");
      String doors = aReader.getAttribute("bayDoor");

      if( null != tubes && !tubes.isEmpty() )
         numTubes = Integer.parseInt(tubes);
      if( null != doors && !doors.isEmpty() )
         hasDoors = Boolean.parseBoolean(doors);
      return new HardPoint(type, numTubes, hasDoors);
   }

}
