/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.command;

import org.lisoft.lsml.messages.ArmourMessage;
import org.lisoft.lsml.messages.ArmourMessage.Type;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.mwo_data.mechs.ArmourSide;
import org.lisoft.mwo_data.mechs.Location;

/**
 * This {@link Command} will change the armour of a {@link ConfiguredComponent}.
 *
 * @author Li Song
 */
public class CmdSetArmour implements Command {
  private final int amount;
  private final ConfiguredComponent component;
  private final Loadout loadout;
  private final boolean manual;
  private final MessageDelivery messageDelivery;
  private final ArmourSide side;
  private int oldAmount = -1;
  private boolean oldManual;

  /**
   * Sets the armour for a given side of the component. Throws if the operation will fail.
   *
   * @param aMessageDelivery The {@link MessageXBar} to announce changes to.
   * @param aLoadout The {@link Loadout} to change.
   * @param aLocation The location to set the armour for.
   * @param aArmourSide The side to set the armour for.
   * @param aArmourAmount The amount to set the armour to.
   * @param aManualSet True if this set operation is done manually. Will disable automatic armour
   *     assignments.
   */
  public CmdSetArmour(
      MessageDelivery aMessageDelivery,
      Loadout aLoadout,
      Location aLocation,
      ArmourSide aArmourSide,
      int aArmourAmount,
      boolean aManualSet) {
    this(
        aMessageDelivery,
        aLoadout,
        aLoadout.getComponent(aLocation),
        aArmourSide,
        aArmourAmount,
        aManualSet);
  }

  /**
   * Sets the armour for a given side of the component. Throws if the operation will fail.
   *
   * @param aMessageDelivery The {@link MessageXBar} to announce changes to.
   * @param aLoadout The {@link Loadout} to change.
   * @param aComponent The {@link ConfiguredComponent} to change.
   * @param aArmourSide The side to set the armour for.
   * @param aArmourAmount The amount to set the armour to.
   * @param aManualSet True if this set operation is done manually. Will disable automatic armour
   *     assignments.
   */
  public CmdSetArmour(
      MessageDelivery aMessageDelivery,
      Loadout aLoadout,
      ConfiguredComponent aComponent,
      ArmourSide aArmourSide,
      int aArmourAmount,
      boolean aManualSet) {
    messageDelivery = aMessageDelivery;
    loadout = aLoadout;
    component = aComponent;
    side = aArmourSide;
    amount = aArmourAmount;
    manual = aManualSet;

    if (amount < 0) {
      throw new IllegalArgumentException("Armour must be positive!");
    }
  }

  @Override
  public void apply() throws EquipException {
    if (amount > component.getInternalComponent().getArmourMax()) {
      throw new IllegalArgumentException("Armour must be less than components max armour!");
    }
    storePreviousState();
    if (operationHasEffect()) {
      operationTryToLegalize();
      setValue(amount, manual);
    }
  }

  /**
   * @see
   *     org.lisoft.lsml.util.CommandStack.Command#canCoalesce(org.lisoft.lsml.util.CommandStack.Command)
   */
  @Override
  public boolean canCoalesce(Command aOperation) {
    if (this == aOperation) {
      return false;
    }
    if (aOperation == null) {
      return false;
    }
    if (!(aOperation instanceof final CmdSetArmour that)) {
      return false;
    }
    if (that.manual != manual) {
      return false;
    }
    if (that.component != component) {
      return false;
    }
    return that.side == side;
  }

  @Override
  public String describe() {
    return "change armour";
  }

  @Override
  public void undo() {
    if (oldAmount < 0) {
      throw new RuntimeException("Apply was not called before undo!");
    }

    if (operationHasEffect()) {
      setValue(oldAmount, oldManual);
    }
    oldAmount = -1;
  }

  private boolean operationHasEffect() {
    return amount != oldAmount || oldManual != manual;
  }

  private void operationTryToLegalize() throws EquipException {
    if (amount > component.getArmourMax(side)) {
      EquipException.checkAndThrow(EquipResult.make(EquipResultType.ExceededMaxArmour));
    }

    final int armourDiff = amount - oldAmount;
    final int totalArmour =
        armourDiff + loadout.getArmour(); // This is important to prevent numerical stability
    // issues.
    // Calculate whole armour in integer precision.
    final double armourTons = loadout.getUpgrades().getArmour().getArmourMass(totalArmour);
    final double freeTonnage =
        loadout.getChassis().getMassMax() - (loadout.getMassStructItems() + armourTons);

    if (freeTonnage < 0) {
      // See if the armour can be freed from a combination of automatic components. They will be
      // redistributed
      // afterwards. FIXME: Devise a proper solution, this is ugly.
      int freed = 0;
      if (manual && freed < armourDiff) {
        for (final ConfiguredComponent otherComponent : loadout.getComponents()) {
          if (component != otherComponent && !otherComponent.hasManualArmour()) {
            freed += otherComponent.getArmourTotal();
            for (final ArmourSide armourSide :
                ArmourSide.allSides(otherComponent.getInternalComponent())) {
              otherComponent.setArmour(armourSide, 0, false);
            }
          }
        }
      }
      if (freed < armourDiff) {
        EquipException.checkAndThrow(EquipResult.make(EquipResultType.TooHeavy));
      }
    }
  }

  private void setValue(int aValue, boolean aManual) {
    component.setArmour(side, aValue, aManual);
    if (messageDelivery != null) {
      messageDelivery.post(new ArmourMessage(component, Type.ARMOUR_CHANGED, aManual));
    }
  }

  private void storePreviousState() {
    oldAmount = component.getArmour(side);
    oldManual = component.hasManualArmour();
  }
}
