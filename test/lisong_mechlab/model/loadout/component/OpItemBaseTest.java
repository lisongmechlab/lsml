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
package lisong_mechlab.model.loadout.component;

import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.Message;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.Message.Type;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for the abstract {@link OpItemBase} class.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class OpItemBaseTest {

	class CutClass extends OpItemBase {
		public CutClass(MessageXBar anXBar, LoadoutBase<ConfiguredComponentBase> aLoadout,
				ConfiguredComponentBase aLoadoutPart, Item aItem) {
			super(anXBar, aLoadout, aLoadoutPart, aItem);
		}

		// @formatter:off
		@Override
		public String describe() {
			return null;
		}

		@Override
		protected void apply() {/* NO-OP */
		}

		@Override
		protected void undo() {/* NO-OP */
		}
		// @formatter:on
	}

	@Mock
	private LoadoutBase<ConfiguredComponentBase>	loadout;
	@Mock
	private ConfiguredComponentBase					configuredComponent;
	@Mock
	private MessageXBar								xBar;

	private CutClass								cut;

	@Before
	public void setup() {
		cut = new CutClass(xBar, loadout, configuredComponent, null);
	}

	/**
	 * removeItem() shall remove the item without qeustinos.
	 */
	@Test
	public final void testRemoveItem() {
		Item ecm = ItemDB.ECM;
		Mockito.when(configuredComponent.canRemoveItem(ecm)).thenReturn(true);

		cut.removeItem(ecm);

		Mockito.verify(configuredComponent).removeItem(ecm);
		Mockito.verify(xBar).post(new Message(configuredComponent, Type.ItemRemoved));
	}

	/**
	 * addItem() shall add an item without performing any checks.
	 */
	@Test
	public final void testAddItem() {
		Item ecm = ItemDB.ECM;

		cut.addItem(ecm);

		Mockito.verify(configuredComponent).addItem(ecm);
		Mockito.verify(xBar).post(new Message(configuredComponent, Type.ItemAdded));
	}

	/**
	 * Adding a standard engine shall not cause a stir.
	 */
	@Test
	public final void testAddItem_StdEngine() {
		Item item = ItemDB.lookup("STD ENGINE 300");

		cut.addItem(item);

		Mockito.verify(configuredComponent).addItem(item);
		Mockito.verify(xBar).post(new Message(configuredComponent, Type.ItemAdded));
	}

	/**
	 * Removing an XL engine shall also remove ENGINE_INTERNAL from side torsos
	 */
	@Test
	public final void testRemoveItem_XLEngine() {
		Item engine = ItemDB.lookup("XL ENGINE 300");
		ConfiguredComponentBase lt = Mockito.mock(ConfiguredComponentBase.class);
		ConfiguredComponentBase rt = Mockito.mock(ConfiguredComponentBase.class);
		Mockito.when(loadout.getComponent(Location.LeftTorso)).thenReturn(lt);
		Mockito.when(loadout.getComponent(Location.RightTorso)).thenReturn(rt);
		Mockito.when(configuredComponent.canRemoveItem(engine)).thenReturn(true);
		Upgrades upgrades = Mockito.mock(Upgrades.class);
		Mockito.when(upgrades.getHeatSink()).thenReturn(UpgradeDB.DOUBLE_HEATSINKS);
		Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);

		cut.removeItem(engine);

		Mockito.verify(configuredComponent).removeItem(engine);
		Mockito.verify(lt).removeItem(ConfiguredComponentBase.ENGINE_INTERNAL);
		Mockito.verify(rt).removeItem(ConfiguredComponentBase.ENGINE_INTERNAL);
		Mockito.verify(xBar).post(new Message(configuredComponent, Type.ItemRemoved));
		Mockito.verify(xBar).post(new Message(lt, Type.ItemRemoved));
		Mockito.verify(xBar).post(new Message(rt, Type.ItemRemoved));
	}

	/**
	 * Removing a standard engine shall also remove engine heat sinks (SHS).
	 */
	@Test
	public final void testRemoveItem_StdEngine_DHS() {
		int numEngineHs = 2;
		Item engine = ItemDB.lookup("STD ENGINE 300");
		Upgrades upgrades = Mockito.mock(Upgrades.class);
		Mockito.when(upgrades.getHeatSink()).thenReturn(UpgradeDB.DOUBLE_HEATSINKS);
		Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
		Mockito.when(configuredComponent.getEngineHeatsinks()).thenReturn(numEngineHs);
		Mockito.when(configuredComponent.canRemoveItem(engine)).thenReturn(true);

		cut.removeItem(engine);

		Mockito.verify(configuredComponent).removeItem(engine);
		Mockito.verify(configuredComponent, Mockito.times(numEngineHs)).removeItem(ItemDB.DHS);
		Mockito.verify(xBar).post(new Message(configuredComponent, Type.ItemRemoved));
	}

	/**
	 * Removing a standard engine shall also remove engine heat sinks (DHS).
	 */
	@Test
	public final void testRemoveItem_StdEngine_SHS() {
		int numEngineHs = 2;
		Item engine = ItemDB.lookup("STD ENGINE 300");
		Upgrades upgrades = Mockito.mock(Upgrades.class);
		Mockito.when(upgrades.getHeatSink()).thenReturn(UpgradeDB.STANDARD_HEATSINKS);
		Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
		Mockito.when(configuredComponent.getEngineHeatsinks()).thenReturn(numEngineHs);
		Mockito.when(configuredComponent.canRemoveItem(engine)).thenReturn(true);

		cut.removeItem(engine);

		Mockito.verify(configuredComponent).removeItem(engine);
		Mockito.verify(configuredComponent, Mockito.times(numEngineHs)).removeItem(ItemDB.SHS);
		Mockito.verify(xBar).post(new Message(configuredComponent, Type.ItemRemoved));
	}

	/**
	 * Adding an XL engine shall add LoadoutPart.ENGINE_INTERNAL to both side torsii.
	 */
	@Test
	public final void testAddItem_XLEngine() {
		ConfiguredComponentBase lt = Mockito.mock(ConfiguredComponentBase.class);
		ConfiguredComponentBase rt = Mockito.mock(ConfiguredComponentBase.class);
		Mockito.when(loadout.getComponent(Location.LeftTorso)).thenReturn(lt);
		Mockito.when(loadout.getComponent(Location.RightTorso)).thenReturn(rt);
		Item item = ItemDB.lookup("XL ENGINE 300");

		cut.addItem(item);

		Mockito.verify(configuredComponent).addItem(item);
		Mockito.verify(lt).addItem(ConfiguredComponentBase.ENGINE_INTERNAL);
		Mockito.verify(rt).addItem(ConfiguredComponentBase.ENGINE_INTERNAL);
		Mockito.verify(xBar).post(new Message(configuredComponent, Type.ItemAdded));
		Mockito.verify(xBar).post(new Message(lt, Type.ItemAdded));
		Mockito.verify(xBar).post(new Message(rt, Type.ItemAdded));
	}

	/**
	 * addItem() has a special case where it shall re-add engine heat sinks that were removed in a previous call to
	 * removeItem(). This is to facilitate undo behavior on engines to include heat sinks (SHS)
	 */
	@Test
	public final void testAddItem_addEngineAfterRemoveSHS() {
		final int numEngineHs = 2;
		Upgrades upgrades = Mockito.mock(Upgrades.class);
		Mockito.when(upgrades.getHeatSink()).thenReturn(UpgradeDB.DOUBLE_HEATSINKS);
		Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
		Mockito.when(configuredComponent.getEngineHeatsinks()).thenReturn(numEngineHs);
		Item engine = ItemDB.lookup("STD ENGINE 300");
		Mockito.when(configuredComponent.canRemoveItem(engine)).thenReturn(true);

		cut.removeItem(engine);
		cut.addItem(engine);

		Mockito.verify(configuredComponent, Mockito.times(numEngineHs)).addItem(ItemDB.DHS);
	}

	/**
	 * addItem() has a special case where it shall re-add engine heat sinks that were removed in a previous call to
	 * removeItem(). This is to facilitate undo behavior on engines to include heat sinks (DHS)
	 */
	@Test
	public final void testAddItem_addEngineAfterRemoveDHS() {
		final int numEngineHs = 2;
		Upgrades upgrades = Mockito.mock(Upgrades.class);
		Mockito.when(upgrades.getHeatSink()).thenReturn(UpgradeDB.DOUBLE_HEATSINKS);
		Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
		Mockito.when(configuredComponent.getEngineHeatsinks()).thenReturn(numEngineHs);
		Item engine = ItemDB.lookup("STD ENGINE 300");
		Mockito.when(configuredComponent.canRemoveItem(engine)).thenReturn(true);

		cut.removeItem(engine);
		cut.addItem(engine);

		Mockito.verify(configuredComponent, Mockito.times(numEngineHs)).addItem(ItemDB.DHS);
	}

}
