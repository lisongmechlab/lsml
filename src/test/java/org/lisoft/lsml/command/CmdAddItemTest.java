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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.ItemMessage.Type;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.NotificationMessage;
import org.lisoft.lsml.messages.NotificationMessage.Severity;
import org.lisoft.lsml.model.chassi.Component;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.item.*;
import org.lisoft.lsml.model.loadout.*;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for {@link CmdAddItem}.
 *
 * @author Li Song
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.Silent.class)
public class CmdAddItemTest {
    @Mock
    private ConfiguredComponent component;
    @Mock
    private Component internalPart;
    @Mock
    private Loadout loadout;
    @Mock
    private MessageDelivery msgDelivery;
    @Mock
    private Upgrades upgrades;

    @Before
    public void setup() {
        when(loadout.getUpgrades()).thenReturn(upgrades);
        when(component.getInternalComponent()).thenReturn(internalPart);
        when(internalPart.getLocation()).thenReturn(Location.CenterTorso);
    }

    /**
     * Adding an item shall be possible if the loadout and component supports it. Necessary manipulation of the
     * component and sending of messages is done in proper order.
     */
    @Test
    public final void testAddItem() throws EquipException {
        // Setup
        final Item item = ItemDB.ECM;
        final int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(component.addItem(item)).thenReturn(index);
        when(component.removeItem(item)).thenReturn(index);
        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        final InOrder io = inOrder(component, msgDelivery);
        io.verify(component).addItem(item);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Added, item, index));

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        io.verify(component).removeItem(item);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Removed, item, index));
    }

    @Test
    public final void testAddItem_AddEnginePossible() throws Exception {
        final Engine engine = (Engine) ItemDB.lookup("STD ENGINE 300");
        final int index = 0;
        when(loadout.canEquipDirectly(engine)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(engine)).thenReturn(EquipResult.SUCCESS);
        when(component.addItem(engine)).thenReturn(index);
        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, engine);

        cut.apply();

        final InOrder io = inOrder(component, msgDelivery);
        io.verify(component).addItem(engine);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Added, engine, index));

        cut.undo();
        io.verify(component).removeItem(engine);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Removed, engine, index));

        verifyNoMoreInteractions(msgDelivery);
    }

    @Test
    public final void testAddItem_AddEngineXLSides() throws Exception {
        final Engine engine = (Engine) ItemDB.lookup("XL ENGINE 300");
        final int index = 0;
        final int indexSideLt = 1;
        final int indexSideRt = 2;

        final ConfiguredComponent lt = mock(ConfiguredComponent.class);
        final ConfiguredComponent rt = mock(ConfiguredComponent.class);
        when(loadout.getComponent(Location.LeftTorso)).thenReturn(lt);
        when(loadout.getComponent(Location.RightTorso)).thenReturn(rt);
        when(loadout.canEquipDirectly(engine)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(engine)).thenReturn(EquipResult.SUCCESS);
        when(component.addItem(engine)).thenReturn(index);
        Internal side = engine.getSide().get();
        when(lt.addItem(side)).thenReturn(indexSideLt);
        when(rt.addItem(side)).thenReturn(indexSideRt);
        when(component.removeItem(engine)).thenReturn(index + 1);
        when(lt.removeItem(side)).thenReturn(indexSideLt + 1);
        when(rt.removeItem(side)).thenReturn(indexSideRt + 1);
        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, engine);

        cut.apply();

        final InOrder io = inOrder(component, msgDelivery);
        io.verify(component).addItem(engine);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Added, engine, index));

        final InOrder ioLt = inOrder(lt, msgDelivery);
        ioLt.verify(lt).addItem(side);
        ioLt.verify(msgDelivery).post(new ItemMessage(lt, Type.Added, side, indexSideLt));

        final InOrder ioRt = inOrder(rt, msgDelivery);
        ioRt.verify(rt).addItem(side);
        ioRt.verify(msgDelivery).post(new ItemMessage(rt, Type.Added, side, indexSideRt));

        cut.undo();

        io.verify(component).removeItem(engine);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Removed, engine, index + 1));

        ioLt.verify(lt).removeItem(side);
        ioLt.verify(msgDelivery).post(new ItemMessage(lt, Type.Removed, side, indexSideLt + 1));

        ioRt.verify(rt).removeItem(side);
        ioRt.verify(msgDelivery).post(new ItemMessage(rt, Type.Removed, side, indexSideRt + 1));

        verifyNoMoreInteractions(msgDelivery);
    }

    /**
     * C.A.S.E. without an engine should not generate a warning notice.
     */
    @Test
    public void testAddItem_CaseNoEngine() throws Exception {
        when(loadout.getEngine()).thenReturn(null);
        when(loadout.canEquipDirectly(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);

        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, ItemDB.CASE);
        cut.apply();

        verify(msgDelivery, never()).post(
                new NotificationMessage(Severity.WARNING, loadout, CmdAddItem.XLCASE_WARNING));
    }

    /**
     * Internals are not valid items for adding.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public final void testAddItem_Internals() throws EquipException {
        // Setup
        final Internal item = mock(Internal.class);
        new CmdAddItem(null, loadout, component, item);
    }

    @Test
    public final void testAddItem_LargeBore_BothHALAA() throws Exception {
        // Setup
        final ConfiguredComponentOmniMech omniComponent = mock(ConfiguredComponentOmniMech.class);

        final Weapon item = (Weapon) ItemDB.lookup("ER PPC");
        assertTrue(item.isLargeBore());
        final int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.addItem(item)).thenReturn(index);
        when(omniComponent.removeItem(item)).thenReturn(index);
        when(omniComponent.getToggleState(ItemDB.HA)).thenReturn(true);
        when(omniComponent.getToggleState(ItemDB.LAA)).thenReturn(true);
        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, omniComponent, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        final InOrder io = inOrder(omniComponent, msgDelivery);
        io.verify(omniComponent).setToggleState(ItemDB.HA, false);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Removed, ItemDB.HA, -1));
        io.verify(omniComponent).setToggleState(ItemDB.LAA, false);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Removed, ItemDB.LAA, -1));
        io.verify(omniComponent).addItem(item);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Added, item, index));

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        io.verify(omniComponent).removeItem(item);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Removed, item, index));
        io.verify(omniComponent).setToggleState(ItemDB.LAA, true);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Added, ItemDB.LAA, -1));
        io.verify(omniComponent).setToggleState(ItemDB.HA, true);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Added, ItemDB.HA, -1));
        verifyNoMoreInteractions(msgDelivery);
    }

    @Test
    public final void testAddItem_LargeBore_NoHALAA() throws Exception {
        // Setup
        final ConfiguredComponentOmniMech omniComponent = mock(ConfiguredComponentOmniMech.class);

        final Weapon item = (Weapon) ItemDB.lookup("ER PPC");
        assertTrue(item.isLargeBore());
        final int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.addItem(item)).thenReturn(index);
        when(omniComponent.removeItem(item)).thenReturn(index);
        when(omniComponent.getToggleState(ItemDB.HA)).thenReturn(false);
        when(omniComponent.getToggleState(ItemDB.LAA)).thenReturn(false);
        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, omniComponent, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        final InOrder io = inOrder(omniComponent, msgDelivery);
        io.verify(omniComponent).addItem(item);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Added, item, index));
        verify(omniComponent, never()).setToggleState(any(Item.class), anyBoolean());

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        io.verify(omniComponent).removeItem(item);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Removed, item, index));
        verify(omniComponent, never()).setToggleState(any(Item.class), anyBoolean());
        verifyNoMoreInteractions(msgDelivery);
    }

    /**
     * Shouldn't throw or anything.
     */
    @Test
    public final void testAddItem_LargeBore_NotOmni() throws Exception {
        // Setup
        final Weapon item = (Weapon) ItemDB.lookup("ER PPC");
        assertTrue(item.isLargeBore());
        final int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(component.addItem(item)).thenReturn(index);
        when(component.removeItem(item)).thenReturn(index);
        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        final InOrder io = inOrder(component, msgDelivery);
        io.verify(component).addItem(item);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Added, item, index));

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        io.verify(component).removeItem(item);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Removed, item, index));
        verifyNoMoreInteractions(msgDelivery);
    }

    @Test
    public final void testAddItem_LargeBore_OnlyLAA() throws Exception {
        // Setup
        final ConfiguredComponentOmniMech omniComponent = mock(ConfiguredComponentOmniMech.class);

        final Weapon item = (Weapon) ItemDB.lookup("ER PPC");
        assertTrue(item.isLargeBore());
        final int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.addItem(item)).thenReturn(index);
        when(omniComponent.removeItem(item)).thenReturn(index);
        when(omniComponent.getToggleState(ItemDB.HA)).thenReturn(false);
        when(omniComponent.getToggleState(ItemDB.LAA)).thenReturn(true);
        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, omniComponent, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        final InOrder io = inOrder(omniComponent, msgDelivery);
        io.verify(omniComponent).setToggleState(ItemDB.LAA, false);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Removed, ItemDB.LAA, -1));
        io.verify(omniComponent).addItem(item);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Added, item, index));
        verify(omniComponent, never()).setToggleState(eq(ItemDB.HA), anyBoolean());

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        io.verify(omniComponent).removeItem(item);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Removed, item, index));
        io.verify(omniComponent).setToggleState(ItemDB.LAA, true);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Added, ItemDB.LAA, -1));
        verify(omniComponent, never()).setToggleState(eq(ItemDB.HA), anyBoolean());
        verifyNoMoreInteractions(msgDelivery);
    }

    /**
     * Only two GaussRifles can be charged simultaneously. Emit a warning if a third is added.
     */
    @Test
    public void testAddItem_ManyGaussWarning() throws Exception {
        // Not really possible to have mixed IS/Clan weapons but helps us test handling
        // both.
        final BallisticWeapon gauss = (BallisticWeapon) ItemDB.lookup("GAUSS RIFLE");
        final BallisticWeapon cgauss = (BallisticWeapon) ItemDB.lookup("C-GAUSS RIFLE");
        final List<BallisticWeapon> ballistics = Arrays.asList(gauss, cgauss);

        when(loadout.items(BallisticWeapon.class)).thenReturn(ballistics);
        when(loadout.canEquipDirectly(gauss)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(gauss)).thenReturn(EquipResult.SUCCESS);

        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, gauss);
        cut.apply();

        verify(msgDelivery).post(new NotificationMessage(Severity.WARNING, loadout, CmdAddItem.MANY_GAUSS_WARNING));
    }

    /**
     * Only two GaussRifles can be charged simultaneously. Emit a warning if a third is added.
     */
    @Test
    public void testAddItem_ManyGaussWarning_LotsGauss() throws Exception {
        // Not really possible to have mixed IS/Clan weapons but helps us test handling
        // both.
        final BallisticWeapon gauss = (BallisticWeapon) ItemDB.lookup("GAUSS RIFLE");
        final BallisticWeapon other = (BallisticWeapon) ItemDB.lookup("MACHINE GUN");
        final List<BallisticWeapon> ballistics = Arrays.asList(gauss, gauss, gauss);

        when(loadout.items(BallisticWeapon.class)).thenReturn(ballistics);
        when(loadout.canEquipDirectly(other)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(other)).thenReturn(EquipResult.SUCCESS);

        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, other);
        cut.apply();

        verify(msgDelivery, never()).post(
                new NotificationMessage(Severity.WARNING, loadout, CmdAddItem.MANY_GAUSS_WARNING));
    }

    /**
     * Only two GaussRifles can be charged simultaneously. Emit a warning if a third is added.
     */
    @Test
    public void testAddItem_ManyGaussWarning_OnlyOneGauss() throws Exception {
        // Not really possible to have mixed IS/Clan weapons but helps us test handling
        // both.
        final BallisticWeapon gauss = (BallisticWeapon) ItemDB.lookup("GAUSS RIFLE");
        final BallisticWeapon other = (BallisticWeapon) ItemDB.lookup("MACHINE GUN");
        final List<BallisticWeapon> ballistics = Arrays.asList(gauss, other, other);

        when(loadout.items(BallisticWeapon.class)).thenReturn(ballistics);
        when(loadout.canEquipDirectly(gauss)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(gauss)).thenReturn(EquipResult.SUCCESS);

        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, gauss);
        cut.apply();

        verify(msgDelivery, never()).post(
                new NotificationMessage(Severity.WARNING, loadout, CmdAddItem.MANY_GAUSS_WARNING));
    }

    /**
     * Operation will fail with a {@link EquipResult} exception if the component cannot support the item.
     */
    @Test
    public final void testAddItem_NoComponentSupport() {
        // Setup
        final Item item = ItemDB.ECM;
        final EquipResult result = EquipResult.make(Location.LeftArm, EquipResultType.NoFreeHardPoints);
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(item)).thenReturn(result);
        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, item);

        // Execute (do)
        try {
            cut.apply();
            fail("Expected exception!");
        } catch (final EquipException e) {
            // No-op
        }

        // No manipulation!
        verify(component, never()).addItem(any(Item.class));
        verifyNoInteractions(msgDelivery);
    }

    /**
     * Operation will fail with a {@link EquipResult} exception if the loadout cannot support the item.
     */
    @Test
    public final void testAddItem_NoLoadoutSupport() {
        // Setup
        final Item item = ItemDB.ECM;
        final EquipResult result = EquipResult.make(Location.LeftArm, EquipResultType.NoFreeHardPoints);
        when(loadout.canEquipDirectly(item)).thenReturn(result);
        when(component.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, item);

        // Execute (do)
        try {
            cut.apply();
            fail("Expected exception!");
        } catch (final EquipException e) {
            // No-op
        }

        // No manipulation!
        verify(component, never()).addItem(any(Item.class));
        verifyNoInteractions(msgDelivery);
    }

    /**
     * Adding an item shall work with a <code>null</code> {@link MessageDelivery}.
     */
    @Test
    public final void testAddItem_NoMessage() throws EquipException {
        // Setup
        final Item item = ItemDB.ECM;
        final int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(component.addItem(item)).thenReturn(index);
        when(component.removeItem(item)).thenReturn(index);
        final CmdAddItem cut = new CmdAddItem(null, loadout, component, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        final InOrder io = inOrder(component);
        io.verify(component).addItem(item);

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        io.verify(component).removeItem(item);
    }

    @Test
    public final void testAddItem_NotLargeBore_HALAAUnaffected() throws Exception {
        // Setup
        final ConfiguredComponentOmniMech omniComponent = mock(ConfiguredComponentOmniMech.class);

        final Weapon item = (Weapon) ItemDB.lookup("LARGE LASER");
        assertFalse(item.isLargeBore());
        final int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.addItem(item)).thenReturn(index);
        when(omniComponent.removeItem(item)).thenReturn(index);
        when(omniComponent.getToggleState(ItemDB.HA)).thenReturn(true);
        when(omniComponent.getToggleState(ItemDB.LAA)).thenReturn(true);
        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, omniComponent, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        final InOrder io = inOrder(omniComponent, msgDelivery);
        io.verify(omniComponent).addItem(item);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Added, item, index));
        verify(omniComponent, never()).setToggleState(any(Item.class), anyBoolean());

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        io.verify(omniComponent).removeItem(item);
        io.verify(msgDelivery).post(new ItemMessage(omniComponent, Type.Removed, item, index));
        verify(omniComponent, never()).setToggleState(any(Item.class), anyBoolean());
        verifyNoMoreInteractions(msgDelivery);
    }

    /**
     * C.A.S.E. together with an engine where one side can be lost should not generate a warning notice.
     */
    @Test
    public void testAddItem_StdCase() throws Exception {
        final Engine engine = mock(Engine.class);
        when(engine.getSidesToLive()).thenReturn(1);
        when(loadout.getEngine()).thenReturn(engine);
        when(loadout.canEquipDirectly(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);

        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, ItemDB.CASE);
        cut.apply();

        verify(msgDelivery, never()).post(
                new NotificationMessage(Severity.WARNING, loadout, CmdAddItem.XLCASE_WARNING));
    }

    /**
     * C.A.S.E. together with an XL engine should generate a warning notice.
     */
    @Test
    public void testAddItem_XLCase() throws Exception {
        final Engine engine = mock(Engine.class);
        when(engine.getSidesToLive()).thenReturn(2);
        when(loadout.getEngine()).thenReturn(engine);
        when(loadout.canEquipDirectly(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);
        when(internalPart.getLocation()).thenReturn(Location.LeftTorso);

        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, ItemDB.CASE);
        cut.apply();

        verify(msgDelivery).post(new NotificationMessage(Severity.WARNING, loadout, CmdAddItem.XLCASE_WARNING));
    }

    /**
     * If an item can't be added, an exception shall be thrown when the operation is applied.
     */
    @Test(expected = EquipException.class)
    public void testCantAddItem() throws Exception {
        CmdAddItem cut = null;
        try {
            final Item item = ItemDB.lookup("LRM 20");
            when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
            when(component.canEquip(item)).thenReturn(EquipResult.make(EquipResultType.NotEnoughSlots));
            cut = new CmdAddItem(msgDelivery, loadout, component, item);
        } catch (final Throwable t) {
            fail("Setup failed");
            return;
        }

        cut.apply();
    }

    @Test
    public void testDescription() throws Exception {
        final Item item = ItemDB.ECM;

        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, item);

        assertTrue(cut.describe().contains("add"));
        assertTrue(cut.describe().contains("to"));
        assertTrue(cut.describe().contains(component.getInternalComponent().getLocation().toString()));
        assertTrue(cut.describe().contains(item.getName()));
    }

    @Test
    public void testDescription_artemis() throws Exception {
        final Item item = ItemDB.lookup("LRM 20");
        when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

        final CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, item);

        assertTrue(cut.describe().contains("add"));
        assertTrue(cut.describe().contains("to"));
        assertTrue(cut.describe().contains(component.getInternalComponent().getLocation().toString()));
        assertTrue(cut.describe().contains(item.getName()));
    }
}
