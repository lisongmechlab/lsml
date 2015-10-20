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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.lisoft.lsml.model.datacache.gamedata.GameVFS.GameFile;
import org.lisoft.lsml.model.datacache.gamedata.XMLPilotTalents.XMLTalent.XMLRank;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Representation of the PilotTalents.xml file.
 * 
 * @author Emily Björk
 */
@XStreamAlias("PilotTalents")
public class XMLPilotTalents {

    public class XMLTalent {
        @XStreamAsAttribute
        public int    talentid;

        @XStreamAsAttribute
        public String name;

        @XStreamAsAttribute
        public int    ranks;

        public class XMLRank {
            @XStreamAsAttribute
            public int    id;

            @XStreamAsAttribute
            public String title;

            @XStreamAsAttribute
            public String description;
        }

        @XStreamImplicit
        public List<XMLRank> rankEntries;

        @XStreamAsAttribute
        public String        category;
    }

    @XStreamImplicit
    public List<XMLTalent> talents;

    public static XMLPilotTalents read(GameVFS aGameVfs) throws IOException {
        GameFile gameFile = aGameVfs.openGameFile(new File("Game/Libs/MechPilotTalents/PilotTalents.xml"));
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
        xstream.alias("PilotTalents", XMLPilotTalents.class);
        xstream.alias("Talent", XMLTalent.class);
        xstream.alias("Rank", XMLRank.class);

        return (XMLPilotTalents) xstream.fromXML(gameFile.stream);
    }

    public XMLTalent getTalent(int aTalentId) {
        for (XMLTalent talent : talents) {
            if (talent.talentid == aTalentId) {
                return talent;
            }
        }
        throw new IllegalArgumentException("No such talent!");
    }
}
