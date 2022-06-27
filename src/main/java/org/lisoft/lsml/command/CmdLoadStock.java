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

import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NoSuchItemException;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.database.OmniPodDB;
import org.lisoft.lsml.model.database.StockLoadoutDB;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.*;
import org.lisoft.lsml.model.loadout.StockLoadout.StockComponent.ActuatorState;

import java.util.Optional;

/**
 * This operation loads a 'mechs stock {@link LoadoutStandard}.
 * <p>
 * TODO: Devise a method for composite commands to wrap the exceptions from their sub commands for useful error
 * messages.
 *
 * @author Li Song
 */
public class CmdLoadStock extends CmdLoadoutBase {
    private final LoadoutBuilder builder = new LoadoutBuilder();
    private final StockLoadout stockLoadout;

    public CmdLoadStock(Chassis aChassiVariation, Loadout aLoadout, MessageDelivery aMessageDelivery)
            throws NoSuchItemException {
        super(aLoadout, aMessageDelivery, "load stock");
        stockLoadout = StockLoadoutDB.lookup(aChassiVariation);
    }

    @Override
    public void buildCommand() throws EquipException, NoSuchItemException {
        addOp(new CmdStripLoadout(messageBuffer, loadout));

        if (loadout instanceof LoadoutStandard) {
            final LoadoutStandard loadoutStandard = (LoadoutStandard) loadout;
            builder.push(new CmdSetStructureType(messageBuffer, loadoutStandard, stockLoadout.getStructureType()));
            builder.push(new CmdSetArmourType(messageBuffer, loadoutStandard, stockLoadout.getArmourType()));
            builder.push(new CmdSetHeatSinkType(messageBuffer, loadoutStandard, stockLoadout.getHeatSinkType()));
        }
        builder.push(new CmdSetGuidanceType(messageBuffer, loadout, stockLoadout.getGuidanceType()));

        for (final StockLoadout.StockComponent stockComponent : stockLoadout.getComponents()) {
            final Location location = stockComponent.getLocation();
            final ConfiguredComponent configured = loadout.getComponent(location);

            if (loadout instanceof LoadoutOmniMech) {
                final LoadoutOmniMech loadoutOmniMech = (LoadoutOmniMech) loadout;
                final ConfiguredComponentOmniMech omniComponent = loadoutOmniMech.getComponent(location);

                final Optional<Integer> optionalOmniPod = stockComponent.getOmniPod();
                if (optionalOmniPod.isPresent()) {
                    final OmniPod omnipod = OmniPodDB.lookup(optionalOmniPod.get());
                    builder.push(new CmdSetOmniPod(messageBuffer, loadoutOmniMech, omniComponent, omnipod));
                }

                final ActuatorState actuatorState = stockComponent.getActuatorState();
                if (actuatorState != null) {
                    switch (stockComponent.getActuatorState()) {
                        case BOTH:
                            safeToggle(loadoutOmniMech, omniComponent, ItemDB.LAA, true);
                            safeToggle(loadoutOmniMech, omniComponent, ItemDB.HA, true);
                            break;
                        case LAA:
                            safeToggle(loadoutOmniMech, omniComponent, ItemDB.HA, false);
                            safeToggle(loadoutOmniMech, omniComponent, ItemDB.LAA, true);
                            break;
                        case NONE:
                            safeToggle(loadoutOmniMech, omniComponent, ItemDB.HA, false);
                            safeToggle(loadoutOmniMech, omniComponent, ItemDB.LAA, false);
                            break;
                        default:
                            throw new RuntimeException("Unknown actuator state encountered!");
                    }
                }
            }

            if (location.isTwoSided()) {
                builder.push(new CmdSetArmour(messageBuffer, loadout, configured, ArmourSide.FRONT, 0, true));
                builder.push(new CmdSetArmour(messageBuffer, loadout, configured, ArmourSide.BACK,
                                              stockComponent.getArmourBack(), true));
                builder.push(new CmdSetArmour(messageBuffer, loadout, configured, ArmourSide.FRONT,
                                              stockComponent.getArmourFront(), true));
            } else {
                builder.push(new CmdSetArmour(messageBuffer, loadout, configured, ArmourSide.ONLY,
                                              stockComponent.getArmourFront(), true));
            }

            for (final Integer item : stockComponent.getItems()) {
                builder.push(new CmdAddItem(messageBuffer, loadout, configured, ItemDB.lookup(item)));
            }
        }

        builder.getAllCommands().forEach(op -> addOp(op));
    }

    /**
     * Because PGI some times produces inconsistent stock loadouts that have actuator states set even though they don't
     * have actuators we need to take some caution applying actuator states from stock loadouts.
     *
     * @param aLoadoutOmniMech The loadout to apply the stock to.
     * @param aOmniComponent   The command to apply to.
     * @param aItem            The item to toggle.
     * @param aNewState        The new toggle state.
     */
    private void safeToggle(Loadout aLoadoutOmniMech, ConfiguredComponentOmniMech aOmniComponent, Item aItem,
                            boolean aNewState) {
        builder.push(new CmdToggleItem(messageBuffer, aLoadoutOmniMech, aOmniComponent, aItem,
                                       aNewState && aOmniComponent.canToggleOn(aItem) == EquipResult.SUCCESS));
    }
}
