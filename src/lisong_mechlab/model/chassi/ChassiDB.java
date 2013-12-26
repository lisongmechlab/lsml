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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.converter.GameDataFile;
import lisong_mechlab.model.mwo_parsing.ItemStatsXml;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsMech;

public class ChassiDB{
   static private final Map<String, Chassi>       variation2chassi;
   static private final Map<String, List<Chassi>> series2chassi;
   static private final Map<Integer, Chassi>      id2chassi;

   /**
    * Looks up a chassi by a short name such as "AS7-D-DC"
    * 
    * @param aShortName
    * @return
    */
   static public Chassi lookup(String aShortName){
      String keyShortName = canonize(aShortName);
      if( !variation2chassi.containsKey(keyShortName) ){
         if( keyShortName.contains("muro") ){
            return lookup("CTF-IM");
         }
         throw new IllegalArgumentException("No chassi variation named: " + aShortName + " !");
      }
      return variation2chassi.get(keyShortName);
   }

   public static Chassi lookup(int aChassiId){
      return id2chassi.get(aChassiId);
   }

   /**
    * Looks up all chassi of the given chassi class.
    * 
    * @param aChassiClass
    * @return
    */
   static public List<Chassi> lookup(ChassiClass aChassiClass){
      List<Chassi> chassii = new ArrayList<>(4 * 4);
      for(Chassi variation : variation2chassi.values()){
         if( variation.getChassiClass() == aChassiClass && !chassii.contains(variation) ){
            chassii.add(variation);
         }
      }
      return chassii;
   }

   public static List<Chassi> lookupSeries(String aSeries){
      String keyShortName = canonize(aSeries);
      if( !series2chassi.containsKey(keyShortName) ){
         throw new IllegalArgumentException("No chassi variation by that name!");
      }
      return series2chassi.get(keyShortName);
   }

   static private String canonize(String aName){
      return aName.toLowerCase().trim();
   }

   static{
      GameDataFile gameData;
      try{
         gameData = new GameDataFile();
      }
      catch( Exception e ){
         throw new RuntimeException("Unable to parse game data files!");
      }

      variation2chassi = new HashMap<>();
      series2chassi = new HashMap<>();
      id2chassi = new TreeMap<>();

      ItemStatsXml statsXml = ItemStatsXml.stats;
      for(ItemStatsMech mech : statsXml.MechList){
         final Chassi chassi = new Chassi(mech, gameData);
         final String model = canonize(chassi.getName());
         final String modelShort = canonize(chassi.getNameShort());

         variation2chassi.put(modelShort, chassi);
         variation2chassi.put(model, chassi);
         id2chassi.put(chassi.getMwoId(), chassi);

         // Figure out the name of the series and add to series list
         String[] mdfsplit = mech.mdf.split("\\\\");
         String series = mdfsplit[1];
         String seriesShort = mech.name.split("-")[0];
         if( !series2chassi.containsKey(series) ){
            List<Chassi> chassilist = new ArrayList<>();
            series2chassi.put(series, chassilist);
            series2chassi.put(seriesShort, chassilist);
         }
         series2chassi.get(seriesShort).add(chassi);
      }
   }
}
