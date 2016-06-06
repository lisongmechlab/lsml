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
package org.lisoft.lsml.view_fx.properties;

import static javafx.beans.binding.Bindings.subtract;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.lisoft.lsml.messages.ArmourMessage;
import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.OmniPodMessage;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.chassi.Component;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutOmniMech;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.NumberExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * This class adapts a {@link Loadout} for suitability to use with JavaFX bindings type APIs.
 *
 * @author Li Song
 */
public class LoadoutModelAdaptor {

    public class ComponentModel {
        public final DoubleBinding health;
        public final DoubleBinding healthEff;
        public final IntegerBinding armour;
        public final IntegerBinding armourBack;
        public final IntegerBinding armourEff;
        public final IntegerBinding armourEffBack;
        public final NumberExpression armourMax;
        public final NumberExpression armourMaxBack;

        public ComponentModel(MessageXBar aXBar, Location aLocation, Predicate<Message> aArmourChanged,
                Predicate<Message> aQuirksChanged) {
            final Component internalComponent = loadout.getComponent(aLocation).getInternalComponent();
            final int localMaxArmour = internalComponent.getArmourMax();

            health = new LsmlDoubleBinding(aXBar, () -> internalComponent.getHitPoints(null), aQuirksChanged);
            healthEff = new LsmlDoubleBinding(aXBar, () -> internalComponent.getHitPoints(loadout.getModifiers()),
                    aQuirksChanged);
            if (aLocation.isTwoSided()) {
                armour = makeArmourBinding(aXBar, ArmourSide.FRONT, aLocation, aArmourChanged);
                armourEff = makeEffectiveArmourBinding(aXBar, ArmourSide.FRONT, aLocation, aArmourChanged);
                armourBack = makeArmourBinding(aXBar, ArmourSide.BACK, aLocation, aArmourChanged);
                armourEffBack = makeEffectiveArmourBinding(aXBar, ArmourSide.BACK, aLocation, aArmourChanged);

                armourMax = subtract(localMaxArmour, armourBack);
                armourMaxBack = subtract(localMaxArmour, armour);
            }
            else {
                armour = makeArmourBinding(aXBar, ArmourSide.ONLY, aLocation, aArmourChanged);
                armourEff = makeEffectiveArmourBinding(aXBar, ArmourSide.ONLY, aLocation, aArmourChanged);
                armourMax = new ReadOnlyIntegerWrapper(localMaxArmour);
                armourBack = null;
                armourEffBack = null;
                armourMaxBack = null;
            }
        }
    }

    // Armour
    public final Map<Location, ComponentModel> components;
    public final NumberBinding globalAvailableArmour;
    public final BooleanBinding hasArtemis;
    public final BooleanProperty hasDoubleBasics;
    public final BooleanBinding hasDoubleHeatSinks;
    public final Map<MechEfficiencyType, BooleanBinding> hasEfficiency;
    public final BooleanBinding hasEndoSteel;
    public final BooleanBinding hasFerroFibrous;

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

