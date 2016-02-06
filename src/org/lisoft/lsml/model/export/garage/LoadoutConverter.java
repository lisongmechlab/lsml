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
package org.lisoft.lsml.model.export.garage;

import javax.swing.JOptionPane;

import org.lisoft.lsml.command.CmdAddModule;
import org.lisoft.lsml.command.CmdRename;
import org.lisoft.lsml.command.CmdSetArmorType;
import org.lisoft.lsml.command.CmdSetGuidanceType;
import org.lisoft.lsml.command.CmdSetHeatSinkType;
import org.lisoft.lsml.command.CmdSetStructureType;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutBuilder;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentStandard;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.view.ProgramInit;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This {@link Converter} is used to load Loadouts from XML.
 * 
 * @author Emily Björk
 */
public class LoadoutConverter implements Converter {
    @Override
    public boolean canConvert(Class aClass) {
        return Loadout.class.isAssignableFrom(aClass);
    }

    @Override
    public void marshal(Object aObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
        Loadout loadout = (Loadout) aObject;

        // Common attributes and nodes
        aWriter.addAttribute("version", "2");
        aWriter.addAttribute("name", loadout.getName());
        aWriter.addAttribute("chassis", Integer.toString(loadout.getChassis().getMwoId()));

        aWriter.startNode("efficiencies");
        aContext.convertAnother(loadout.getEfficiencies());
        aWriter.endNode();

        // Specific to LoadoutStandard
        aWriter.startNode("upgrades");
        if (loadout instanceof LoadoutStandard) {
            aContext.convertAnother(loadout.getUpgrades());
        }
        else if (loadout instanceof LoadoutOmniMech) {
            aWriter.startNode("guidance");
            aWriter.setValue(Integer.toString(loadout.getUpgrades().getGuidance().getMwoId()));
            aWriter.endNode();
        }
        else {
            throw new IllegalArgumentException("Unsupported loadout type: " + loadout.getClass());
        }
        aWriter.endNode();

        for (ConfiguredComponent part : loadout.getComponents()) {
            aWriter.startNode("component");
            aContext.convertAnother(part);
            aWriter.endNode();
        }

        aWriter.startNode("pilotmodules");
        for (PilotModule module : loadout.getModules()) {
            aWriter.startNode("module");
            aContext.convertAnother(module);
            aWriter.endNode();
        }
        aWriter.endNode();

        aWriter.startNode("weapongroups");
        aContext.convertAnother(loadout.getWeaponGroups());
        aWriter.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        String version = aReader.getAttribute("version");
        if (version == null || version.isEmpty() || version.equals("1")) {
            return parseV1(aReader, aContext);
        }
        else if (version.equals("2")) {
            return parseV2(aReader, aContext);
        }
        else {
            throw new RuntimeException("Unsupported loadout version: " + version);
        }
    }

