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
package lisong_mechlab.mwo_data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.DataCache;
import lisong_mechlab.model.chassi.HardPoint;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.quirks.Quirk;
import lisong_mechlab.model.quirks.Quirks;
import lisong_mechlab.mwo_data.XMLOmniPods.XMLOmniPodsSet.XMLOmniPodsComponent;
import lisong_mechlab.mwo_data.helpers.ItemStatsOmniPodType;
import lisong_mechlab.mwo_data.helpers.MdfComponent;
import lisong_mechlab.mwo_data.helpers.MdfComponent.MdfHardpoint;
import lisong_mechlab.mwo_data.helpers.MdfItem;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This class is used for parsing {@link OmniPod}s from "chassis-omnipods.xml" files.
 * 
 * @author Li Song
 */
public class XMLOmniPods {
    public static class XMLOmniPodsSet {
        public static class XMLOmniPodsSetBonuses {
            public static class XMLOmniPodsBonus {
                @XStreamAsAttribute
                private int                    PieceCount;
                @XStreamImplicit(itemFieldName = "Quirk")
                private List<XMLQuirk> quirks;
            }

            XMLOmniPodsBonus Bonus;
        }

        public static class XMLOmniPodsComponent {
            @XStreamAsAttribute
            private String                 name;
            @XStreamImplicit(itemFieldName = "Fixed")
            private List<MdfItem>          fixedItems;
            @XStreamImplicit(itemFieldName = "Internal")
            private List<MdfItem>          internals;
            @XStreamImplicit(itemFieldName = "Hardpoint")
            private List<MdfHardpoint>     hardpoints;
            @XStreamImplicit(itemFieldName = "Quirk")
            private List<XMLQuirk> quirks;
            @XStreamAsAttribute
            private int                    CanEquipECM;
        }

        @XStreamAsAttribute
        private String             name;

        XMLOmniPodsSetBonuses      SetBonuses;

        @XStreamImplicit(itemFieldName = "component")
        List<XMLOmniPodsComponent> omniPods;
    }

    @XStreamImplicit(itemFieldName = "Set")
    List<XMLOmniPodsSet> sets;

    public List<OmniPod> asOmniPods(XMLItemStats aItemStatsXml, XMLHardpoints aHardPointsXML, DataCache aDataCache) {
        List<OmniPod> ans = new ArrayList<>();

        for (XMLOmniPodsSet set : sets) {
            for (XMLOmniPodsComponent component : set.omniPods) {

                ItemStatsOmniPodType type = null;
                for (ItemStatsOmniPodType omniPodType : aItemStatsXml.OmniPodList) {
                    if (omniPodType.set.equals(set.name) && omniPodType.component.equals(component.name)) {
                        type = omniPodType;
                        break;
                    }
                }
                if (type == null) {
                    throw new IllegalArgumentException("No matching omnipod in itemstats.xml");
                }

                int maxJumpjets = 0;
                int maxPilotModules = 0;
                List<Quirk> quirksList = new ArrayList<>();
                if (null != component.quirks) {
                    for (XMLQuirk quirk : component.quirks) {
                        if ("jumpjetslots_additive".equals(quirk.name.toLowerCase())) {
                            maxJumpjets = (int) quirk.value;
                        }
                        else {
                            quirksList.add(quirk.toQuirk(aDataCache));
                        }
                        // TODO: check for pilot modules as soon as we know what they're called.
                    }
                }

                Location location = Location.fromMwoName(component.name);
                List<HardPoint> hardPoints = MdfComponent.getHardPoints(location, aHardPointsXML, component.hardpoints,
                        component.CanEquipECM, set.name);
                Quirks quirks = new Quirks(quirksList);

                List<Item> fixedItems = MdfComponent.getFixedItems(aDataCache, component.internals,
                        component.fixedItems);
                List<Item> toggleableItems = MdfComponent.getToggleableItems(aDataCache, component.internals,
                        component.fixedItems);

                ans.add(new OmniPod(type.id, location, type.chassis, set.name, quirks, hardPoints, fixedItems,
                        toggleableItems, maxJumpjets, maxPilotModules));
            }
        }

        return ans;
    }

    public static XMLOmniPods fromXml(InputStream is) {
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
        xstream.alias("OmniPods", XMLOmniPods.class);
        return (XMLOmniPods) xstream.fromXML(is);
    }
}
