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
package org.lisoft.mwo_data.mwo_parser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.io.InputStream;
import java.util.List;
import org.lisoft.mwo_data.Database;

/**
 * This class models the format of MechIdMap.xml from the game data files to facilitate easy
 * parsing.
 *
 * @author Li Song
 */
@XStreamAlias("MechIdMap")
class XMLMechIdMap {
  static class Mech {
    @XStreamAsAttribute int baseID;
    @XStreamAsAttribute int variantID;
  }

  @XStreamImplicit(itemFieldName = "Mech")
  List<Mech> MechIdMap;

  private XMLMechIdMap() {}

  static XMLMechIdMap fromXml(InputStream is) {
    final XStream xstream = Database.makeMwoSuitableXStream();
    xstream.alias("MechIdMap", XMLMechIdMap.class);
    return (XMLMechIdMap) xstream.fromXML(is);
  }
}
