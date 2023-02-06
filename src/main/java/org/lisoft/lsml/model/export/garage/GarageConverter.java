/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.model.export.garage;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import java.util.HashMap;
import java.util.Map;
import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.model.garage.Garage;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.mwo_data.Faction;
import org.lisoft.lsml.mwo_data.mechs.ChassisClass;

/**
 * XStream converter for {@link Garage}. This is the main converter for saved garages.
 *
 * @author Li Song
 */
public class GarageConverter extends ReflectionConverter {
  private static final String DROP_SHIPS_NODE = "dropships";
  private static final int MAX_VERSION = 2;
  private static final String MECHS_NODE = "mechs";
  private static final String VERSION = "version";

  public GarageConverter(Mapper aMapper, ReflectionProvider aReflectionProvider) {
    super(aMapper, aReflectionProvider);
  }

  @Override
  public boolean canConvert(Class aClass) {
    return Garage.class == aClass;
  }

  @Override
  public void marshal(
      Object aOriginal, HierarchicalStreamWriter aWriter, MarshallingContext aContext) {
    aWriter.addAttribute(VERSION, Integer.toString(MAX_VERSION));
    super.marshal(aOriginal, aWriter, aContext);
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader aReader, UnmarshallingContext aContext) {
    final String versionAttribute = aReader.getAttribute(VERSION);
    int version = 1;
    if (versionAttribute != null) {
      version = Integer.parseInt(versionAttribute);
    }

    if (version == 1) {
      // Convert version 1 garage to version 2, create default folders.
      final Garage garage = new Garage();
      final Map<ChassisClass, GarageDirectory<Loadout>> loadoutDirs = new HashMap<>();
      for (final ChassisClass chassisClass : ChassisClass.values()) {
        if (chassisClass == ChassisClass.COLOSSAL) {
          continue;
        }
        final GarageDirectory<Loadout> directory = new GarageDirectory<>(chassisClass.getUiName());
        loadoutDirs.put(chassisClass, directory);
        garage.getLoadoutRoot().getDirectories().add(directory);
      }

      final Map<Faction, GarageDirectory<DropShip>> dropShipDirs = new HashMap<>();
      for (final Faction faction : Faction.values()) {
        if (faction == Faction.ANY) {
          continue;
        }
        final GarageDirectory<DropShip> directory = new GarageDirectory<>(faction.getUiName());
        dropShipDirs.put(faction, directory);
        garage.getDropShipRoot().getDirectories().add(directory);
      }

      while (aReader.hasMoreChildren()) {
        aReader.moveDown();
        switch (aReader.getNodeName()) {
          case MECHS_NODE:
            while (aReader.hasMoreChildren()) {
              aReader.moveDown();
              final Loadout loadout = (Loadout) aContext.convertAnother(garage, Loadout.class);
              loadoutDirs.get(loadout.getChassis().getChassisClass()).getValues().add(loadout);
              aReader.moveUp();
            }
            break;
          case DROP_SHIPS_NODE:
            while (aReader.hasMoreChildren()) {
              aReader.moveDown();
              final DropShip dropShip = (DropShip) aContext.convertAnother(garage, DropShip.class);
              dropShipDirs.get(dropShip.getFaction()).getValues().add(dropShip);
              aReader.moveUp();
            }
            break;
          default:
            throw new IllegalStateException("Unknown node: " + aReader.getNodeName());
        }
        aReader.moveUp();
      }
      return garage;
    } else if (version == MAX_VERSION) {
      return super.unmarshal(aReader, aContext);
    }
    throw new IllegalStateException("Unsupported garage version!");
  }
}
