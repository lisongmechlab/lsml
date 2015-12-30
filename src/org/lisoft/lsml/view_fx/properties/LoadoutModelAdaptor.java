/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
package org.lisoft.lsml.view_fx.properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.lisoft.lsml.command.CmdSetArmorType;
import org.lisoft.lsml.command.CmdSetGuidanceType;
import org.lisoft.lsml.command.CmdSetHeatSinkType;
import org.lisoft.lsml.command.CmdSetStructureType;
import org.lisoft.lsml.command.CmdToggleItem;
import org.lisoft.lsml.messages.ArmorMessage;
import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.chassi.ComponentBase;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.upgrades.ArmorUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.util.CommandStack;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * This class adapts a {@link LoadoutBase} for suitability to use with JavaFX bindings type APIs.
 * 
 * @author Emily Björk
 */
public class LoadoutModelAdaptor {

    public class ComponentModel {
        public final DoubleBinding  health;
        public final DoubleBinding  healthEff;
        public final IntegerBinding armor;
        public final IntegerBinding armorBack;
        public final IntegerBinding armorEff;
        public final IntegerBinding armorEffBack;
        public final NumberBinding  armorMax;
        public final NumberBinding  armorMaxBack;

        public ComponentModel(MessageXBar aXBar, Location aLocation, Predicate<Message> aArmorChanged,
                Predicate<Message> aQuirksChanged) {
            ComponentBase internalComponent = loadout.getComponent(aLocation).getInternalComponent();
            int localMaxArmor = internalComponent.getArmorMax();

            health = new LsmlDoubleBinding(aXBar, () -> internalComponent.getHitPoints(null), aQuirksChanged);
            healthEff = new LsmlDoubleBinding(aXBar, () -> internalComponent.getHitPoints(loadout.getModifiers()),
                    aQuirksChanged);
            if (aLocation.isTwoSided()) {
                armor = makeArmorBinding(aXBar, ArmorSide.FRONT, aLocation, aArmorChanged);
                armorEff = makeEffectiveArmorBinding(aXBar, ArmorSide.FRONT, aLocation, aArmorChanged);
                armorBack = makeArmorBinding(aXBar, ArmorSide.BACK, aLocation, aArmorChanged);
                armorEffBack = makeEffectiveArmorBinding(aXBar, ArmorSide.BACK, aLocation, aArmorChanged);

                armorMax = Bindings.min(Bindings.subtract(localMaxArmor, armorBack), globalAvailableArmor.add(armor));
                armorMaxBack = Bindings.min(Bindings.subtract(localMaxArmor, armor),
                        globalAvailableArmor.add(armorBack));
            }
            else {
                armor = makeArmorBinding(aXBar, ArmorSide.ONLY, aLocation, aArmorChanged);
                armorEff = makeEffectiveArmorBinding(aXBar, ArmorSide.ONLY, aLocation, aArmorChanged);
                armorMax = Bindings.min(localMaxArmor, globalAvailableArmor.add(armor));
                armorBack = null;
                armorEffBack = null;
                armorMaxBack = null;
            }
        }
    }

    // Armor
    public final Map<Location, ComponentModel>            components;
    public final NumberBinding                            globalAvailableArmor;
    public final BooleanProperty                          hasArtemis;

    public final BooleanProperty                          hasDoubleBasics;
    public final BooleanProperty                          hasDoubleHeatSinks;
    // Efficiencies
    public final Map<MechEfficiencyType, BooleanProperty> hasEfficiency;
    // Upgrades
    public final BooleanProperty                          hasEndoSteel;
    public final BooleanProperty                          hasFerroFibrous;

    // Toggles
    public final BooleanProperty                          hasLeftHA;
    public final BooleanProperty                          hasLeftLAA;
    public final BooleanProperty                          hasRightHA;
    public final BooleanProperty                          hasRightLAA;

    public final LoadoutBase<?>                           loadout;
    public final LoadoutMetrics                           metrics;

