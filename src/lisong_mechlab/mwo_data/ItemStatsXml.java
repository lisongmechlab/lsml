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
package lisong_mechlab.mwo_data;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.mwo_data.GameVFS.GameFile;
import lisong_mechlab.mwo_data.helpers.ItemStatsMech;
import lisong_mechlab.mwo_data.helpers.ItemStatsModule;
import lisong_mechlab.mwo_data.helpers.ItemStatsUpgradeType;
import lisong_mechlab.mwo_data.helpers.ItemStatsWeapon;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This class models the format of ItemStats.xml from the game data files to facilitate easy parsing.
 * 
 * @author Li Song
 */
@XStreamAlias("ItemStats")
public class ItemStatsXml{
   @XStreamImplicit
   public List<ItemStatsMech>        MechList        = new ArrayList<>();
   @XStreamImplicit
   public List<ItemStatsWeapon>      WeaponList      = new ArrayList<>();
   @XStreamImplicit
   public List<ItemStatsModule>      ModuleList      = new ArrayList<>();
   @XStreamImplicit
   public List<ItemStatsUpgradeType> UpgradeTypeList = new ArrayList<>();

   public static ItemStatsXml fromXml(GameFile aGameFile){
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
      xstream.alias("WeaponList", ItemStatsXml.class);
      xstream.alias("MechList", ItemStatsXml.class);
      xstream.alias("UpgradeTypeList", ItemStatsXml.class);
      xstream.alias("ModuleList", ItemStatsXml.class);
      xstream.alias("Mech", ItemStatsMech.class);
      xstream.alias("Weapon", ItemStatsWeapon.class);
      xstream.alias("Module", ItemStatsModule.class);
      xstream.alias("UpgradeType", ItemStatsUpgradeType.class);

      // Fixes for broken XML from PGI
      xstream.aliasAttribute("Ctype", "CType");

      return (ItemStatsXml)xstream.fromXML(aGameFile.stream);
   }

   public void append(GameFile aGameFile){
      ItemStatsXml xml = fromXml(aGameFile);
      if( null != xml.MechList )
         MechList.addAll(xml.MechList);
      if( null != xml.WeaponList )
         WeaponList.addAll(xml.WeaponList);
      if( null != xml.ModuleList )
         ModuleList.addAll(xml.ModuleList);
      if( null != xml.UpgradeTypeList )
         UpgradeTypeList.addAll(xml.UpgradeTypeList);
   }
}
