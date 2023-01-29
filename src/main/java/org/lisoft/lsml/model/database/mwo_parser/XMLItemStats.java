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
package org.lisoft.lsml.model.database.mwo_parser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;
import java.util.List;
import org.lisoft.lsml.model.database.Database;
import org.lisoft.lsml.model.database.mwo_parser.GameVFS.GameFile;

/**
 * This class models the format of ItemStats.xml from the game data files to facilitate easy
 * parsing.
 *
 * @author Li Song
 */
class XMLItemStats {
  @XStreamImplicit List<XMLItemStatsMech> MechList = new ArrayList<>();
  @XStreamImplicit List<ItemStatsModule> ModuleList = new ArrayList<>();
  @XStreamImplicit List<ItemStatsOmniPodType> OmniPodList = new ArrayList<>();
  @XStreamImplicit List<ItemStatsUpgradeType> UpgradeTypeList = new ArrayList<>();
  @XStreamImplicit List<ItemStatsWeapon> WeaponList = new ArrayList<>();

  static XMLItemStats fromXml(GameFile aGameFile) {
    final XStream xstream = Database.makeMwoSuitableXStream();
    xstream.alias("WeaponList", XMLItemStats.class);
    xstream.alias("MechList", XMLItemStats.class);
    xstream.alias("OmniPodList", XMLItemStats.class);
    xstream.alias("UpgradeTypeList", XMLItemStats.class);
    xstream.alias("ModuleList", XMLItemStats.class);
    xstream.alias("MechEfficiencies", XMLItemStats.class);

    xstream.alias("Mech", XMLItemStatsMech.class);
    xstream.alias("Weapon", ItemStatsWeapon.class);
    xstream.alias("Module", ItemStatsModule.class);
    xstream.alias("Internal", ItemStatsModule.class);
    xstream.alias("UpgradeType", ItemStatsUpgradeType.class);
    xstream.alias("OmniPod", ItemStatsOmniPodType.class);

    // Fixes for broken XML from PGI
    xstream.aliasAttribute("Ctype", "CType");
    // xstream.aliasAttribute("talentid", "talentId");

    return (XMLItemStats) xstream.fromXML(aGameFile.stream);
  }

  void append(GameFile aGameFile) {
    final XMLItemStats xml = fromXml(aGameFile);
    if (null != xml.MechList) {
      MechList.addAll(xml.MechList);
    }
    if (null != xml.WeaponList) {
      WeaponList.addAll(xml.WeaponList);
    }
    if (null != xml.ModuleList) {
      ModuleList.addAll(xml.ModuleList);
    }
    if (null != xml.UpgradeTypeList) {
      UpgradeTypeList.addAll(xml.UpgradeTypeList);
    }
    if (null != xml.OmniPodList) {
      OmniPodList.addAll(xml.OmniPodList);
    }
  }
}
