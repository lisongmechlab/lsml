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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

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
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link CmdAddItem}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class CmdAddItemTest {
    @Mock
    private ConfiguredComponent component;
    @Mock
    private Component internalPart;
    @Mock
    private Loadout loadout;
    @Mock
    private Upgrades upgrades;
    @Mock
    private MessageDelivery msgDelivery;

    @Before
    public void setup() {
        when(loadout.getUpgrades()).thenReturn(upgrades);
        when(component.getInternalComponent()).thenReturn(internalPart);
        when(internalPart.getLocation()).thenReturn(Location.CenterTorso);
    }

    /**
     * Internals are not valid items for adding
     * 
     * @throws EquipException
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testAddItem_Internals() throws EquipException {
        // Setup
        Internal item = mock(Internal.class);
        @SuppressWarnings("unused") // Should throw
        CmdAddItem cut = new CmdAddItem(null, loadout, component, item);
    }

    /**
     * Adding an item shall work with a <code>null</code> {@link MessageDelivery}.
     * 
     * @throws EquipException
     */
    @Test
    public final void testAddItem_NoMessage() throws EquipException {
        // Setup
        Item item = ItemDB.ECM;
        int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(component.addItem(item)).thenReturn(index);
        when(component.removeItem(item)).thenReturn(index);
        CmdAddItem cut = new CmdAddItem(null, loadout, component, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        InOrder io = inOrder(component);
        io.verify(component).addItem(item);

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        io.verify(component).removeItem(item);
    }

    /**
     * Adding an item shall be possible if the loadout and component supports it. Necessary manipulation of the
     * component and sending of messages is done in proper order.
     * 
     * @throws EquipException
     */
    @Test
    public final void testAddItem() throws EquipException {
        // Setup
        Item item = ItemDB.ECM;
        int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(component.addItem(item)).thenReturn(index);
        when(component.removeItem(item)).thenReturn(index);
        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        InOrder io = inOrder(component, msgDelivery);
        io.verify(component).addItem(item);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Added, item, index));

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        io.verify(component).removeItem(item);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Removed, item, index));
    }

    /**
     * Shouldn't throw or anything.
     * 
     * @throws EquipException
     */
    @Test
    public final void testAddItem_LargeBore_NotOmni() throws EquipException {
        // Setup
        Weapon item = (Weapon) ItemDB.lookup("ER PPC");
        assertTrue(item.isLargeBore());
        int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(component.addItem(item)).thenReturn(index);
        when(component.removeItem(item)).thenReturn(index);
        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        InOrder io = inOrder(component, msgDelivery);
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
    public final void testAddItem_NotLargeBore_HALAAUnaffected() throws EquipException {
        // Setup
        ConfiguredComponentOmniMech omniComponent = mock(ConfiguredComponentOmniMech.class);

        Weapon item = (Weapon) ItemDB.lookup("LARGE LASER");
        assertFalse(item.isLargeBore());
        int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.addItem(item)).thenReturn(index);
        when(omniComponent.removeItem(item)).thenReturn(index);
        when(omniComponent.getToggleState(ItemDB.HA)).thenReturn(true);
        when(omniComponent.getToggleState(ItemDB.LAA)).thenReturn(true);
        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, omniComponent, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        InOrder io = inOrder(omniComponent, msgDelivery);
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

    @Test
    public final void testAddItem_LargeBore_NoHALAA() throws EquipException {
        // Setup
        ConfiguredComponentOmniMech omniComponent = mock(ConfiguredComponentOmniMech.class);

        Weapon item = (Weapon) ItemDB.lookup("ER PPC");
        assertTrue(item.isLargeBore());
        int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.addItem(item)).thenReturn(index);
        when(omniComponent.removeItem(item)).thenReturn(index);
        when(omniComponent.getToggleState(ItemDB.HA)).thenReturn(false);
        when(omniComponent.getToggleState(ItemDB.LAA)).thenReturn(false);
        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, omniComponent, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        InOrder io = inOrder(omniComponent, msgDelivery);
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

    @Test
    public final void testAddItem_LargeBore_OnlyLAA() throws EquipException {
        // Setup
        ConfiguredComponentOmniMech omniComponent = mock(ConfiguredComponentOmniMech.class);

        Weapon item = (Weapon) ItemDB.lookup("ER PPC");
        assertTrue(item.isLargeBore());
        int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.addItem(item)).thenReturn(index);
        when(omniComponent.removeItem(item)).thenReturn(index);
        when(omniComponent.getToggleState(ItemDB.HA)).thenReturn(false);
        when(omniComponent.getToggleState(ItemDB.LAA)).thenReturn(true);
        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, omniComponent, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        InOrder io = inOrder(omniComponent, msgDelivery);
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

    @Test
    public final void testAddItem_LargeBore_BothHALAA() throws EquipException {
        // Setup
        ConfiguredComponentOmniMech omniComponent = mock(ConfiguredComponentOmniMech.class);

        Weapon item = (Weapon) ItemDB.lookup("ER PPC");
        assertTrue(item.isLargeBore());
        int index = 5;
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        when(omniComponent.addItem(item)).thenReturn(index);
        when(omniComponent.removeItem(item)).thenReturn(index);
        when(omniComponent.getToggleState(ItemDB.HA)).thenReturn(true);
        when(omniComponent.getToggleState(ItemDB.LAA)).thenReturn(true);
        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, omniComponent, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        InOrder io = inOrder(omniComponent, msgDelivery);
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

    /**
     * Operation will fail with a {@link EquipResult} exception if the loadout cannot support the item.
     * 
     * @throws EquipException
     */
    @Test
    public final void testAddItem_NoLoadoutSupport() throws EquipException {
        // Setup
        Item item = ItemDB.ECM;
        EquipResult result = EquipResult.make(Location.LeftArm, EquipResultType.NoFreeHardPoints);
        when(loadout.canEquipDirectly(item)).thenReturn(result);
        when(component.canEquip(item)).thenReturn(EquipResult.SUCCESS);
        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, item);

        // Execute (do)
        try {
            cut.apply();
            fail("Expected exception!");
        }
        catch (EquipException e) {
            // No-op
        }

        // No manipulation!
        verify(component, never()).addItem(any(Item.class));
        verifyZeroInteractions(msgDelivery);
    }

    /**
     * Operation will fail with a {@link EquipResult} exception if the component cannot support the item.
     * 
     * @throws EquipException
     */
    @Test
    public final void testAddItem_NoComponentSupport() throws EquipException {
        // Setup
        Item item = ItemDB.ECM;
        EquipResult result = EquipResult.make(Location.LeftArm, EquipResultType.NoFreeHardPoints);
        when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(item)).thenReturn(result);
        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, item);

        // Execute (do)
        try {
            cut.apply();
            fail("Expected exception!");
        }
        catch (EquipException e) {
            // No-op
        }

        // No manipulation!
        verify(component, never()).addItem(any(Item.class));
        verifyZeroInteractions(msgDelivery);
    }

    @Test
    public final void testAddItem_AddEnginePossible() throws EquipException {
        Engine engine = (Engine) ItemDB.lookup("STD ENGINE 300");
        int index = 0;
        when(loadout.canEquipDirectly(engine)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(engine)).thenReturn(EquipResult.SUCCESS);
        when(component.addItem(engine)).thenReturn(index);
        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, engine);

        cut.apply();

        InOrder io = inOrder(component, msgDelivery);
        io.verify(component).addItem(engine);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Added, engine, index));

        cut.undo();
        io.verify(component).removeItem(engine);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Removed, engine, index));

        verifyNoMoreInteractions(msgDelivery);
    }

    @Test
    public final void testAddItem_AddEngineXLSides() throws EquipException {
        Engine engine = (Engine) ItemDB.lookup("XL ENGINE 300");
        int index = 0;
        int indexSideLt = 1;
        int indexSideRt = 2;

        ConfiguredComponent lt = mock(ConfiguredComponent.class);
        ConfiguredComponent rt = mock(ConfiguredComponent.class);
        when(loadout.getComponent(Location.LeftTorso)).thenReturn(lt);
        when(loadout.getComponent(Location.RightTorso)).thenReturn(rt);
        when(loadout.canEquipDirectly(engine)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(engine)).thenReturn(EquipResult.SUCCESS);
        when(component.addItem(engine)).thenReturn(index);
        when(lt.addItem(engine.getSide())).thenReturn(indexSideLt);
        when(rt.addItem(engine.getSide())).thenReturn(indexSideRt);
        when(component.removeItem(engine)).thenReturn(index + 1);
        when(lt.removeItem(engine.getSide())).thenReturn(indexSideLt + 1);
        when(rt.removeItem(engine.getSide())).thenReturn(indexSideRt + 1);
        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, engine);

        cut.apply();

        InOrder io = inOrder(component, msgDelivery);
        io.verify(component).addItem(engine);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Added, engine, index));

        InOrder ioLt = inOrder(lt, msgDelivery);
        ioLt.verify(lt).addItem(engine.getSide());
        ioLt.verify(msgDelivery).post(new ItemMessage(lt, Type.Added, engine.getSide(), indexSideLt));

        InOrder ioRt = inOrder(rt, msgDelivery);
        ioRt.verify(rt).addItem(engine.getSide());
        ioRt.verify(msgDelivery).post(new ItemMessage(rt, Type.Added, engine.getSide(), indexSideRt));

        cut.undo();

        io.verify(component).removeItem(engine);
        io.verify(msgDelivery).post(new ItemMessage(component, Type.Removed, engine, index + 1));

        ioLt.verify(lt).removeItem(engine.getSide());
        ioLt.verify(msgDelivery).post(new ItemMessage(lt, Type.Removed, engine.getSide(), indexSideLt + 1));

        ioRt.verify(rt).removeItem(engine.getSide());
        ioRt.verify(msgDelivery).post(new ItemMessage(rt, Type.Removed, engine.getSide(), indexSideRt + 1));

        verifyNoMoreInteractions(msgDelivery);
    }

    /**
     * C.A.S.E. together with an XL engine should generate a warning notice.
     * 
     * @throws Exception
     */
    @Test
    public void testAddItem_XLCase() throws Exception {
        Engine engine = mock(Engine.class);
        when(engine.getType()).thenReturn(EngineType.XL);
        when(loadout.getEngine()).thenReturn(engine);
        when(loadout.canEquipDirectly(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);

        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, ItemDB.CASE);
        cut.apply();

        verify(msgDelivery).post(new NotificationMessage(Severity.WARNING, loadout, CmdAddItem.XLCASE_WARNING));
    }

    /**
     * C.A.S.E. together with an STD engine should not generate a warning notice.
     * 
     * @throws Exception
     */
    @Test
    public void testAddItem_StdCase() throws Exception {
        Engine engine = mock(Engine.class);
        when(engine.getType()).thenReturn(EngineType.STD);
        when(loadout.getEngine()).thenReturn(engine);
        when(loadout.canEquipDirectly(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);

        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, ItemDB.CASE);
        cut.apply();

        verify(msgDelivery, never())
                .post(new NotificationMessage(Severity.WARNING, loadout, CmdAddItem.XLCASE_WARNING));
    }

    /**
     * C.A.S.E. without an engine should not generate a warning notice.
     * 
     * @throws Exception
     */
    @Test
    public void testAddItem_CaseNoEngine() throws Exception {
        when(loadout.getEngine()).thenReturn(null);
        when(loadout.canEquipDirectly(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(ItemDB.CASE)).thenReturn(EquipResult.SUCCESS);

        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, ItemDB.CASE);
        cut.apply();

        verify(msgDelivery, never())
                .post(new NotificationMessage(Severity.WARNING, loadout, CmdAddItem.XLCASE_WARNING));
    }

    /**
     * Only two GaussRifles can be charged simultaneously. Emit a warning if a third is added.
     * 
     * @throws Exception
     */
    @Test
    public void testAddItem_ManyGaussWarning() throws Exception {
        // Not really possible to have mixed IS/Clan weapons but helps us test handling both.
        BallisticWeapon gauss = (BallisticWeapon) ItemDB.lookup("GAUSS RIFLE");
        BallisticWeapon cgauss = (BallisticWeapon) ItemDB.lookup("C-GAUSS RIFLE");
        List<BallisticWeapon> ballistics = Arrays.asList(gauss, cgauss);

        when(loadout.items(BallisticWeapon.class)).thenReturn(ballistics);
        when(loadout.canEquipDirectly(gauss)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(gauss)).thenReturn(EquipResult.SUCCESS);

        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, gauss);
        cut.apply();

        verify(msgDelivery).post(new NotificationMessage(Severity.WARNING, loadout, CmdAddItem.MANY_GAUSS_WARNING));
    }

    /**
     * Only two GaussRifles can be charged simultaneously. Emit a warning if a third is added.
     * 
     * @throws Exception
     */
    @Test
    public void testAddItem_ManyGaussWarning_OnlyOneGauss() throws Exception {
        // Not really possible to have mixed IS/Clan weapons but helps us test handling both.
        BallisticWeapon gauss = (BallisticWeapon) ItemDB.lookup("GAUSS RIFLE");
        BallisticWeapon other = (BallisticWeapon) ItemDB.lookup("MACHINE GUN");
        List<BallisticWeapon> ballistics = Arrays.asList(gauss, other, other);

        when(loadout.items(BallisticWeapon.class)).thenReturn(ballistics);
        when(loadout.canEquipDirectly(gauss)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(gauss)).thenReturn(EquipResult.SUCCESS);

        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, gauss);
        cut.apply();

        verify(msgDelivery, never())
                .post(new NotificationMessage(Severity.WARNING, loadout, CmdAddItem.MANY_GAUSS_WARNING));
    }

    /**
     * Only two GaussRifles can be charged simultaneously. Emit a warning if a third is added.
     * 
     * @throws Exception
     */
    @Test
    public void testAddItem_ManyGaussWarning_LotsGauss() throws Exception {
        // Not really possible to have mixed IS/Clan weapons but helps us test handling both.
        BallisticWeapon gauss = (BallisticWeapon) ItemDB.lookup("GAUSS RIFLE");
        BallisticWeapon other = (BallisticWeapon) ItemDB.lookup("MACHINE GUN");
        List<BallisticWeapon> ballistics = Arrays.asList(gauss, gauss, gauss);

        when(loadout.items(BallisticWeapon.class)).thenReturn(ballistics);
        when(loadout.canEquipDirectly(other)).thenReturn(EquipResult.SUCCESS);
        when(component.canEquip(other)).thenReturn(EquipResult.SUCCESS);

        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, other);
        cut.apply();

        verify(msgDelivery, never())
                .post(new NotificationMessage(Severity.WARNING, loadout, CmdAddItem.MANY_GAUSS_WARNING));
    }

    /**
     * If an item can't be added, an exception shall be thrown when the operation is applied.
     * 
     * @throws Exception
     */
    @Test(expected = EquipException.class)
    public void testCantAddItem() throws Exception {
        CmdAddItem cut = null;
        try {
            Item item = ItemDB.lookup("LRM 20");
            when(loadout.canEquipDirectly(item)).thenReturn(EquipResult.SUCCESS);
            when(component.canEquip(item)).thenReturn(EquipResult.make(EquipResultType.NotEnoughSlots));
            cut = new CmdAddItem(msgDelivery, loadout, component, item);
        }
        catch (Throwable t) {
            fail("Setup failed");
            return;
        }

        cut.apply();
    }

    @Test
    public void testDescription() throws Exception {
        Item item = ItemDB.ECM;

        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, item);

        assertTrue(cut.describe().contains("add"));
        assertTrue(cut.describe().contains("to"));
        assertTrue(cut.describe().contains(component.getInternalComponent().getLocation().toString()));
        assertTrue(cut.describe().contains(item.getName()));
    }

    @Test
    public void testDescription_artemis() throws Exception {
        Item item = ItemDB.lookup("LRM 20");
        when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

        CmdAddItem cut = new CmdAddItem(msgDelivery, loadout, component, item);

        assertTrue(cut.describe().contains("add"));
        assertTrue(cut.describe().contains("to"));
        assertTrue(cut.describe().contains(component.getInternalComponent().getLocation().toString()));
        assertTrue(cut.describe().contains(item.getName()));
    }
}
