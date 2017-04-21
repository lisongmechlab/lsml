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
package org.lisoft.lsml.model.database.gamedata;

import java.io.InputStream;
import java.util.List;

import org.lisoft.lsml.model.database.Database;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * @author Li Song
 */
public class XMLLoadout {

    public static class ActuatorState {
        @XStreamAsAttribute
        public String LeftActuatorState;
        @XStreamAsAttribute
        public String RightActuatorState;
    }

    @XStreamAlias("component")
    public static class Component {
        public static class Item {
            @XStreamAsAttribute
            public int ItemID;
        }

        public static class Weapon {
            @XStreamAsAttribute
            public int ItemID;
            @XStreamAsAttribute
            public int WeaponGroup;
        }

        @XStreamAlias("Name")
        @XStreamAsAttribute
        public String ComponentName;

        @XStreamAsAttribute
        public String OmniPod;

        @XStreamAsAttribute
        public int Armor;

        @XStreamImplicit
        public List<Weapon> Weapon;

        @XStreamImplicit
        public List<Item> Module;

        @XStreamImplicit
        public List<Item> Ammo;
    }

    public static class Upgrades {
        public static class Armor {
            @XStreamAsAttribute
            public int ItemID;
        }

        public static class Artemis {
            @XStreamAsAttribute
            public int Equipped;
        }

        public static class HeatSinks {
            @XStreamAlias("ItemId")
            // Typo in VTR-9SC
            @XStreamAsAttribute
            public int ItemID;
        }

        public static class Structure {
            @XStreamAsAttribute
            public int ItemID;
        }

        @XStreamAlias("Armor")
        public Armor armor;
        @XStreamAlias("Structure")
        public Structure structure;
        @XStreamAlias("HeatSinks")
        public HeatSinks heatsinks;
        @XStreamAlias("Artemis")
        public Artemis artemis;
    }

    public static XMLLoadout fromXml(InputStream is) {
        final XStream xstream = Database.makeMwoSuitableXStream();
        xstream.alias("Loadout", XMLLoadout.class);

        return (XMLLoadout) xstream.fromXML(is);
    }

    @XStreamAlias("Upgrades")
    public Upgrades upgrades;

    public List<Component> ComponentList;

    @XStreamAsAttribute
    int MechID;

    @XStreamAsAttribute
    String Name;

    @XStreamAlias("ActuatorState")
    public ActuatorState actuatorState;
}
