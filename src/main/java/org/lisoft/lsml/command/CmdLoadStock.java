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

import java.util.Optional;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.lsml.model.StockLoadoutDB;
import org.lisoft.lsml.model.loadout.*;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.NoSuchItemException;
import org.lisoft.mwo_data.mechs.*;
import org.lisoft.mwo_data.mechs.StockLoadout.StockComponent.ActuatorState;

/**
 * This operation loads a 'mechs stock {@link LoadoutStandard}.
 *
 * <p>TODO: Devise a method for composite commands to wrap the exceptions from their sub commands
 * for useful error messages.
 *
 * @author Li Song
 */
public class CmdLoadStock extends CmdLoadoutBase {
  private final LoadoutBuilder builder = new LoadoutBuilder();
  private final StockLoadout stockLoadout;

  public CmdLoadStock(Chassis aChassisVariation, Loadout aLoadout, MessageDelivery aMessageDelivery)
      throws NoSuchItemException {
    super(aLoadout, aMessageDelivery, "load stock");
    stockLoadout = StockLoadoutDB.lookup(aChassisVariation);
  }

  @Override
  public void buildCommand() throws NoSuchItemException {
    addOp(new CmdStripLoadout(messageBuffer, loadout));

    if (loadout instanceof final LoadoutStandard loadoutStandard) {
      builder.push(
          new CmdSetStructureType(messageBuffer, loadoutStandard, stockLoadout.getStructureType()));
      builder.push(
          new CmdSetArmourType(messageBuffer, loadoutStandard, stockLoadout.getArmourType()));
      builder.push(
          new CmdSetHeatSinkType(messageBuffer, loadoutStandard, stockLoadout.getHeatSinkType()));
    }
    builder.push(new CmdSetGuidanceType(messageBuffer, loadout, stockLoadout.getGuidanceType()));

    for (final StockLoadout.StockComponent stockComponent : stockLoadout.getComponents()) {
      final Location location = stockComponent.getLocation();
      final ConfiguredComponent configured = loadout.getComponent(location);

      if (loadout instanceof final LoadoutOmniMech loadoutOmniMech) {
        final ConfiguredComponentOmniMech omniComponent = loadoutOmniMech.getComponent(location);

        final Optional<OmniPod> optionalOmniPod = stockComponent.getOmniPod();
        optionalOmniPod.ifPresent(
            omniPod ->
                builder.push(
                    new CmdSetOmniPod(messageBuffer, loadoutOmniMech, omniComponent, omniPod)));

        final ActuatorState actuatorState = stockComponent.getActuatorState();
        if (actuatorState != null) {
          switch (stockComponent.getActuatorState()) {
            case BOTH -> {
              safeToggle(loadoutOmniMech, omniComponent, ItemDB.LAA, true);
              safeToggle(loadoutOmniMech, omniComponent, ItemDB.HA, true);
            }
            case LAA -> {
              safeToggle(loadoutOmniMech, omniComponent, ItemDB.HA, false);
              safeToggle(loadoutOmniMech, omniComponent, ItemDB.LAA, true);
            }
            case NONE -> {
              safeToggle(loadoutOmniMech, omniComponent, ItemDB.HA, false);
              safeToggle(loadoutOmniMech, omniComponent, ItemDB.LAA, false);
            }
            default -> throw new RuntimeException("Unknown actuator state encountered!");
          }
        }
      }

      if (location.isTwoSided()) {
        builder.push(
            new CmdSetArmour(messageBuffer, loadout, configured, ArmourSide.FRONT, 0, true));
        builder.push(
            new CmdSetArmour(
                messageBuffer,
                loadout,
                configured,
                ArmourSide.BACK,
                stockComponent.getArmourBack(),
                true));
        builder.push(
            new CmdSetArmour(
                messageBuffer,
                loadout,
                configured,
                ArmourSide.FRONT,
                stockComponent.getArmourFront(),
                true));
      } else {
        builder.push(
            new CmdSetArmour(
                messageBuffer,
                loadout,
                configured,
                ArmourSide.ONLY,
                stockComponent.getArmourFront(),
                true));
      }

      for (final Item item : stockComponent.getItems()) {
        builder.push(new CmdAddItem(messageBuffer, loadout, configured, item));
      }
    }

    builder.getAllCommands().forEach(this::addOp);
  }

  /**
   * Because PGI sometimes produces inconsistent stock loadouts that have actuator states set even
   * though they don't have actuators we need to take some caution applying actuator states from
   * stock loadouts.
   *
   * @param aLoadoutOmniMech The loadout to apply the stock to.
   * @param aOmniComponent The command to apply to.
   * @param aItem The item to toggle.
   * @param aNewState The new toggle state.
   */
  private void safeToggle(
      Loadout aLoadoutOmniMech,
      ConfiguredComponentOmniMech aOmniComponent,
      Item aItem,
      boolean aNewState) {
    builder.push(
        new CmdToggleItem(
            messageBuffer,
            aLoadoutOmniMech,
            aOmniComponent,
            aItem,
            aNewState && aOmniComponent.canToggleOn(aItem) == EquipResult.SUCCESS));
  }
}
