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
package org.lisoft.lsml.model.database.mwo_parser;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.item.*;
import org.lisoft.lsml.model.item.Module;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;

class ItemStatsModule extends ItemStats {
  @XStreamAsAttribute protected String CType;
  @XmlElement protected ModuleStatsTag ModuleStats;
  @XmlElement private AmmoTypeStatsTag AmmoTypeStats;
  @XmlElement private ConsumableStatsTag ConsumableStats;
  @XmlElement private EngineStatsTag EngineStats;
  @XmlElement private HeatSinkStatsTag HeatSinkStats;
  @XmlElement private JumpJetStatsTag JumpJetStats;
  @XmlElement private MascStatsTag MASCStats;
  @XmlElement private TargetingComputerStatsTag TargetingComputerStats;

  public Optional<Consumable> asConsumable() {
    switch (CType) {
      case "CCoolantFlushStats":
      case "CStrategicStrikeStats":
      case "CUAVStats":
        {
          return Optional.of(
              new Consumable(
                  getUiName(),
                  getUiShortName(),
                  getUiDescription(),
                  getMwoKey(),
                  getMwoId(),
                  getFaction(),
                  ConsumableType.fromMwo(ConsumableStats.equipType)));
        }
      default:
        return Optional.empty();
    }
  }

  public Optional<Item> asItem() {
    return switch (CType) {
      case "CAmmoTypeStats" -> Optional.of(asAmmunition());
      case "CEngineStats" -> Optional.of(asEngine());
      case "CHeatSinkStats" -> Optional.of(asHeatSink());
      case "CJumpJetStats" -> Optional.of(asJumpJet());
      case "CGECMStats" -> Optional.of(asECM());
      case "CTargetingComputerStats" -> Optional.of(asTargetingComputer());
      case "CMASCStats" -> Optional.of(asMasc());
      case "CBAPStats", "CClanBAPStats", "CClanLightBAPStats" -> Optional.of(asActiveProbe());
      case "CCASEStats" -> Optional.of(asGenericModule());
      case "CAdvancedSensorsStats", "CLowerArmActuatorStats", "CInternalStats" -> Optional.of(
          asInternal());
      default -> Optional.empty();
    };
  }

  private Internal asInternal() {
    return new Internal(
        getUiName(),
        getUiDescription(),
        getMwoKey(),
        getMwoId(),
        ModuleStats.slots,
        ModuleStats.tons,
        HardPointType.NONE,
        ModuleStats.health,
        getFaction());
  }

  private Module asGenericModule() {
    return new Module(
        getUiName(),
        getUiDescription(),
        getMwoKey(),
        getMwoId(),
        ModuleStats.slots,
        ModuleStats.tons,
        HardPointType.NONE,
        ModuleStats.health,
        getFaction(),
        ModuleStats.getLocations(),
        ModuleStats.getMechClasses(),
        null);
  }

  private HeatSink asHeatSink() {
    return new HeatSink(
        getUiName(),
        getUiDescription(),
        getMwoKey(),
        getMwoId(),
        ModuleStats.slots,
        ModuleStats.tons,
        HardPointType.NONE,
        ModuleStats.health,
        getFaction(),
        HeatSinkStats.cooling,
        HeatSinkStats.engineCooling,
        -HeatSinkStats.heatbase,
        -HeatSinkStats.engineHeatbase);
  }

  private JumpJet asJumpJet() {
    // Two values, first is heat for one JJ
    final double heat = Double.parseDouble(JumpJetStats.heat.split(",")[0]);
    return new JumpJet(
        getUiName(),
        getUiDescription(),
        getMwoKey(),
        getMwoId(),
        ModuleStats.slots,
        ModuleStats.tons,
        HardPointType.NONE,
        ModuleStats.health,
        getFaction(),
        ModuleStats.getLocations(),
        ModuleStats.getMechClasses(),
        JumpJetStats.minTons,
        JumpJetStats.maxTons,
        JumpJetStats.boost_z,
        JumpJetStats.duration,
        heat);
  }

