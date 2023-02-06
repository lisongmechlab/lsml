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
package org.lisoft.lsml.mwo_data;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.MXParserDriver;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lisoft.lsml.mwo_data.equipment.*;
import org.lisoft.lsml.mwo_data.equipment.Module;
import org.lisoft.lsml.mwo_data.mechs.*;
import org.lisoft.lsml.mwo_data.modifiers.*;
import org.lisoft.lsml.mwo_data.mwo_parser.AttributeConverter;
import org.lisoft.lsml.mwo_data.mwo_parser.HardPointConverter;
import org.lisoft.lsml.mwo_data.mwo_parser.ModifierDescriptionConverter;

/**
 * This class provides a centralized access point for all game data.
 *
 * @author Li Song
 */
public class Database {

  private final List<Chassis> chassis;
  /** Filename - CRC */
  private final Map<String, Long> checksums;

  private final List<Environment> environments;
  private final List<Item> items;
  @XStreamAsAttribute private final String lsmlVersion;
  private final Map<String, ModifierDescription> modifierDescriptions;
  private final List<Consumable> modules;
  private final List<OmniPod> omniPods;
  private final List<StockLoadout> stockLoadouts;
  private final List<Upgrade> upgrades;

  public Database(
      String aLsmlVersion,
      Map<String, Long> aChecksums,
      List<Item> aItems,
      List<Upgrade> aUpgrades,
      List<OmniPod> aOmniPods,
      List<Consumable> aModules,
      List<Chassis> aChassis,
      List<Environment> aEnvironments,
      List<StockLoadout> aStockLoadouts,
      Map<String, ModifierDescription> aModifierDescriptions) {
    lsmlVersion = aLsmlVersion;
    checksums = aChecksums;
    items = aItems;
    upgrades = aUpgrades;
    omniPods = aOmniPods;
    modules = aModules;
    chassis = aChassis;
    environments = aEnvironments;
    stockLoadouts = aStockLoadouts;
    modifierDescriptions = aModifierDescriptions;
  }

  public static XStream makeDatabaseXStream() {
    final XStream stream = new XStream(new MXParserDriver());
    stream.autodetectAnnotations(true);
    stream.setMode(XStream.ID_REFERENCES);
    stream.alias("database", Database.class);
    stream.alias("jumpjet", JumpJet.class);
    stream.alias("ammunition", Ammunition.class);
    stream.alias("chassis", ChassisStandard.class);
    stream.alias("chassisOmni", ChassisOmniMech.class);
    stream.alias("hardpoint", HardPoint.class);
    stream.alias("component", ComponentStandard.class);
    stream.alias("componentOmni", ComponentOmniMech.class);
    stream.alias("env", Environment.class);
    stream.alias("ammoweapon", AmmoWeapon.class);
    stream.alias("ballisticweapon", BallisticWeapon.class);
    stream.alias("energyweapon", EnergyWeapon.class);
    stream.alias("engine", Engine.class);
    stream.alias("ecm", ECM.class);
    stream.alias("heatsink", HeatSink.class);
    stream.alias("internal", Internal.class);
    stream.alias("masc", MASC.class);
    stream.alias("missileweapon", MissileWeapon.class);
    stream.alias("module", Module.class);
    stream.alias("part", Location.class);
    stream.alias("pilotmodule", Consumable.class);
    stream.alias("basemovementprofile", BaseMovementProfile.class);
    stream.alias("omnipod", OmniPod.class);
    stream.alias("attribute", Attribute.class);
    stream.alias("modifierdescription", ModifierDescription.class);
    stream.alias("modifier", Modifier.class);
    stream.alias("structureupgrade", StructureUpgrade.class);
    stream.alias("heatsinkupgrade", HeatSinkUpgrade.class);
    stream.alias("armorupgrade", ArmourUpgrade.class);
    stream.alias("guidanceupgrade", GuidanceUpgrade.class);
    stream.alias("targetingcomp", TargetingComputer.class);
    stream.alias("chassisclass", ChassisClass.class);
    stream.registerConverter(new HardPointConverter());
    stream.registerConverter(new AttributeConverter());
    stream.registerConverter(new ModifierDescriptionConverter());

    stream.addDefaultImplementation(HashMap.class, Map.class);

    stream.allowTypeHierarchy(Database.class);
    stream.allowTypeHierarchy(MwoObject.class);
    stream.allowTypeHierarchy(Component.class);
    stream.allowTypeHierarchy(MovementProfile.class);
    stream.allowTypeHierarchy(Environment.class);
    stream.allowTypeHierarchy(StockLoadout.class);
    stream.allowTypeHierarchy(StockLoadout.StockComponent.class);
    stream.allowTypeHierarchy(ModifierDescription.class);
    stream.allowTypeHierarchy(Modifier.class);
    stream.allowTypeHierarchy(HardPoint.class);
    stream.allowTypeHierarchy(WeaponRangeProfile.class);
    stream.allowTypeHierarchy(WeaponRangeProfile.RangeNode.class);

    return stream;
  }

  public static XStream makeMwoSuitableXStream() {
    final XStream xstream = new XStream(new MXParserDriver(new NoNameCoder()));
    xstream.ignoreUnknownElements();
    xstream.autodetectAnnotations(true);
    xstream.allowTypesByWildcard(new String[] {"org.lisoft.lsml.mwo_data.mwo_parser.**"});

    return xstream;
  }

  /**
   * @return An unmodifiable {@link List} of all inner sphere {@link ChassisStandard}s.
   */
  public List<Chassis> getChassis() {
    return Collections.unmodifiableList(chassis);
  }

  /**
   * @return the checksums
   */
  public Map<String, Long> getChecksums() {
    return Collections.unmodifiableMap(checksums);
  }

  /**
   * @return An unmodifiable {@link List} of all {@link Environment}s.
   */
  public List<Environment> getEnvironments() {
    return Collections.unmodifiableList(environments);
  }

  /**
   * @return An unmodifiable {@link List} of all {@link Item}s.
   */
  public List<Item> getItems() {
    return Collections.unmodifiableList(items);
  }

  /**
   * @return A {@link Map} of all the modifier descriptions indexed by their keys.
   */
  public Map<String, ModifierDescription> getModifierDescriptions() {
    return modifierDescriptions;
  }

  /**
   * @return An unmodifiable {@link List} of {@link OmniPod}s.
   */
  public List<OmniPod> getOmniPods() {
    return omniPods;
  }

  /**
   * @return An unmodifiable {@link List} of all {@link Consumable}s.
   */
  public List<Consumable> getPilotModules() {
    return Collections.unmodifiableList(modules);
  }

  /**
   * @return An unmodifiable {@link List} of all {@link StockLoadout}s.
   */
  public List<StockLoadout> getStockLoadouts() {
    return Collections.unmodifiableList(stockLoadouts);
  }

  /**
   * @return An unmodifiable {@link List} of all {@link Upgrade}s.
   */
  public List<Upgrade> getUpgrades() {
    return Collections.unmodifiableList(upgrades);
  }

  /**
   * @return The LSML version that this database is compatible with.
   */
  public String getVersion() {
    return lsmlVersion;
  }
}
