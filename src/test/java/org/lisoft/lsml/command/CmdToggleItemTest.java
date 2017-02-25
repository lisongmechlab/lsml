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
package org.lisoft.lsml.command;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.ItemMessage.Type;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.chassi.ComponentOmniMech;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.Loadout;
import org.mockito.InOrder;

@SuppressWarnings("javadoc")
public class CmdToggleItemTest {

    private Location location;
    private ComponentOmniMech internalComponent;
    private ConfiguredComponentOmniMech component;
    private MessageDelivery msgDelivery;

    @Before
    public void setup() {
        location = Location.CenterTorso;

        internalComponent = mock(ComponentOmniMech.class);
        when(internalComponent.getLocation()).thenReturn(location);

        component = mock(ConfiguredComponentOmniMech.class);
        when(component.getInternalComponent()).thenReturn(internalComponent);

        msgDelivery = mock(MessageDelivery.class);
    }

    @Test
    public final void testDescribe() {
        final Loadout loadout = makeLoadoutMock(10);
        final CmdToggleItem cmdToggleItem = new CmdToggleItem(msgDelivery, loadout, component, ItemDB.LAA, true);
        final String description = cmdToggleItem.describe().toLowerCase();
        assertTrue(description.contains("toggle"));
        assertTrue(description.contains(ItemDB.LAA.getName().toLowerCase()));
    }

    @Test
    public final void testToggle_DisableHA() throws Exception {
        // Setup
        final Loadout loadout = makeLoadoutMock(0); // Disable shall work with 0 free crit slots
        when(component.getToggleState(ItemDB.HA)).thenReturn(true);
        final CmdToggleItem cut = new CmdToggleItem(msgDelivery, loadout, component, ItemDB.HA, false);

        // Execute (do)
        cut.apply();

        // Verify (do)
        final InOrder inOrder = inOrder(msgDelivery, component);
        inOrder.verify(component).setToggleState(ItemDB.HA, false);
        inOrder.verify(msgDelivery).post(new ItemMessage(component, Type.Removed, ItemDB.HA, -1));

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        inOrder.verify(component).setToggleState(ItemDB.HA, true);
        inOrder.verify(msgDelivery).post(new ItemMessage(component, Type.Added, ItemDB.HA, -1));
    }

    @Test
    public final void testToggle_DisableLAA_HasHA() throws Exception {
        // Setup
        final boolean oldState = true;
        final boolean haOldState = true;

        final Loadout loadout = makeLoadoutMock(0); // Disable shall work with 0 free crit slots
        when(component.getToggleState(ItemDB.LAA)).thenReturn(oldState);
        when(component.getToggleState(ItemDB.HA)).thenReturn(haOldState);
        final CmdToggleItem cut = new CmdToggleItem(msgDelivery, loadout, component, ItemDB.LAA, false);

        // Execute (do)
        cut.apply();

        // Verify (do)
        final InOrder inOrder = inOrder(msgDelivery, component);
        inOrder.verify(component).setToggleState(ItemDB.HA, false);
        inOrder.verify(msgDelivery).post(new ItemMessage(component, Type.Removed, ItemDB.HA, -1));
        inOrder.verify(component).setToggleState(ItemDB.LAA, false);
        inOrder.verify(msgDelivery).post(new ItemMessage(component, Type.Removed, ItemDB.LAA, -1));

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        inOrder.verify(component).setToggleState(ItemDB.HA, true);
        inOrder.verify(msgDelivery).post(new ItemMessage(component, Type.Added, ItemDB.HA, -1));
        inOrder.verify(component).setToggleState(ItemDB.LAA, true);
        inOrder.verify(msgDelivery).post(new ItemMessage(component, Type.Added, ItemDB.LAA, -1));
    }

    @Test
    public final void testToggle_DisableLAA_NoHA() throws Exception {
        // Setup
        final boolean oldState = true;
        final Loadout loadout = makeLoadoutMock(0); // Disable shall work with 0 free crit slots
        when(component.getToggleState(ItemDB.LAA)).thenReturn(oldState);
        final CmdToggleItem cut = new CmdToggleItem(msgDelivery, loadout, component, ItemDB.LAA, false);

        // Execute (do)
        cut.apply();

        // Verify (do)
        final InOrder inOrder = inOrder(msgDelivery, component);
        inOrder.verify(component).setToggleState(ItemDB.LAA, false);
        inOrder.verify(msgDelivery).post(new ItemMessage(component, Type.Removed, ItemDB.LAA, -1));

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        inOrder.verify(component).setToggleState(ItemDB.LAA, true);
        inOrder.verify(msgDelivery).post(new ItemMessage(component, Type.Added, ItemDB.LAA, -1));
    }

    @Test
    public final void testToggle_EnableHA() throws Exception {
        // Setup
        final Loadout loadout = makeLoadoutMock(1); // Disable shall work with 0 free crit slots
        when(component.getToggleState(ItemDB.LAA)).thenReturn(true);
        when(component.getToggleState(ItemDB.HA)).thenReturn(false);
        when(component.canToggleOn(ItemDB.HA)).thenReturn(EquipResult.SUCCESS);

        final CmdToggleItem cut = new CmdToggleItem(msgDelivery, loadout, component, ItemDB.HA, true);

        // Execute (do)
        cut.apply();

        // Verify (do)
        final InOrder inOrder = inOrder(msgDelivery, component);
        inOrder.verify(component).setToggleState(ItemDB.HA, true);
        inOrder.verify(msgDelivery).post(new ItemMessage(component, Type.Added, ItemDB.HA, -1));

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        inOrder.verify(component).setToggleState(ItemDB.HA, false);
        inOrder.verify(msgDelivery).post(new ItemMessage(component, Type.Removed, ItemDB.HA, -1));
    }

