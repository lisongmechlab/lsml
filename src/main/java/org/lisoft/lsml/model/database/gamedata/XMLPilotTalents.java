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

import java.io.File;
import java.util.List;

import org.lisoft.lsml.model.database.Database;
import org.lisoft.lsml.model.database.gamedata.GameVFS.GameFile;
import org.lisoft.lsml.model.database.gamedata.XMLPilotTalents.XMLTalent.XMLRank;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Representation of the PilotTalents.xml file.
 *
 * @author Li Song
 */
@XStreamAlias("PilotTalents")
public class XMLPilotTalents {

    public static class XMLTalent {
        public static class XMLRank {
            @XStreamAsAttribute
            public int id;

            @XStreamAsAttribute
            public String title;

            @XStreamAsAttribute
            public String description;
        }

        @XStreamAsAttribute
        public int talentid;

        @XStreamAsAttribute
        public String name;

        @XStreamAsAttribute
        public int ranks;

        @XStreamImplicit
        public List<XMLRank> rankEntries;

        @XStreamAsAttribute
        public String category;
    }

    public static XMLPilotTalents read(GameVFS aGameVfs) throws Exception {
        try (GameFile gameFile = aGameVfs.openGameFile(new File("Game/Libs/MechPilotTalents/PilotTalents.xml"))) {
            final XStream xstream = Database.makeMwoSuitableXStream();
            xstream.alias("PilotTalents", XMLPilotTalents.class);
            xstream.alias("Talent", XMLTalent.class);
            xstream.alias("Rank", XMLRank.class);

            return (XMLPilotTalents) xstream.fromXML(gameFile.stream);
        }
    }

    @XStreamImplicit
    public List<XMLTalent> talents;

    public XMLTalent getTalent(int aTalentId) {
        for (final XMLTalent talent : talents) {
            if (talent.talentid == aTalentId) {
                return talent;
            }
        }
        throw new IllegalArgumentException("No such talent!");
    }
}
