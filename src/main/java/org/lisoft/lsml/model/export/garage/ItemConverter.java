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

import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.Item;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This converter serializes an item as a reference instead of as a full item.
 *
 * @author Li Song
 */
public class ItemConverter implements Converter {

    @Override
    public boolean canConvert(Class aClass) {
        return Item.class.isAssignableFrom(aClass);
    }

    @Override
    public void marshal(Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
        final Item item = (Item) anObject;
        final int mwoIdx = item.getMwoId();
        if (mwoIdx > 0) {
            aWriter.addAttribute("id", Integer.toString(mwoIdx));
        }
        else {
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
            return ItemDB.lookup(mwoidx);
        }
        return ItemDB.lookup(aReader.getAttribute("key"));
    }
}
