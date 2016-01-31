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
package org.lisoft.lsml.view_fx.style;

import java.text.DecimalFormat;
import java.util.Collection;

import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.TargetingComputer;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.view_fx.loadout.component.ComponentItemToolTip;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * This class can build tool tips for items accounting for loadout quirks.
 * 
 * @author Li Song
 */
public class ItemToolTipFormatter {

    private final ModifierFormatter modifierFormatter   = new ModifierFormatter();
    private DecimalFormat           df                  = new DecimalFormat("#.##");

    private VBox                    root                = new VBox();

    private Label                   descText            = new Label();
    private Region                  descSpacer          = new Region();

    private VBox                    noteBox             = new VBox();
    private Region                  noteSpacer          = new Region();
    private Label                   noteHeader          = new Label();
    private Label                   noteDpsJamProb      = new Label();
    private Label                   noteQuirky          = new Label();

    private HBox                    weaponBox           = new HBox();
    private VBox                    weaponBaseBox       = new VBox();
    private VBox                    weaponMetaBox       = new VBox();
    private Label                   weaponDamage        = new Label();
    private Label                   weaponHeat          = new Label();
    private Label                   weaponRange         = new Label();
    private Label                   weaponImpulse       = new Label();
    private Label                   weaponSpeed         = new Label();
    private Label                   weaponSpread        = new Label();
    private Label                   weaponCooldown      = new Label();
    private Label                   weaponBurnTime      = new Label();
    private Label                   weaponMaxFreeAlpha  = new Label();
    private Label                   weaponJamChance     = new Label();
    private Label                   weaponJamTime       = new Label();
    private Label                   weaponDps           = new Label();
    private Label                   weaponDph           = new Label();
    private Label                   weaponHps           = new Label();
    private Label                   weaponAmmoPerTon    = new Label();

    private Label                   heatSinkCooling     = new Label();
    private Label                   heatSinkCapacity    = new Label();

    private Label                   engineTopSpeed      = new Label();
    private Label                   engineInternalSinks = new Label();
    private Label                   engineExternalSinks = new Label();

    private VBox                    tcQuirkBox          = new VBox();
    private Tooltip                 tooltip             = new Tooltip();
    private ComponentItemToolTip    componentItemToolTip;

    public ItemToolTipFormatter() {
        componentItemToolTip = new ComponentItemToolTip();
        root.setPrefWidth(300);
        descText.setWrapText(true);
        descSpacer.setPrefHeight(10);

        noteHeader.setText("Notes:");
        noteSpacer.setPrefHeight(10);

        noteQuirky.setText("* All values shown are taking your quirks, modules and other equipment into account.");
        noteQuirky.setWrapText(true);
        noteQuirky.prefHeightProperty().bind(noteQuirky.minHeightProperty());

        noteDpsJamProb.setText("* The above take double fire into account and calculates a statistical average DPS.");
        noteDpsJamProb.setWrapText(true);

        weaponMetaBox.setAlignment(Pos.BASELINE_RIGHT);
        weaponBox.getChildren().setAll(weaponBaseBox, weaponMetaBox);
        tooltip.setAutoHide(false);
        tooltip.setAutoFix(true);
    }

    private void setText(Label aLabel, String aText, double aValue) {
        aLabel.setText(aText + df.format(aValue));
    }

    private void setText(Label aLabel, String aText, double aValue, String aNextText, double aNextValue) {
        aLabel.setText(aText + df.format(aValue) + aNextText + df.format(aNextValue));
    }

    public Tooltip format(Item aItem, LoadoutBase<?> aLoadout, Collection<Modifier> aModifiers) {
        descText.setText(aItem.getDescription());
        MovementProfile mp = aLoadout.getMovementProfile();

        if (aItem instanceof Weapon) {
            formatWeapon(aItem, aModifiers);
        }
        else if (aItem instanceof HeatSink) {
            HeatSink heatSink = (HeatSink) aItem;
            setText(heatSinkCooling, "Dissipation: ", heatSink.getDissipation());
            setText(heatSinkCapacity, "Capacity: ", heatSink.getCapacity());
            root.getChildren().setAll(descText, descSpacer, heatSinkCooling, heatSinkCapacity);
        }
        else if (aItem instanceof TargetingComputer) {
            TargetingComputer targetingComputer = (TargetingComputer) aItem;

            tcQuirkBox.getChildren().clear();
            modifierFormatter.format(targetingComputer.getModifiers(), tcQuirkBox.getChildren());

            root.getChildren().setAll(descText, descSpacer, tcQuirkBox);
        }
        else if (aItem instanceof Engine) {
            Engine engine = (Engine) aItem;

            setText(engineTopSpeed, "Top Speed: ",
                    TopSpeed.calculate(engine.getRating(), mp, aLoadout.getChassis().getMassMax(), aModifiers));
            setText(engineInternalSinks, "Internal Sinks: ", engine.getNumInternalHeatsinks());
            setText(engineExternalSinks, "Heat Sink Slots: ", engine.getNumHeatsinkSlots());

            root.getChildren().setAll(descText, descSpacer, engineTopSpeed, engineInternalSinks, engineExternalSinks);

        }
        else {
            root.getChildren().setAll(descText);
        }

        tooltip.setGraphic(new Group(root));
        return tooltip;
    }

