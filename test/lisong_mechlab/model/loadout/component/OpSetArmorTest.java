package lisong_mechlab.model.loadout.component;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ComponentBase;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.Message.Type;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class OpSetArmorTest {
	private static final int						TEST_MAX_ARMOR			= 40;
	private ArmorSide								armorSide				= ArmorSide.ONLY;
	@Mock
	private LoadoutBase<ConfiguredComponentBase>	loadout;
	@Mock
	private Upgrades								upgrades;
	@Mock
	private ConfiguredComponentBase					loadoutPart;
	@Mock
	private MessageXBar								xBar;
	@Mock
	private ComponentBase							internalPart;
	private double									armorPerTon				= 32;
	@Mock
	private ArmorUpgrade							armorUpgrade;
	@Mock
	private ChassisBase								chassis;

	private Integer									chassisMass				= 100;
	private double									itemMass				= 50;
	private int										priorArmor				= 300;
	private int										oldArmor				= 20;
	private List<ConfiguredComponentBase>			parts					= new ArrayList<>();
	private Boolean									oldManualArmor			= false;
	private int										componentMaxArmorLeft	= TEST_MAX_ARMOR;

	public OpSetArmor makeCUT(int armor, boolean isManual) {
		Mockito.when(chassis.getMassMax()).thenReturn(chassisMass);

		double armorMass = priorArmor / armorPerTon;
		double freeMass = chassisMass - (itemMass + armorMass);
		Mockito.when(loadout.getFreeMass()).thenReturn(freeMass);
		Mockito.when(loadout.getMassStructItems()).thenReturn(itemMass);
		Mockito.when(loadout.getArmor()).thenReturn(priorArmor);
		Mockito.when(loadout.getChassis()).thenReturn(chassis);
		Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
		Mockito.when(loadout.getComponents()).thenReturn(parts);

		Mockito.when(loadoutPart.getInternalComponent()).thenReturn(internalPart);
		Mockito.when(loadoutPart.getArmor(armorSide)).thenReturn(oldArmor);
		Mockito.when(loadoutPart.getArmorMax(armorSide)).thenReturn(componentMaxArmorLeft);
		Mockito.when(loadoutPart.allowAutomaticArmor()).thenReturn(!oldManualArmor);

		Mockito.when(internalPart.getLocation()).thenReturn(Location.CenterTorso);
		Mockito.when(internalPart.getArmorMax()).thenReturn(TEST_MAX_ARMOR);

		Mockito.when(upgrades.getArmor()).thenReturn(armorUpgrade);

		Mockito.when(armorUpgrade.getArmorPerTon()).thenReturn(armorPerTon);
		Mockito.when(armorUpgrade.getArmorMass(Matchers.anyInt())).thenAnswer(new Answer<Double>() {
			@Override
			public Double answer(InvocationOnMock aInvocation) throws Throwable {
				int arg0 = ((Integer) aInvocation.getArguments()[0]);
				return arg0 / armorPerTon;
			}
		});

		return new OpSetArmor(xBar, loadout, loadoutPart, armorSide, armor, isManual);
	}

	/**
	 * The description shall contain the words "armor" and "change".
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testDescribe() throws Exception {
		int armor = 13;
		OpSetArmor cut = makeCUT(armor, true);

		assertTrue(cut.describe().contains("armor"));
		assertTrue(cut.describe().contains("change"));
	}

	/**
	 * Any attempt to create an {@link OpSetArmor} with negative armor shall throw an {@link IllegalArgumentException}
	 * on creation.
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testCtorNegativeArmor() throws Exception {
		makeCUT(-1, true);
	}

	/**
	 * Any attempt to create a {@link OpSetArmor} with more armor than the internal component can hold (100% failure
	 * regardless of current armor value) shall throw an {@link IllegalArgumentException} on construction.
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testCtorTooMuchArmor() throws Exception {
		makeCUT(TEST_MAX_ARMOR + 1, true);
	}

	/**
	 * Two set armor operations can coalescele if they refer to the same (equality is not enough) component, same side
	 * and have the same manual status.
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCanCoalescele() throws Exception {
		int armor = 20;
		ConfiguredComponentBase part1 = Mockito.mock(ConfiguredComponentBase.class);
		ConfiguredComponentBase part2 = Mockito.mock(ConfiguredComponentBase.class);

		// Part 1 & 2 are identical but not the same.
		Mockito.when(part1.getInternalComponent()).thenReturn(internalPart);
		Mockito.when(part1.getArmor(ArmorSide.BACK)).thenReturn(armor);
		Mockito.when(part1.getArmor(ArmorSide.FRONT)).thenReturn(armor);
		Mockito.when(part2.getInternalComponent()).thenReturn(internalPart);
		Mockito.when(part2.getArmor(ArmorSide.BACK)).thenReturn(armor);
		Mockito.when(part2.getArmor(ArmorSide.FRONT)).thenReturn(armor);
		Mockito.when(internalPart.getLocation()).thenReturn(Location.CenterTorso);
		Mockito.when(internalPart.getArmorMax()).thenReturn(TEST_MAX_ARMOR);

		OpSetArmor cut1 = new OpSetArmor(xBar, loadout, part1, ArmorSide.FRONT, armor, true);
		OpSetArmor cut2 = new OpSetArmor(xBar, loadout, part1, ArmorSide.FRONT, armor, false);
		OpSetArmor cut3 = new OpSetArmor(xBar, loadout, part1, ArmorSide.BACK, armor, true);
		OpSetArmor cut4 = new OpSetArmor(xBar, loadout, part2, ArmorSide.FRONT, armor, true);
		OpSetArmor cut5 = new OpSetArmor(xBar, loadout, part1, ArmorSide.FRONT, armor - 1, true);
		Operation operation = Mockito.mock(Operation.class);

		assertFalse(cut1.canCoalescele(operation));
		assertFalse(cut1.canCoalescele(null));
		assertFalse(cut1.canCoalescele(cut1)); // Can't coalescele with self.
		assertFalse(cut1.canCoalescele(cut2));
		assertFalse(cut1.canCoalescele(cut3));
		assertFalse(cut1.canCoalescele(cut4));
		assertTrue(cut1.canCoalescele(cut5));
	}

	/**
	 * Attempting to add armor that would cause the mass limit on the loadout to be exceeded shall result in an
	 * {@link IllegalArgumentException} when the operation is applied.
	 * <p>
	 * It must not be thrown on creation as there may be a composite operation that will be executed before this one
	 * that reduces the armor so that this operation will succeed.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testApply_TooHeavy() {
		// Setup
		armorPerTon = 32 * 1.12;
		chassisMass = 10;
		itemMass = 9;
		oldArmor = 20;
		priorArmor = (int) (armorPerTon - 10);
		final int newArmor = oldArmor + 11;

		OpSetArmor cut = null;
		try {
			cut = makeCUT(newArmor, true);
		} catch (Throwable t) {
			fail("Setup threw!");
			return;
		}

		// Execute
		cut.apply();

		// Verify (automatic)
	}

	/**
	 * An armor operation that would result in no change shall not execute.
	 */
	@Test
	public final void testApply_NoChange() {
		// Setup
		makeCUT(oldArmor, oldManualArmor).apply();

		Mockito.verifyZeroInteractions(xBar);
		Mockito.verify(loadoutPart, Mockito.never()).setArmor(Matchers.any(ArmorSide.class), Matchers.anyInt(),
				Matchers.anyBoolean());
	}

	/**
	 * Apply should correctly handle numerical precision problems that arise from armor amounts that result in
	 * irrational tonnages.
	 */
	@Test
	public final void testApply_FloatingPointPrecision() {
		// Setup
		armorPerTon = 38.4;
		chassisMass = 30;
		priorArmor = 192 - 20;

		// DO NOT SIMPLIFY!
		// These repeated additions are required to simulate compounding rounding errors of floating point numbers that
		// occur
		// in real usage cases!
		itemMass = 1.5 + 5.5 + 1.0 + 1.0 + 1.0; // Structure, engine, fixed HS
		itemMass += 0.5 + 0.5 + 0.5 + 6.0 + 1.0 + 0.5; // RA
		itemMass += 0.5 + 0.5 + 1.0 + 1.0; // LA
		itemMass += 1.0 + 1.0 + 1.0; // RT, LT RL

		oldArmor = 0;
		final int newArmor = 20;

		OpSetArmor cut = null;
		try {
			cut = makeCUT(newArmor, true);
		} catch (Throwable t) {
			fail("Setup threw!");
			return;
		}

		// Execute
		cut.apply();

		// Verify
		Mockito.verify(loadoutPart).setArmor(armorSide, newArmor, false);
		Mockito.verify(xBar).post(new ConfiguredComponentBase.Message(loadoutPart, Type.ArmorChanged));
	}

	/**
	 * Attempting to set armor that is more than the side can support (but less than free tonnage) shall fail with an
	 * {@link IllegalArgumentException}.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testApply_TooMuchArmorForSide() {
		// Setup
		componentMaxArmorLeft = TEST_MAX_ARMOR / 2;
		itemMass = 0;
		chassisMass = 100;

		final int newArmor = componentMaxArmorLeft + 1;

		OpSetArmor cut = null;
		try {
			cut = makeCUT(newArmor, oldManualArmor);
		} catch (Throwable t) {
			fail("Setup threw!");
			return;
		}

		// Execute
		cut.apply();
	}

	/**
	 * Apply shall successfully change the armor value if called with an armor amount less than the current amount and
	 * the 'mech is over-tonnage.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testApply_ReduceWhenOverTonnage() throws Exception {

		// Setup
		chassisMass = 100;
		itemMass = 90;
		oldArmor = (int) (1.5 * (chassisMass - itemMass) * armorPerTon); // Over-tonnage

		OpSetArmor cut = null;
		try {
			cut = makeCUT(1, true);
		} catch (Throwable t) {
			fail("Setup threw!");
			return;
		}

		// Execute
		cut.apply();

		// Verify
		Mockito.verify(loadoutPart).setArmor(armorSide, 1, false);
		Mockito.verify(xBar).post(new ConfiguredComponentBase.Message(loadoutPart, Type.ArmorChanged));
	}

	/**
	 * Undoing an operation where the new value is the old value shall not do anything.
	 */
	@Test
	public final void testUndo_NoChange() {
		// Setup

		OpSetArmor cut = makeCUT(oldArmor, oldManualArmor);

		// Execute
		cut.apply();
		cut.undo();

		Mockito.verifyZeroInteractions(xBar);
		Mockito.verify(loadoutPart, Mockito.never()).setArmor(Matchers.any(ArmorSide.class), Matchers.anyInt(),
				Matchers.anyBoolean());

	}

	/**
	 * Undoing when apply has not been called shall throw an instance of {@link RuntimeException}.
	 */
	@Test(expected = RuntimeException.class)
	public final void testUndo_WithoutApply() {
		// Setup
		OpSetArmor cut = null;
		try {
			int newArmor = 20;
			oldArmor = 21;
			cut = makeCUT(newArmor, oldManualArmor);
		} catch (Throwable t) {
			fail("Setup threw!");
			return;
		}
		// Execute
		cut.undo();
	}

	/**
	 * Undoing twice to only one apply shall throw an instance of {@link RuntimeException}.
	 */
	@Test(expected = RuntimeException.class)
	public final void testUndo_DoubleUndoAfterApply() {
		// Setup
		OpSetArmor cut = null;
		try {
			int newArmor = 20;
			oldArmor = 21;
			cut = makeCUT(newArmor, oldManualArmor);
		} catch (Throwable t) {
			fail("Setup threw!");
			return;
		}

		// Execute
		cut.apply();
		cut.undo();
		cut.undo();
	}

	/**
	 * Undoing the operation shall set the armor that was at the time of the operation was applied.
	 */
	@Test
	public final void testUndo() {
		// Setup
		int newArmor = 20;
		oldArmor = 25;

		boolean oldAuto = !oldManualArmor;
		boolean newManual = !oldManualArmor;
		boolean newAuto = !newManual;

		OpSetArmor cut = makeCUT(newArmor, newManual);

		// Execute
		cut.apply();
		cut.undo();

		InOrder inOrder = Mockito.inOrder(xBar, loadoutPart);
		inOrder.verify(loadoutPart).setArmor(armorSide, newArmor, newAuto);
		inOrder.verify(xBar).post(new ConfiguredComponentBase.Message(loadoutPart, Type.ArmorChanged));
		inOrder.verify(loadoutPart).setArmor(armorSide, oldArmor, oldAuto);
		inOrder.verify(xBar).post(new ConfiguredComponentBase.Message(loadoutPart, Type.ArmorChanged));
	}
}
