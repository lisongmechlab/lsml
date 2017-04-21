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
package org.lisoft.lsml.model.export.garage;

import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This handles reading old and new upgrades
 * 
 * @author Li Song
 */
public class UpgradesConverter implements Converter {
    @Override
    public boolean canConvert(Class aClass) {
        return Upgrades.class.isAssignableFrom(aClass);
    }

    @Override
    public void marshal(Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
        Upgrades upgrades = (Upgrades) anObject;

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
        if (versionString == null)
            version = 1;
        else
            version = Integer.parseInt(versionString);

        GuidanceUpgrade guidance = UpgradeDB.STD_GUIDANCE;
        ArmourUpgrade armour = null;
        StructureUpgrade structure = null;
        HeatSinkUpgrade heatSinks = null;

        if (version == 1) {
            armour = UpgradeDB.getArmour(Faction.INNERSPHERE, false);
            structure = UpgradeDB.getStructure(Faction.INNERSPHERE, false);
            heatSinks = UpgradeDB.getHeatSinks(Faction.INNERSPHERE, false);
            // <artemis>bool</artemis><ferroFibrous>bool</ferroFibrous><endoSteel>bool</endoSteel><dhs>bool</dhs>
            while (aReader.hasMoreChildren()) {
                aReader.moveDown();
                switch (aReader.getNodeName()) {
                    case "artemis":
                        if (Boolean.parseBoolean(aReader.getValue())) {
                            guidance = UpgradeDB.ARTEMIS_IV;
                        }
                        break;
                    case "ferroFibrous":
                        if (Boolean.parseBoolean(aReader.getValue())) {
                            armour = UpgradeDB.getArmour(Faction.INNERSPHERE, true);
                        }
                        break;
                    case "endoSteel":
                        if (Boolean.parseBoolean(aReader.getValue())) {
                            structure = UpgradeDB.getStructure(Faction.INNERSPHERE, true);
                        }
                        break;
                    case "dhs":
                        if (Boolean.parseBoolean(aReader.getValue())) {
                            heatSinks = UpgradeDB.getHeatSinks(Faction.INNERSPHERE, true);
                        }
                        break;
                    default:
                        throw new ConversionException("Unknown upgrade element: " + aReader.getNodeName());
                }
                aReader.moveUp();
            }
        }
        else if (version == 2) {
            // <armor>mwoId</armor><structure>mwoId</structure><guidance>mwoId</guidance><heatsinks>mwoId</heatsinks>
            while (aReader.hasMoreChildren()) {
                aReader.moveDown();
                switch (aReader.getNodeName()) {
                    case "guidance":
                        guidance = (GuidanceUpgrade) UpgradeDB.lookup(Integer.parseInt(aReader.getValue()));
                        break;
                    case "armor":
                        armour = (ArmourUpgrade) UpgradeDB.lookup(Integer.parseInt(aReader.getValue()));
                        break;
                    case "structure":
                        structure = (StructureUpgrade) UpgradeDB.lookup(Integer.parseInt(aReader.getValue()));
                        break;
                    case "heatsinks":
                        heatSinks = (HeatSinkUpgrade) UpgradeDB.lookup(Integer.parseInt(aReader.getValue()));
                        break;
                    default:
                        throw new ConversionException("Unknown upgrade element: " + aReader.getNodeName());
                }
                aReader.moveUp();
            }
        }
        else
            throw new ConversionException("Unsupported version number on upgrades tag! :" + versionString);

        return new Upgrades(armour, structure, guidance, heatSinks);
    }
}