    public final IntegerBinding                           statsArmor;
    public final IntegerBinding                           statsArmorFree;
    public final DoubleBinding                            statsFreeMass;
    // General
    public final DoubleBinding                            statsMass;

    public final IntegerBinding                           statsSlots;
    private final CommandStack                            cmdStack;

    public LoadoutModelAdaptor(LoadoutBase<?> aLoadout, MessageXBar aXBar, CommandStack aCmdStack) {
        cmdStack = aCmdStack;
        loadout = aLoadout;
        metrics = new LoadoutMetrics(loadout, null, aXBar);
        Faction faction = loadout.getChassis().getFaction();
        Upgrades upgrades = loadout.getUpgrades();
        StructureUpgrade structureES = UpgradeDB.getStructure(faction, true);
        StructureUpgrade structureSTD = UpgradeDB.getStructure(faction, false);
        ArmorUpgrade armorFF = UpgradeDB.getArmor(faction, true);
        ArmorUpgrade armorSTD = UpgradeDB.getArmor(faction, false);
        HeatSinkUpgrade hsDHS = UpgradeDB.getHeatSinks(faction, true);
        HeatSinkUpgrade hsSTD = UpgradeDB.getHeatSinks(faction, false);
        Predicate<Message> armorChanged = (aMsg) -> aMsg instanceof ArmorMessage;
        Predicate<Message> itemsChanged = (aMsg) -> aMsg instanceof ItemMessage;
        Predicate<Message> upgradesChanged = (aMsg) -> aMsg instanceof UpgradesMessage;
        Predicate<Message> effsChanged = (aMsg) -> aMsg instanceof EfficienciesMessage;
        Predicate<Message> omniPodChanged = (aMsg) -> aMsg instanceof OmniPodMessage;
        Predicate<Message> slotsChanged = (aMsg) -> itemsChanged.test(aMsg) || upgradesChanged.test(aMsg);
        Predicate<Message> massChanged = (aMsg) -> armorChanged.test(aMsg) || slotsChanged.test(aMsg);

        //
        // General
        //
        statsMass = new LsmlDoubleBinding(aXBar, () -> loadout.getMass(), massChanged);
        statsFreeMass = statsMass.negate().add(loadout.getChassis().getMassMax());
        statsArmor = new LsmlIntegerBinding(aXBar, () -> loadout.getArmor(), armorChanged);
        statsArmorFree = statsArmor.negate().add(loadout.getChassis().getArmorMax());
        statsSlots = new LsmlIntegerBinding(aXBar, () -> loadout.getNumCriticalSlotsUsed(), slotsChanged);

        //
        // Upgrades
        //
        if (loadout instanceof LoadoutStandard) {
            LoadoutStandard loadoutSTD = (LoadoutStandard) loadout;
            hasEndoSteel = new LsmlBooleanProperty(aXBar, () -> structureES == upgrades.getStructure(), (aValue) -> {
                cmdStack.pushAndApply(new CmdSetStructureType(aXBar, loadoutSTD, aValue ? structureES : structureSTD));
                return true;
            } , upgradesChanged);

            hasDoubleHeatSinks = new LsmlBooleanProperty(aXBar, () -> hsDHS == upgrades.getHeatSink(), (aValue) -> {
                cmdStack.pushAndApply(new CmdSetHeatSinkType(aXBar, loadoutSTD, aValue ? hsDHS : hsSTD));
                return true;
            } , upgradesChanged);

            hasFerroFibrous = new LsmlBooleanProperty(aXBar, () -> armorFF == upgrades.getArmor(), (aValue) -> {
                cmdStack.pushAndApply(new CmdSetArmorType(aXBar, loadoutSTD, aValue ? armorFF : armorSTD));
                return true;
            } , upgradesChanged);
        }
        else {
            hasEndoSteel = null;
            hasDoubleHeatSinks = null;
            hasFerroFibrous = null;
        }

        hasArtemis = new LsmlBooleanProperty(aXBar, () -> UpgradeDB.ARTEMIS_IV == upgrades.getGuidance(), (aValue) -> {
            cmdStack.pushAndApply(
                    new CmdSetGuidanceType(aXBar, loadout, aValue ? UpgradeDB.ARTEMIS_IV : UpgradeDB.STD_GUIDANCE));
            return true;
        } , upgradesChanged);

        //
        // Efficiencies
        //
        Efficiencies effs = loadout.getEfficiencies();
        Map<MechEfficiencyType, BooleanProperty> localHasEffMap = new HashMap<>();
        for (MechEfficiencyType type : MechEfficiencyType.values()) {
            localHasEffMap.put(type, new LsmlBooleanProperty(aXBar, () -> effs.hasEfficiency(type), (aValue) -> {
                effs.setEfficiency(type, aValue, aXBar);
                return true;
            } , effsChanged));
        }
        hasEfficiency = Collections.unmodifiableMap(localHasEffMap);

        hasDoubleBasics = new SimpleBooleanProperty(effs.hasDoubleBasics());
        hasDoubleBasics.addListener((aObservable, aOld, aNew) -> effs.setDoubleBasics(aNew, aXBar));

        //
        // Toggles
        //
        hasLeftHA = makeToggle(aXBar, Location.LeftArm, ItemDB.HA, itemsChanged);
        hasLeftLAA = makeToggle(aXBar, Location.LeftArm, ItemDB.LAA, itemsChanged);
        hasRightHA = makeToggle(aXBar, Location.RightArm, ItemDB.HA, itemsChanged);
        hasRightLAA = makeToggle(aXBar, Location.RightArm, ItemDB.LAA, itemsChanged);

        // Globally available armor
        DoubleBinding freeArmorByMass = statsFreeMass.multiply(loadout.getUpgrades().getArmor().getArmorPerTon());
        if (null != hasFerroFibrous) {
            hasFerroFibrous.addListener((aObservable, aOld, aNew) -> {
                freeArmorByMass.invalidate();
            });
        }
        globalAvailableArmor = Bindings.min(freeArmorByMass, statsArmorFree);

        // Components
        Map<Location, ComponentModel> localComponents = new HashMap<>();
        for (Location location : Location.values()) {
            localComponents.put(location, new ComponentModel(aXBar, location, armorChanged, omniPodChanged));
        }
        components = Collections.unmodifiableMap(localComponents);
    }