    public LoadoutModelAdaptor(Loadout aLoadout, MessageXBar aXBar) {
        loadout = aLoadout;
        final Faction faction = loadout.getChassis().getFaction();
        final Upgrades upgrades = loadout.getUpgrades();
        final StructureUpgrade structureES = UpgradeDB.getStructure(faction, true);
        final ArmourUpgrade armourFF = UpgradeDB.getArmour(faction, true);
        final HeatSinkUpgrade hsDHS = UpgradeDB.getHeatSinks(faction, true);
        final Predicate<Message> armourChanged = (aMsg) -> aMsg instanceof ArmourMessage;
        final Predicate<Message> itemsChanged = (aMsg) -> aMsg instanceof ItemMessage;
        final Predicate<Message> upgradesChanged = (aMsg) -> aMsg instanceof UpgradesMessage;
        final Predicate<Message> effsChanged = (aMsg) -> aMsg instanceof EfficienciesMessage;
        final Predicate<Message> omniPodChanged = (aMsg) -> aMsg instanceof OmniPodMessage;
        final Predicate<Message> slotsChanged = (aMsg) -> itemsChanged.test(aMsg) || upgradesChanged.test(aMsg);
        final Predicate<Message> massChanged = (aMsg) -> armourChanged.test(aMsg) || slotsChanged.test(aMsg)
                || omniPodChanged.test(aMsg);

        //
        // General
        //
        statsMass = new LsmlDoubleBinding(aXBar, () -> loadout.getMass(), massChanged);
        statsFreeMass = statsMass.negate().add(loadout.getChassis().getMassMax());
        statsArmour = new LsmlIntegerBinding(aXBar, () -> loadout.getArmour(), armourChanged);
        statsArmourFree = statsArmour.negate().add(loadout.getChassis().getArmourMax());
        statsSlots = new LsmlIntegerBinding(aXBar, () -> loadout.getSlotsUsed(), slotsChanged);

        //
        // Upgrades
        //
        hasEndoSteel = new LsmlBooleanBinding(aXBar, () -> structureES == upgrades.getStructure(), upgradesChanged);
        hasDoubleHeatSinks = new LsmlBooleanBinding(aXBar, () -> hsDHS == upgrades.getHeatSink(), upgradesChanged);
        hasFerroFibrous = new LsmlBooleanBinding(aXBar, () -> armourFF == upgrades.getArmour(), upgradesChanged);
        hasArtemis = new LsmlBooleanBinding(aXBar, () -> UpgradeDB.ARTEMIS_IV == upgrades.getGuidance(),
                upgradesChanged);

        //
        // Efficiencies
        //
        final Efficiencies effs = loadout.getEfficiencies();
        final Map<MechEfficiencyType, BooleanBinding> localHasEffMap = new HashMap<>();
        for (final MechEfficiencyType type : MechEfficiencyType.values()) {
            localHasEffMap.put(type, new LsmlBooleanBinding(aXBar, () -> effs.hasEfficiency(type), effsChanged));
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

        // Globally available armour
        final DoubleBinding freeArmourByMass = statsFreeMass
                .multiply(loadout.getUpgrades().getArmour().getArmourPerTon());
        if (null != hasFerroFibrous) {
            hasFerroFibrous.addListener((aObservable, aOld, aNew) -> {
                freeArmourByMass.invalidate();
            });
        }
        globalAvailableArmour = Bindings.min(freeArmourByMass, statsArmourFree);

        // Components
        final Map<Location, ComponentModel> localComponents = new HashMap<>();
        for (final Location location : Location.values()) {
            localComponents.put(location, new ComponentModel(aXBar, location, armourChanged, omniPodChanged));
        }
        components = Collections.unmodifiableMap(localComponents);
    }

    private LsmlIntegerBinding makeArmourBinding(MessageXBar aXBar, ArmourSide aArmourSide, Location location,
            Predicate<Message> armourChanged) {
        final ConfiguredComponent component = loadout.getComponent(location);
        return new LsmlIntegerBinding(aXBar, () -> component.getArmour(aArmourSide),
                aMsg -> armourChanged.test(aMsg) && ((ArmourMessage) aMsg).component == component);
    }

    private LsmlIntegerBinding makeEffectiveArmourBinding(MessageXBar aXBar, ArmourSide aArmourSide, Location location,
            Predicate<Message> armourChanged) {
        final ConfiguredComponent component = loadout.getComponent(location);
        return new LsmlIntegerBinding(aXBar, () -> component.getEffectiveArmour(aArmourSide, loadout.getModifiers()),
                aMsg -> armourChanged.test(aMsg) && ((ArmourMessage) aMsg).component == component);
    }

    private BooleanBinding makeToggle(MessageXBar aXBar, Location aLocation, Item aItem,
            Predicate<Message> aItemsChanged) {
        if (loadout instanceof LoadoutOmniMech) {
            final LoadoutOmniMech loadoutOmni = (LoadoutOmniMech) loadout;
            final ConfiguredComponentOmniMech component = loadoutOmni.getComponent(aLocation);
            if (component.getOmniPod().getToggleableItems().contains(aItem)) {
                return new LsmlBooleanBinding(aXBar, () -> component.getToggleState(aItem), aItemsChanged);
            }
        }
        return null;
    }
}
