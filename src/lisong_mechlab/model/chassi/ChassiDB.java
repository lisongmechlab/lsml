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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.converter.GameDataFile;
import lisong_mechlab.model.mwo_parsing.HardpointsXml;
import lisong_mechlab.model.mwo_parsing.ItemStatsXml;
import lisong_mechlab.model.mwo_parsing.MechDefinition;
import lisong_mechlab.model.mwo_parsing.MechIdMap;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsMech;

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
   static public Chassis lookup(String aShortName){
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
   static public Collection<Chassis> lookup(ChassiClass aChassiClass){
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

   static private String canonize(String aName){
      return aName.toLowerCase().trim();
   }

   static{
      GameDataFile gameData;
      MechIdMap mechIdMap;
      try{
         gameData = new GameDataFile();
         mechIdMap = MechIdMap.fromXml(gameData.openGameFile(GameDataFile.MECH_ID_MAP_XML));
         if( mechIdMap.MechIdMap == null )
            throw new RuntimeException("Null mechID Map");
      }
      catch( Exception e ){
         throw new RuntimeException("Error reading chassi information!", e);
      }

      name2chassi = new HashMap<>();
      series2chassi = new HashMap<>();
      id2chassi = new TreeMap<>();
      chassis2variant = new HashMap<>();

      ItemStatsXml statsXml = ItemStatsXml.stats;
      for(ItemStatsMech mech : statsXml.MechList){
         int basevariant = -1;
         for(MechIdMap.Mech mappedmech : mechIdMap.MechIdMap){
            if( mappedmech.variantID == mech.id ){
               basevariant = mappedmech.baseID;
               break;
            }
         }

         MechDefinition mdf = null;
         HardpointsXml hardpoints = null;
         try{
            String mdfFile = mech.mdf.replace('\\', '/');
            mdf = MechDefinition.fromXml(gameData.openGameFile(new File(GameDataFile.MDF_ROOT, mdfFile)));
            hardpoints = HardpointsXml.fromXml(gameData.openGameFile(new File("Game", mdf.HardpointPath)));
         }
         catch( Exception e ){
            throw new RuntimeException("Unable to load chassi configuration!", e);
         }

         if( mdf.Mech.VariantParent > 0 ){
            if( basevariant > 0 && mdf.Mech.VariantParent != basevariant ){
               // Inconsistency between MechIDMap and ParentAttribute.
               throw new RuntimeException("MechIDMap.xml and VariantParent attribute are inconsistent for: " + mech.name);
            }
            basevariant = mdf.Mech.VariantParent;
         }
         // else{
         // if( basevariant > 0 )
         // System.out.println("No variant parent for: " + mech.name);
         // }

         final Chassis chassi = new Chassis(mech, mdf, hardpoints);
         final String model = canonize(chassi.getName());
         final String modelShort = canonize(chassi.getNameShort());

         addToVariationDb(basevariant, chassi);
         name2chassi.put(modelShort, chassi);
         name2chassi.put(model, chassi);
         id2chassi.put(chassi.getMwoId(), chassi);

         // Figure out the name of the series and add to series list
         String[] mdfsplit = mech.mdf.split("\\\\");
         String series = mdfsplit[1];
         String seriesShort = mech.name.split("-")[0];
         if( !series2chassi.containsKey(series) ){
            List<Chassis> chassilist = new ArrayList<>();
            series2chassi.put(series, chassilist);
            series2chassi.put(seriesShort, chassilist);
         }
         series2chassi.get(seriesShort).add(chassi);
      }
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
}
