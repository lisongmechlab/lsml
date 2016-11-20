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

import java.util.Optional;

import org.lisoft.lsml.command.CmdAddModule;
import org.lisoft.lsml.command.CmdSetArmourType;
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
import org.lisoft.lsml.model.loadout.LoadoutBuilder.ErrorReportingCallback;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentStandard;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This {@link Converter} is used to load Loadouts from XML.
 *
 * @author Li Song
 */
public class LoadoutConverter implements Converter {

    private final ErrorReportingCallback errorReporter;

    /**
     * @param aErrorReporter
     *            A reporter to give the errors of the {@link Loadout} (if any) to.
     */
    public LoadoutConverter(ErrorReportingCallback aErrorReporter) {
        errorReporter = aErrorReporter;
    }

    @Override
    public boolean canConvert(Class aClass) {
        return Loadout.class.isAssignableFrom(aClass);
    }

    @Override
    public void marshal(Object aObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
        final Loadout loadout = (Loadout) aObject;

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

        for (final ConfiguredComponent part : loadout.getComponents()) {
            aWriter.startNode("component");
            aContext.convertAnother(part);
            aWriter.endNode();
        }

        aWriter.startNode("pilotmodules");
        for (final PilotModule module : loadout.getModules()) {
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
        final String version = aReader.getAttribute("version");
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

    private Loadout parseV1(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        final String chassisVariation = aReader.getAttribute("chassi");
        final String name = aReader.getAttribute("name");
        final Chassis chassis = ChassisDB.lookup(chassisVariation);
        if (!(chassis instanceof ChassisStandard)) {
            throw new RuntimeException(
                    "Error parsing loadout: " + name + " expected standard mech but found an omni mech chassis.");
        }

        final LoadoutStandard loadout = (LoadoutStandard) DefaultLoadoutFactory.instance.produceEmpty(chassis);
        final LoadoutBuilder builder = new LoadoutBuilder();
        loadout.setName(name);

        while (aReader.hasMoreChildren()) {
            aReader.moveDown();
            if ("upgrades".equals(aReader.getNodeName())) {
                final Upgrades upgrades = (Upgrades) aContext.convertAnother(loadout, Upgrades.class);
                builder.push(new CmdSetGuidanceType(null, loadout, upgrades.getGuidance()));
                builder.push(new CmdSetHeatSinkType(null, loadout, upgrades.getHeatSink()));
                builder.push(new CmdSetStructureType(null, loadout, upgrades.getStructure()));
                builder.push(new CmdSetArmourType(null, loadout, upgrades.getArmour()));

                // Cheat here to preserve backwards compatibility if really old V1 garages.
                // Doing this here, triggers artemis fixes to be applied in v1 parser in ConfiguredComponentConverter
                loadout.getUpgrades().setGuidance(upgrades.getGuidance());
            }
            else if ("efficiencies".equals(aReader.getNodeName())) {
                final Efficiencies eff = (Efficiencies) aContext.convertAnother(loadout, Efficiencies.class);
                loadout.getEfficiencies().assign(eff);
            }
            else if ("component".equals(aReader.getNodeName())) {
                aContext.convertAnother(loadout, ConfiguredComponentStandard.class,
                        new ConfiguredComponentConverter(loadout, builder));
            }
            aReader.moveUp();
        }
        builder.apply();
        builder.reportErrors(Optional.of(loadout), errorReporter);
        return loadout;
    }

    private Loadout parseV2(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        final String name = aReader.getAttribute("name");
        final String chassisName = aReader.getAttribute("chassis");
        Chassis chassis;
        try {
            chassis = ChassisDB.lookup(Integer.parseInt(chassisName));
        }
        catch (final Throwable t) {
            chassis = ChassisDB.lookup(chassisName);
        }
        final Loadout loadout = DefaultLoadoutFactory.instance.produceEmpty(chassis);
        final LoadoutBuilder builder = new LoadoutBuilder();
        loadout.setName(name);

        while (aReader.hasMoreChildren()) {
            aReader.moveDown();
            if ("upgrades".equals(aReader.getNodeName())) {
                if (loadout instanceof LoadoutStandard) {
                    final LoadoutStandard loadoutStd = (LoadoutStandard) loadout;
                    final Upgrades upgrades = (Upgrades) aContext.convertAnother(loadoutStd, Upgrades.class);
                    builder.push(new CmdSetGuidanceType(null, loadoutStd, upgrades.getGuidance()));
                    builder.push(new CmdSetHeatSinkType(null, loadoutStd, upgrades.getHeatSink()));
                    builder.push(new CmdSetStructureType(null, loadoutStd, upgrades.getStructure()));
                    builder.push(new CmdSetArmourType(null, loadoutStd, upgrades.getArmour()));
                }
                else if (loadout instanceof LoadoutOmniMech) {
                    while (aReader.hasMoreChildren()) {
                        aReader.moveDown();
                        if (aReader.getNodeName().equals("guidance")) {
                            final GuidanceUpgrade artemis = (GuidanceUpgrade) UpgradeDB
                                    .lookup(Integer.parseInt(aReader.getValue()));
                            builder.push(new CmdSetGuidanceType(null, loadout, artemis));
                        }
                        aReader.moveUp();
                    }
                }
            }
            else if ("efficiencies".equals(aReader.getNodeName())) {
                final Efficiencies eff = (Efficiencies) aContext.convertAnother(loadout, Efficiencies.class);
                loadout.getEfficiencies().assign(eff);
            }
            else if ("component".equals(aReader.getNodeName())) {
                aContext.convertAnother(loadout, ConfiguredComponentStandard.class,
                        new ConfiguredComponentConverter(loadout, builder));
            }
            else if ("pilotmodules".equals(aReader.getNodeName())) {

                while (aReader.hasMoreChildren()) {
                    aReader.moveDown();
                    if (!"module".equals(aReader.getNodeName())) {
                        throw new RuntimeException("Malformed XML! Expected <module> got: " + aReader.getNodeName());
                    }

                    final PilotModule module = (PilotModule) aContext.convertAnother(null, PilotModule.class);
                    builder.push(new CmdAddModule(null, loadout, module));

                    aReader.moveUp();
                }
            }
            else if ("weapongroups".equals(aReader.getNodeName())) {
                final WeaponGroups wg = (WeaponGroups) aContext.convertAnother(loadout, WeaponGroups.class);
                loadout.getWeaponGroups().assign(wg);
            }
            aReader.moveUp();
        }
        builder.apply();
        builder.reportErrors(Optional.of(loadout), errorReporter);
        return loadout;
    }
}
