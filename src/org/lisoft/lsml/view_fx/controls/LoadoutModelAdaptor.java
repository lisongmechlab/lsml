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
package org.lisoft.lsml.view_fx.controls;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.lisoft.lsml.command.CmdSetArmor;
import org.lisoft.lsml.command.CmdSetArmorType;
import org.lisoft.lsml.command.CmdSetGuidanceType;
import org.lisoft.lsml.command.CmdSetHeatSinkType;
import org.lisoft.lsml.command.CmdSetStructureType;
import org.lisoft.lsml.command.CmdToggleItem;
import org.lisoft.lsml.messages.ComponentMessage;
import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.model.chassi.ArmorSide;
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

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Pair;

/**
 * This class adapts a {@link LoadoutBase} for suitability to use with JavaFX bindings type APIs.
 * 
 * @author Emily Björk
 */
public class LoadoutModelAdaptor {

    static public class ComponentModel {
        IntegerProperty armor;
        IntegerProperty armorMax;
    }

    public final LoadoutBase<?>                                                         loadout;
    public final LoadoutMetrics                                                         metrics;
    private final CommandStack                                                          cmdStack;

    // General
    public final DoubleBinding                                                          statsMass;
    public final IntegerBinding                                                         statsArmor;
    public final IntegerBinding                                                         statsSlots;

    // Upgrades
    public final BooleanProperty                                                        hasEndoSteel;
    public final BooleanProperty                                                        hasDoubleHeatSinks;
    public final BooleanProperty                                                        hasFerroFibrous;
    public final BooleanProperty                                                        hasArtemis;

    // Efficiencies
    public final Map<MechEfficiencyType, BooleanProperty>                               hasEfficiency;
    public final BooleanProperty                                                        hasDoubleBasics;

    // Toggles
    public final BooleanProperty                                                        hasLeftHA;
    public final BooleanProperty                                                        hasRightHA;
    public final BooleanProperty                                                        hasLeftLAA;
    public final BooleanProperty                                                        hasRightLAA;

    // Armor
    public final Map<Pair<Location, ArmorSide>, Pair<IntegerProperty, IntegerProperty>> armor;
    public final IntegerProperty                                                        totalArmorMax;

    public LoadoutModelAdaptor(LoadoutBase<?> aLoadout, MessageXBar aXBar, CommandStack aCmdStack) {
        cmdStack = aCmdStack;
        loadout = aLoadout;
        metrics = new LoadoutMetrics(loadout, null, aXBar);
        boolean isClan = loadout.getChassis().getFaction() == Faction.Clan;
        Upgrades upgrades = loadout.getUpgrades();
        StructureUpgrade structureES = isClan ? UpgradeDB.CLAN_ES_STRUCTURE : UpgradeDB.IS_ES_STRUCTURE;
        StructureUpgrade structureSTD = isClan ? UpgradeDB.CLAN_STD_STRUCTURE : UpgradeDB.IS_STD_STRUCTURE;
        ArmorUpgrade armorFF = isClan ? UpgradeDB.CLAN_FF_ARMOR : UpgradeDB.IS_FF_ARMOR;
        ArmorUpgrade armorSTD = isClan ? UpgradeDB.CLAN_STD_ARMOR : UpgradeDB.IS_STD_ARMOR;
        HeatSinkUpgrade hsDHS = isClan ? UpgradeDB.CLAN_DHS : UpgradeDB.IS_DHS;
        HeatSinkUpgrade hsSTD = isClan ? UpgradeDB.CLAN_SHS : UpgradeDB.IS_SHS;
        Predicate<Message> armorChanged = (aMsg) -> aMsg instanceof ComponentMessage;
        Predicate<Message> itemsChanged = (aMsg) -> aMsg instanceof ItemMessage;
        Predicate<Message> upgradesChanged = (aMsg) -> aMsg instanceof UpgradesMessage;
        Predicate<Message> effsChanged = (aMsg) -> aMsg instanceof EfficienciesMessage;
        Predicate<Message> slotsChanged = (aMsg) -> itemsChanged.test(aMsg) || upgradesChanged.test(aMsg);
        Predicate<Message> massChanged = (aMsg) -> armorChanged.test(aMsg) || slotsChanged.test(aMsg);

        //
        // General
        //
        statsMass = new LsmlDoubleBinding(aXBar, () -> loadout.getMass(), massChanged);
        statsArmor = new LsmlIntegerBinding(aXBar, () -> loadout.getArmor(), armorChanged);
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

        // Armor
        Map<Pair<Location, ArmorSide>, IntegerProperty> localArmor = new HashMap<>();
        for (Location location : Location.values()) {
            if (location.isTwoSided()) {
                localArmor.put(new Pair<>(location, ArmorSide.FRONT),
                        makeArmorProperty(aXBar, ArmorSide.FRONT, location, armorChanged));
                localArmor.put(new Pair<>(location, ArmorSide.BACK),
                        makeArmorProperty(aXBar, ArmorSide.BACK, location, armorChanged));
            }
            else {
                localArmor.put(new Pair<>(location, ArmorSide.ONLY),
                        makeArmorProperty(aXBar, ArmorSide.ONLY, location, armorChanged));
            }
        }
        armor = Collections.unmodifiableMap(localArmor);
    }

    private LsmlIntegerProperty makeArmorProperty(MessageXBar aXBar, ArmorSide aArmorSide, Location location,
            Predicate<Message> armorChanged) {
        ConfiguredComponentBase component = loadout.getComponent(location);

        return new LsmlIntegerProperty(aXBar, () -> component.getArmor(aArmorSide), (aValue) -> {
            cmdStack.pushAndApply(new CmdSetArmor(aXBar, loadout, component, aArmorSide, aValue.intValue(), true));
            return true;
        } , armorChanged);
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
