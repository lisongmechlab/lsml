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
package org.lisoft.lsml.model.datacache.gamedata;

import java.util.ArrayList;
import java.util.List;

import org.lisoft.lsml.model.datacache.gamedata.GameVFS.GameFile;
import org.lisoft.lsml.model.datacache.gamedata.helpers.ItemStatsModule;
import org.lisoft.lsml.model.datacache.gamedata.helpers.ItemStatsOmniPodType;
import org.lisoft.lsml.model.datacache.gamedata.helpers.ItemStatsUpgradeType;
import org.lisoft.lsml.model.datacache.gamedata.helpers.ItemStatsWeapon;
import org.lisoft.lsml.model.datacache.gamedata.helpers.XMLItemStatsMech;
import org.lisoft.lsml.model.datacache.gamedata.helpers.XMLPilotModuleStats;
import org.lisoft.lsml.model.datacache.gamedata.helpers.XMLPilotModuleWeaponStats;
import org.lisoft.lsml.model.datacache.gamedata.helpers.XMLWeaponStats;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This class models the format of ItemStats.xml from the game data files to facilitate easy parsing.
 * 
 * @author Emily Björk
 */
public class XMLItemStats {
    @XStreamImplicit
    public List<XMLItemStatsMech>     MechList        = new ArrayList<>();
    @XStreamImplicit
    public List<ItemStatsWeapon>      WeaponList      = new ArrayList<>();
    @XStreamImplicit
    public List<ItemStatsModule>      ModuleList      = new ArrayList<>();
    @XStreamImplicit
    public List<ItemStatsUpgradeType> UpgradeTypeList = new ArrayList<>();
    @XStreamImplicit
    public List<ItemStatsOmniPodType> OmniPodList     = new ArrayList<>();

    public static XMLItemStats fromXml(GameFile aGameFile) {
        XStream xstream = new XStream(new StaxDriver(new NoNameCoder())) {
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
        xstream.alias("WeaponList", XMLItemStats.class);
        xstream.alias("MechList", XMLItemStats.class);
        xstream.alias("OmniPodList", XMLItemStats.class);
        xstream.alias("UpgradeTypeList", XMLItemStats.class);
        xstream.alias("ModuleList", XMLItemStats.class);

        xstream.alias("Mech", XMLItemStatsMech.class);
        xstream.alias("Weapon", ItemStatsWeapon.class);
        xstream.alias("Module", ItemStatsModule.class);
        xstream.alias("Internal", ItemStatsModule.class);
        xstream.alias("UpgradeType", ItemStatsUpgradeType.class);
        xstream.alias("OmniPod", ItemStatsOmniPodType.class);
        xstream.alias("PilotModuleStats", XMLPilotModuleStats.class);
        xstream.alias("WeaponStats", XMLWeaponStats.class);
        xstream.alias("PilotModuleWeaponStats", XMLPilotModuleWeaponStats.class);

        // Fixes for broken XML from PGI
        xstream.aliasAttribute("Ctype", "CType");
        // xstream.aliasAttribute("talentid", "talentId");

        return (XMLItemStats) xstream.fromXML(aGameFile.stream);
    }

    public void append(GameFile aGameFile) {
        XMLItemStats xml = fromXml(aGameFile);
        if (null != xml.MechList)
            MechList.addAll(xml.MechList);
        if (null != xml.WeaponList)
            WeaponList.addAll(xml.WeaponList);
        if (null != xml.ModuleList)
            ModuleList.addAll(xml.ModuleList);
        if (null != xml.UpgradeTypeList)
            UpgradeTypeList.addAll(xml.UpgradeTypeList);
        if (null != xml.OmniPodList)
            OmniPodList.addAll(xml.OmniPodList);
    }
}
