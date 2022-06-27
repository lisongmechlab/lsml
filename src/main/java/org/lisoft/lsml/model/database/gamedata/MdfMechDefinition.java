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
package org.lisoft.lsml.model.database.gamedata;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.lisoft.lsml.model.chassi.*;
import org.lisoft.lsml.model.database.Database;
import org.lisoft.lsml.model.database.gamedata.helpers.*;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents the XML content of the .mdf files.
 *
 * @author Li Song
 */
public class MdfMechDefinition {
    public List<MdfComponent> ComponentList;
    public MdfMech Mech;
    public MdfMovementTuning MovementTuningConfiguration;
    public List<XMLQuirk> QuirkList;
    @XStreamAsAttribute
    public String Version;

    public static MdfMechDefinition fromXml(InputStream is) {
        final XStream xstream = Database.makeMwoSuitableXStream();
        xstream.alias("MechDefinition", MdfMechDefinition.class);
        xstream.alias("Mech", MdfMech.class);
        xstream.alias("Component", MdfComponent.class);
        xstream.alias("Internal", MdfItem.class);
        xstream.alias("Fixed", MdfItem.class);
        xstream.alias("MovementTuningConfiguration", MdfMovementTuning.class);
        xstream.alias("Quirk", XMLQuirk.class);
        return (MdfMechDefinition) xstream.fromXML(is);
    }

    public ChassisOmniMech asChassisOmniMech(XMLItemStatsMech aMech, Map<Integer, Object> aId2obj,
                                             XMLMechIdMap aMechIdMap, XMLLoadout aLoadout) throws IOException {
        final int baseVariant = getBaseVariant(aMechIdMap, aMech);
        final String name = Localisation.key2string("@" + aMech.name);
        final String shortName = Localisation.key2string("@" + aMech.name + "_short");
        final Faction faction = Faction.fromMwo(aMech.faction);

        final ComponentOmniMech[] components = new ComponentOmniMech[Location.values().length];

        // Determine engine type first
        Engine engine = null;
        for (final MdfComponent component : ComponentList) {
            if (component.getLocation() == Location.CenterTorso) {
                if (component.isRear()) {
                    continue;
                }
                final ComponentOmniMech componentStandard = component.asComponentOmniMech(aId2obj, null);
                for (final Item item : componentStandard.getFixedItems()) {
                    if (item instanceof Engine) {
                        engine = (Engine) item;
                    }
                }
                components[componentStandard.getLocation().ordinal()] = componentStandard;
            }
        }

        if (null == engine) {
            throw new IOException("Unable to find engine for " + name);
        }

        for (final MdfComponent component : ComponentList) {
            if (component.getLocation() != Location.CenterTorso) {
                if (component.isRear()) {
                    continue;
                }
                final ComponentOmniMech componentStandard = component.asComponentOmniMech(aId2obj, engine);
                components[componentStandard.getLocation().ordinal()] = componentStandard;
            }
        }

        final StructureUpgrade structure = (StructureUpgrade) aId2obj.get(aLoadout.upgrades.structure.ItemID);
        final ArmourUpgrade armour = (ArmourUpgrade) aId2obj.get(aLoadout.upgrades.armor.ItemID);
        final HeatSinkUpgrade heatSink = (HeatSinkUpgrade) aId2obj.get(aLoadout.upgrades.heatsinks.ItemID);

        return new ChassisOmniMech(aMech.id, aMech.name, aMech.chassis, name, shortName, Mech.MaxTons,
                                   ChassisVariant.fromString(name, Mech.VariantType), baseVariant,
                                   MovementTuningConfiguration.asMovementProfile(), faction, components, structure,
                                   armour, heatSink, Mech.CanEquipMASC == 1);
    }

    public ChassisStandard asChassisStandard(XMLItemStatsMech aMech, Map<Integer, Object> aId2obj,
                                             Map<String, ModifierDescription> aModifierDescriptors,
                                             XMLMechIdMap aMechIdMap, XMLHardpoints aHardPointsXML) {
        final int baseVariant = getBaseVariant(aMechIdMap, aMech);
        final String name = Localisation.key2string("@" + aMech.name);
        String shortName;
        try {
            shortName = Localisation.key2string("@" + aMech.name + "_short");
        } catch (final IllegalArgumentException e) {
            shortName = name;
        }
        final Faction faction = Faction.fromMwo(aMech.faction);

        final ComponentStandard[] components = new ComponentStandard[Location.values().length];
        for (final MdfComponent component : ComponentList) {
            if (component.isRear()) {
                continue;
            }
            final ComponentStandard componentStandard = component.asComponentStandard(aId2obj, aHardPointsXML,
                                                                                      aMech.name);
            components[componentStandard.getLocation().ordinal()] = componentStandard;
        }

        final List<Modifier> quirkList = new ArrayList<>();
        if (null != QuirkList) {
            for (final XMLQuirk quirk : QuirkList) {
                quirkList.add(QuirkModifiers.createModifier(quirk, aModifierDescriptors, aId2obj));
            }
        }

        return new ChassisStandard(aMech.id, aMech.name, aMech.chassis, name, shortName, Mech.MaxTons,
                                   ChassisVariant.fromString(name, Mech.VariantType), baseVariant,
                                   MovementTuningConfiguration.asMovementProfile(), faction, Mech.MinEngineRating,
                                   Mech.MaxEngineRating, Mech.MaxJumpJets, components, quirkList,
                                   Mech.CanEquipMASC == 1);
    }

    public boolean isOmniMech() {
        for (final MdfComponent component : ComponentList) {
            if (component.isOmniComponent()) {
                return true;
            }
        }
        return false;
    }

    public boolean isUsable() {
        return 0 == Mech.UnstoppableByPlayers;
    }

    private int getBaseVariant(XMLMechIdMap aMechIdMap, XMLItemStatsMech aMech) {
        int baseVariant = -1;
        for (final XMLMechIdMap.Mech mappedmech : aMechIdMap.MechIdMap) {
            if (mappedmech.variantID == aMech.id) {
                baseVariant = mappedmech.baseID;
                break;
            }
        }
        if (Mech.VariantParent > 0) {
            if (baseVariant > 0 && Mech.VariantParent != baseVariant) {
                // Inconsistency between MechIDMap and ParentAttribute.
                throw new IllegalArgumentException(
                        "MechIDMap.xml and VariantParent attribute are inconsistent for: " + aMech.name);
            }
            baseVariant = Mech.VariantParent;
        }
        return baseVariant;
    }
}
