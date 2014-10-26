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
package lisong_mechlab.model.garage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.io.IOException;

import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.garage.MechGarage.GarageMessage;
import lisong_mechlab.model.garage.MechGarage.GarageMessage.Type;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.PilotModuleDB;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutOmniMech;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.OpAddModule;
import lisong_mechlab.model.loadout.OpLoadStock;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.message.MessageXBar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MechGarageTest {

    File        testFile = null;

    @Mock
    MessageXBar xBar;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        testFile = new File("test_mechgarage_" + Math.random() + ".xml");
    }

    @After
    public void teardown() {
        testFile.delete();
    }

    /**
     * Default constructing a mech garage gives an unnamed, empty garage.
     */
    @Test
    public void testMechGarage() {
        // Execute
        MechGarage cut = new MechGarage(xBar);

        // Verify
        verify(xBar).post(new GarageMessage(MechGarage.GarageMessage.Type.NewGarage, cut));

        assertTrue(cut.getMechs().isEmpty());
        assertNull(cut.getFile());
    }

    /**
     * Loading an empty garage shall produce an empty garage with the correct file path set.
     * 
     * @throws IOException
     */
    @Test
    public void testOpen() throws IOException {
        // Setup
        MechGarage savedGarage = new MechGarage(xBar);
        savedGarage.saveas(testFile);
        reset(xBar);

        // Execute
        MechGarage c = MechGarage.open(testFile, xBar);

        // Verify
        verify(xBar).post(new GarageMessage(MechGarage.GarageMessage.Type.NewGarage, c));

        assertTrue(c.getMechs().isEmpty());
        assertSame(testFile, c.getFile());
    }

    /**
     * Saving a mech garage that has not been loaded or saveas-ed before shall throw an error as it has no associated
     * file name.
     */
    @Test
    public void testSaveWithoutName() {
        // Setup
        MechGarage cut = new MechGarage(xBar);
        reset(xBar);

        // Execute
        try {
            cut.save();
            fail();
        }

        // Verify
        catch (IOException exception) {/* Expected exception */
        }
        verifyZeroInteractions(xBar);
    }

    /**
     * Attempting to use {@link MechGarage#saveas(File)} on a file that already exist shall throw without editing the
     * file.
     * 
     * @throws IOException
     *             Shouldn't be thrown.
     */
    @Test
    public void testSaveOverwrite() throws IOException {
        // Setup
        MechGarage cut = new MechGarage(xBar);
        cut.saveas(testFile);
        testFile.setLastModified(0);
        reset(xBar);

        // Execute
        try {
            cut.saveas(testFile); // File already exists
            fail(); // Must throw!
        }

        // Verify
        catch (IOException e) {
            assertEquals(0, testFile.lastModified()); // Must not have been modified
        }
        verifyZeroInteractions(xBar);
    }

    /**
     * {@link MechGarage#saveas(File)} shall produce a file that can be subsequently
     * {@link MechGarage#open(File, MessageXBar)}ed to restore the contents of the garage before the call to
     * {@link MechGarage#saveas(File)}
     * 
     * @throws Exception
     *             Shouldn't be thrown.
     */
    @Test
    public void testSaveAsOpen() throws Exception {
        // Setup
        LoadoutStandard lo1 = new LoadoutStandard("as7-d-dc");
        LoadoutStandard lo2 = new LoadoutStandard("as7-k");
        LoadoutOmniMech lo3 = new LoadoutOmniMech(ComponentBuilder.getOmniComponentFactory(),
                (ChassisOmniMech) ChassisDB.lookup("nva-prime"));
        LoadoutOmniMech lo4 = new LoadoutOmniMech(ComponentBuilder.getOmniComponentFactory(),
                (ChassisOmniMech) ChassisDB.lookup("tbr-c"));

        OperationStack stack = new OperationStack(0);
        stack.pushAndApply(new OpLoadStock(lo3.getChassis(), lo3, xBar));
        stack.pushAndApply(new OpLoadStock(lo4.getChassis(), lo4, xBar));

        stack.pushAndApply(new OpAddModule(null, lo1, PilotModuleDB.lookup("ADVANCED UAV")));
        stack.pushAndApply(new OpAddModule(null, lo4, PilotModuleDB.lookup("COOL SHOT 6")));

        MechGarage cut = new MechGarage(xBar);
        cut.add(lo1);
        cut.add(lo2);
        cut.add(lo3);
        cut.add(lo4);
        reset(xBar);

        // Execute
        cut.saveas(testFile);
        MechGarage loadedGarage = MechGarage.open(testFile, xBar);

        // Verify
        verify(xBar).post(new MechGarage.GarageMessage(Type.Saved, cut));
        verify(xBar).post(new MechGarage.GarageMessage(Type.NewGarage, loadedGarage));
        assertEquals(4, loadedGarage.getMechs().size());
        assertEquals(lo1, loadedGarage.getMechs().get(0));
        assertEquals(lo2, loadedGarage.getMechs().get(1));
        assertEquals(lo3, loadedGarage.getMechs().get(2));
        assertEquals(lo4, loadedGarage.getMechs().get(3));
    }

    /**
     * {@link MechGarage#save()} shall overwrite previously saved garage.
     * 
     * @throws Exception
     *             Shouldn't be thrown.
     */
    @Test
    public void testSave() throws Exception {
        // Setup
        LoadoutStandard lo1 = new LoadoutStandard("as7-d-dc");
        LoadoutStandard lo2 = new LoadoutStandard("as7-k");
        MechGarage cut = new MechGarage(xBar);
        cut.add(lo1);
        cut.saveas(testFile); // Create garage with one mech and save it.
        cut = MechGarage.open(testFile, xBar);
        cut.add(lo2); // Add a mech and use the save() function. The same file should be overwritten.
        reset(xBar);

        // Execute
        cut.save();

        // Open the garage to verify.
        verify(xBar).post(new MechGarage.GarageMessage(Type.Saved, cut));

        cut = MechGarage.open(testFile, xBar);
        assertEquals(2, cut.getMechs().size());
        assertEquals(lo1, cut.getMechs().get(0));
        assertEquals(lo2, cut.getMechs().get(1));
    }

    /**
     * add(Loadout, boolean) shall add a loadout to the garage that can subsequently be removed with remove(Loadout,
     * boolean).
     * 
     * @throws Exception
     *             Shouldn't be thrown.
     */
    @Test
    public void testAddRemoveLoadout() throws Exception {
        // Setup
        LoadoutStandard loadout = new LoadoutStandard("as7-d-dc");
        MechGarage cut = new MechGarage(xBar);

        // Execute
        cut.add(loadout);

        // Verify
        assertEquals(1, cut.getMechs().size());
        assertSame(loadout, cut.getMechs().get(0));
        verify(xBar).post(new GarageMessage(MechGarage.GarageMessage.Type.LoadoutAdded, cut, loadout));

        // Execute
        cut.remove(loadout);

        // Verify
        assertTrue(cut.getMechs().isEmpty());
        verify(xBar).post(new GarageMessage(MechGarage.GarageMessage.Type.LoadoutRemoved, cut, loadout));
    }

    /**
     * Removing an nonexistent loadout is a no-op.
     * 
     * @throws Exception
     *             Shouldn't be thrown.
     */
    @Test
    public void testRemoveLoadoutNonexistent() throws Exception {
        // Setup
        LoadoutStandard loadout = new LoadoutStandard("as7-d-dc");
        MechGarage cut = new MechGarage(xBar);
        reset(xBar);
        cut.remove(loadout);

        verifyZeroInteractions(xBar);
    }

    /**
     * Make sure that we can load many of the stock builds saved from 1.5.0.
     * <p>
     * Note, this is a backwards compatibility test.
     * 
     * @throws IOException
     */
    @Test
    public void testLoadStockBuilds_150() throws IOException {
        MechGarage garage = MechGarage.open(new File("resources/resources/stock1.5.0.xml"), xBar);
        OperationStack stack = new OperationStack(0);
        assertEquals(64, garage.getMechs().size());

        for (LoadoutBase<?> loadout : garage.getMechs()) {
            LoadoutStandard loadoutStandard = (LoadoutStandard) loadout;
            
            LoadoutStandard clone = new LoadoutStandard(ComponentBuilder.getStandardComponentFactory(), loadoutStandard);
            stack.pushAndApply(new OpLoadStock(clone.getChassis(), clone, xBar));

            assertEquals(clone, loadout);
        }
    }

    /**
     * Issue #337. Actuator state is not saved properly.
     * 
     * @throws IOException
     */
    @Test
    public void testActuatorStateSaved() throws IOException {
        ChassisOmniMech chassi = (ChassisOmniMech) ChassisDB.lookup("WHK-B");
        LoadoutOmniMech loadout = new LoadoutOmniMech(ComponentBuilder.getOmniComponentFactory(), chassi);

        loadout.getComponent(Location.RightArm).setToggleState(ItemDB.LAA, false);

        MechGarage garage = new MechGarage(xBar);
        garage.add(loadout);
        garage.saveas(testFile);
        garage = null;
        garage = MechGarage.open(testFile, xBar);

        LoadoutOmniMech loaded = (LoadoutOmniMech) garage.getMechs().get(0);

        assertFalse(loaded.getComponent(Location.RightArm).getToggleState(ItemDB.LAA));
    }

}
