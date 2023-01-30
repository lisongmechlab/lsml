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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lisoft.lsml.model.database.Database;
import org.lisoft.lsml.model.database.mwo_parser.GameVFS.GameFile;

/**
 * In the before times, all the data files lived happily in a single file called "ItemStats.xml".
 * Then the mean Grinch came and split the file up into multiple smaller, more manageable files that
 * still had the same structure and elements. This class facilitates merging all the smaller xml
 * files into one larger in-memory structure that can be use resolve references between elements,
 * inheritance and construct links between items (such as weapon to ammo).
 *
 * @author Li Song
 */
class RawMergedXML {
  @XStreamImplicit List<MechReferenceXML> MechList = new ArrayList<>();
  @XStreamImplicit List<ModuleXML> ModuleList = new ArrayList<>();
  @XStreamImplicit List<ItemStatsOmniPodType> OmniPodList = new ArrayList<>();
  @XStreamImplicit List<UpgradeTypeXML> UpgradeTypeList = new ArrayList<>();
  @XStreamImplicit List<WeaponXML> WeaponList = new ArrayList<>();

  Map<Integer, Object> id2Obj = new HashMap<>();

  static RawMergedXML fromXml(GameFile aGameFile) {
    final XStream xstream = Database.makeMwoSuitableXStream();
    xstream.alias("WeaponList", RawMergedXML.class);
    xstream.alias("MechList", RawMergedXML.class);
    xstream.alias("OmniPodList", RawMergedXML.class);
    xstream.alias("UpgradeTypeList", RawMergedXML.class);
    xstream.alias("ModuleList", RawMergedXML.class);
    xstream.alias("MechEfficiencies", RawMergedXML.class);

    xstream.alias("Mech", MechReferenceXML.class);
    xstream.alias("Weapon", WeaponXML.class);
    xstream.alias("Module", ModuleXML.class);
    xstream.alias("Internal", ModuleXML.class);
    xstream.alias("UpgradeType", UpgradeTypeXML.class);
    xstream.alias("OmniPod", ItemStatsOmniPodType.class);

    // Fixes for broken XML from PGI
    xstream.aliasAttribute("Ctype", "CType");
    // xstream.aliasAttribute("talentid", "talentId");

    return (RawMergedXML) xstream.fromXML(aGameFile.stream);
  }

  void append(GameFile aGameFile) {
    final RawMergedXML xml = fromXml(aGameFile);
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
