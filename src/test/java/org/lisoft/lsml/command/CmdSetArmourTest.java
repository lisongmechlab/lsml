package org.lisoft.lsml.command;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.messages.ArmourMessage;
import org.lisoft.lsml.messages.ArmourMessage.Type;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.util.CommandStack.Command;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;

@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.Silent.class)
public class CmdSetArmourTest {
    private static final int TEST_MAX_ARMOUR = 40;
    private final ArmourSide armourSide = ArmourSide.ONLY;
    private final MockLoadoutContainer mlc = new MockLoadoutContainer();
    private final List<ConfiguredComponent> parts = new ArrayList<>();
    private int armour = 20;
    private double armourPerTon = 32;
    private Integer chassisMass = 100;
    private int componentMaxArmourLeft = TEST_MAX_ARMOUR;
    private double itemMass = 50;
    private Boolean manual = false;
    @Mock
    private MessageXBar messageRecipint;
    private int priorArmour = 300;

    public CmdSetArmour makeCUT(int aSetArmour, boolean aSetIsManual) {
        final double armourMass = priorArmour / armourPerTon;
        final double freeMass = chassisMass - (itemMass + armourMass);

        Mockito.when(mlc.chassis.getMassMax()).thenReturn(chassisMass);
        Mockito.when(mlc.loadout.getFreeMass()).thenReturn(freeMass);
        Mockito.when(mlc.loadout.getMassStructItems()).thenReturn(itemMass);
        Mockito.when(mlc.loadout.getArmour()).thenReturn(priorArmour);
        Mockito.when(mlc.loadout.getComponents()).thenReturn(parts);

        Mockito.when(mlc.ct.getArmour(armourSide)).thenReturn(armour);
        Mockito.when(mlc.ct.getArmourMax(armourSide)).thenReturn(componentMaxArmourLeft);
        Mockito.when(mlc.ct.hasManualArmour()).thenReturn(manual);

        Mockito.when(mlc.ict.getArmourMax()).thenReturn(TEST_MAX_ARMOUR);

        Mockito.when(mlc.armourUpgrade.getArmourPerTon()).thenReturn(armourPerTon);
        Mockito.when(mlc.armourUpgrade.getArmourMass(anyInt())).thenAnswer(aInvocation -> {
            final int arg0 = (Integer) aInvocation.getArguments()[0];
            return arg0 / armourPerTon;
        });

        return new CmdSetArmour(messageRecipint, mlc.loadout, mlc.ct, armourSide, aSetArmour, aSetIsManual);
    }

    /**
     * Setting the armour automatically after an automatic set shall produce correct results.
     */
    @Test
    public final void testApplyUndo_Auto2Auto() throws Exception {
        applyUndoTestTemplate(false, false, 25, 20);
    }

    /**
     * Setting the armour manually after an automatic set shall produce correct results.
     */
    @Test
    public final void testApplyUndo_Auto2Manual() throws Exception {
        applyUndoTestTemplate(false, true, 25, 20);
    }

    /**
     * Setting the armour automatically after a manual set produce correct result and override manual set flag.
     */
    @Test
    public final void testApplyUndo_Manual2Auto() throws Exception {
        applyUndoTestTemplate(true, false, 25, 20);
    }

    /**
     * Setting the armour manually after a manual set shall produce correct results.
     */
    @Test
    public final void testApplyUndo_Manual2Manual() throws Exception {
        applyUndoTestTemplate(true, true, 25, 20);
    }

    /**
     * An armour operation that would result in no change shall not execute.
     */
    @Test
    public final void testApplyUndo_NoChange() throws Exception {
        // Setup
        final CmdSetArmour cut = makeCUT(armour, manual);

        // Execute
        cut.apply();
        cut.undo();

        Mockito.verifyZeroInteractions(messageRecipint);
        Mockito.verify(mlc.ct, Mockito.never()).setArmour(any(ArmourSide.class), anyInt(), anyBoolean());
    }

    /**
     * An armour operation shall complete even if the message crossbar is null.
     */
    @Test
    public final void testApplyUndo_NullXBarOK() throws Exception {
        messageRecipint = null;
        applyUndoTestTemplate(true, true, 25, 20);
    }

    /**
     * Changing just the manual flag shall produce the expected result.
     */
    @Test
    public final void testApplyUndo_OnlyManualStatus() throws Exception {
        applyUndoTestTemplate(false, true, 20, 20);

    }

