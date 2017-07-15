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

import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.modifiers.PilotSkills;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This converter is used for loading {@link PilotSkills} for {@link Loadout}s.
 *
 * @author Li Song
 */
public class EfficienciesConverter implements Converter {
    private static final String _1 = "1";
    private static final String _2 = "2";
    private static final String _3 = "3";
    private static final String VERSION = "version";

    @Override
    public boolean canConvert(Class aType) {
        return PilotSkills.class == aType;
    }

    @Override
    public void marshal(Object aSource, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
        // final PilotSkills efficiencies = (PilotSkills) aSource;
        aWriter.addAttribute(VERSION, _3);
        // TODO: Implement marshaling when we add pilot skill support.
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        final PilotSkills ans = new PilotSkills();
        final String version = aReader.getAttribute(VERSION);
        if (version == null || version.isEmpty() || _1.equals(version) || _2.equals(version)) {
            // Simply ignore this as it is data that is no longer supported by MWO.
        }
        else if (_3.equals(version)) {
            // TODO: Implement unmarshaling when we add pilot skill support.
        }
        else {
            throw new IllegalArgumentException("Unsupported version of efficiencies: " + version);
        }
        return ans;
    }

}
