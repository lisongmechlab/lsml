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
package org.lisoft.lsml.model.export.garage;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.lisoft.lsml.mwo_data.equipment.*;
import org.lisoft.lsml.mwo_data.mechs.Upgrades;

/**
 * This handles reading old and new upgrades.
 *
 * @author Li Song
 */
public class UpgradesConverter implements Converter {
  @Override
  public boolean canConvert(Class aClass) {
    return Upgrades.class.isAssignableFrom(aClass);
  }

  @Override
  public void marshal(
      Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
    final Upgrades upgrades = (Upgrades) anObject;

    aWriter.addAttribute("version", "2");

    aWriter.startNode("armor");
    aContext.convertAnother(upgrades.getArmour());
    aWriter.endNode();

    aWriter.startNode("structure");
    aContext.convertAnother(upgrades.getStructure());
    aWriter.endNode();

    aWriter.startNode("guidance");
    aContext.convertAnother(upgrades.getGuidance());
    aWriter.endNode();

    aWriter.startNode("heatsinks");
    aContext.convertAnother(upgrades.getHeatSink());
    aWriter.endNode();
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
    final String versionString = aReader.getAttribute("version");
    final int version;
    if (versionString == null) {
      version = 1;
    } else {
      version = Integer.parseInt(versionString);
    }

    GuidanceUpgrade guidance = UpgradeDB.STD_GUIDANCE;
    ArmourUpgrade armour = null;
    StructureUpgrade structure = null;
    HeatSinkUpgrade heatSinks = null;

    // Version 1 upgrades are no longer supported.
    if (version == 2) {
      // <armor>mwoId</armor><structure>mwoId</structure><guidance>mwoId</guidance><heatsinks>mwoId</heatsinks>
      while (aReader.hasMoreChildren()) {
        aReader.moveDown();
        switch (aReader.getNodeName()) {
          case "guidance" -> guidance = (GuidanceUpgrade) aContext.convertAnother(this, GuidanceUpgrade.class);
          case "armor" -> armour = (ArmourUpgrade) aContext.convertAnother(this, ArmourUpgrade.class);
          case "structure" -> structure = (StructureUpgrade) aContext.convertAnother(this, StructureUpgrade.class);
          case "heatsinks" -> heatSinks = (HeatSinkUpgrade) aContext.convertAnother(this, HeatSinkUpgrade.class);
          default -> throw new ConversionException("Unknown upgrade element: " + aReader.getNodeName());
        }
        aReader.moveUp();
      }
    } else {
      throw new ConversionException(
          "Unsupported version number on upgrades tag! :" + versionString);
    }

    return new Upgrades(armour, structure, guidance, heatSinks);
  }
}
