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
package org.lisoft.lsml.command;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.NotificationMessage;
import org.lisoft.lsml.messages.NotificationMessage.Severity;
import org.lisoft.lsml.model.chassi.ComponentBase;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdAddItem}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class CmdAddItemTest {
    @Mock
    private ConfiguredComponentBase configuredComponent;
    @Mock
    private LoadoutBase<?>          loadout;
    @Mock
    private Upgrades                upgrades;
    @Mock
    private MessageXBar             xBar;
    @Mock
    private ComponentBase           internalPart;

    @Before
    public void setup() {
        Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
        Mockito.when(configuredComponent.getInternalComponent()).thenReturn(internalPart);
        Mockito.when(internalPart.getLocation()).thenReturn(Location.CenterTorso);
    }

    @Test
    public void testDescription() throws Exception {
        Item item = ItemDB.ECM;

        CmdAddItem cut = new CmdAddItem(xBar, loadout, configuredComponent, item);

        assertTrue(cut.describe().contains("add"));
        assertTrue(cut.describe().contains("to"));
        assertTrue(cut.describe().contains(configuredComponent.getInternalComponent().getLocation().toString()));
        assertTrue(cut.describe().contains(item.getName()));
    }

    @Test
    public void testDescription_artemis() throws Exception {
        Item item = ItemDB.lookup("LRM 20");
        Mockito.when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

        CmdAddItem cut = new CmdAddItem(xBar, loadout, configuredComponent, item);

        assertTrue(cut.describe().contains("add"));
        assertTrue(cut.describe().contains("to"));
        assertTrue(cut.describe().contains(configuredComponent.getInternalComponent().getLocation().toString()));
        assertTrue(cut.describe().contains(item.getName()));
    }

    /**
     * If an item can't be added, an exception shall be thrown when the operation is applied.
     * 
     * @throws Exception
     */
    @Test(expected = EquipResult.class)
    public void testCantAddItem() throws Exception {
        CmdAddItem cut = null;
        try {
            Item item = ItemDB.lookup("LRM 20");
            Mockito.when(loadout.canEquip(item)).thenReturn(EquipResult.SUCCESS);
            Mockito.when(configuredComponent.canEquip(item))
                    .thenReturn(EquipResult.make(EquipResultType.NotEnoughSlots));
            cut = new CmdAddItem(xBar, loadout, configuredComponent, item);
        }
        catch (Throwable t) {
            fail("Setup failed");
            return;
        }

        cut.apply();
    }

    /**
     * C.A.S.E. together with an XL engine should generate a warning notice
     * 
     * @throws Exception
     */
    @Test
    public void testAddItem_XLCase() throws Exception {
        Engine engine = Mockito.mock(Engine.class);
        Mockito.when(engine.getType()).thenReturn(EngineType.XL);
        Mockito.when(loadout.getEngine()).thenReturn(engine);
        Mockito.when(loadout.canEquip(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);
        Mockito.when(configuredComponent.canEquip(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);

        CmdAddItem cut = new CmdAddItem(xBar, loadout, configuredComponent, ItemDB.CASE);
        cut.apply();

        Mockito.verify(xBar).post(
                new NotificationMessage(Severity.WARNING, loadout, "C.A.S.E. together with XL engine has no effect."));
    }
}
