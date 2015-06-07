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
package org.lisoft.lsml.parsing.datacache;

import org.lisoft.lsml.model.chassi.ChassisDB;
import org.lisoft.lsml.model.chassi.ChassisStandard;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ChassiConverter implements Converter {

    @Override
    public boolean canConvert(Class aClass) {
        return ChassisStandard.class == aClass;
    }

    @Override
    public void marshal(Object anObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
        ChassisStandard chassi = (ChassisStandard) anObject;
        aWriter.setValue(chassi.getNameShort());
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        String variation = aReader.getValue();
        return ChassisDB.lookup(variation);
    }

}