    /**
     * Apply should correctly handle numerical precision problems that arise from armour amounts that result in
     * irrational tonnages.
     */
    @Test
    public final void testApply_FloatingPointPrecision() throws Exception {
        // Setup
        armourPerTon = 38.4;
        chassisMass = 30;
        priorArmour = 192 - 20;

        // DO NOT SIMPLIFY!
        // These repeated additions are required to simulate compounding rounding errors
        // of floating point numbers that
        // occur
        // in real usage cases!
        itemMass = 1.5 + 5.5 + 1.0 + 1.0 + 1.0; // Structure, engine, fixed HS
        itemMass += 0.5 + 0.5 + 0.5 + 6.0 + 1.0 + 0.5; // RA
        itemMass += 0.5 + 0.5 + 1.0 + 1.0; // LA
        itemMass += 1.0 + 1.0 + 1.0; // RT, LT RL

        armour = 0;
        final int newArmour = 20;

        CmdSetArmour cut = null;
        try {
            cut = makeCUT(newArmour, true);
        } catch (final Throwable t) {
            fail("Setup threw!");
            return;
        }

        // Execute
        cut.apply();

        // Verify
        Mockito.verify(mlc.ct).setArmour(armourSide, newArmour, true);
        Mockito.verify(messageRecipint).post(new ArmourMessage(mlc.ct, Type.ARMOUR_CHANGED, true));
    }

    /**
     * Apply shall successfully change the armour value if called with an armour amount less than the current amount and
     * the 'mech is over-tonnage.
     */
    @Test
    public void testApply_ReduceWhenOverTonnage() throws Exception {

        // Setup
        chassisMass = 100;
        itemMass = 90;
        armour = (int) (1.5 * (chassisMass - itemMass) * armourPerTon); // Over-tonnage

        CmdSetArmour cut = null;
        try {
            cut = makeCUT(1, true);
        } catch (final Throwable t) {
            fail("Setup threw!");
            return;
        }

        // Execute
        cut.apply();

        // Verify
        Mockito.verify(mlc.ct).setArmour(armourSide, 1, true);
        Mockito.verify(messageRecipint).post(new ArmourMessage(mlc.ct, Type.ARMOUR_CHANGED, true));
    }

    /**
     * Attempting to add armour that would cause the mass limit on the loadout to be exceeded shall result in an
     * {@link EquipResult} when the operation is applied.
     * <p>
     * It must not be thrown on creation as there may be a composite operation that will be executed before this one
     * that reduces the armour so that this operation will succeed.
     */
    @Test(expected = EquipException.class)
    public final void testApply_TooHeavy() throws Exception {
        // Setup
        armourPerTon = 32 * 1.12;
        chassisMass = 10;
        itemMass = 9;
        armour = 20;
        priorArmour = (int) (armourPerTon - 10);
        final int newArmour = armour + 11;

        CmdSetArmour cut = null;
        try {
            cut = makeCUT(newArmour, true);
        } catch (final Throwable t) {
            fail("Setup threw!");
            return;
        }

        // Execute
        cut.apply();

        // Verify (automatic)
    }

    /**
     * Attempting to set armour that is more than the side can support (but less than free tonnage) shall fail with an
     * {@link EquipResult}.
     */
    @Test(expected = EquipException.class)
    public final void testApply_TooMuchArmourForSide() throws Exception {
        // Setup
        componentMaxArmourLeft = TEST_MAX_ARMOUR / 2;
        itemMass = 0;
        chassisMass = 100;

        final int newArmour = componentMaxArmourLeft + 1;

        CmdSetArmour cut = null;
        try {
            cut = makeCUT(newArmour, !manual);
        } catch (final Throwable t) {
            fail("Setup threw!");
            return;
        }

        // Execute
        cut.apply();
    }

