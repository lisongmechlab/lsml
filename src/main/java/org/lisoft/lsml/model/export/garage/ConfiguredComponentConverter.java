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

import org.lisoft.lsml.command.CmdAddItem;
import org.lisoft.lsml.command.CmdSetArmour;
import org.lisoft.lsml.command.CmdToggleItem;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.OmniPodDB;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.loadout.ConfiguredComponentStandard;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutBuilder;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ConfiguredComponentConverter implements Converter {
    private final LoadoutBuilder builder;
    private final Loadout loadout;

    public ConfiguredComponentConverter(Loadout aLoadoutBase, LoadoutBuilder aBuilder) {
        loadout = aLoadoutBase;
        builder = aBuilder;
    }

    @Override
    public boolean canConvert(Class aClass) {
        return ConfiguredComponentStandard.class.isAssignableFrom(aClass)
                || ConfiguredComponentOmniMech.class.isAssignableFrom(aClass);
    }

    @Override
    public void marshal(Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
        ConfiguredComponent component = (ConfiguredComponent) anObject;
        ConfiguredComponentOmniMech omniComponent = null;
        if (component instanceof ConfiguredComponentOmniMech) {
            omniComponent = (ConfiguredComponentOmniMech) component;
        }

        aWriter.addAttribute("version", "2");
        aWriter.addAttribute("location", component.getInternalComponent().getLocation().toString());
        aWriter.addAttribute("autoarmor", Boolean.toString(!component.hasManualArmour()));

        if (null != omniComponent) {
            if (!omniComponent.getInternalComponent().hasFixedOmniPod()) {
                aWriter.addAttribute("omnipod", Integer.toString(omniComponent.getOmniPod().getMwoId()));
            }
        }

        if (component.getInternalComponent().getLocation().isTwoSided()) {
            aWriter.addAttribute("armor",
                    component.getArmour(ArmourSide.FRONT) + "/" + component.getArmour(ArmourSide.BACK));
        }
        else {
            aWriter.addAttribute("armor", Integer.toString(component.getArmour(ArmourSide.ONLY)));
        }

        if (null != omniComponent) {
            for (Item togglable : omniComponent.getOmniPod().getToggleableItems()) {
                aWriter.startNode("togglestate");
                aWriter.addAttribute("item", Integer.toString(togglable.getMwoId()));
                aWriter.addAttribute("enabled", Boolean.toString(omniComponent.getToggleState(togglable)));
                aWriter.endNode();
            }
        }

        for (Item item : component.getItemsEquipped()) {
            if (item instanceof Internal) {
                continue;
            }
            aWriter.startNode("item");
            aContext.convertAnother(item);
            aWriter.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        String version = aReader.getAttribute("version");
        if (version == null || version.isEmpty() || version.equals("1")) {
            parseV1(aReader, aContext);
        }
        else if (version.equals("2")) {
            parseV2(aReader, aContext);
        }
        return null; // We address directly into the given loadout, this is a trap.
    }

    private void parseV2(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        Location partType = Location.valueOf(aReader.getAttribute("location"));
        boolean autoArmour = Boolean.parseBoolean(aReader.getAttribute("autoarmor"));
        ConfiguredComponent loadoutPart = loadout.getComponent(partType);

        if (loadout instanceof LoadoutOmniMech) {
            LoadoutOmniMech omniMech = ((LoadoutOmniMech) loadout);
            if (!omniMech.getComponent(partType).getInternalComponent().hasFixedOmniPod()) {
                OmniPod omnipod = OmniPodDB.lookup(Integer.parseInt(aReader.getAttribute("omnipod")));
                omniMech.getComponent(partType).changeOmniPod(omnipod);
            }
        }

        try {
            if (partType.isTwoSided()) {
                String[] armours = aReader.getAttribute("armor").split("/");
                if (armours.length == 2) {
                    builder.push(new CmdSetArmour(null, loadout, loadoutPart, ArmourSide.FRONT,
                            Integer.parseInt(armours[0]), !autoArmour));
                    builder.push(new CmdSetArmour(null, loadout, loadoutPart, ArmourSide.BACK,
                            Integer.parseInt(armours[1]), !autoArmour));
                }
            }
            else {
                builder.push(new CmdSetArmour(null, loadout, loadoutPart, ArmourSide.ONLY,
                        Integer.parseInt(aReader.getAttribute("armor")), !autoArmour));
            }
        }
        catch (IllegalArgumentException exception) {
            builder.pushError(exception);
        }

        while (aReader.hasMoreChildren()) {
            aReader.moveDown();
            if ("item".equals(aReader.getNodeName())) {
                try {
                    Item item = (Item) aContext.convertAnother(null, Item.class);
                    builder.push(new CmdAddItem(null, loadout, loadoutPart, item));
                }
                catch (Throwable t) {
                    builder.pushError(t);
                }
            }
            else if ("togglestate".equals(aReader.getNodeName())) {
                Item item = ItemDB.lookup(Integer.parseInt(aReader.getAttribute("item")));
                builder.push(new CmdToggleItem(null, loadout, (ConfiguredComponentOmniMech) loadoutPart, item,
                        Boolean.parseBoolean(aReader.getAttribute("enabled"))));
            }
            aReader.moveUp();
        }
    }

    private void parseV1(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        Location partType = Location.valueOf(aReader.getAttribute("part"));
        ConfiguredComponent loadoutPart = loadout.getComponent(partType);

        String autoArmourString = aReader.getAttribute("autoarmor");
        boolean autoArmour = false;
        if (autoArmourString != null) {
            autoArmour = Boolean.parseBoolean(autoArmourString);
        }

        try {
            if (partType.isTwoSided()) {
                String[] armours = aReader.getAttribute("armor").split("/");
                if (armours.length == 2) {
                    builder.push(new CmdSetArmour(null, loadout, loadoutPart, ArmourSide.FRONT,
                            Integer.parseInt(armours[0]), !autoArmour));
                    builder.push(new CmdSetArmour(null, loadout, loadoutPart, ArmourSide.BACK,
                            Integer.parseInt(armours[1]), !autoArmour));
                }
            }
            else {
                builder.push(new CmdSetArmour(null, loadout, loadoutPart, ArmourSide.ONLY,
                        Integer.parseInt(aReader.getAttribute("armor")), !autoArmour));
            }
        }
        catch (IllegalArgumentException exception) {
            builder.pushError(exception);
        }

        while (aReader.hasMoreChildren()) {
            aReader.moveDown();
            if ("item".equals(aReader.getNodeName())) {
                try {
                    Item item = (Item) aContext.convertAnother(null, Item.class);
                    item = CompatibilityHelper.fixArtemis(item, loadout.getUpgrades().getGuidance());
                    builder.push(new CmdAddItem(null, loadout, loadoutPart, item));
                }
                catch (Throwable t) {
                    builder.pushError(t);
                }
            }
            aReader.moveUp();
        }
    }
}