    private Loadout parseV2(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        String name = aReader.getAttribute("name");
        String chassisName = aReader.getAttribute("chassis");
        Chassis chassis;
        try {
            chassis = ChassisDB.lookup(Integer.parseInt(chassisName));
        }
        catch (Throwable t) {
            chassis = ChassisDB.lookup(chassisName);
        }
        Loadout loadoutBase = DefaultLoadoutFactory.instance.produceEmpty(chassis);
        LoadoutBuilder builder = new LoadoutBuilder();
        builder.push(new CmdRename(loadoutBase, null, name));

        while (aReader.hasMoreChildren()) {
            aReader.moveDown();
            if ("upgrades".equals(aReader.getNodeName())) {
                if (loadoutBase instanceof LoadoutStandard) {
                    LoadoutStandard loadout = (LoadoutStandard) loadoutBase;
                    Upgrades upgrades = (Upgrades) aContext.convertAnother(loadout, Upgrades.class);
                    builder.push(new CmdSetGuidanceType(null, loadout, upgrades.getGuidance()));
                    builder.push(new CmdSetHeatSinkType(null, loadout, upgrades.getHeatSink()));
                    builder.push(new CmdSetStructureType(null, loadout, upgrades.getStructure()));
                    builder.push(new CmdSetArmorType(null, loadout, upgrades.getArmor()));
                }
                else if (loadoutBase instanceof LoadoutOmniMech) {
                    while (aReader.hasMoreChildren()) {
                        aReader.moveDown();
                        if (aReader.getNodeName().equals("guidance")) {
                            GuidanceUpgrade artemis = (GuidanceUpgrade) UpgradeDB
                                    .lookup(Integer.parseInt(aReader.getValue()));
                            builder.push(new CmdSetGuidanceType(null, loadoutBase, artemis));
                        }
                        aReader.moveUp();
                    }
                }
            }
            else if ("efficiencies".equals(aReader.getNodeName())) {
                Efficiencies eff = (Efficiencies) aContext.convertAnother(loadoutBase, Efficiencies.class);
                loadoutBase.getEfficiencies().assign(eff);
            }
            else if ("component".equals(aReader.getNodeName())) {
                aContext.convertAnother(loadoutBase, ConfiguredComponentStandard.class,
                        new ConfiguredComponentConverter(loadoutBase, builder));
            }
            else if ("pilotmodules".equals(aReader.getNodeName())) {

                while (aReader.hasMoreChildren()) {
                    aReader.moveDown();
                    if (!"module".equals(aReader.getNodeName())) {
                        throw new RuntimeException("Malformed XML! Expected <module> got: " + aReader.getNodeName());
                    }

                    PilotModule module = (PilotModule) aContext.convertAnother(null, PilotModule.class);
                    builder.push(new CmdAddModule(null, loadoutBase, module));

                    aReader.moveUp();
                }
            }
            else if ("weapongroups".equals(aReader.getNodeName())) {
                WeaponGroups wg = (WeaponGroups) aContext.convertAnother(loadoutBase, WeaponGroups.class);
                loadoutBase.getWeaponGroups().assign(wg);
            }
            aReader.moveUp();
        }
        builder.apply();
        reportErrors(builder, name);
        return loadoutBase;
    }

    private Loadout parseV1(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        String chassisVariation = aReader.getAttribute("chassi");
        String name = aReader.getAttribute("name");
        Chassis chassis = ChassisDB.lookup(chassisVariation);
        if (!(chassis instanceof ChassisStandard))
            throw new RuntimeException(
                    "Error parsing loadout: " + name + " expected standard mech but found an omni mech chassis.");

        LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance.produceEmpty(chassis);
        LoadoutBuilder builder = new LoadoutBuilder();
        builder.push(new CmdRename(loadout, null, name));

        while (aReader.hasMoreChildren()) {
            aReader.moveDown();
            if ("upgrades".equals(aReader.getNodeName())) {
                Upgrades upgrades = (Upgrades) aContext.convertAnother(loadout, Upgrades.class);
                builder.push(new CmdSetGuidanceType(null, loadout, upgrades.getGuidance()));
                builder.push(new CmdSetHeatSinkType(null, loadout, upgrades.getHeatSink()));
                builder.push(new CmdSetStructureType(null, loadout, upgrades.getStructure()));
                builder.push(new CmdSetArmorType(null, loadout, upgrades.getArmor()));

                // Cheat here to preserve backwards compatibility if really old V1 garages.
                // Doing this here, triggers artemis fixes to be applied in v1 parser in ConfiguredComponentConverter
                loadout.getUpgrades().setGuidance(upgrades.getGuidance());
            }
            else if ("efficiencies".equals(aReader.getNodeName())) {
                Efficiencies eff = (Efficiencies) aContext.convertAnother(loadout, Efficiencies.class);
                loadout.getEfficiencies().assign(eff);
            }
            else if ("component".equals(aReader.getNodeName())) {
                aContext.convertAnother(loadout, ConfiguredComponentStandard.class,
                        new ConfiguredComponentConverter(loadout, builder));
            }
            aReader.moveUp();
        }
        builder.apply();
        reportErrors(builder, name);
        return loadout;
    }

    // FIXME: Get rid of this in the model! It should be reported to the library user somehow.
    private void reportErrors(LoadoutBuilder builder, String name) {
        String errors = builder.getErrors(name);
        if (null != errors) {
            JOptionPane.showMessageDialog(ProgramInit.lsml(), errors, "Error parsing loadout: " + name,
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}
