package org.lisoft.lsml.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.ArmorMessage;
import org.lisoft.lsml.messages.ArmorMessage.Type;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.util.CommandStack.Command;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class CmdSetArmorTest {
    private static final int          TEST_MAX_ARMOR        = 40;
    private ArmorSide                 armorSide             = ArmorSide.ONLY;
    @Mock
    private MessageXBar               messageRecipint;
    private double                    armorPerTon           = 32;
    private MockLoadoutContainer      mlc                   = new MockLoadoutContainer();

    private Integer                   chassisMass           = 100;
    private double                    itemMass              = 50;
    private int                       priorArmor            = 300;
    private int                       armor                 = 20;
    private List<ConfiguredComponent> parts                 = new ArrayList<>();
    private Boolean                   manual                = false;
    private int                       componentMaxArmorLeft = TEST_MAX_ARMOR;

    public CmdSetArmor makeCUT(int aSetArmor, boolean aSetIsManual) {
        final double armorMass = priorArmor / armorPerTon;
        final double freeMass = chassisMass - (itemMass + armorMass);

        Mockito.when(mlc.chassis.getMassMax()).thenReturn(chassisMass);
        Mockito.when(mlc.loadout.getFreeMass()).thenReturn(freeMass);
        Mockito.when(mlc.loadout.getMassStructItems()).thenReturn(itemMass);
        Mockito.when(mlc.loadout.getArmor()).thenReturn(priorArmor);
        Mockito.when(mlc.loadout.getComponents()).thenReturn(parts);

        Mockito.when(mlc.ct.getArmor(armorSide)).thenReturn(armor);
        Mockito.when(mlc.ct.getArmorMax(armorSide)).thenReturn(componentMaxArmorLeft);
        Mockito.when(mlc.ct.hasManualArmor()).thenReturn(manual);

        Mockito.when(mlc.ict.getArmorMax()).thenReturn(TEST_MAX_ARMOR);

        Mockito.when(mlc.armorUpgrade.getArmorPerTon()).thenReturn(armorPerTon);
        Mockito.when(mlc.armorUpgrade.getArmorMass(Matchers.anyInt())).thenAnswer(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock aInvocation) throws Throwable {
                int arg0 = ((Integer) aInvocation.getArguments()[0]);
                return arg0 / armorPerTon;
            }
        });

        return new CmdSetArmor(messageRecipint, mlc.loadout, mlc.ct, armorSide, aSetArmor, aSetIsManual);
    }

    /**
     * The description shall contain the words "armor" and "change".
     * 
     * @throws Exception
     */
    @Test
    public final void testDescribe() throws Exception {
        int newArmor = 13;
        CmdSetArmor cut = makeCUT(newArmor, true);

        assertTrue(cut.describe().contains("armor"));
        assertTrue(cut.describe().contains("change"));
    }

    /**
     * Any attempt to create an {@link CmdSetArmor} with negative armor shall throw an {@link IllegalArgumentException}
     * on creation.
     * 
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testCtorNegativeArmor() throws Exception {
        makeCUT(-1, true);
    }

    /**
     * Any attempt to create a {@link CmdSetArmor} with more armor than the internal component can hold (100% failure
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
        int newArmor = 20;
        ConfiguredComponent part1 = Mockito.mock(ConfiguredComponent.class);
        ConfiguredComponent part2 = Mockito.mock(ConfiguredComponent.class);

        // Part 1 & 2 are identical but not the same.
        Mockito.when(part1.getInternalComponent()).thenReturn(mlc.ict);
        Mockito.when(part1.getArmor(ArmorSide.BACK)).thenReturn(newArmor);
        Mockito.when(part1.getArmor(ArmorSide.FRONT)).thenReturn(newArmor);
        Mockito.when(part2.getInternalComponent()).thenReturn(mlc.ict);
        Mockito.when(part2.getArmor(ArmorSide.BACK)).thenReturn(newArmor);
        Mockito.when(part2.getArmor(ArmorSide.FRONT)).thenReturn(newArmor);
        Mockito.when(mlc.ict.getLocation()).thenReturn(Location.CenterTorso);
        Mockito.when(mlc.ict.getArmorMax()).thenReturn(TEST_MAX_ARMOR);

        CmdSetArmor cut1 = new CmdSetArmor(messageRecipint, mlc.loadout, part1, ArmorSide.FRONT, newArmor, true);
        CmdSetArmor cut2 = new CmdSetArmor(messageRecipint, mlc.loadout, part1, ArmorSide.FRONT, newArmor, false);
        CmdSetArmor cut3 = new CmdSetArmor(messageRecipint, mlc.loadout, part1, ArmorSide.BACK, newArmor, true);
        CmdSetArmor cut4 = new CmdSetArmor(messageRecipint, mlc.loadout, part2, ArmorSide.FRONT, newArmor, true);
        CmdSetArmor cut5 = new CmdSetArmor(messageRecipint, mlc.loadout, part1, ArmorSide.FRONT, newArmor - 1, true);
        Command operation = Mockito.mock(Command.class);

        assertFalse(cut1.canCoalescele(operation));
        assertFalse(cut1.canCoalescele(null));
        assertFalse(cut1.canCoalescele(cut1)); // Can't coalescele with self.
        assertFalse(cut1.canCoalescele(cut2));
        assertFalse(cut1.canCoalescele(cut3));
        assertFalse(cut1.canCoalescele(cut4));
        assertTrue(cut1.canCoalescele(cut5));
    }

    /**
     * Setting the armor manually after an automatic set shall produce correct results.
     * 
     * @throws Exception
     */
    @Test
    public final void testApplyUndo_Auto2Manual() throws Exception {
        applyUndoTestTemplate(false, true, 25, 20);
    }

    /**
     * Setting the armor automatically after an automatic set shall produce correct results.
     * 
     * @throws Exception
     */
    @Test
    public final void testApplyUndo_Auto2Auto() throws Exception {
        applyUndoTestTemplate(false, false, 25, 20);
    }

    /**
     * Setting the armor manually after a manual set shall produce correct results.
     * 
     * @throws Exception
     */
    @Test
    public final void testApplyUndo_Manual2Manual() throws Exception {
        applyUndoTestTemplate(true, true, 25, 20);
    }

    /**
     * Setting the armor automatically after a manual set produce correct result and override manual set flag.
     * 
     * @throws Exception
     */
    @Test
    public final void testApplyUndo_Manual2Auto() throws Exception {
        applyUndoTestTemplate(true, false, 25, 20);
    }

    /**
     * Changing just the manual flag shall produce the expected result.
     * 
     * @throws Exception
     */
    @Test
    public final void testApplyUndo_OnlyManualStatus() throws Exception {
        applyUndoTestTemplate(false, true, 20, 20);

    }

    /**
     * An armor operation shall complete even if the message crossbar is null.
     * 
     * @throws Exception
     */
    @Test
    public final void testApplyUndo_NullXBarOK() throws Exception {
        messageRecipint = null;
        applyUndoTestTemplate(true, true, 25, 20);
    }

    /**
     * An armor operation that would result in no change shall not execute.
     * 
     * @throws Exception
     */
    @Test
    public final void testApplyUndo_NoChange() throws Exception {
        // Setup
        CmdSetArmor cut = makeCUT(armor, manual);

        // Execute
        cut.apply();
        cut.undo();

        Mockito.verifyZeroInteractions(messageRecipint);
        Mockito.verify(mlc.ct, Mockito.never()).setArmor(Matchers.any(ArmorSide.class), Matchers.anyInt(),
                Matchers.anyBoolean());
    }

    /**
     * Attempting to add armor that would cause the mass limit on the loadout to be exceeded shall result in an
     * {@link EquipResult} when the operation is applied.
     * <p>
     * It must not be thrown on creation as there may be a composite operation that will be executed before this one
     * that reduces the armor so that this operation will succeed.
     * 
     * @throws Exception
     */
    @Test(expected = EquipException.class)
    public final void testApply_TooHeavy() throws Exception {
        // Setup
        armorPerTon = 32 * 1.12;
        chassisMass = 10;
        itemMass = 9;
        armor = 20;
        priorArmor = (int) (armorPerTon - 10);
        final int newArmor = armor + 11;

        CmdSetArmor cut = null;
        try {
            cut = makeCUT(newArmor, true);
        }
        catch (Throwable t) {
            fail("Setup threw!");
            return;
        }

        // Execute
        cut.apply();

        // Verify (automatic)
    }

    /**
     * Apply should correctly handle numerical precision problems that arise from armor amounts that result in
     * irrational tonnages.
     * 
     * @throws Exception
     */
    @Test
    public final void testApply_FloatingPointPrecision() throws Exception {
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

        armor = 0;
        final int newArmor = 20;

        CmdSetArmor cut = null;
        try {
            cut = makeCUT(newArmor, true);
        }
        catch (Throwable t) {
            fail("Setup threw!");
            return;
        }

        // Execute
        cut.apply();

        // Verify
        Mockito.verify(mlc.ct).setArmor(armorSide, newArmor, true);
        Mockito.verify(messageRecipint).post(new ArmorMessage(mlc.ct, Type.ARMOR_CHANGED, true));
    }

    /**
     * Attempting to set armor that is more than the side can support (but less than free tonnage) shall fail with an
     * {@link EquipResult}.
     * 
     * @throws Exception
     */
    @Test(expected = EquipException.class)
    public final void testApply_TooMuchArmorForSide() throws Exception {
        // Setup
        componentMaxArmorLeft = TEST_MAX_ARMOR / 2;
        itemMass = 0;
        chassisMass = 100;

        final int newArmor = componentMaxArmorLeft + 1;

        CmdSetArmor cut = null;
        try {
            cut = makeCUT(newArmor, !manual);
        }
        catch (Throwable t) {
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
        armor = (int) (1.5 * (chassisMass - itemMass) * armorPerTon); // Over-tonnage

        CmdSetArmor cut = null;
        try {
            cut = makeCUT(1, true);
        }
        catch (Throwable t) {
            fail("Setup threw!");
            return;
        }

        // Execute
        cut.apply();

        // Verify
        Mockito.verify(mlc.ct).setArmor(armorSide, 1, true);
        Mockito.verify(messageRecipint).post(new ArmorMessage(mlc.ct, Type.ARMOR_CHANGED, true));
    }

    /**
     * Undoing when apply has not been called shall throw an instance of {@link RuntimeException}.
     */
    @Test(expected = RuntimeException.class)
    public final void testUndo_WithoutApply() {
        // Setup
        CmdSetArmor cut = null;
        try {
            int newArmor = 20;
            armor = 21;
            cut = makeCUT(newArmor, !manual);
        }
        catch (Throwable t) {
            fail("Setup threw!");
            return;
        }
        // Execute
        cut.undo();
    }

    /**
     * Undoing twice to only one apply shall throw an instance of {@link RuntimeException}.
     * 
     * @throws Exception
     */
    @Test(expected = RuntimeException.class)
    public final void testUndo_DoubleUndoAfterApply() throws Exception {
        // Setup
        CmdSetArmor cut = null;
        try {
            int newArmor = 20;
            armor = 21;
            cut = makeCUT(newArmor, !manual);
        }
        catch (Throwable t) {
            fail("Setup threw!");
            return;
        }

        // Execute
        cut.apply();
        cut.undo();
        cut.undo();
    }

    private final void applyUndoTestTemplate(boolean aWasManual, boolean aManualSet, int aOldArmor, int aNewArmor)
            throws Exception {
        // Setup
        armor = aOldArmor;
        manual = aWasManual;
        CmdSetArmor cut = makeCUT(aNewArmor, aManualSet);

        // Execute
        cut.apply();
        cut.undo();

        // Verify
        final InOrder inOrder;
        if (messageRecipint != null)
            inOrder = Mockito.inOrder(messageRecipint, mlc.ct);
        else
            inOrder = Mockito.inOrder(mlc.ct);
        inOrder.verify(mlc.ct).setArmor(armorSide, aNewArmor, aManualSet);
        if (messageRecipint != null)
            inOrder.verify(messageRecipint).post(new ArmorMessage(mlc.ct, Type.ARMOR_CHANGED, aManualSet));
        inOrder.verify(mlc.ct).setArmor(armorSide, armor, aWasManual);
        if (messageRecipint != null)
            inOrder.verify(messageRecipint).post(new ArmorMessage(mlc.ct, Type.ARMOR_CHANGED, aWasManual));
    }
}
