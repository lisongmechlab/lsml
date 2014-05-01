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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.model.DataCache;

public class ChassiDB{
   static private final Map<String, Chassis>        name2chassi;
   static private final Map<String, List<Chassis>>  series2chassi;
   static private final Map<Integer, Chassis>       id2chassi;
   static private final Map<Integer, List<Chassis>> chassis2variant;

   /**
    * Looks up a chassis by a short name such as "AS7-D-DC"
    * 
    * @param aShortName
    * @return The chassis that matches the lookup string.
    */
   public static Chassis lookup(String aShortName){
      String keyShortName = canonize(aShortName);
      if( !name2chassi.containsKey(keyShortName) ){
         if( keyShortName.contains("muro") ){
            return lookup("CTF-IM");
         }
         throw new IllegalArgumentException("No chassi variation named: " + aShortName + " !");
      }
      return name2chassi.get(keyShortName);
   }

   public static Chassis lookup(int aChassiId){
      return id2chassi.get(aChassiId);
   }

   /**
    * @param aChassis
    *           A {@link Chassis} to get variations for.
    * @return A {@link List} of all variants of this chassis (normal, champion, phoenix etc)
    */
   public static Collection<Chassis> lookupVariations(Chassis aChassis){
      return chassis2variant.get(aChassis.getMwoId());
   }

   /**
    * Looks up all chassis of the given chassis class.
    * 
    * @param aChassiClass
    * @return An {@link List} of all {@link Chassis} with the given {@link ChassiClass}.
    */
   public static Collection<Chassis> lookup(ChassiClass aChassiClass){
      List<Chassis> chassii = new ArrayList<>(4 * 4);
      for(Chassis chassis : name2chassi.values()){
         if( chassis.getChassiClass() == aChassiClass && !chassii.contains(chassis) ){
            chassii.add(chassis);
         }
      }
      return chassii;
   }

   public static Collection<Chassis> lookupSeries(String aSeries){
      String keyShortName = canonize(aSeries);
      if( !series2chassi.containsKey(keyShortName) ){
         throw new IllegalArgumentException("No chassi variation by that name!");
      }
      return series2chassi.get(keyShortName);
   }

   private static void addToVariationDb(int aBaseID, Chassis aChassis){
      if( aBaseID < 0 ){
         aBaseID = aChassis.getMwoId();
      }

      List<Chassis> list = chassis2variant.get(aBaseID);
      if( null == list ){
         list = new ArrayList<>();
         chassis2variant.put(aBaseID, list);
      }
      if( aBaseID != aChassis.getMwoId() ){
         chassis2variant.put(aChassis.getMwoId(), list);
      }

      list.add(aChassis);
   }

   static private String canonize(String aName){
      return aName.toLowerCase().trim();
   }

   /**
    * A decision has been made to rely on static initializers for *DB classes. The motivation is that all items are
    * immutable, and this is the only way that allows providing global item constans such as ItemDB.AMS.
    */
   static{
      DataCache dataCache;
      try{
         dataCache = DataCache.getInstance();
      }
      catch( IOException e ){
         throw new RuntimeException(e); // Promote to unchecked. This is a critical failure.
      }

      name2chassi = new HashMap<>();
      series2chassi = new HashMap<>();
      id2chassi = new TreeMap<>();
      chassis2variant = new HashMap<>();

      for(Chassis chassis : dataCache.getChassis()){
         final String model = canonize(chassis.getName());
         final String modelShort = canonize(chassis.getNameShort());

         addToVariationDb(chassis.getBaseVariantId(), chassis);
         name2chassi.put(modelShort, chassis);
         name2chassi.put(model, chassis);
         id2chassi.put(chassis.getMwoId(), chassis);

         if( !series2chassi.containsKey(chassis.getSeriesName()) ){
            List<Chassis> chassilist = new ArrayList<>();
            series2chassi.put(chassis.getSeriesName(), chassilist);
            series2chassi.put(chassis.getSeriesNameShort(), chassilist);
         }
         series2chassi.get(chassis.getSeriesNameShort()).add(chassis);
      }
   }
}
