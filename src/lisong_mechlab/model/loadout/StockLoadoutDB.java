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
package lisong_mechlab.model.loadout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lisong_mechlab.model.DataCache;
import lisong_mechlab.model.StockLoadout;
import lisong_mechlab.model.chassi.Chassis;

/**
 * A database class that holds descriptions of all stock loadouts.
 * 
 * @author Emily Björk
 */
public class StockLoadoutDB{
   private static final Map<Chassis, StockLoadout> stockloadouts;

   /**
    * Will find the stock loadout matching the given {@link Chassis}.
    * 
    * @param aChassis
    *           The {@link Chassis} to get the stock loadout for.
    * @return A {@link StockLoadout} description of the stock loadout.
    */
   public static StockLoadout lookup(Chassis aChassis){
      StockLoadout ans = stockloadouts.get(aChassis);
      if( null == ans ){
         throw new IllegalArgumentException("No stock loadouts found for: " + aChassis);
      }
      return ans;
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

      stockloadouts = new HashMap<>();
      for(StockLoadout loadout : dataCache.getStockLoadouts()){
         stockloadouts.put(loadout.getChassis(), loadout);
      }
   }
}
