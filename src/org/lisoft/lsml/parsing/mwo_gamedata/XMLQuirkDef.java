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
package org.lisoft.lsml.parsing.mwo_gamedata;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.modifiers.ModifierDescription.Operation;
import org.lisoft.lsml.model.modifiers.ModifierDescription.ValueType;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Used for parsing the quirk definitions
 * 
 * @author Emily Björk
 */
public class XMLQuirkDef {
    public class Category {
        public class Quirk {
            public class Modify {
                @XStreamAsAttribute
                public String specifier;
                @XStreamAsAttribute
                public String operation;
                @XStreamAsAttribute
                public String context;
                @SuppressWarnings("hiding")
                @XStreamAsAttribute
                public String loc;
            }

            @SuppressWarnings("hiding")
            @XStreamAsAttribute
            public String       name;
            @XStreamAsAttribute
            public String       loc;
            @XStreamImplicit(itemFieldName = "Modify")
            public List<Modify> modifiers;
        }

        @XStreamAsAttribute
        public String         name;
        @XStreamImplicit(itemFieldName = "Quirk")
        public List<Quirk>    quirks;

        @XStreamImplicit(itemFieldName = "Category")
        public List<Category> subcategory;

        List<ModifierDescription> getAllModifiers() {
            List<ModifierDescription> ans = new ArrayList<>();
            if (quirks != null) {
                for (Category.Quirk quirk : quirks) {
                    for (Category.Quirk.Modify modify : quirk.modifiers) {
                        // quirk.name_[modify.specifier]_modify.operation
                        String keyName = quirk.name;
                        if (modify.specifier != null && !modify.specifier.isEmpty())
                            keyName += "_" + modify.specifier;
                        keyName += "_" + modify.operation;
                        keyName = keyName.toLowerCase();

                        // qrk_{quirk.loctag||quirk.name}_[modify.specifier]_modify.operation
                        // String uiKey = "qrk_";
                        // if (quirk.loc != null && !quirk.loc.isEmpty())
                        // uiKey += quirk.loc;
                        // else
                        // uiKey = quirk.name;
                        // if (modify.specifier != null && !modify.specifier.isEmpty())
                        // uiKey += "_" + modify.specifier;
                        // uiKey += "_" + Operation.fromString(modify.operation).uiAbbrev();
                        // uiKey = uiKey.toLowerCase();

                        String uiName;
                        if (quirk.loc != null && !quirk.loc.isEmpty()) {
                            uiName = quirk.loc;
                        }
                        else {
                            uiName = quirk.name.toUpperCase();
                        }

                        if (modify.loc != null && !modify.loc.isEmpty()) {
                            uiName += " " + modify.loc;
                        }
                        else if (modify.specifier != null && !modify.specifier.isEmpty()) {
                            uiName += " " + modify.specifier.toUpperCase();
                        }

                        ans.add(new ModifierDescription(uiName, keyName, Operation.fromString(modify.operation), Arrays
                                .asList(quirk.name), modify.specifier, ValueType.fromMwo(modify.context)));
                    }
                }
            }
            if (subcategory != null) {
                for (Category category : subcategory) {
                    ans.addAll(category.getAllModifiers());
                }
            }
            return ans;
        }
    }

    @XStreamImplicit(itemFieldName = "Category")
    public List<Category> QuirkList;

    public static List<ModifierDescription> fromXml(InputStream is) {
        XMLQuirkDef xml = getXml(is);
        return xml.asModifiers();
    }

    private List<ModifierDescription> asModifiers() {
        List<ModifierDescription> ans = new ArrayList<>();
        for (Category category : QuirkList) {
            ans.addAll(category.getAllModifiers());
        }
        return ans;
    }

    private static XMLQuirkDef getXml(InputStream is) {
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
        xstream.alias("QuirkList", XMLQuirkDef.class);

        return (XMLQuirkDef) xstream.fromXML(is);
    }
}
