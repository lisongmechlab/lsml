/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.mwo_data.mwo_parser;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.lisoft.mwo_data.mechs.HardPoint;
import org.lisoft.mwo_data.mechs.HardPointType;

/**
 * XStream converter for {@link HardPoint}.
 *
 * @author Li Song
 */
public class HardPointConverter implements Converter {

  @Override
  public boolean canConvert(Class aClass) {
    return HardPoint.class.isAssignableFrom(aClass);
  }

  @Override
  public void marshal(
      Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
    final HardPoint hp = (HardPoint) anObject;

    aWriter.addAttribute("type", hp.getType().toString());
    if (hp.getNumMissileTubes() > 0) {
      aWriter.addAttribute("tubes", Integer.toString(hp.getNumMissileTubes()));
    }
    if (hp.hasMissileBayDoor()) {
      aWriter.addAttribute("bayDoor", Boolean.toString(hp.hasMissileBayDoor()));
    }
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
    final HardPointType type = HardPointType.valueOf(aReader.getAttribute("type"));
    int numTubes = 0;
    boolean hasDoors = false;

    final String tubes = aReader.getAttribute("tubes");
    final String doors = aReader.getAttribute("bayDoor");

    if (null != tubes && !tubes.isEmpty()) {
      numTubes = Integer.parseInt(tubes);
    }
    if (null != doors && !doors.isEmpty()) {
      hasDoors = Boolean.parseBoolean(doors);
    }
    return new HardPoint(type, numTubes, hasDoors);
  }
}