    /**
     * Two set armour operations can coalesce if they refer to the same (equality is not enough) component, same side
     * and have the same manual status.
     */
    @Test
    public final void testCanCoalescele() throws Exception {
        final int newArmour = 20;
        final ConfiguredComponent part1 = Mockito.mock(ConfiguredComponent.class);
        final ConfiguredComponent part2 = Mockito.mock(ConfiguredComponent.class);

        // Part 1 & 2 are identical but not the same.
        Mockito.when(part1.getInternalComponent()).thenReturn(mlc.ict);
        Mockito.when(part1.getArmour(ArmourSide.BACK)).thenReturn(newArmour);
        Mockito.when(part1.getArmour(ArmourSide.FRONT)).thenReturn(newArmour);
        Mockito.when(part2.getInternalComponent()).thenReturn(mlc.ict);
        Mockito.when(part2.getArmour(ArmourSide.BACK)).thenReturn(newArmour);
        Mockito.when(part2.getArmour(ArmourSide.FRONT)).thenReturn(newArmour);
        Mockito.when(mlc.ict.getLocation()).thenReturn(Location.CenterTorso);
        Mockito.when(mlc.ict.getArmourMax()).thenReturn(TEST_MAX_ARMOUR);

        final CmdSetArmour cut1 = new CmdSetArmour(messageRecipint, mlc.loadout, part1, ArmourSide.FRONT, newArmour,
                                                   true);
        final CmdSetArmour cut2 = new CmdSetArmour(messageRecipint, mlc.loadout, part1, ArmourSide.FRONT, newArmour,
                                                   false);
        final CmdSetArmour cut3 = new CmdSetArmour(messageRecipint, mlc.loadout, part1, ArmourSide.BACK, newArmour,
                                                   true);
        final CmdSetArmour cut4 = new CmdSetArmour(messageRecipint, mlc.loadout, part2, ArmourSide.FRONT, newArmour,
                                                   true);
        final CmdSetArmour cut5 = new CmdSetArmour(messageRecipint, mlc.loadout, part1, ArmourSide.FRONT, newArmour - 1,
                                                   true);
        final Command operation = Mockito.mock(Command.class);

        assertFalse(cut1.canCoalesce(operation));
        assertFalse(cut1.canCoalesce(null));
        assertFalse(cut1.canCoalesce(cut1)); // Can't coalescele with self.
        assertFalse(cut1.canCoalesce(cut2));
        assertFalse(cut1.canCoalesce(cut3));
        assertFalse(cut1.canCoalesce(cut4));
        assertTrue(cut1.canCoalesce(cut5));
    }

    /**
     * Any attempt to create an {@link CmdSetArmour} with negative armour shall throw an
     * {@link IllegalArgumentException} on creation.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testCtorNegativeArmour() throws Exception {
        makeCUT(-1, true);
    }

    /**
     * Any attempt to create a {@link CmdSetArmour} with more armour than the internal component can hold (100% failure
     * regardless of current armour value) shall throw an {@link IllegalArgumentException} on construction.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testCtorTooMuchArmour() throws Exception {
        makeCUT(TEST_MAX_ARMOUR + 1, true).apply();
    }

    /**
     * The description shall contain the words "armour" and "change".
     */
    @Test
    public final void testDescribe() throws Exception {
        final int newArmour = 13;
        final CmdSetArmour cut = makeCUT(newArmour, true);

        assertTrue(cut.describe().contains("armour"));
        assertTrue(cut.describe().contains("change"));
    }

    /**
     * Undoing twice to only one apply shall throw an instance of {@link RuntimeException}.
     */
    @Test(expected = RuntimeException.class)
    public final void testUndo_DoubleUndoAfterApply() throws Exception {
        // Setup
        CmdSetArmour cut = null;
        try {
            final int newArmour = 20;
            armour = 21;
            cut = makeCUT(newArmour, !manual);
        } catch (final Throwable t) {
            fail("Setup threw!");
            return;
        }

        // Execute
        cut.apply();
        cut.undo();
        cut.undo();
    }

    /**
     * Undoing when apply has not been called shall throw an instance of {@link RuntimeException}.
     */
    @Test(expected = RuntimeException.class)
    public final void testUndo_WithoutApply() {
        // Setup
        CmdSetArmour cut = null;
        try {
            final int newArmour = 20;
            armour = 21;
            cut = makeCUT(newArmour, !manual);
        } catch (final Throwable t) {
            fail("Setup threw!");
            return;
        }
        // Execute
        cut.undo();
    }

    private final void applyUndoTestTemplate(boolean aWasManual, boolean aManualSet, int aOldArmour, int aNewArmour)
            throws Exception {
        // Setup
        armour = aOldArmour;
        manual = aWasManual;
        final CmdSetArmour cut = makeCUT(aNewArmour, aManualSet);

        // Execute
        cut.apply();
        cut.undo();

        // Verify
        final InOrder inOrder;
        if (messageRecipint != null) {
            inOrder = Mockito.inOrder(messageRecipint, mlc.ct);
        } else {
            inOrder = Mockito.inOrder(mlc.ct);
        }
        inOrder.verify(mlc.ct).setArmour(armourSide, aNewArmour, aManualSet);
        if (messageRecipint != null) {
            inOrder.verify(messageRecipint).post(new ArmourMessage(mlc.ct, Type.ARMOUR_CHANGED, aManualSet));
        }
        inOrder.verify(mlc.ct).setArmour(armourSide, armour, aWasManual);
        if (messageRecipint != null) {
            inOrder.verify(messageRecipint).post(new ArmourMessage(mlc.ct, Type.ARMOUR_CHANGED, aWasManual));
        }
    }
}