    private LsmlIntegerBinding makeArmorBinding(MessageXBar aXBar, ArmorSide aArmorSide, Location location,
            Predicate<Message> armorChanged) {
        ConfiguredComponentBase component = loadout.getComponent(location);
        return new LsmlIntegerBinding(aXBar, () -> component.getArmor(aArmorSide),
                aMsg -> armorChanged.test(aMsg) && ((ArmorMessage) aMsg).component == component);
    }

    private LsmlIntegerBinding makeEffectiveArmorBinding(MessageXBar aXBar, ArmorSide aArmorSide, Location location,
            Predicate<Message> armorChanged) {
        ConfiguredComponentBase component = loadout.getComponent(location);
        return new LsmlIntegerBinding(aXBar, () -> component.getEffectiveArmor(aArmorSide, loadout.getModifiers()),
                aMsg -> armorChanged.test(aMsg) && ((ArmorMessage) aMsg).component == component);
    }

    private BooleanProperty makeToggle(MessageXBar aXBar, Location aLocation, Item aItem,
            Predicate<Message> aItemsChanged) {
        if (loadout instanceof LoadoutOmniMech) {
            LoadoutOmniMech loadoutOmni = (LoadoutOmniMech) loadout;
            ConfiguredComponentOmniMech component = loadoutOmni.getComponent(aLocation);
            if (component.getOmniPod().getToggleableItems().contains(aItem)) {
                return new LsmlBooleanProperty(aXBar, () -> component.getToggleState(aItem), (aNewValue) -> {
                    cmdStack.pushAndApply(new CmdToggleItem(aXBar, loadoutOmni, component, aItem, aNewValue));
                    return true;
                } , aItemsChanged);
            }
        }
        return null;
    }
}
