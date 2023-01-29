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
package org.lisoft.lsml.model.database.mwo_parser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.io.InputStream;
import java.util.List;
import org.lisoft.lsml.model.database.Database;

/**
 * @author Li Song
 */
class XMLLoadout {

  static class ActuatorState {
    @XStreamAsAttribute String LeftActuatorState;
    @XStreamAsAttribute String RightActuatorState;
  }

  @XStreamAlias("component")
  static class Component {
    static class Item {
      @XStreamAsAttribute int ItemID;
    }

    static class Weapon {
      @XStreamAsAttribute int ItemID;
    }

    @XStreamImplicit List<Item> Ammo;
    @XStreamAsAttribute int Armor;

    @XStreamAlias("Name")
    @XStreamAsAttribute
    String ComponentName;

    @XStreamImplicit List<Item> Module;
    @XStreamAsAttribute String OmniPod;
    @XStreamImplicit List<Weapon> Weapon;
  }

  static class Upgrades {
    static class Armor {
      @XStreamAsAttribute int ItemID;
    }

    static class Artemis {
      @XStreamAsAttribute int Equipped;
    }

    static class HeatSinks {
      @XStreamAlias("ItemId")
      // Typo in VTR-9SC
      @XStreamAsAttribute
      int ItemID;
    }

    static class Structure {
      @XStreamAsAttribute int ItemID;
    }

    @XStreamAlias("Armor")
    Armor armor;

    @XStreamAlias("Artemis")
    Artemis artemis;

    @XStreamAlias("HeatSinks")
    HeatSinks heatsinks;

    @XStreamAlias("Structure")
    Structure structure;
  }

  List<Component> ComponentList;

  @XStreamAlias("ActuatorState")
  ActuatorState actuatorState;

  @XStreamAlias("Upgrades")
  Upgrades upgrades;

  static XMLLoadout fromXml(InputStream is) {
    final XStream xstream = Database.makeMwoSuitableXStream();
    xstream.alias("Loadout", XMLLoadout.class);

    return (XMLLoadout) xstream.fromXML(is);
  }
}
