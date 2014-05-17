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
package lisong_mechlab.model.upgrades;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import lisong_mechlab.mwo_data.Localization;
import lisong_mechlab.mwo_data.helpers.ItemStatsUpgradeType;

/**
 * Base class for all upgrades for 'mechs.
 * 
 * @author Emily Björk
 */
public class Upgrade{
   @XStreamAsAttribute
   private final String name;
   @XStreamAsAttribute
   private final int    mwoId;
   private final String description;
   @XStreamAsAttribute
   private final int    associatedItem;

   protected Upgrade(String aName, String aDescription, int aMwoId, int aAssociatedItem){
      name = aName;
      mwoId = aMwoId;
      description = aDescription;
      associatedItem = aAssociatedItem;
   }

   protected Upgrade(ItemStatsUpgradeType aUpgradeType){
      this(Localization.key2string(aUpgradeType.Loc.nameTag), Localization.key2string(aUpgradeType.Loc.descTag), Integer.parseInt(aUpgradeType.id),
           aUpgradeType.UpgradeTypeStats.associatedItem);
   }

   /**
    * @return The localized name of the upgrade.
    */
   public String getName(){
      return name;
   }

   /**
    * @return The MW:O ID for the upgrade.
    */
   public int getMwoId(){
      return mwoId;
   }

   /**
    * @return The MW:O description of the upgrade.
    */
   public String getDescription(){
      return description;
   }

   public int getAssociateItemId(){
      return associatedItem;
   }
}