  private ECM asECM() {
    return new ECM(
        getUiName(),
        getUiDescription(),
        getMwoKey(),
        getMwoId(),
        ModuleStats.slots,
        ModuleStats.tons,
        ModuleStats.health,
        getFaction(),
        ModuleStats.amountAllowed);
  }

  private ActiveProbe asActiveProbe() {
    return new ActiveProbe(
        getUiName(),
        getUiDescription(),
        getMwoKey(),
        getMwoId(),
        ModuleStats.slots,
        ModuleStats.tons,
        HardPointType.NONE,
        ModuleStats.health,
        getFaction(),
        ModuleStats.getLocations(),
        ModuleStats.getMechClasses(),
        ModuleStats.amountAllowed);
  }

  /**
   * Creates a new MASC item using this object.
   *
   * @return a {@link MASC}.
   */
  private MASC asMasc() {
    return new MASC(
        getUiName(),
        getUiDescription(),
        getMwoKey(),
        getMwoId(),
        ModuleStats.slots,
        ModuleStats.tons,
        ModuleStats.health,
        getFaction(),
        ModuleStats.amountAllowed,
        ModuleStats.TonsMin,
        ModuleStats.TonsMax,
        MASCStats.BoostSpeed,
        MASCStats.BoostAccel,
        MASCStats.BoostDecel,
        MASCStats.BoostTurn);
  }

  private Engine asEngine() {
    final int hs = EngineStats.heatsinks;
    final int internalHs = Math.min(10, hs);
    final int heatSinkSlots = hs - internalHs;

    final String lcName = getMwoKey().toLowerCase();
    final EngineType engineType;
    if (lcName.contains("xl")) {
      engineType = EngineType.XL;
    } else if (lcName.contains("light")) {
      engineType = EngineType.LE;
    } else if (lcName.contains("std")) {
      engineType = EngineType.STD;
    } else {
      throw new IllegalArgumentException("Unknown engine type: " + getUiName());
    }
    return new Engine(
        getUiName(),
        getUiDescription(),
        getMwoKey(),
        getMwoId(),
        EngineStats.slots,
        EngineStats.tons,
        EngineStats.health,
        getFaction(),
        ItemStatsModule.EngineStatsTag.ENGINE_HEAT,
        EngineStats.rating,
        engineType,
        internalHs,
        heatSinkSlots,
        EngineStats.sideSlots,
        EngineStats.movementHeatMultiplier);
  }

  private Ammunition asAmmunition() {
    return new Ammunition(
        getUiName(),
        getUiDescription(),
        getMwoKey(),
        getMwoId(),
        ModuleStats.slots,
        ModuleStats.tons,
        HardPointType.NONE,
        ModuleStats.health,
        getFaction(),
        AmmoTypeStats.numShots,
        AmmoTypeStats.type,
        AmmoTypeStats.internalDamage);
  }

  private TargetingComputer asTargetingComputer() {

    final List<Modifier> modifiers = new ArrayList<>();
    if (null != TargetingComputerStats.WeaponStatsFilter) {
      for (final TargetingComputerStatsTag.WeaponStatsFilter filter :
          TargetingComputerStats.WeaponStatsFilter) {
        for (final TargetingComputerStatsTag.WeaponStatsFilter.WeaponStats stats :
            filter.WeaponStats) {
          final double range = filter.range != null ? filter.range.multiplier : 0.0;
          modifiers.addAll(
              QuirkModifiers.createModifiers(
                  getUiName(),
                  stats.operation,
                  filter.compatibleWeapons,
                  0,
                  range,
                  stats.speed,
                  0,
                  0));
        }
      }
    }

    return new TargetingComputer(
        getUiName(),
        getUiDescription(),
        getMwoKey(),
        getMwoId(),
        ModuleStats.slots,
        ModuleStats.tons,
        HardPointType.NONE,
        ModuleStats.health,
        getFaction(),
        ModuleStats.getLocations(),
        ModuleStats.getMechClasses(),
        ModuleStats.amountAllowed,
        modifiers);
  }

