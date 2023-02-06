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
package org.lisoft.lsml.mwo_data.mwo_parser;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.lisoft.lsml.mwo_data.Faction;

class ModuleBaseXML {
  @XmlElement private ItemStatsLoc Loc;
  @XStreamAsAttribute private String faction;
  @XStreamAsAttribute private String id;
  @XStreamAsAttribute private String name;

  protected void inheritFrom(ModuleBaseXML aThat) {
    if (Loc.descTag == null) {
      Loc.descTag = aThat.Loc.descTag;
    }
  }

  protected Faction getFaction() {
    return Faction.fromMwo(faction);
  }

  protected int getMwoId() {
    return Integer.parseInt(id);
  }

  protected String getMwoKey() {
    return name;
  }

  protected String getUiDescription(PartialDatabase aPartialDatabase) {
    return aPartialDatabase.localise(Loc.descTag);
  }

  protected String getUiName(PartialDatabase aPartialDatabase) {
    return aPartialDatabase.localise(Loc.nameTag).replace("ARMOR", "ARMOUR");
  }

  protected String getUiShortName(PartialDatabase aPartialDatabase) {
    return Loc.shortNameTag == null ? null : aPartialDatabase.localise(Loc.shortNameTag);
  }

  private static class ItemStatsLoc {
    @XStreamAsAttribute public String descTag;
    @XStreamAsAttribute public String nameTag;
    @XStreamAsAttribute public String shortNameTag;
  }
}
