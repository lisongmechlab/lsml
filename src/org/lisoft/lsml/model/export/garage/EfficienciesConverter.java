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
package org.lisoft.lsml.model.export.garage;

import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This converter is used for loading {@link Efficiencies} for {@link LoadoutBase}s.
 * 
 * @author Emily Björk
 */
public class EfficienciesConverter implements Converter {
    private static final String DOUBLE_BASICS = "doubleBasics";
    private static final String EFFICIENCY    = "Efficiency";
    private static final String _1            = "1";
    private static final String _2            = "2";
    private static final String VERSION       = "version";

    @Override
    public boolean canConvert(Class aType) {
        return Efficiencies.class == aType;
    }

    @Override
    public void marshal(Object aSource, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
        Efficiencies efficiencies = (Efficiencies) aSource;
        aWriter.addAttribute(VERSION, _2);
        aWriter.addAttribute(DOUBLE_BASICS, Boolean.toString(efficiencies.hasDoubleBasics()));
        for (MechEfficiencyType type : MechEfficiencyType.values()) {
            if (efficiencies.hasEfficiency(type)) {
                aWriter.startNode(EFFICIENCY);
                aWriter.setValue(type.toString());
                aWriter.endNode();
            }
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        Efficiencies ans = new Efficiencies();
        String version = aReader.getAttribute(VERSION);
        if (version == null || version.isEmpty() || _1.equals(version)) {
            while (aReader.hasMoreChildren()) {
                aReader.moveDown();
                boolean value = Boolean.parseBoolean(aReader.getValue());
                String eff = aReader.getNodeName();
                if (DOUBLE_BASICS.equals(eff)) {
                    ans.setDoubleBasics(value, null);
                }
                else {
                    ans.setEfficiency(MechEfficiencyType.fromOldName(aReader.getNodeName()), value, null);
                }
                aReader.moveUp();
            }
        }
        else if (_2.equals(version)) {
            ans.setDoubleBasics(Boolean.parseBoolean(aReader.getAttribute(DOUBLE_BASICS)), null);
            while (aReader.hasMoreChildren()) {
                aReader.moveDown();
                ans.setEfficiency(MechEfficiencyType.valueOf(aReader.getValue()), true, null);
                aReader.moveUp();
            }
        }
        else {
            throw new IllegalArgumentException("Unsupported version of efficiencies: " + version);
        }
        return ans;
    }

}
