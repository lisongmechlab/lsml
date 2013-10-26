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
         m.put(0, new Hardpoint(HardpointType.MISSILE, 4));
         map.put("jr7-d", m);
         map.put("jr7-d-founder", m);
         map.put("jr7-ds", m);
      }
      
      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(20, new Hardpoint(HardpointType.MISSILE, 4));
         map.put("jr7-k", m);
      }
      
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
         m.put(13, new Hardpoint(HardpointType.MISSILE, 15));
         m.put(14, new Hardpoint(HardpointType.MISSILE, 15));
         map.put("cplt-c1", m);
         map.put("cplt-c1-founder", m);
      }
      
      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(0, new Hardpoint(HardpointType.MISSILE, 15));
         m.put(1, new Hardpoint(HardpointType.MISSILE, 15));
         map.put("cplt-a1", m);
         map.put("cplt-a1c", m);
      }
      
      {
         Map<Integer, Hardpoint> m = new TreeMap<>();
         m.put(21, new Hardpoint(HardpointType.MISSILE, 20));
         m.put(22, new Hardpoint(HardpointType.MISSILE, 20));
         map.put("cplt-c4", m);
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