    @Test(expected = EquipException.class)
    public final void testToggle_EnableHABeforeLAA() throws Exception {
        // Setup
        final Loadout loadout = makeLoadoutMock(1); // Disable shall work with 0 free crit slots
        when(component.getToggleState(ItemDB.LAA)).thenReturn(false);
        when(component.getToggleState(ItemDB.HA)).thenReturn(false);
        when(component.canToggleOn(ItemDB.HA)).thenReturn(EquipResult.SUCCESS);

        final CmdToggleItem cut = new CmdToggleItem(msgDelivery, loadout, component, ItemDB.HA, true);

        // Execute (do)
        cut.apply();
    }

    @Test
    public final void testToggle_EnableLAA() throws Exception {
        // Setup
        final Loadout loadout = makeLoadoutMock(1); // Disable shall work with 0 free crit slots
        when(component.getToggleState(ItemDB.LAA)).thenReturn(false);
        when(component.getToggleState(ItemDB.HA)).thenReturn(false);
        when(component.canToggleOn(ItemDB.LAA)).thenReturn(EquipResult.SUCCESS);
        final CmdToggleItem cut = new CmdToggleItem(msgDelivery, loadout, component, ItemDB.LAA, true);

        // Execute (do)
        cut.apply();

        // Verify (do)
        final InOrder inOrder = inOrder(msgDelivery, component);
        inOrder.verify(component).setToggleState(ItemDB.LAA, true);
        inOrder.verify(msgDelivery).post(new ItemMessage(component, Type.Added, ItemDB.LAA, -1));

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        inOrder.verify(component).setToggleState(ItemDB.LAA, false);
        inOrder.verify(msgDelivery).post(new ItemMessage(component, Type.Removed, ItemDB.LAA, -1));
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public final void testToggle_InvalidItem() {
        final Loadout loadout = makeLoadoutMock(10);
        new CmdToggleItem(msgDelivery, loadout, component, ItemDB.BAP, true);
    }

    /**
     * Functions correctly even with a <code>null</code> {@link MessageDelivery}.
     *
     * @throws Exception
     */
    @Test
    public final void testToggle_NoMessages() throws Exception {
        // Setup
        final Loadout loadout = makeLoadoutMock(1); // Disable shall work with 0 free crit slots
        when(component.getToggleState(ItemDB.LAA)).thenReturn(true);
        when(component.getToggleState(ItemDB.HA)).thenReturn(false);
        when(component.canToggleOn(ItemDB.HA)).thenReturn(EquipResult.SUCCESS);

        final CmdToggleItem cut = new CmdToggleItem(null, loadout, component, ItemDB.HA, true);

        // Execute (do)
        cut.apply();

        // Verify (do)
        final InOrder inOrder = inOrder(msgDelivery, component);
        inOrder.verify(component).setToggleState(ItemDB.HA, true);

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        inOrder.verify(component).setToggleState(ItemDB.HA, false);
    }

    @Test(expected = EquipException.class)
    public final void testToggle_NotAllowed() throws Exception {
        // Setup
        final Loadout loadout = makeLoadoutMock(1); // Disable shall work with 0 free crit slots
        when(component.getToggleState(ItemDB.LAA)).thenReturn(true);
        when(component.getToggleState(ItemDB.HA)).thenReturn(false);
        when(component.canToggleOn(ItemDB.HA)).thenReturn(EquipResult.make(EquipResultType.NoComponentSupport));

        final CmdToggleItem cut = new CmdToggleItem(msgDelivery, loadout, component, ItemDB.HA, true);

        // Execute (do)
        cut.apply();
    }

    @Test(expected = EquipException.class)
    public final void testToggle_NotEnoughSlots() throws Exception {
        // Setup
        final Loadout loadout = makeLoadoutMock(0); // Disable shall work with 0 free crit slots
        when(component.getToggleState(ItemDB.LAA)).thenReturn(true);
        when(component.getToggleState(ItemDB.HA)).thenReturn(false);
        when(component.canToggleOn(ItemDB.HA)).thenReturn(EquipResult.SUCCESS);

        final CmdToggleItem cut = new CmdToggleItem(msgDelivery, loadout, component, ItemDB.HA, true);

        // Execute (do)
        cut.apply();
    }

    @Test
    public final void testToggle_SameState() throws Exception {
        final Loadout loadout = makeLoadoutMock(10);
        when(component.getToggleState(ItemDB.LAA)).thenReturn(true);

        final CmdToggleItem cut = new CmdToggleItem(msgDelivery, loadout, component, ItemDB.LAA, true);
        cut.apply();
        cut.undo();

        verify(component, never()).setToggleState(any(Item.class), anyBoolean());
    }

    private Loadout makeLoadoutMock(int freeSlots) {
        final Loadout loadout = mock(Loadout.class);
        when(loadout.getFreeSlots()).thenReturn(freeSlots);
        return loadout;
    }
}
