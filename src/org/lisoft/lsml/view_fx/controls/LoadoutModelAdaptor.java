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
package org.lisoft.lsml.view_fx.controls;

import java.util.HashMap;
import java.util.Map;

import org.lisoft.lsml.command.CmdSetArmorType;
import org.lisoft.lsml.command.CmdSetGuidanceType;
import org.lisoft.lsml.command.CmdSetHeatSinkType;
import org.lisoft.lsml.command.CmdSetStructureType;
import org.lisoft.lsml.messages.ComponentMessage;
import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.ItemMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.model.datacache.UpgradeDB;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.upgrades.ArmorUpgrade;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.util.CommandStack;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Callback;

/**
 * This class adapts a {@link LoadoutBase} for suitability to use with JavaFX bindings type APIs.
 * 
 * @author Li Song
 */
public class LoadoutModelAdaptor {
    public final LoadoutBase<?>                           loadout;
    public final LoadoutMetrics                           metrics;
    private final CommandStack                            cmdStack;
    public final DoubleBinding                            statsMass;
    public final IntegerBinding                           statsArmor;
    public final IntegerBinding                           statsSlots;
    public final ObjectBinding<StructureUpgrade>          upgradeStructure;
    public final ObjectBinding<HeatSinkUpgrade>           upgradeHeatSinks;
    public final ObjectBinding<ArmorUpgrade>              upgradeArmor;
    public final ObjectBinding<GuidanceUpgrade>           upgradeGuidance;

    public final BooleanProperty                          hasEndoSteel;
    public final BooleanProperty                          hasDoubleHeatSinks;
    public final BooleanProperty                          hasFerroFibrous;
    public final BooleanProperty                          hasArtemis;

    public final Map<MechEfficiencyType, BooleanBinding>  effMap;
    public final Map<MechEfficiencyType, BooleanProperty> hasEffMap;
    public final BooleanBinding                           effDoubleBasics;
    public final BooleanProperty                          hasDoubleBasics;

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
        Callback<Message, Boolean> armorChanged = (aMsg) -> aMsg instanceof ComponentMessage;
        Callback<Message, Boolean> itemsChanged = (aMsg) -> aMsg instanceof ItemMessage;
        Callback<Message, Boolean> upgradesChanged = (aMsg) -> aMsg instanceof UpgradesMessage;
        Callback<Message, Boolean> effsChanged = (aMsg) -> aMsg instanceof EfficienciesMessage;
        Callback<Message, Boolean> slotsChanged = (aMsg) -> itemsChanged.call(aMsg) || upgradesChanged.call(aMsg);
        Callback<Message, Boolean> massChanged = (aMsg) -> armorChanged.call(aMsg) || slotsChanged.call(aMsg);

        statsMass = new LoadoutDoubleBinding(aXBar, () -> loadout.getMass(), massChanged);
        statsArmor = new LoadoutIntegerBinding(aXBar, () -> loadout.getArmor(), armorChanged);
        statsSlots = new LoadoutIntegerBinding(aXBar, () -> loadout.getNumCriticalSlotsUsed(), slotsChanged);

        upgradeStructure = new LoadoutObjectBinding<>(aXBar, () -> upgrades.getStructure(), upgradesChanged);
        upgradeHeatSinks = new LoadoutObjectBinding<>(aXBar, () -> upgrades.getHeatSink(), upgradesChanged);
        upgradeArmor = new LoadoutObjectBinding<>(aXBar, () -> upgrades.getArmor(), upgradesChanged);
        upgradeGuidance = new LoadoutObjectBinding<>(aXBar, () -> upgrades.getGuidance(), upgradesChanged);

        hasEndoSteel = new SimpleBooleanProperty(structureES == upgrades.getStructure());
        hasDoubleHeatSinks = new SimpleBooleanProperty(hsDHS == upgrades.getHeatSink());
        hasFerroFibrous = new SimpleBooleanProperty(armorFF == upgrades.getArmor());
        hasArtemis = new SimpleBooleanProperty(UpgradeDB.ARTEMIS_IV == upgrades.getGuidance());

        if (loadout instanceof LoadoutStandard) {
            LoadoutStandard loadoutStandard = (LoadoutStandard) loadout;

            hasEndoSteel.addListener(new SafeBooleanPropertyChangeListener((aValue) -> {
                StructureUpgrade structure = aValue ? structureES : structureSTD;
                if (structure != upgrades.getStructure()) {
                    cmdStack.pushAndApply(new CmdSetStructureType(aXBar, loadoutStandard, structure));
                }
            }));

            hasDoubleHeatSinks.addListener(new SafeBooleanPropertyChangeListener((aValue) -> {
                HeatSinkUpgrade heatSinks = aValue ? hsDHS : hsSTD;
                if (heatSinks != upgrades.getHeatSink()) {
                    cmdStack.pushAndApply(new CmdSetHeatSinkType(aXBar, loadoutStandard, heatSinks));
                }
            }));

            hasFerroFibrous.addListener(new SafeBooleanPropertyChangeListener((aValue) -> {
                ArmorUpgrade armor = aValue ? armorFF : armorSTD;
                if (armor != upgrades.getArmor()) {
                    cmdStack.pushAndApply(new CmdSetArmorType(aXBar, loadoutStandard, armor));
                }
            }));
        }

        hasArtemis.addListener(new SafeBooleanPropertyChangeListener((aValue) -> {
            GuidanceUpgrade guidance = aValue ? UpgradeDB.ARTEMIS_IV : UpgradeDB.STD_GUIDANCE;
            if (guidance != upgrades.getGuidance()) {
                cmdStack.pushAndApply(new CmdSetGuidanceType(aXBar, loadout, guidance));
            }
        }));

        Efficiencies loadoutEffs = loadout.getEfficiencies();
        effMap = new HashMap<>();
        hasEffMap = new HashMap<>();
        for (MechEfficiencyType type : MechEfficiencyType.values())

        {
            BooleanBinding binding = new LoadoutBooleanBinding(aXBar, () -> loadoutEffs.hasEfficiency(type),
                    effsChanged);
            effMap.put(type, binding);

            BooleanProperty property = new SimpleBooleanProperty(binding.get());
            property.addListener((aObservable, aOld, aNew) -> loadoutEffs.setEfficiency(type, aNew, aXBar));
            hasEffMap.put(type, property);
        }

        effDoubleBasics = new LoadoutBooleanBinding(aXBar, () -> loadoutEffs.hasDoubleBasics(), effsChanged);
        hasDoubleBasics = new SimpleBooleanProperty(loadoutEffs.hasDoubleBasics());
        hasDoubleBasics.addListener((aObservable, aOld, aNew) -> loadoutEffs.setDoubleBasics(aNew, aXBar));
    }
}
