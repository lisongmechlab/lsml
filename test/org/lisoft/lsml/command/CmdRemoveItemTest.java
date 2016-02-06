package org.lisoft.lsml.command;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.calls;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.ItemMessage.Type;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.Component;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CmdRemoveItemTest {
    @Mock
    private ConfiguredComponent component;
    @Mock
    private Loadout             loadout;
    @Mock
    private Upgrades                upgrades;
    @Mock
    private MessageXBar             xBar;
    @Mock
    private Component           internalPart;

    @Before
    public void setup() {
        Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
        Mockito.when(component.getInternalComponent()).thenReturn(internalPart);
        Mockito.when(internalPart.getLocation()).thenReturn(Location.CenterTorso);
    }

    @Test
    public void testDescription() throws Exception {
        Item item = ItemDB.ECM;

        CmdRemoveItem cut = new CmdRemoveItem(xBar, loadout, component, item);

        assertTrue(cut.describe().contains("remove"));
        assertTrue(cut.describe().contains("from"));
        assertTrue(cut.describe().contains(component.getInternalComponent().getLocation().toString()));
        assertTrue(cut.describe().contains(item.getName()));
    }

    @Test
    public void testDescription_artemis() throws Exception {
        Item item = ItemDB.lookup("LRM 20");
        Mockito.when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

        CmdRemoveItem cut = new CmdRemoveItem(xBar, loadout, component, item);

        assertTrue(cut.describe().contains("remove"));
        assertTrue(cut.describe().contains("from"));
        assertTrue(cut.describe().contains(component.getInternalComponent().getLocation().toString()));
        assertTrue(cut.describe().contains(item.getName()));
    }

    /**
     * If an item can't be removed, an exception shall be thrown when the operation is applied.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCantRemoveItem() {
        CmdRemoveItem cut = null;
        try {
            Item item = ItemDB.lookup("LRM 20");
            Mockito.when(component.getItemsEquipped()).thenReturn(new ArrayList<Item>());
            cut = new CmdRemoveItem(xBar, loadout, component, item);
        }
        catch (Throwable t) {
            fail("Setup failed");
            return;
        }

        cut.apply();
    }

    @Test
    public final void testRemoveItem() throws EquipException {
        // Setup
        Item item = ItemDB.ECM;
        int index = 4;
        when(component.canRemoveItem(item)).thenReturn(true);
        when(component.removeItem(item)).thenReturn(index);
        when(component.addItem(item)).thenReturn(index);
        CmdRemoveItem cut = new CmdRemoveItem(xBar, loadout, component, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        InOrder io = inOrder(component, xBar);
        io.verify(component).removeItem(item);
        io.verify(xBar).post(new ItemMessage(component, Type.Removed, item, index));

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        io.verify(component).addItem(item);
        io.verify(xBar).post(new ItemMessage(component, Type.Added, item, index));
    }

    @Test
    public final void testRemoveItem_NoMessages() throws EquipException {
        // Setup
        Item item = ItemDB.ECM;
        int index = 4;
        when(component.canRemoveItem(item)).thenReturn(true);
        when(component.removeItem(item)).thenReturn(index);
        when(component.addItem(item)).thenReturn(index);
        CmdRemoveItem cut = new CmdRemoveItem(null, loadout, component, item);

        // Execute (do)
        cut.apply();

        // Verify (do)
        InOrder io = inOrder(component, xBar);
        io.verify(component).removeItem(item);

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        io.verify(component).addItem(item);
    }

    private final void testRemoveEngine(Engine aEngine, HeatSinkUpgrade aSinkUpgrade, int aEngineHS)
            throws EquipException {
        // Setup
        Item hsType = aSinkUpgrade.getHeatSinkType();
        int index = 0;
        int indexLt = 3;
        int indexRt = 5;

        ConfiguredComponent lt = mock(ConfiguredComponent.class);
        ConfiguredComponent rt = mock(ConfiguredComponent.class);
        when(lt.removeItem(aEngine.getSide())).thenReturn(indexLt);
        when(rt.removeItem(aEngine.getSide())).thenReturn(indexRt);
        when(lt.addItem(aEngine.getSide())).thenReturn(indexLt);
        when(rt.addItem(aEngine.getSide())).thenReturn(indexRt);

        when(upgrades.getHeatSink()).thenReturn(aSinkUpgrade);
        when(loadout.getComponent(Location.LeftTorso)).thenReturn(lt);
        when(loadout.getComponent(Location.RightTorso)).thenReturn(rt);
        when(loadout.getUpgrades()).thenReturn(upgrades);
        when(component.getEngineHeatSinks()).thenReturn(aEngineHS);
        when(component.canRemoveItem(aEngine)).thenReturn(true);
        when(component.removeItem(aEngine)).thenReturn(index);
        when(component.addItem(aEngine)).thenReturn(index);
        when(component.removeItem(hsType)).thenReturn(-1);
        when(component.addItem(hsType)).thenReturn(-1);
        CmdRemoveItem cut = new CmdRemoveItem(xBar, loadout, component, aEngine);

        // Execute (do)
        cut.apply();

        // Verify (do)
        if (aEngine.getType() == EngineType.XL) {
            InOrder ioLeft = inOrder(lt, xBar);
            ioLeft.verify(lt).removeItem(aEngine.getSide());
            ioLeft.verify(xBar).post(new ItemMessage(lt, Type.Removed, aEngine.getSide(), indexLt));

            InOrder ioRight = inOrder(rt, xBar);
            ioRight.verify(rt).removeItem(aEngine.getSide());
            ioRight.verify(xBar).post(new ItemMessage(rt, Type.Removed, aEngine.getSide(), indexRt));
        }

        InOrder io = inOrder(component, xBar);
        for (int i = 0; i < aEngineHS; ++i) {
            io.verify(component, calls(1)).removeItem(hsType); // HS first
            io.verify(xBar, calls(1)).post(new ItemMessage(component, Type.Removed, hsType, -1));
        }
        io.verify(component).removeItem(aEngine);
        io.verify(xBar).post(new ItemMessage(component, Type.Removed, aEngine, index));

        // Execute (undo)
        cut.undo();

        // Verify (undo)
        if (aEngine.getType() == EngineType.XL) {
            InOrder ioLeft = inOrder(lt, xBar);
            ioLeft.verify(lt).addItem(aEngine.getSide());
            ioLeft.verify(xBar).post(new ItemMessage(lt, Type.Added, aEngine.getSide(), indexLt));

            InOrder ioRight = inOrder(rt, xBar);
            ioRight.verify(rt).addItem(aEngine.getSide());
            ioRight.verify(xBar).post(new ItemMessage(rt, Type.Added, aEngine.getSide(), indexRt));
        }
        io.verify(component).addItem(aEngine);
        io.verify(xBar).post(new ItemMessage(component, Type.Added, aEngine, index));
        for (int i = 0; i < aEngineHS; ++i) {
            io.verify(component, calls(1)).addItem(hsType); // HS first
            io.verify(xBar, calls(1)).post(new ItemMessage(component, Type.Added, hsType, -1));
        }
    }

    /**
     * Removing an XL engine shall also remove ENGINE_INTERNAL from side torsii
     * 
     * @throws EquipException
     */
    @Test
    public final void testRemoveItem_XLEngine() throws EquipException {
        Engine engine = (Engine) ItemDB.lookup("XL ENGINE 300");
        testRemoveEngine(engine, UpgradeDB.IS_DHS, 0);
    }

    /**
     * Removing a standard engine shall also remove engine heat sinks (SHS).
     * 
     * @throws EquipException
     */
    @Test
    public final void testRemoveItem_StdEngine_DHS() throws EquipException {
        Engine engine = (Engine) ItemDB.lookup("STD ENGINE 300");
        testRemoveEngine(engine, UpgradeDB.IS_DHS, 2);
    }

    /**
     * Removing a standard engine shall also remove engine heat sinks (DHS).
     * 
     * @throws EquipException
     */
    @Test
    public final void testRemoveItem_StdEngine_SHS() throws EquipException {
        Engine engine = (Engine) ItemDB.lookup("STD ENGINE 300");
        testRemoveEngine(engine, UpgradeDB.IS_SHS, 2);
    }

    /**
     * Internal items can't be removed. Shall throw directly on creation.
     * 
     * It is a programmer error to attempt to remove an internal.
     * 
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCantRemoveInternal() throws Exception {
        Internal item = Mockito.mock(Internal.class);
        @SuppressWarnings("unused")
        CmdRemoveItem cmdRemoveItem = new CmdRemoveItem(xBar, loadout, component, item);
    }
}
