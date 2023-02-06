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
package org.lisoft.lsml.view_fx.properties;

import static javafx.beans.binding.Bindings.subtract;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberExpression;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javax.inject.Inject;
import javax.inject.Named;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.messages.*;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.mwo_data.ItemDB;
import org.lisoft.lsml.mwo_data.equipment.Item;
import org.lisoft.lsml.mwo_data.mechs.ArmourSide;
import org.lisoft.lsml.mwo_data.mechs.Component;
import org.lisoft.lsml.mwo_data.mechs.Location;

/**
 * This class adapts a {@link Loadout} for suitability to use with JavaFX bindings type APIs.
 *
 * @author Li Song
 */
public class LoadoutModelAdaptor {

  public class ComponentModel {
    public final IntegerBinding armour;
    public final IntegerBinding armourBack;
    public final IntegerBinding armourEff;
    public final IntegerBinding armourEffBack;
    public final NumberExpression armourMax;
    public final NumberExpression armourMaxBack;
    public final DoubleBinding health;
    public final DoubleBinding healthEff;

    public ComponentModel(
        MessageXBar aXBar,
        Location aLocation,
        Predicate<Message> aArmourChanged,
        Predicate<Message> aQuirksChanged,
        ErrorReporter aErrorReporter) {
      final Component internalComponent = loadout.getComponent(aLocation).getInternalComponent();
      final int localMaxArmour = internalComponent.getArmourMax();

      health =
          new LsmlDoubleBinding(
              aXBar, () -> internalComponent.getHitPoints(null), aQuirksChanged, aErrorReporter);
      healthEff =
          new LsmlDoubleBinding(
              aXBar,
              () -> internalComponent.getHitPoints(loadout.getAllModifiers()),
              aQuirksChanged,
              aErrorReporter);
      if (aLocation.isTwoSided()) {
        armour =
            makeArmourBinding(aXBar, ArmourSide.FRONT, aLocation, aArmourChanged, aErrorReporter);
        armourEff =
            makeEffectiveArmourBinding(
                aXBar, ArmourSide.FRONT, aLocation, aArmourChanged, aErrorReporter);
        armourBack =
            makeArmourBinding(aXBar, ArmourSide.BACK, aLocation, aArmourChanged, aErrorReporter);
        armourEffBack =
            makeEffectiveArmourBinding(
                aXBar, ArmourSide.BACK, aLocation, aArmourChanged, aErrorReporter);

        armourMax = subtract(localMaxArmour, armourBack);
        armourMaxBack = subtract(localMaxArmour, armour);
      } else {
        armour =
            makeArmourBinding(aXBar, ArmourSide.ONLY, aLocation, aArmourChanged, aErrorReporter);
        armourEff =
            makeEffectiveArmourBinding(
                aXBar, ArmourSide.ONLY, aLocation, aArmourChanged, aErrorReporter);
        armourMax = new ReadOnlyIntegerWrapper(localMaxArmour);
        armourBack = null;
        armourEffBack = null;
        armourMaxBack = null;
      }
    }
  }
  // Armour
  public final Map<Location, ComponentModel> components;
  // Toggles
  public final BooleanBinding hasLeftHA;
  public final BooleanBinding hasLeftLAA;
  public final BooleanBinding hasRightHA;
  public final BooleanBinding hasRightLAA;
  public final Loadout loadout;
  public final IntegerBinding statsArmour;
  public final IntegerBinding statsArmourFree;
  public final DoubleBinding statsFreeMass;
  public final DoubleBinding statsMass;
  public final IntegerBinding statsSlots;

  @Inject
  public LoadoutModelAdaptor(
      Loadout aLoadout, @Named("local") MessageXBar aXBar, ErrorReporter aER) {
    loadout = aLoadout;
    final Predicate<Message> armourChanged = (aMsg) -> aMsg instanceof ArmourMessage;
    final Predicate<Message> itemsChanged = (aMsg) -> aMsg instanceof ItemMessage;
    final Predicate<Message> upgradesChanged = (aMsg) -> aMsg instanceof UpgradesMessage;
    final Predicate<Message> omniPodChanged = (aMsg) -> aMsg instanceof OmniPodMessage;
    final Predicate<Message> slotsChanged =
        (aMsg) -> itemsChanged.test(aMsg) || upgradesChanged.test(aMsg);
    final Predicate<Message> massChanged =
        (aMsg) -> armourChanged.test(aMsg) || slotsChanged.test(aMsg) || omniPodChanged.test(aMsg);

    //
    // General
    //
    statsMass = new LsmlDoubleBinding(aXBar, loadout::getMass, massChanged, aER);
    statsFreeMass = statsMass.negate().add(loadout.getChassis().getMassMax());
    statsArmour = new LsmlIntegerBinding(aXBar, loadout::getArmour, armourChanged, aER);
    statsArmourFree = statsArmour.negate().add(loadout.getChassis().getArmourMax());
    statsSlots = new LsmlIntegerBinding(aXBar, loadout::getSlotsUsed, slotsChanged, aER);

    //
    // Toggles
    //
    hasLeftHA = makeToggle(aXBar, Location.LeftArm, ItemDB.HA, itemsChanged, aER);
    hasLeftLAA = makeToggle(aXBar, Location.LeftArm, ItemDB.LAA, itemsChanged, aER);
    hasRightHA = makeToggle(aXBar, Location.RightArm, ItemDB.HA, itemsChanged, aER);
    hasRightLAA = makeToggle(aXBar, Location.RightArm, ItemDB.LAA, itemsChanged, aER);

    // Components
    final Map<Location, ComponentModel> localComponents = new HashMap<>();
    for (final Location location : Location.values()) {
      localComponents.put(
          location, new ComponentModel(aXBar, location, armourChanged, omniPodChanged, aER));
    }
    components = Collections.unmodifiableMap(localComponents);
  }

  private LsmlIntegerBinding makeArmourBinding(
      MessageXBar aXBar,
      ArmourSide aArmourSide,
      Location location,
      Predicate<Message> armourChanged,
      ErrorReporter aErrorReporter) {
    final ConfiguredComponent component = loadout.getComponent(location);
    return new LsmlIntegerBinding(
        aXBar,
        () -> component.getArmour(aArmourSide),
        aMsg -> armourChanged.test(aMsg) && ((ArmourMessage) aMsg).component == component,
        aErrorReporter);
  }

  private LsmlIntegerBinding makeEffectiveArmourBinding(
      MessageXBar aXBar,
      ArmourSide aArmourSide,
      Location location,
      Predicate<Message> armourChanged,
      ErrorReporter aErrorReporter) {
    final ConfiguredComponent component = loadout.getComponent(location);
    return new LsmlIntegerBinding(
        aXBar,
        () -> component.getEffectiveArmour(aArmourSide, loadout.getAllModifiers()),
        aMsg -> armourChanged.test(aMsg) && ((ArmourMessage) aMsg).component == component,
        aErrorReporter);
  }

  private BooleanBinding makeToggle(
      MessageXBar aXBar,
      Location aLocation,
      Item aItem,
      Predicate<Message> aItemsChanged,
      ErrorReporter aErrorReporter) {
    if (loadout instanceof LoadoutOmniMech) {
      final LoadoutOmniMech loadoutOmni = (LoadoutOmniMech) loadout;
      final ConfiguredComponentOmniMech component = loadoutOmni.getComponent(aLocation);
      if (component.getOmniPod().getToggleableItems().contains(aItem)) {
        return new LsmlBooleanBinding(
            aXBar, () -> component.getToggleState(aItem), aItemsChanged, aErrorReporter);
      }
    }
    return null;
  }
}
