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
package org.lisoft.lsml.mwo_data.mwo_parser;

import java.util.HashMap;
import java.util.Map;
import org.lisoft.lsml.mwo_data.mechs.HardPoint;
import org.lisoft.lsml.mwo_data.mechs.HardPointType;
import org.lisoft.lsml.mwo_data.mechs.Location;

/**
 * This class provides a way to manually specify missile tube counts for hard points on certain
 * chassis until PGI provides a way to parse it reliably for all chassis.
 *
 * @author Li Song
 */
class HardPointCache {
  private static final Map<String, Map<Integer, HardPoint>> map;

  static {
    map = new HashMap<>();

    {
      Map<Integer, HardPoint> m = new HashMap<>();
      m.put(42, new HardPoint(HardPointType.MISSILE, 2, false));
      m.put(45, new HardPoint(HardPointType.MISSILE, 2, false));
      map.put("cda-x5", m);
    }

    {
      Map<Integer, HardPoint> m = new HashMap<>();
      m.put(10, new HardPoint(HardPointType.MISSILE, 10, false));
      map.put("drg-1n", m);
    }

    {
      Map<Integer, HardPoint> m = new HashMap<>();
      m.put(0, new HardPoint(HardPointType.MISSILE, 10, false));
      map.put("drg-1c", m);
    }

    {
      Map<Integer, HardPoint> m = new HashMap<>();
      m.put(20, new HardPoint(HardPointType.MISSILE, 10, false));
      map.put("drg-5n", m);
      map.put("drg-5nc", m);
    }

    {
      Map<Integer, HardPoint> m = new HashMap<>();
      m.put(25, new HardPoint(HardPointType.MISSILE, 4, false));
      map.put("ctf-2x", m);
    }

    {
      Map<Integer, HardPoint> m = new HashMap<>();
      m.put(30, new HardPoint(HardPointType.MISSILE, 5, false));
      map.put("ctf-4x", m);
    }

    {
      Map<Integer, HardPoint> m = new HashMap<>();
      m.put(11, new HardPoint(HardPointType.MISSILE, 15, false));
      m.put(12, new HardPoint(HardPointType.MISSILE, 15, false));
      map.put("aws-8r", m);
    }

    {
      Map<Integer, HardPoint> m = new HashMap<>();
      m.put(21, new HardPoint(HardPointType.MISSILE, 15, false));
      m.put(22, new HardPoint(HardPointType.MISSILE, 15, false));
      map.put("aws-8t", m);
    }

    {
      Map<Integer, HardPoint> m = new HashMap<>();
      m.put(32, new HardPoint(HardPointType.MISSILE, 15, false));
      map.put("aws-8v", m);
    }

    {
      Map<Integer, HardPoint> m = new HashMap<>();
      m.put(52, new HardPoint(HardPointType.MISSILE, 15, false));
      map.put("aws-pb", m);
    }

    {
      Map<Integer, HardPoint> m = new HashMap<>();
      m.put(44, new HardPoint(HardPointType.MISSILE, 2, false));
      m.put(46, new HardPoint(HardPointType.MISSILE, 2, false));
      map.put("aws-9m", m);
    }
  }

  static HardPoint getHardpoint(int anHardpointId, String aChassis, Location aPart) {
    if (!map.containsKey(aChassis)) {
      throw new IllegalArgumentException("No mappings for chassis: " + aChassis);
    }

    if (!map.get(aChassis).containsKey(anHardpointId)) {
      throw new IllegalArgumentException(
          "No tubes for hardpoint ID: "
              + anHardpointId
              + " ("
              + aPart
              + ") on chassis: "
              + aChassis);
    }

    return map.get(aChassis).get(anHardpointId);
  }
}
