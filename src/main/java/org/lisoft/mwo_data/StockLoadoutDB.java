/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.mwo_data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.mwo_data.equipment.NoSuchItemException;
import org.lisoft.mwo_data.mechs.Chassis;
import org.lisoft.mwo_data.mechs.ChassisStandard;
import org.lisoft.mwo_data.mechs.StockLoadout;

/**
 * A database class that holds descriptions of all stock loadouts.
 *
 * @author Li Song
 */
public class StockLoadoutDB {
  private static final Map<Chassis, StockLoadout> stockLoadouts;

  /*
   * A decision has been made to rely on static initializers for *DB classes. The motivation is that
   * all items are immutable, and this is the only way that allows providing global item constants
   * such as ItemDB.AMS.
   */
  static {
    final Database database = LiSongMechLab.getDatabase();

    stockLoadouts = new HashMap<>();
    for (final StockLoadout stock : database.getStockLoadouts()) {
      try {
        stockLoadouts.put(stock.getChassis(), stock);
      } catch (final NoSuchItemException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static Collection<StockLoadout> all() {
    return stockLoadouts.values();
  }

  /**
   * Will find the stock loadout matching the given {@link ChassisStandard}.
   *
   * @param aChassis The {@link ChassisStandard} to get the stock loadout for.
   * @return A {@link StockLoadout} description of the stock loadout.
   * @throws NoSuchItemException if no stock loadout was found for the chassis.
   */
  public static StockLoadout lookup(Chassis aChassis) throws NoSuchItemException {
    final StockLoadout ans = stockLoadouts.get(aChassis);
    if (null == ans) {
      throw new NoSuchItemException("No stock loadouts found for: " + aChassis);
    }
    return ans;
  }
}
