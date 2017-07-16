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
package org.lisoft.lsml.model.database;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.lisoft.lsml.model.chassi.BaseMovementProfile;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.ComponentOmniMech;
import org.lisoft.lsml.model.chassi.ComponentStandard;
import org.lisoft.lsml.model.chassi.HardPoint;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.export.garage.HardPointConverter;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.Consumable;
import org.lisoft.lsml.model.item.ECM;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.model.item.MASC;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.item.Module;
import org.lisoft.lsml.model.item.TargetingComputer;
import org.lisoft.lsml.model.loadout.StockLoadout;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrade;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * This class provides a centralised access point for all game data.
 *
 * @author Li Song
 */
public class Database {

    public static XStream makeDatabaseXStream() {
        final XStream stream = new XStream();
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
        return stream;
    }

    public static XStream makeMwoSuitableXStream() {
        final XStream xstream = new XStream(new XppDriver(new NoNameCoder()));
        xstream.ignoreUnknownElements();
        xstream.autodetectAnnotations(true);
        return xstream;
    }

    @XStreamAsAttribute
    private final String lsmlVersion;

    /** Filename - CRC */
    private final Map<String, Long> checksums;

    private final Map<String, ModifierDescription> modifierDescriptions;
    private final List<Item> items;
    private final List<Upgrade> upgrades;
    private final List<OmniPod> omniPods;
    private final List<Consumable> modules;
    private final List<Chassis> chassis;
    private final List<Environment> environments;
    private final List<StockLoadout> stockLoadouts;

    public Database(String aLsmlVersion, Map<String, Long> aChecksums, List<Item> aItems, List<Upgrade> aUpgrades,
            List<OmniPod> aOmniPods, List<Consumable> aModules, List<Chassis> aChassis, List<Environment> aEnvironments,
            List<StockLoadout> aStockLoadouts, Map<String, ModifierDescription> aModifierDescriptions) {
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
