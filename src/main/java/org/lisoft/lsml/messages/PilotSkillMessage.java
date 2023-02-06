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
package org.lisoft.lsml.messages;

import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.mwo_data.modifiers.PilotSkills;

public class PilotSkillMessage implements Message {
  public final PilotSkillMessage.Type type;
  private final boolean affectsHeat;
  private final PilotSkills pilotSkills;

  public PilotSkillMessage(
      PilotSkills aPilotSkills, PilotSkillMessage.Type aType, boolean aAffectsHeat) {
    pilotSkills = aPilotSkills;
    type = aType;
    affectsHeat = aAffectsHeat;
  }

  @Override
  public boolean affectsHeatOrDamage() {
    return affectsHeat;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PilotSkillMessage) {
      final PilotSkillMessage other = (PilotSkillMessage) obj;
      return pilotSkills == other.pilotSkills && type == other.type;
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    return prime
        * (Boolean.hashCode(affectsHeat)
            + prime * (pilotSkills.hashCode() + prime * type.hashCode()));
  }

  @Override
  public boolean isForMe(Loadout aLoadout) {
    return aLoadout.getEfficiencies() == pilotSkills;
  }

  public enum Type {
    Changed
  }
}
