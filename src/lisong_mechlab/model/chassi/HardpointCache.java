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
package lisong_mechlab.model.chassi;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class provides a way to manually specify missile tube counts for hard points on certain chassis until PGI
 * provides a way to parse it reliably for all chassii.
 * 
 * @author Emily Björk
 */
public class HardpointCache{
   static private Map<String, Map<Integer, Hardpoint>> map;

   static{
      map = new TreeMap<>();

      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(42, new Hardpoint(HardpointType.MISSILE, 2));
         m.put(45, new Hardpoint(HardpointType.MISSILE, 2));
         map.put("cda-x5", m);
      }

      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(10, new Hardpoint(HardpointType.MISSILE, 10));
         map.put("drg-1n", m);
      }

      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(0, new Hardpoint(HardpointType.MISSILE, 10));
         map.put("drg-1c", m);
      }

      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(20, new Hardpoint(HardpointType.MISSILE, 10));
         map.put("drg-5n", m);
         map.put("drg-5nc", m);
      }

      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(25, new Hardpoint(HardpointType.MISSILE, 4));
         map.put("ctf-2x", m);
      }

      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(30, new Hardpoint(HardpointType.MISSILE, 5));
         map.put("ctf-4x", m);
      }

      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(11, new Hardpoint(HardpointType.MISSILE, 15));
         m.put(12, new Hardpoint(HardpointType.MISSILE, 15));
         map.put("aws-8r", m);
      }

      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(21, new Hardpoint(HardpointType.MISSILE, 15));
         m.put(22, new Hardpoint(HardpointType.MISSILE, 15));
         map.put("aws-8t", m);
      }

      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(32, new Hardpoint(HardpointType.MISSILE, 15));
         map.put("aws-8v", m);
      }

      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(52, new Hardpoint(HardpointType.MISSILE, 15));
         map.put("aws-pb", m);
      }

      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(44, new Hardpoint(HardpointType.MISSILE, 2));
         m.put(46, new Hardpoint(HardpointType.MISSILE, 2));
         map.put("aws-9m", m);
      }
   }

   public static Hardpoint getHardpoint(int anHardpointId, String aChassi, Part aPart){
      if( !map.containsKey(aChassi) ){
         throw new IllegalArgumentException("No mappings for chassis: " + aChassi);
      }

      if( !map.get(aChassi).containsKey(anHardpointId) ){
         throw new IllegalArgumentException("No tubes for hardpoint ID: " + anHardpointId + " (" + aPart + ") on chassis: " + aChassi);
      }

      return map.get(aChassi).get(anHardpointId);
   }
}
