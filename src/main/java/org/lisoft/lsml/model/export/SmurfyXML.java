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
package org.lisoft.lsml.model.export;

import java.io.StringWriter;

import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.upgrades.Upgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;

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
public class SmurfyXML {
    /**
     * Will convert the given {@link LoadoutStandard} to Smurfy-compatible XML.
     *
     * @param aLoadout
     *            The {@link LoadoutStandard} to convert.
     * @return A {@link String} with the XML (including embedded new lines).
     */
    static public String toXml(final Loadout aLoadout) {
        final StringWriter sw = new StringWriter();
        sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        stream().marshal(aLoadout, new PrettyPrintWriter(sw, new NoNameCoder()) {
            @Override
            protected void writeText(QuickWriter writer, String text) {
                writer.write(text);
            }
        });
        return sw.toString();
    }

    static private XStream stream() {
        final XStream stream = new XStream(new StaxDriver(new NoNameCoder()));
        stream.setMode(XStream.NO_REFERENCES);
        stream.alias("loadout", Loadout.class);
        stream.alias("loadout", LoadoutStandard.class);
        stream.alias("loadout", LoadoutOmniMech.class);
        XStream.setupDefaultSecurity(stream);
        stream.registerConverter(new Converter() {
            @Override
            public boolean canConvert(Class aClass) {
                return Loadout.class.isAssignableFrom(aClass);
            }

            @Override
            public void marshal(Object aObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {

                final Loadout loadoutBase = (Loadout) aObject;
                final LoadoutOmniMech loadoutOmniMech = loadoutBase instanceof LoadoutOmniMech
                        ? (LoadoutOmniMech) loadoutBase
                        : null;

                writeCData(aWriter, "id", loadoutBase.getName());
                writeValueTag(aWriter, "mech_id", loadoutBase.getChassis().getId());
                writeValueTag(aWriter, "valid", true);

                aWriter.startNode("configuration");
                {
                    for (final Location location : new Location[] { Location.Head, Location.LeftTorso,
                            Location.CenterTorso, Location.RightTorso, Location.LeftLeg, Location.RightLeg,
                            Location.RightArm, Location.LeftArm }) {
                        final ConfiguredComponent part = loadoutBase.getComponent(location);
                        aWriter.startNode("component");

                        writeCData(aWriter, "name", part.getInternalComponent().getLocation().toMwoName());
                        if (part.getInternalComponent().getLocation().isTwoSided()) {
                            writeValueTag(aWriter, "armor", part.getArmour(ArmourSide.FRONT));
                        }
                        else {
                            writeValueTag(aWriter, "armor", part.getArmourTotal());
                        }

                        if (loadoutOmniMech != null) {
                            final ConfiguredComponentOmniMech componentOmniMech = loadoutOmniMech
                                    .getComponent(location);

                            boolean actuatorsStarted = false;
                            for (final Item togglable : componentOmniMech.getOmniPod().getToggleableItems()) {
                                if (!actuatorsStarted) {
                                    aWriter.startNode("actuators");
                                    actuatorsStarted = true;
                                }

                                aWriter.startNode("actuator");
                                writeValueTag(aWriter, "id", togglable.getId());
                                writeValueTag(aWriter, "enabled",
                                        Boolean.valueOf(componentOmniMech.getToggleState(togglable)));
                                aWriter.endNode();
                            }

                            if (actuatorsStarted) {
                                aWriter.endNode();
                            }

                            if (location != Location.CenterTorso) {
                                writeValueTag(aWriter, "omni_pod", componentOmniMech.getOmniPod().getId());
                            }
                        }

                        boolean itemsStarted = false;
                        for (final Item item : part.getItemsEquipped()) {
                            if (item instanceof Internal) {
                                continue;
                            }

                            if (!itemsStarted) {
                                aWriter.startNode("items");
                                itemsStarted = true;
                            }

                            aWriter.startNode("item");

                            writeCData(aWriter, "id", item.getId());
                            writeCData(aWriter, "type",
                                    item instanceof Weapon ? "weapon" : item instanceof Ammunition ? "ammo" : "module");
                            writeCData(aWriter, "name", item.getName());
                            aWriter.endNode();
                        }

                        if (itemsStarted) {
                            aWriter.endNode();
                        }

                        aWriter.endNode();

                    }
                    for (final Location type : new Location[] { Location.LeftTorso, Location.CenterTorso,
                            Location.RightTorso }) {
                        final ConfiguredComponent part = loadoutBase.getComponent(type);
                        aWriter.startNode("component");
                        writeCData(aWriter, "name", part.getInternalComponent().getLocation().toMwoRearName());
                        writeValueTag(aWriter, "armor", part.getArmour(ArmourSide.BACK));
                        aWriter.endNode();
                    }
                }
                aWriter.endNode();

                aWriter.startNode("upgrades");
                {
                    final Upgrades upgrades = loadoutBase.getUpgrades();
                    final Upgrade ups[] = new Upgrade[] { upgrades.getArmour(), upgrades.getStructure(),
                            upgrades.getHeatSink(), upgrades.getGuidance() };
                    for (final Upgrade up : ups) {
                        aWriter.startNode("upgrade");
                        writeCData(aWriter, "id", up.getId());
                        writeCData(aWriter, "type", up.getType().toSmurfy());
                        writeCData(aWriter, "name", up.getName().replace("ARMOUR", "ARMOR"));
                        aWriter.endNode();
                    }
                }
                aWriter.endNode();
            }

            @Override
            public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
                return null; // Implement this if we need to import from smurfy.
            }

            private <T> void writeCData(HierarchicalStreamWriter aWriter, String aName, T aVal) {
                writeValueTag(aWriter, aName, "<![CDATA[" + aVal + "]]>");
            }

            private <T> void writeValueTag(HierarchicalStreamWriter aWriter, String aName, T aVal) {
                aWriter.startNode(aName);
                aWriter.setValue(aVal.toString());
                aWriter.endNode();
            }
        });
        return stream;
    }
}
