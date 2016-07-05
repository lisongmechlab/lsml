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
package org.lisoft.lsml.model.datacache.gamedata;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.lisoft.lsml.model.datacache.DataCache;
import org.lisoft.lsml.model.modifiers.ModifierDescription;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Used for parsing the quirk definitions
 *
 * @author Li Song
 */
public class XMLQuirkDef {
    public static class Category {
        public static class Quirk {
            public static class Modify {
                @XStreamAsAttribute
                public String context;
                @XStreamAsAttribute
                public String loc;
                @XStreamAsAttribute
                public String operation;
                @XStreamAsAttribute
                public String specifier;
            }

            @XStreamAsAttribute
            public String loc;
            @XStreamImplicit(itemFieldName = "Modify")
            public List<Modify> modifiers;
            @XStreamAsAttribute
            public String name;
        }

        @XStreamAsAttribute
        public String name;
        @XStreamImplicit(itemFieldName = "Quirk")
        public List<Quirk> quirks;

        @XStreamImplicit(itemFieldName = "Category")
        public List<Category> subcategory;

        List<ModifierDescription> getAllModifiers() {
            final List<ModifierDescription> ans = new ArrayList<>();
            if (quirks != null) {
                for (final Category.Quirk quirk : quirks) {
                    ans.addAll(QuirkModifiers.createModifierDescription(quirk));
                }
            }
            if (subcategory != null) {
                for (final Category category : subcategory) {
                    ans.addAll(category.getAllModifiers());
                }
            }
            return ans;
        }
    }

    public static Map<String, ModifierDescription> fromXml(InputStream is) {
        final XStream xstream = DataCache.makeMwoSuitableXStream();
        xstream.alias("QuirkList", XMLQuirkDef.class);
        final XMLQuirkDef xml = (XMLQuirkDef) xstream.fromXML(is);
        return xml.QuirkList.stream().map(aCategory -> aCategory.getAllModifiers()).flatMap(List::stream)
                .collect(Collectors.toMap(aModifier -> aModifier.getKey(), Function.identity()));
    }

    @XStreamImplicit(itemFieldName = "Category")
    public List<Category> QuirkList;
}
