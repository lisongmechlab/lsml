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
package lisong_mechlab.mwo_data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.DataCache;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.ChassisVariant;
import lisong_mechlab.model.chassi.ComponentOmniMech;
import lisong_mechlab.model.chassi.ComponentStandard;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Faction;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.quirks.Modifier;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.StructureUpgrade;
import lisong_mechlab.mwo_data.helpers.MdfCockpit;
import lisong_mechlab.mwo_data.helpers.MdfComponent;
import lisong_mechlab.mwo_data.helpers.MdfItem;
import lisong_mechlab.mwo_data.helpers.MdfMech;
import lisong_mechlab.mwo_data.helpers.MdfMovementTuning;
import lisong_mechlab.mwo_data.helpers.XMLItemStatsMech;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This class represents the XML content of the .mdf files.
 * 
 * @author Emily Björk
 */
public class MdfMechDefinition {
    public MdfMech            Mech;
    public List<MdfComponent> ComponentList;
    @XStreamAsAttribute
    public String             Version;
    public MdfCockpit         Cockpit;

    public MdfMovementTuning  MovementTuningConfiguration;

    public List<XMLQuirk>     QuirkList;

    public boolean isOmniMech() {
        for (MdfComponent component : ComponentList) {
            if (component.isOmniComponent())
                return true;
        }
        return false;
    }

    public ChassisStandard asChassisStandard(XMLItemStatsMech aMech, DataCache aDataCache, XMLMechIdMap aMechIdMap,
            XMLHardpoints aHardPointsXML) {
        int baseVariant = getBaseVariant(aMechIdMap, aMech);
        String name = Localization.key2string("@" + aMech.name);
        String shortName = Localization.key2string("@" + aMech.name + "_short");
        Faction faction = Faction.fromMwo(aMech.faction);

        ComponentStandard[] components = new ComponentStandard[Location.values().length];
        for (MdfComponent component : ComponentList) {
            if (component.isRear()) {
                continue;
            }
            ComponentStandard componentStandard = component.asComponentStandard(aDataCache, aHardPointsXML, aMech.name);
            components[componentStandard.getLocation().ordinal()] = componentStandard;
        }

        List<Modifier> quirkList = new ArrayList<>();
        if (null != QuirkList) {
            for (XMLQuirk quirk : QuirkList) {
                quirkList.add(quirk.toQuirk(aDataCache));
            }
        }

        return new ChassisStandard(aMech.id, aMech.name, aMech.chassis, name, shortName, Mech.MaxTons,
                ChassisVariant.fromString(name, Mech.VariantType), baseVariant,
                MovementTuningConfiguration.asMovementProfile(), faction, Mech.MinEngineRating, Mech.MaxEngineRating,
                Mech.MaxJumpJets, components, Cockpit.TechSlots, Cockpit.ConsumableSlots, Cockpit.WeaponModSlots,
                quirkList);
    }

    public ChassisOmniMech asChassisOmniMech(XMLItemStatsMech aMech, DataCache aDataCache, XMLMechIdMap aMechIdMap,
            XMLLoadout aLoadout) throws IOException {
        int baseVariant = getBaseVariant(aMechIdMap, aMech);
        String name = Localization.key2string("@" + aMech.name);
        String shortName = Localization.key2string("@" + aMech.name + "_short");
        Faction faction = Faction.fromMwo(aMech.faction);

        ComponentOmniMech[] components = new ComponentOmniMech[Location.values().length];

        // Determine engine type first
        Engine engine = null;
        for (MdfComponent component : ComponentList) {
            if (component.getLocation() == Location.CenterTorso) {
                if (component.isRear()) {
                    continue;
                }
                ComponentOmniMech componentStandard = component.asComponentOmniMech(aDataCache, null);
                for (Item item : componentStandard.getFixedItems()) {
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

        for (MdfComponent component : ComponentList) {
            if (component.getLocation() != Location.CenterTorso) {
                if (component.isRear()) {
                    continue;
                }
                ComponentOmniMech componentStandard = component.asComponentOmniMech(aDataCache, engine);
                components[componentStandard.getLocation().ordinal()] = componentStandard;
            }
        }

        StructureUpgrade structure = (StructureUpgrade) aDataCache.findUpgrade(aLoadout.upgrades.structure.ItemID);
        ArmorUpgrade armor = (ArmorUpgrade) aDataCache.findUpgrade(aLoadout.upgrades.armor.ItemID);
        HeatSinkUpgrade heatSink = (HeatSinkUpgrade) aDataCache.findUpgrade(aLoadout.upgrades.heatsinks.ItemID);

        return new ChassisOmniMech(aMech.id, aMech.name, aMech.chassis, name, shortName, Mech.MaxTons,
                ChassisVariant.fromString(name, Mech.VariantType), baseVariant,
                MovementTuningConfiguration.asMovementProfile(), faction, components, Cockpit.TechSlots,
                Cockpit.ConsumableSlots, Cockpit.WeaponModSlots, structure, armor, heatSink);
    }

    private int getBaseVariant(XMLMechIdMap aMechIdMap, XMLItemStatsMech aMech) {
        int baseVariant = -1;
        for (XMLMechIdMap.Mech mappedmech : aMechIdMap.MechIdMap) {
            if (mappedmech.variantID == aMech.id) {
                baseVariant = mappedmech.baseID;
                break;
            }
        }
        if (Mech.VariantParent > 0) {
            if (baseVariant > 0 && Mech.VariantParent != baseVariant) {
                // Inconsistency between MechIDMap and ParentAttribute.
                throw new IllegalArgumentException("MechIDMap.xml and VariantParent attribute are inconsistent for: "
                        + aMech.name);
            }
            baseVariant = Mech.VariantParent;
        }
        return baseVariant;
    }

    public static MdfMechDefinition fromXml(InputStream is) {
        XStream xstream = new XStream(new StaxDriver()) {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    @Override
                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        if (definedIn == Object.class) {
                            return false;
                        }
                        return super.shouldSerializeMember(definedIn, fieldName);
                    }
                };
            }
        };
        xstream.autodetectAnnotations(true);
        xstream.alias("MechDefinition", MdfMechDefinition.class);
        xstream.alias("Mech", MdfMech.class);
        xstream.alias("Cockpit", MdfCockpit.class);
        xstream.alias("Component", MdfComponent.class);
        xstream.alias("Internal", MdfItem.class);
        xstream.alias("Fixed", MdfItem.class);
        xstream.alias("MovementTuningConfiguration", MdfMovementTuning.class);
        xstream.alias("Quirk", XMLQuirk.class);
        return (MdfMechDefinition) xstream.fromXML(is);
    }
}
