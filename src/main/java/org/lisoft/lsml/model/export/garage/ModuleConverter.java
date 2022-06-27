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
package org.lisoft.lsml.model.export.garage;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.lisoft.lsml.model.NoSuchItemException;
import org.lisoft.lsml.model.database.ConsumableDB;
import org.lisoft.lsml.model.item.Consumable;
import org.lisoft.lsml.model.loadout.LoadoutBuilder;

/**
 * This converter serialises a {@link Consumable} as a reference instead of as a full item.
 *
 * @author Li Song
 */
public class ModuleConverter implements Converter {

    final private LoadoutBuilder builder;

    public ModuleConverter(LoadoutBuilder aBuilder) {
        builder = aBuilder;
    }

    @Override
    public boolean canConvert(Class aClass) {
        return Consumable.class.isAssignableFrom(aClass);
    }

    @Override
    public void marshal(Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
        final Consumable item = (Consumable) anObject;
        final int mwoIdx = item.getId();
        if (mwoIdx > 0) {
            aWriter.addAttribute("id", Integer.toString(mwoIdx));
        } else {
            aWriter.addAttribute("key", item.getKey());
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        String id = aReader.getAttribute("id");
        if (id == null || id.isEmpty()) {
            id = aReader.getValue();
        }
        if (id != null && !id.isEmpty()) {
            final int mwoidx = Integer.parseInt(id);
            try {
                return ConsumableDB.lookup(mwoidx);
            } catch (final NoSuchItemException e) {
                builder.pushError(e);
                return null;
            }
        }
        try {
            return ConsumableDB.lookup(aReader.getAttribute("key"));
        } catch (final NoSuchItemException e) {
            builder.pushError(e);
        }
        return null;
    }
}
