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

import java.io.InputStream;
import java.util.List;

import org.lisoft.lsml.model.database.Database;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class models the format of MechIdMap.xml from the game data files to facilitate easy parsing.
 *
 * @author Li Song
 */
@XStreamAlias("MechIdMap")
public class XMLMechIdMap {
    public static class Mech {
        @XStreamAsAttribute
        public int baseID;
        @XStreamAsAttribute
        public int variantID;
    }

    public static XMLMechIdMap fromXml(InputStream is) {
        final XStream xstream = Database.makeMwoSuitableXStream();
        xstream.alias("MechIdMap", XMLMechIdMap.class);
        return (XMLMechIdMap) xstream.fromXML(is);
    }

    @XStreamImplicit(itemFieldName = "Mech")
    public List<Mech> MechIdMap;

    private XMLMechIdMap() {
    }
}
