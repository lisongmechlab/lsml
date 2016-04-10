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
package org.lisoft.lsml.model.datacache;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.modifiers.ModifierDescription.ModifierType;
import org.lisoft.lsml.model.modifiers.ModifierDescription.Operation;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ModifierDescriptionConverter implements Converter {
    private static final String SELECTORS2 = "selectors";
    private static final String TYPE = "type";
    private static final String SPECIFIER = "specifier";
    private static final String OP = "op";
    private static final String KEY = "key";
    private static final String NAME = "name";

    @Override
    public boolean canConvert(Class aType) {
        return ModifierDescription.class.isAssignableFrom(aType);
    }

    @Override
    public void marshal(Object aSource, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
        ModifierDescription modifierDescription = (ModifierDescription) aSource;

        aWriter.addAttribute(NAME, modifierDescription.getUiName());
        if (modifierDescription.getKey() != null) {
            aWriter.addAttribute(KEY, modifierDescription.getKey());
        }
        aWriter.addAttribute(OP, modifierDescription.getOperation().toString());
        if (modifierDescription.getSpecifier() != null) {
            aWriter.addAttribute(SPECIFIER, modifierDescription.getSpecifier());
        }
        aWriter.addAttribute(TYPE, modifierDescription.getModifierType().toString());

        String selectors = modifierDescription.getSelectors().stream().collect(Collectors.joining(","));
        aWriter.addAttribute(SELECTORS2, selectors);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        final String uiName = aReader.getAttribute(NAME);
        final String key = aReader.getAttribute(KEY);
        final String specifier = aReader.getAttribute(SPECIFIER);
        final ModifierType type = ModifierType.valueOf(aReader.getAttribute(TYPE));
        final Operation op = Operation.fromString(aReader.getAttribute(OP));
        final List<String> selectors = Arrays.asList(aReader.getAttribute(SELECTORS2).split(","));

        return new ModifierDescription(uiName, key, op, selectors, specifier, type);
    }

}
