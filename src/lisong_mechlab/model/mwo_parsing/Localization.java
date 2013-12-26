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
package lisong_mechlab.model.mwo_parsing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lisong_mechlab.converter.GameDataFile;
import lisong_mechlab.model.mwo_parsing.helpers.Workbook;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This class will provide localization (and implicitly all naming) of items through the MWO data files.
 * 
 * @author Emily
 */
public class Localization{
   private static final Map<String, String> key2string;

   public static String key2string(String aKey){
      String canon = canonize(aKey);
      if( !key2string.containsKey(canon) ){
         System.out.println(key2string);
         throw new RuntimeException("No such key found!: " + canon);
      }
      return key2string.get(canon);
   }

   private static String canonize(String aKey){
      // We need to normalize MKII -> MK2 etc
      aKey = aKey.toLowerCase();
      if( aKey.contains("_mk") ){
         aKey = aKey.replaceAll("_mkvi", "_mk6");
         aKey = aKey.replaceAll("_mkv", "_mk5");
         aKey = aKey.replaceAll("_mkiv", "_mk4");
         aKey = aKey.replaceAll("_mkiii", "_mk3");
         aKey = aKey.replaceAll("_mkii", "_mk2");
         aKey = aKey.replaceAll("_mki", "_mk1");
         aKey = aKey.replaceAll("_mkl", "_mk1"); // They've mistaken an l (ell) for an 1 (one)
      }

      if( !aKey.startsWith("@") ){
         aKey = "@" + aKey;
      }

      return aKey;
   }

   static{
      key2string = new HashMap<String, String>();

      GameDataFile dataFile;
      try{
         dataFile = new GameDataFile();
      }
      catch( IOException e1 ){
         throw new RuntimeException("Couldn't load data files!", e1);
      }
      File[] files = new File[] {new File("Game/Localized/Languages/Loc.xml")};
      /*
       * , new File("Game/Localized/Languages/ui_Mech_Loc.xml"), new File("Game/Localized/Languages/General.xml"), new
       * File("Game/Localized/Languages/Mechlab.xml"), new File("Game/Localized/Languages/text_ui_menus.xml")};
       */
      /*
       * for(File file : files){ try{ XmlReader reader = new XmlReader(dataFile.openGameFile(file)); for(Element row :
       * reader.getElementsByTagName("Row")){ List<Element> cells = reader.getElementsByTagName("Cell", row); if(
       * cells.size() < 3 || !cells.get(0).getAttribute("ss:Index").equals("2") ){ continue; } List<Element> data0 =
       * reader.getElementsByTagName("Data", cells.get(0)); List<Element> data2 = reader.getElementsByTagName("Data",
       * cells.get(2)); if( data0.size() != 1 || data2.size() != 1 ){ continue; } String tag0 =
       * canonize(reader.getTagValue("Data", cells.get(0))); String tag2 = reader.getTagValue("Data", cells.get(2));
       * key2string.put(tag0, tag2); System.out.println(file + " ## " + tag0 + " = " + tag2); } } catch( Exception e ){
       * throw new RuntimeException(e); } }
       */

      XStream xstream = new XStream(){
         @Override
         protected MapperWrapper wrapMapper(MapperWrapper next){
            return new MapperWrapper(next){
               @Override
               public boolean shouldSerializeMember(Class definedIn, String fieldName){
                  if( definedIn == Object.class ){
                     return false;
                  }
                  return super.shouldSerializeMember(definedIn, fieldName);
               }
            };
         }
      };
      xstream.alias("Workbook", Workbook.class);
      xstream.autodetectAnnotations(true);
      for(File file : files){
         try{
            Workbook workbook = (Workbook)xstream.fromXML(dataFile.openGameFile(file));
            for(Workbook.Worksheet.Table.Row row : workbook.Worksheet.Table.rows){ // Skip past junk
               if( row.cells == null || row.cells.size() < 1 ){
                  // debugprintrow(row);
                  continue;
               }
               if( row.cells.get(0).Data == null ){
                  // debugprintrow(row);
                  continue;
               }
               if( row.cells.size() >= 2 ){
                  String key = row.cells.get(0).Data;
                  String data = row.cells.get(1).Data;
                  if( data == null || data.length() < 2 ){
                     debugprintrow(row);
                  }
                  key2string.put(canonize(key), data);
               }
               else{
                  debugprintrow(row); // Debug Breakpoint
               }
            }
         }
         catch( Exception e ){
            throw new RuntimeException(e);
         }
      }

   }

   @SuppressWarnings("unused")
   static private void debugprintrow(Workbook.Worksheet.Table.Row row){
      // if( row.cells != null ){
      // System.out.print("{");
      // for(Workbook.Worksheet.Table.Row.Cell cell : row.cells){
      // System.out.print(cell.Data + "##");
      // }
      // System.out.println("}");
      // }
   }
}
