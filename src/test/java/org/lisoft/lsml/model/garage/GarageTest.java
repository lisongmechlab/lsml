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
package org.lisoft.lsml.model.garage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.lisoft.lsml.model.datacache.ChassisDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;

public class GarageTest {

    @Test
    public void testGetLoadoutRoot() {
        Garage cut = new Garage();

        GarageDirectory<Loadout> root = cut.getLoadoutRoot();
        assertEquals("Garage", root.getName());
        assertTrue(root.getDirectories().isEmpty());
        assertTrue(root.getValues().isEmpty());
    }

    @Test
    public void testGetDropShipRoot() {
        Garage cut = new Garage();

        GarageDirectory<DropShip> root = cut.getDropShipRoot();
        assertEquals("Garage", root.getName());
        assertTrue(root.getDirectories().isEmpty());
        assertTrue(root.getValues().isEmpty());
    }

    @Test
    public void testEquals_Null() {
        Garage cut = new Garage();
        assertFalse(cut.equals(null));
    }

    @Test
    public void testEquals_Self() {
        Garage cut = new Garage();
        assertTrue(cut.equals(cut));
    }

    @Test
    public void testEquals_WrongClass() {
        Garage cut = new Garage();
        assertFalse(cut.equals("Foo"));
    }

    /**
     * Two garages are equal if they have the same structures and all mechs and drop ships are identical.
     * 
     * @throws Exception
     */
    @Test
    public void testEquals_EqualsLoadouts() throws Exception {
        Garage cut1 = new Garage();
        GarageDirectory<Loadout> sub1 = new GarageDirectory<>("Sub1");
        GarageDirectory<Loadout> sub2 = new GarageDirectory<>("Sub2");
        sub1.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("CPLT-A1")));
        sub1.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("CPLT-C1")));
        sub2.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-C")));
        sub2.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-A")));
        cut1.getLoadoutRoot().getDirectories().add(sub1);
        cut1.getLoadoutRoot().getDirectories().add(sub2);
        cut1.getLoadoutRoot().getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("JR7-F")));
        cut1.getLoadoutRoot().getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("JR7-K")));

        Garage cut2 = new Garage();
        GarageDirectory<Loadout> sub21 = new GarageDirectory<>("Sub1");
        GarageDirectory<Loadout> sub22 = new GarageDirectory<>("Sub2");
        sub21.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("CPLT-A1")));
        sub21.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("CPLT-C1")));
        sub22.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-C")));
        sub22.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-A")));
        cut2.getLoadoutRoot().getDirectories().add(sub21);
        cut2.getLoadoutRoot().getDirectories().add(sub22);
        cut2.getLoadoutRoot().getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("JR7-F")));
        cut2.getLoadoutRoot().getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("JR7-K")));

        assertTrue(cut1.equals(cut2));
    }

    @Test
    public void testEquals_InequalsLoadouts() throws Exception {
        Garage cut1 = new Garage();
        GarageDirectory<Loadout> sub1 = new GarageDirectory<>("Sub1");
        GarageDirectory<Loadout> sub2 = new GarageDirectory<>("Sub2");
        sub1.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("CPLT-A1")));
        sub1.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("CPLT-C1")));
        sub2.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-C")));
        sub2.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-A")));
        cut1.getLoadoutRoot().getDirectories().add(sub1);
        cut1.getLoadoutRoot().getDirectories().add(sub2);
        cut1.getLoadoutRoot().getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("JR7-F")));
        cut1.getLoadoutRoot().getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("JR7-K")));

        Garage cut2 = new Garage();
        GarageDirectory<Loadout> sub21 = new GarageDirectory<>("Sub1");
        GarageDirectory<Loadout> sub22 = new GarageDirectory<>("Sub2");
        sub21.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("CPLT-A1")));
        sub21.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("CPLT-C1")));
        sub22.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-C")));
        sub22.getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-A")));
        cut2.getLoadoutRoot().getDirectories().add(sub21);
        cut2.getLoadoutRoot().getDirectories().add(sub22);
        cut2.getLoadoutRoot().getValues().add(DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("JR7-K")));

        assertFalse(cut1.equals(cut2));
    }

    @Test
    public void testEquals_EqualsDropShips() throws Exception {
        DropShip dropShip1 = new DropShip(Faction.CLAN);
        dropShip1.setMech(0, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-C")));
        dropShip1.setMech(1, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-A")));
        dropShip1.setMech(2, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("ACH-A")));
        dropShip1.setMech(3, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("ACH-C")));

        DropShip dropShip2 = new DropShip(Faction.CLAN);
        dropShip2.setMech(0, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-C")));
        dropShip2.setMech(1, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-A")));
        dropShip2.setMech(2, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("ACH-A")));
        dropShip2.setMech(3, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("ACH-C")));

        Garage cut1 = new Garage();
        cut1.getDropShipRoot().getValues().add(dropShip1);

        Garage cut2 = new Garage();
        cut2.getDropShipRoot().getValues().add(dropShip2);

        assertTrue(cut1.equals(cut2));
    }

    @Test
    public void testEquals_InequalsDropShips() throws Exception {
        DropShip dropShip1 = new DropShip(Faction.CLAN);
        dropShip1.setMech(0, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-C")));
        dropShip1.setMech(1, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-A")));
        dropShip1.setMech(2, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("ACH-A")));
        dropShip1.setMech(3, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("ACH-A")));

        DropShip dropShip2 = new DropShip(Faction.CLAN);
        dropShip2.setMech(0, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-C")));
        dropShip2.setMech(1, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("TBR-A")));
        dropShip2.setMech(2, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("ACH-A")));
        dropShip2.setMech(3, DefaultLoadoutFactory.instance.produceStock(ChassisDB.lookup("ACH-C")));

        Garage cut1 = new Garage();
        cut1.getDropShipRoot().getValues().add(dropShip1);

        Garage cut2 = new Garage();
        cut2.getDropShipRoot().getValues().add(dropShip2);

        assertFalse(cut1.equals(cut2));
    }

}