    public Tooltip format(Item aItem, ConfiguredComponentBase aComponent, Collection<Modifier> aModifiers) {
        componentItemToolTip.update(aComponent, aItem, aModifiers);
        tooltip.setGraphic(new Group(componentItemToolTip));
        return tooltip;
    }

    private void formatWeapon(Item aItem, Collection<Modifier> aModifiers) {
        Weapon weapon = (Weapon) aItem;

        setText(weaponDamage, "Damage: ", weapon.getDamagePerShot());
        setText(weaponHeat, "Heat: ", weapon.getHeat(aModifiers));
        setText(weaponRange, "Range: ", weapon.getRangeLong(aModifiers), " / ", weapon.getRangeMax(aModifiers));
        setText(weaponCooldown, "Cooldown: ", weapon.getCoolDown(aModifiers));
        setText(weaponImpulse, "Impulse: ", weapon.getImpulse());
        setText(weaponSpeed, "Projectile Speed: ", weapon.getProjectileSpeed());

        weaponBaseBox.getChildren().setAll(weaponDamage, weaponHeat, weaponRange, weaponCooldown, weaponSpeed,
                weaponImpulse);

        setText(weaponDps, "Damage/Second: ", weapon.getStat("d/s", aModifiers));
        setText(weaponDph, "Damage/Heat: ", weapon.getStat("d/h", aModifiers));
        setText(weaponHps, "Heat/Second: ", weapon.getStat("h/s", aModifiers));
        weaponMetaBox.getChildren().setAll(weaponDps, weaponDph, weaponHps);

        boolean showNotes = aModifiers != null;
        noteBox.getChildren().setAll(noteSpacer);
        if (showNotes) {
            noteBox.getChildren().add(noteQuirky);
        }

        if (aItem instanceof AmmoWeapon) {
            AmmoWeapon ammoWeapon = (AmmoWeapon) aItem;
            Ammunition ammo = (Ammunition) ItemDB.lookup(ammoWeapon.getAmmoType());
            setText(weaponAmmoPerTon, "Ammo/Ton: ", ammo.getNumRounds());
            weaponMetaBox.getChildren().add(weaponAmmoPerTon);

            if (ammoWeapon.hasSpread()) {
                setText(weaponSpread, "Spread σ°:", ammoWeapon.getSpread(aModifiers));
                weaponBaseBox.getChildren().add(weaponSpread);
            }
        }

        if (aItem instanceof EnergyWeapon) {
            EnergyWeapon energyWeapon = (EnergyWeapon) aItem;
            double burn = energyWeapon.getDuration(aModifiers);
            if (burn > 0) {
                setText(weaponBurnTime, "Burn time: ", burn);
                weaponBaseBox.getChildren().add(weaponBurnTime);
            }
        }
        else if (aItem instanceof BallisticWeapon) {
            BallisticWeapon ballistic = (BallisticWeapon) aItem;
            double jamProb = ballistic.getJamProbability(aModifiers);
            if (jamProb > 0) {
                setText(weaponJamChance, "Jam chance: ", jamProb);
                setText(weaponJamTime, "Jam time: ", ballistic.getJamTime(aModifiers));
                weaponBaseBox.getChildren().addAll(weaponJamChance, weaponJamTime);
                showNotes = true;
                noteBox.getChildren().add(noteDpsJamProb);

            }
        }

        int freeAlpha = weapon.getGhostHeatMaxFreeAlpha();
        if (freeAlpha > 0) {
            setText(weaponMaxFreeAlpha, "Ghost heat after: ", freeAlpha);
            weaponMetaBox.getChildren().add(weaponMaxFreeAlpha);
        }

        root.getChildren().setAll(descText, descSpacer, weaponBox);
        if (showNotes)
            root.getChildren().add(noteBox);
    }
}
