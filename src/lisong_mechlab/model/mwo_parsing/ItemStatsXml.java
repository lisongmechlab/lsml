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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import lisong_mechlab.converter.GameDataFile;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsMech;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsModule;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsUpgradeType;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This class models the format of ItemStats.xml from the game data files to facilitate easy parsing.
 * 
 * @author Emily
 */
@XStreamAlias("ItemStats")
public class ItemStatsXml{
   public List<ItemStatsMech>        MechList;
   public List<ItemStatsWeapon>      WeaponList;
   public List<ItemStatsModule>      ModuleList;
   public List<ItemStatsUpgradeType> UpgradeTypeList;

   public final static ItemStatsXml  stats;

   private ItemStatsXml(){}

   private static ItemStatsXml fromXml(InputStream is){
      XStream xstream = new XStream(new StaxDriver(new NoNameCoder())){
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
      xstream.autodetectAnnotations(true);
      xstream.alias("ItemStats", ItemStatsXml.class);
      xstream.alias("Mech", ItemStatsMech.class);
      xstream.alias("Weapon", ItemStatsWeapon.class);
      xstream.alias("Module", ItemStatsModule.class);
      xstream.alias("UpgradeType", ItemStatsUpgradeType.class);

      // Fixes for broken XML from PGI
      xstream.aliasAttribute("Ctype", "CType");

      return (ItemStatsXml)xstream.fromXML(is);
   }

   static{
      try{
         stats = ItemStatsXml.fromXml(new GameDataFile().openGameFile(GameDataFile.ITEM_STATS_XML));
      }
      catch( IOException e ){
         throw new RuntimeException(e);
      }
   }
}
