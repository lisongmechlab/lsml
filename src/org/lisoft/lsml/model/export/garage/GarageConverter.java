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

import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.model.garage.MechGarage;
import org.lisoft.lsml.model.loadout.LoadoutBase;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class GarageConverter implements Converter {
    private static final String MECHS_NODE      = "mechs";
    private static final String DROP_SHIPS_NODE = "dropships";

    @Override
    public boolean canConvert(Class aClass) {
        return MechGarage.class == aClass;
    }

    @Override
    public void marshal(Object aObject, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
        MechGarage garage = (MechGarage) aObject;

        aWriter.startNode(MECHS_NODE);
        for (LoadoutBase<?> loadout : garage.getMechs()) {
            aWriter.startNode("loadout");
            aContext.convertAnother(loadout);
            aWriter.endNode();
        }
        aWriter.endNode();

        aWriter.startNode(DROP_SHIPS_NODE);
        for (DropShip dropShip : garage.getDropShips()) {
            aWriter.startNode("dropship");
            aContext.convertAnother(dropShip);
            aWriter.endNode();
        }
        aWriter.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
        MechGarage garage = new MechGarage(null);

        while (aReader.hasMoreChildren()) {
            aReader.moveDown();
            switch (aReader.getNodeName()) {
                case MECHS_NODE:
                    while (aReader.hasMoreChildren()) {
                        aReader.moveDown();
                        LoadoutBase<?> loadout = (LoadoutBase<?>) aContext.convertAnother(garage, LoadoutBase.class);
                        garage.add(loadout);
                        aReader.moveUp();
                    }
                    break;
                case DROP_SHIPS_NODE:
                    while (aReader.hasMoreChildren()) {
                        aReader.moveDown();
                        DropShip dropShip = (DropShip) aContext.convertAnother(garage, DropShip.class);
                        garage.add(dropShip);
                        aReader.moveUp();
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown node: " + aReader.getNodeName());
            }
            aReader.moveUp();
        }
        return garage;
    }

}
