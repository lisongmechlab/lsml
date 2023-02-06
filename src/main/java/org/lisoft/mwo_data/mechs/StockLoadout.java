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
package org.lisoft.mwo_data.mechs;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.lisoft.mwo_data.equipment.*;

/**
 * This immutable class defines a stock loadout pattern for a {@link Chassis}.
 *
 * @author Li Song
 */
@XStreamAlias("StockLoadout")
public class StockLoadout {
  /**
   * This immutable class defines a component in a stock loadout.
   *
   * @author Li Song
   */
  @XStreamAlias("Component")
  public static class StockComponent {
    @XStreamAsAttribute private final ActuatorState actuatorState;
    @XStreamAsAttribute private final Integer armourBack;
    @XStreamAsAttribute private final Integer armourFront;
    @XStreamImplicit private final List<Item> items;
    @XStreamAsAttribute private final Location location;
    @XStreamAsAttribute private final OmniPod omniPod;

    /**
     * Creates a new {@link StockComponent}.
     *
     * @param aPart The {@link Location} that this {@link StockComponent} is for.
     * @param aFront The front armour (or total armour if one-sided).
     * @param aBack The back armour (must be zero if one-sided).
     * @param aItems A {@link List} of items in the component.
     * @param aOmniPod The omnipod to use (or null if none/fixed in chassis)
     * @param aActuatorState The state of the actuators for this component, may be <code>null</code>
     *     .
     */
    public StockComponent(
        Location aPart,
        int aFront,
        int aBack,
        List<Item> aItems,
        OmniPod aOmniPod,
        ActuatorState aActuatorState) {
      location = aPart;
      armourFront = aFront;
      if (location.isTwoSided()) {
        armourBack = aBack;
      } else {
        armourBack = null;
      }
      items = Collections.unmodifiableList(aItems);
      omniPod = aOmniPod;
      actuatorState = aActuatorState;
    }

    /**
     * @return The actuator state for this {@link StockComponent} or <code>null</code> if not
     *     applicable.
     */
    public ActuatorState getActuatorState() {
      return actuatorState;
    }

    /**
     * @return The back armour of this {@link StockComponent}. Will throw if the component is
     *     one-sided.
     */
    public int getArmourBack() {
      return armourBack;
    }

    /**
     * @return The front armour of this {@link StockComponent}. Or total armour if the component is
     *     one-sided.
     */
    public int getArmourFront() {
      return armourFront;
    }

    /**
     * @return The {@link Item} IDs that are housed in this {@link StockComponent}.
     */
    public List<Item> getItems() {
      if (items == null) {
        return new ArrayList<>();
      }
      return items;
    }

    /**
     * @return The {@link Location} that defines this {@link StockComponent}.
     */
    public Location getLocation() {
      return location;
    }

    /**
     * @return The omnipod to use for this component or empty if no omnipod is specified.
     */
    public Optional<OmniPod> getOmniPod() {
      return Optional.ofNullable(omniPod);
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append(location.shortName());

      sb.append(' ').append(armourFront);
      if (null != armourBack) {
        sb.append("/").append(armourBack);
      }
      if (omniPod != null) {
        sb.append(" (pod: ").append(omniPod).append(')');
      }
      if (items != null) {
        sb.append(" [");
        boolean first = true;
        for (final Item item : items) {
          if (!first) {
            sb.append(", ");
          }
          first = false;
          sb.append(item.getShortName());
        }
        sb.append(']');
      }
      return sb.toString();
    }

    public enum ActuatorState {
      NONE,
      LAA,
      BOTH;

      public static ActuatorState fromMwoString(String aString) {
        if (aString == null || aString.isEmpty()) {
          return null;
        }

        return switch (aString.toLowerCase()) {
          case "eactuatorstate_none" -> ActuatorState.NONE;
          case "eactuatorstate_handsandarms" -> ActuatorState.BOTH;
          case "eactuatorstate_armsonly" -> ActuatorState.LAA;
          default -> throw new IllegalArgumentException(
              "Unknown actuator state: [" + aString + "]");
        };
      }
    }
  }

  @XStreamAsAttribute private final ArmourUpgrade armourUpgrade;
  @XStreamAsAttribute private final Chassis chassis;
  @XStreamImplicit private final List<StockComponent> components;
  @XStreamAsAttribute private final GuidanceUpgrade guidanceUpgrade;
  @XStreamAsAttribute private final HeatSinkUpgrade heatSinkUpgrade;
  @XStreamAsAttribute private final StructureUpgrade structureUpgrade;

  /**
   * Creates a new {@link StockLoadout}
   *
   * @param aChassis The chassis that this loadout was originally for.
   * @param aComponents The list of {@link StockComponent} that make up this {@link StockLoadout}.
   * @param aArmour The armour upgrade type.
   * @param aStructure The structure upgrade type.
   * @param aHeatSink The heat sink upgrade type.
   * @param aGuidance The guidance upgrade type.
   */
  public StockLoadout(
      Chassis aChassis,
      List<StockComponent> aComponents,
      ArmourUpgrade aArmour,
      StructureUpgrade aStructure,
      HeatSinkUpgrade aHeatSink,
      GuidanceUpgrade aGuidance) {
    chassis = aChassis;
    armourUpgrade = aArmour;
    structureUpgrade = aStructure;
    heatSinkUpgrade = aHeatSink;
    guidanceUpgrade = aGuidance;
    components = Collections.unmodifiableList(aComponents);
  }

  /**
   * @return The {@link ArmourUpgrade} for this {@link StockLoadout}.
   */
  public ArmourUpgrade getArmourType() {
    return armourUpgrade;
  }

  /**
   * @return The {@link Chassis} for this {@link StockLoadout}.
   */
  public Chassis getChassis() {
    return chassis;
  }

  /**
   * @return The {@link StockComponent}s in this {@link StockLoadout}.
   */
  public List<StockComponent> getComponents() {
    return components;
  }

  /**
   * @return The {@link GuidanceUpgrade} for this {@link StockLoadout}.
   */
  public GuidanceUpgrade getGuidanceType() {
    return guidanceUpgrade;
  }

  /**
   * @return The {@link HeatSinkUpgrade} for this {@link StockLoadout}.
   */
  public HeatSinkUpgrade getHeatSinkType() {
    return heatSinkUpgrade;
  }

  /**
   * @return The {@link StructureUpgrade} for this {@link StockLoadout}.
   */
  public StructureUpgrade getStructureType() {
    return structureUpgrade;
  }

  @Override
  public String toString() {
    return chassis.getName()
        + " ("
        + armourUpgrade.getName()
        + ", "
        + structureUpgrade.getName()
        + ", "
        + heatSinkUpgrade.getName()
        + ", "
        + guidanceUpgrade.getName()
        + ") "
        + components.toString();
  }
}