  private static class HeatSinkStatsTag {
    @XStreamAsAttribute private double cooling;
    @XStreamAsAttribute private double engineCooling;
    @XStreamAsAttribute private double engineHeatbase;
    @XStreamAsAttribute private double heatbase;
  }

  /**
   * A helper class for parsing MASC data from the MWO data files.
   *
   * @author Li Song
   */
  private static class MascStatsTag {
    @XStreamAsAttribute private double BoostAccel;
    @XStreamAsAttribute private double BoostDecel;
    @XStreamAsAttribute private double BoostSpeed;
    @XStreamAsAttribute private double BoostTurn;
  }

  private static class EngineStatsTag extends ModuleStatsTag {
    private static final Attribute ENGINE_HEAT =
        new Attribute(
            Engine.ENGINE_HEAT_FULL_THROTTLE, ModifierDescription.SEL_HEAT_MOVEMENT, null);
    @XStreamAsAttribute private int heatsinks;
    @XStreamAsAttribute private double movementHeatMultiplier;
    @XStreamAsAttribute private int rating;
    @XStreamAsAttribute private int sideSlots;
  }

  private static class AmmoTypeStatsTag {
    @XStreamAsAttribute private double internalDamage;
    @XStreamAsAttribute private int numShots;
    @XStreamAsAttribute private String type;
  }

  /**
   * Helper class for parsing targeting computer information from XML files.
   *
   * @author Li Song
   */
  private static class TargetingComputerStatsTag {
    @XStreamImplicit private List<WeaponStatsFilter> WeaponStatsFilter;

    @XStreamAlias("WeaponStatsFilter")
    private static class WeaponStatsFilter {

      @XStreamImplicit private List<WeaponStats> WeaponStats;
      @XStreamAsAttribute private String compatibleWeapons;

      @XStreamAlias("Range")
      private Range range;

      @XStreamAlias("WeaponStats")
      private static class WeaponStats {
        @XStreamAsAttribute private String operation;
        @XStreamAsAttribute private double speed;
      }
    }
  }

  private static class ModuleStatsTag {
    @XStreamAsAttribute protected int TonsMax; // Currently only used by MASC?
    @XStreamAsAttribute protected int TonsMin; // Currently only used by MASC?
    @XStreamAsAttribute protected Integer amountAllowed;
    @XStreamAsAttribute protected String components;

    @XStreamAlias("Health")
    @XStreamAsAttribute
    protected double health;

    @XStreamAsAttribute protected String mechClass;
    @XStreamAsAttribute protected int slots;

    @XStreamAlias(value = "weight")
    @XStreamAsAttribute
    protected double tons;

    protected List<Location> getLocations() {
      if (null != components) {
        final String[] comps = components.split("\\s*,\\s*");
        final List<Location> ans = new ArrayList<>();
        for (final String component : comps) {
          ans.add(Location.fromMwoName(component));
        }
        return ans;
      }
      return null;
    }

    protected List<ChassisClass> getMechClasses() {
      if (null != mechClass) {
        final String[] classes = mechClass.split("\\s*,\\s*");
        final List<ChassisClass> ans = new ArrayList<>();
        for (final String clazz : classes) {
          ans.add(ChassisClass.valueOf(clazz.toUpperCase()));
        }
        return ans;
      }
      return null;
    }
  }

  private static class JumpJetStatsTag {
    @XStreamAsAttribute private double boost_z;
    @XStreamAsAttribute private double duration;
    @XStreamAsAttribute private String heat;
    @XStreamAsAttribute private int maxTons;
    @XStreamAsAttribute private int minTons;
  }

  /**
   * A data model object for the MWO data files.
   *
   * @author Li Song
   */
  private static class ConsumableStatsTag {
    @XStreamAsAttribute private String equipType;
  }
}
