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
package org.lisoft.lsml.model.garage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.lisoft.lsml.model.database.ChassisDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;

@SuppressWarnings("javadoc")
public class GarageTest {

    private final LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();

    @Test
    public void testEquals_EqualsDropShips() throws Exception {
        final DropShip dropShip1 = new DropShip(Faction.CLAN);
        dropShip1.setMech(0, loadoutFactory.produceStock(ChassisDB.lookup("TBR-C")));
        dropShip1.setMech(1, loadoutFactory.produceStock(ChassisDB.lookup("TBR-A")));
        dropShip1.setMech(2, loadoutFactory.produceStock(ChassisDB.lookup("ACH-A")));
        dropShip1.setMech(3, loadoutFactory.produceStock(ChassisDB.lookup("ACH-C")));

        final DropShip dropShip2 = new DropShip(Faction.CLAN);
        dropShip2.setMech(0, loadoutFactory.produceStock(ChassisDB.lookup("TBR-C")));
        dropShip2.setMech(1, loadoutFactory.produceStock(ChassisDB.lookup("TBR-A")));
        dropShip2.setMech(2, loadoutFactory.produceStock(ChassisDB.lookup("ACH-A")));
        dropShip2.setMech(3, loadoutFactory.produceStock(ChassisDB.lookup("ACH-C")));

        final Garage cut1 = new Garage();
        cut1.getDropShipRoot().getValues().add(dropShip1);

        final Garage cut2 = new Garage();
        cut2.getDropShipRoot().getValues().add(dropShip2);

        assertTrue(cut1.equals(cut2));
    }

    /**
     * Two garages are equal if they have the same structures and all mechs and drop ships are identical.
     *
     * @throws Exception
     */
    @Test
    public void testEquals_EqualsLoadouts() throws Exception {
        final Garage cut1 = new Garage();
        final GarageDirectory<Loadout> sub1 = new GarageDirectory<>("Sub1");
        final GarageDirectory<Loadout> sub2 = new GarageDirectory<>("Sub2");
        sub1.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("CPLT-A1")));
        sub1.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("CPLT-C1")));
        sub2.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("TBR-C")));
        sub2.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("TBR-A")));
        cut1.getLoadoutRoot().getDirectories().add(sub1);
        cut1.getLoadoutRoot().getDirectories().add(sub2);
        cut1.getLoadoutRoot().getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("JR7-F")));
        cut1.getLoadoutRoot().getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("JR7-K")));

        final Garage cut2 = new Garage();
        final GarageDirectory<Loadout> sub21 = new GarageDirectory<>("Sub1");
        final GarageDirectory<Loadout> sub22 = new GarageDirectory<>("Sub2");
        sub21.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("CPLT-A1")));
        sub21.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("CPLT-C1")));
        sub22.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("TBR-C")));
        sub22.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("TBR-A")));
        cut2.getLoadoutRoot().getDirectories().add(sub21);
        cut2.getLoadoutRoot().getDirectories().add(sub22);
        cut2.getLoadoutRoot().getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("JR7-F")));
        cut2.getLoadoutRoot().getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("JR7-K")));

        assertTrue(cut1.equals(cut2));
    }

    @Test
    public void testEquals_InequalsDropShips() throws Exception {
        final DropShip dropShip1 = new DropShip(Faction.CLAN);
        dropShip1.setMech(0, loadoutFactory.produceStock(ChassisDB.lookup("TBR-C")));
        dropShip1.setMech(1, loadoutFactory.produceStock(ChassisDB.lookup("TBR-A")));
        dropShip1.setMech(2, loadoutFactory.produceStock(ChassisDB.lookup("ACH-A")));
        dropShip1.setMech(3, loadoutFactory.produceStock(ChassisDB.lookup("ACH-A")));

        final DropShip dropShip2 = new DropShip(Faction.CLAN);
        dropShip2.setMech(0, loadoutFactory.produceStock(ChassisDB.lookup("TBR-C")));
        dropShip2.setMech(1, loadoutFactory.produceStock(ChassisDB.lookup("TBR-A")));
        dropShip2.setMech(2, loadoutFactory.produceStock(ChassisDB.lookup("ACH-A")));
        dropShip2.setMech(3, loadoutFactory.produceStock(ChassisDB.lookup("ACH-C")));

        final Garage cut1 = new Garage();
        cut1.getDropShipRoot().getValues().add(dropShip1);

        final Garage cut2 = new Garage();
        cut2.getDropShipRoot().getValues().add(dropShip2);

        assertFalse(cut1.equals(cut2));
    }

    @Test
    public void testEquals_InequalsLoadouts() throws Exception {
        final Garage cut1 = new Garage();
        final GarageDirectory<Loadout> sub1 = new GarageDirectory<>("Sub1");
        final GarageDirectory<Loadout> sub2 = new GarageDirectory<>("Sub2");
        sub1.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("CPLT-A1")));
        sub1.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("CPLT-C1")));
        sub2.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("TBR-C")));
        sub2.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("TBR-A")));
        cut1.getLoadoutRoot().getDirectories().add(sub1);
        cut1.getLoadoutRoot().getDirectories().add(sub2);
        cut1.getLoadoutRoot().getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("JR7-F")));
        cut1.getLoadoutRoot().getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("JR7-K")));

        final Garage cut2 = new Garage();
        final GarageDirectory<Loadout> sub21 = new GarageDirectory<>("Sub1");
        final GarageDirectory<Loadout> sub22 = new GarageDirectory<>("Sub2");
        sub21.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("CPLT-A1")));
        sub21.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("CPLT-C1")));
        sub22.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("TBR-C")));
        sub22.getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("TBR-A")));
        cut2.getLoadoutRoot().getDirectories().add(sub21);
        cut2.getLoadoutRoot().getDirectories().add(sub22);
        cut2.getLoadoutRoot().getValues().add(loadoutFactory.produceStock(ChassisDB.lookup("JR7-K")));

        assertFalse(cut1.equals(cut2));
    }

    @Test
    public void testEquals_Null() {
        final Garage cut = new Garage();
        assertFalse(cut.equals(null));
    }

    @Test
    public void testEquals_Self() {
        final Garage cut = new Garage();
        assertTrue(cut.equals(cut));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testEquals_WrongClass() {
        final Garage cut = new Garage();
        assertFalse(cut.equals("Foo"));
    }

    @Test
    public void testGetDropShipRoot() {
        final Garage cut = new Garage();

        final GarageDirectory<DropShip> root = cut.getDropShipRoot();
        assertEquals("Garage", root.getName());
        assertTrue(root.getDirectories().isEmpty());
        assertTrue(root.getValues().isEmpty());
    }

    @Test
    public void testGetLoadoutRoot() {
        final Garage cut = new Garage();

        final GarageDirectory<Loadout> root = cut.getLoadoutRoot();
        assertEquals("Garage", root.getName());
        assertTrue(root.getDirectories().isEmpty());
        assertTrue(root.getValues().isEmpty());
    }

}
