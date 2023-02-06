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
package org.lisoft.lsml.mwo_data.mwo_parser;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.io.File;

/**
 * In the file "Libs/Items/Mechs/Mechs.xml" there's a long list that maps an ID to faction, chassis
 * name and model of every mech. This class defines one such entry which defines what 'Mechs are in
 * the game and serves as a dictionary for further lookup of MDF files.
 */
class MechReferenceXML {
  private static final String MDF_ROOT = "Game/mechs/Objects/mechs/";
  private static final String STOCK_LOADOUTS_ROOT = "Game/Libs/MechLoadout/";

  @XStreamAsAttribute String chassis;
  @XStreamAsAttribute String faction;
  @XStreamAsAttribute int id;
  @XStreamAsAttribute String name;

  File mdfFilePath() {
    return new File(MDF_ROOT, chassis + "/" + name + ".mdf");
  }

  File stockLoadoutPath() {
    return new File(STOCK_LOADOUTS_ROOT + name + ".xml");
  }

  File hardPointsXmlPath() {
    return hardPointsXmlPath(chassis);
  }

  static File hardPointsXmlPath(String chassis) {
    return new File(MDF_ROOT, chassis + "/" + chassis + "-hardpoints.xml");
  }

  static File omniPodsXmlPath(String chassis) {
    return new File(MDF_ROOT, chassis + "/" + chassis + "-omnipods.xml");
  }
}
