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

import org.lisoft.lsml.model.NoSuchItemException;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.loadout.LoadoutBuilder;
import org.lisoft.lsml.model.upgrades.Upgrade;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * XStream converter for {@link Upgrade}s.
 * 
 * @author Li Song
 */
public class UpgradeConverter implements Converter {

    private final LoadoutBuilder builder;

    public UpgradeConverter(LoadoutBuilder aBuilder) {
        builder = aBuilder;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean canConvert(Class aClass) {
        return Upgrade.class.isAssignableFrom(aClass);
    }

    @Override
    public void marshal(Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
        final Upgrade item = (Upgrade) anObject;
        final int mwoIdx = item.getMwoId();
        aWriter.setValue(Integer.toString(mwoIdx));
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        final int mwoidx = Integer.parseInt(aReader.getValue());
        try {
            return UpgradeDB.lookup(mwoidx);
        }
        catch (final NoSuchItemException e) {
            builder.pushError(e);
        }
        return null;
    }

}
