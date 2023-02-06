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
package org.lisoft.mwo_data.equipment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.mechs.ChassisClass;
import org.lisoft.mwo_data.mechs.HardPointType;
import org.lisoft.mwo_data.mechs.Location;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * Models the various types of BAPs.
 *
 * <p>TODO: Implement the attributes of BAP to make some kind of sense.
 *
 * @author Li Song
 */
public class ActiveProbe extends Module implements ModifierEquipment {

  public ActiveProbe(
      String aName,
      String aDesc,
      String aMwoName,
      int aMwoId,
      int aSlots,
      double aTons,
      HardPointType aHardpointType,
      double aHP,
      Faction aFaction,
      List<Location> aAllowedLocations,
      List<ChassisClass> aAllowedChassisClasses,
      Integer aAllowedAmount) {
    super(
        aName,
        aDesc,
        aMwoName,
        aMwoId,
        aSlots,
        aTons,
        aHardpointType,
        aHP,
        aFaction,
        aAllowedLocations,
        aAllowedChassisClasses,
        aAllowedAmount);
  }

  @Override
  public Collection<Modifier> getModifiers() {
    return Collections.emptyList();
  }
}
