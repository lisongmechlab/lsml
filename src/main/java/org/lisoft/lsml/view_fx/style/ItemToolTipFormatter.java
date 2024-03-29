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
package org.lisoft.lsml.view_fx.style;

import java.text.DecimalFormat;
import java.util.Collection;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javax.inject.Inject;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.view_fx.controllers.loadoutwindow.ComponentItemToolTipController;
import org.lisoft.mwo_data.equipment.*;
import org.lisoft.mwo_data.mechs.MovementProfile;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * This class can build tool tips for items accounting for loadout quirks.
 *
 * @author Li Song
 */
public class ItemToolTipFormatter {

  private final ComponentItemToolTipController componentItemToolTip;
  private final Region descSpacer = new Region();
  private final Label descText = new Label();
  private final DecimalFormat df = new DecimalFormat("#.##");
  private final Label engineExternalSinks = new Label();
  private final Label engineInternalSinks = new Label();
  private final Label engineTopSpeed = new Label();
  private final Label heatSinkCapacity = new Label();
  private final Label heatSinkCooling = new Label();
  private final ModifierFormatter modifierFormatter;
  private final VBox noteBox = new VBox();
  private final Label noteDpsJamProb = new Label();
  private final Label noteQuirky = new Label();
  private final Region noteSpacer = new Region();
  private final VBox root = new VBox();
  private final VBox tcQuirkBox = new VBox();
  private final Tooltip tooltip = new Tooltip();
  private final Label weaponAmmoPerTon = new Label();
  private final VBox weaponBaseBox = new VBox();
  private final HBox weaponBox = new HBox();
  private final Label weaponBurnTime = new Label();
  private final Label weaponCoolDown = new Label();
  private final Label weaponDamage = new Label();
  private final Label weaponDph = new Label();
  private final Label weaponDps = new Label();
  private final Label weaponHeat = new Label();
  private final Label weaponHps = new Label();
  private final Label weaponImpulse = new Label();
  private final Label weaponJamChance = new Label();
  private final Label weaponJamTime = new Label();
  private final Label weaponMaxFreeAlpha = new Label();
  private final VBox weaponMetaBox = new VBox();
  private final Label weaponRange = new Label();
  private final Label weaponSpeed = new Label();
  private final Label weaponSpread = new Label();

  private static final Duration TOOLTIP_DURATION = Duration.INDEFINITE;

  @Inject
  public ItemToolTipFormatter() {
    // FIXME Inject these
    modifierFormatter = new ModifierFormatter();
    componentItemToolTip = new ComponentItemToolTipController();
    root.setPrefWidth(300);
    descText.setWrapText(true);
    descSpacer.setPrefHeight(10);

    Label noteHeader = new Label();
    noteHeader.setText("Notes:");
    noteSpacer.setPrefHeight(10);

    noteQuirky.setText(
        "* All values shown are taking your quirks, modules and other equipment into account.");
    noteQuirky.setWrapText(true);
    noteQuirky.prefHeightProperty().bind(noteQuirky.minHeightProperty());

    noteDpsJamProb.setText(
        "* The above take double fire into account and calculates a statistical average DPS.");
    noteDpsJamProb.setWrapText(true);

    weaponMetaBox.setAlignment(Pos.BASELINE_RIGHT);
    weaponBox.getChildren().setAll(weaponBaseBox, weaponMetaBox);
    tooltip.setAutoHide(false);
    tooltip.setAutoFix(true);
  }

  public Tooltip format(
      Item aItem, ConfiguredComponent aComponent, Collection<Modifier> aModifiers) {
    componentItemToolTip.update(aComponent, aItem, aModifiers);
    tooltip.setGraphic(new Group(componentItemToolTip.getView()));
    tooltip.setShowDuration(TOOLTIP_DURATION);
    return tooltip;
  }

  public Tooltip format(MwoObject aItem, Loadout aLoadout, Collection<Modifier> aModifiers) {
    descText.setText(aItem.getDescription());
    final MovementProfile mp = aLoadout.getMovementProfile();

    if (aItem instanceof Weapon) {
      formatWeapon((Item) aItem, aModifiers);
    } else if (aItem instanceof final HeatSink heatSink) {
      setText(heatSinkCooling, "Dissipation: ", heatSink.getDissipation());
      setText(heatSinkCapacity, "Capacity: ", heatSink.getCapacity());
      root.getChildren().setAll(descText, descSpacer, heatSinkCooling, heatSinkCapacity);
    } else if (aItem instanceof final TargetingComputer targetingComputer) {

      tcQuirkBox.getChildren().clear();
      modifierFormatter.format(targetingComputer.getModifiers(), tcQuirkBox.getChildren());

      root.getChildren().setAll(descText, descSpacer, tcQuirkBox);
    } else if (aItem instanceof final Engine engine) {

      setText(
          engineTopSpeed,
          "Top Speed: ",
          TopSpeed.calculate(
              engine.getRating(), mp, aLoadout.getChassis().getMassMax(), aModifiers));
      setText(engineInternalSinks, "Internal Sinks: ", engine.getNumInternalHeatsinks());
      setText(engineExternalSinks, "Heat Sink Slots: ", engine.getNumHeatsinkSlots());

      root.getChildren()
          .setAll(descText, descSpacer, engineTopSpeed, engineInternalSinks, engineExternalSinks);
    } else {
      root.getChildren().setAll(descText);
    }

    tooltip.setGraphic(new Group(root));
    tooltip.setShowDuration(TOOLTIP_DURATION);
    return tooltip;
  }

  private void formatWeapon(Item aItem, Collection<Modifier> aModifiers) {
    final Weapon weapon = (Weapon) aItem;

    final WeaponRangeProfile.Range range =
        weapon.getRangeProfile().getPercentileRange(0.9, aModifiers);

    setText(weaponDamage, "Damage: ", weapon.getDamagePerShot());
    setText(weaponHeat, "Heat: ", weapon.getHeat(aModifiers));
    setText(weaponRange, "90% Range: ", range.minimum, " / ", range.maximum);
    setText(weaponCoolDown, "Cool down: ", weapon.getCoolDown(aModifiers));
    setText(weaponImpulse, "Impulse: ", weapon.getImpulse());
    setText(weaponSpeed, "Projectile Speed: ", weapon.getProjectileSpeed(aModifiers));

    weaponBaseBox
        .getChildren()
        .setAll(weaponDamage, weaponHeat, weaponRange, weaponCoolDown, weaponSpeed, weaponImpulse);

    setText(weaponDps, "Damage/Second: ", weapon.getStat("d/s", aModifiers));
    setText(weaponDph, "Damage/Heat: ", weapon.getStat("d/h", aModifiers));
    setText(weaponHps, "Heat/Second: ", weapon.getStat("h/s", aModifiers));
    weaponMetaBox.getChildren().setAll(weaponDps, weaponDph, weaponHps);

    boolean showNotes = aModifiers != null;
    noteBox.getChildren().setAll(noteSpacer);
    if (showNotes) {
      noteBox.getChildren().add(noteQuirky);
    }

    if (aItem instanceof final AmmoWeapon ammoWeapon) {
      if (!ammoWeapon.hasBuiltInAmmo()) {
        final Ammunition ammo = ammoWeapon.getAmmoType();
        setText(weaponAmmoPerTon, "Ammo/Ton: ", ammo.getNumRounds(aModifiers));
        weaponMetaBox.getChildren().add(weaponAmmoPerTon);
      } else {
        // FIXME: Add info about how many shots are built in.
      }

      if (ammoWeapon.hasSpread()) {
        setText(
            weaponSpread, "Spread σ°:", ammoWeapon.getRangeProfile().getSpread().value(aModifiers));
        weaponBaseBox.getChildren().add(weaponSpread);
      }
    }

    if (aItem instanceof final EnergyWeapon energyWeapon) {
      final double burn = energyWeapon.getDuration(aModifiers);
      if (burn > 0) {
        setText(weaponBurnTime, "Burn time: ", burn);
        weaponBaseBox.getChildren().add(weaponBurnTime);
      }
    } else if (aItem instanceof final BallisticWeapon ballistic) {
      final double jamProb = ballistic.getJamProbability(aModifiers);
      if (jamProb > 0) {
        setText(weaponJamChance, "Jam chance: ", jamProb);
        setText(weaponJamTime, "Jam time: ", ballistic.getJamTime(aModifiers));
        weaponBaseBox.getChildren().addAll(weaponJamChance, weaponJamTime);
        showNotes = true;
        noteBox.getChildren().add(noteDpsJamProb);
      }
    }

    final int freeAlpha = weapon.getGhostHeatMaxFreeAlpha(aModifiers);
    if (freeAlpha > 0) {
      setText(weaponMaxFreeAlpha, "Ghost heat after: ", freeAlpha);
      weaponMetaBox.getChildren().add(weaponMaxFreeAlpha);
    }

    root.getChildren().setAll(descText, descSpacer, weaponBox);
    if (showNotes) {
      root.getChildren().add(noteBox);
    }
  }

  private void setText(Label aLabel, String aText, double aValue) {
    aLabel.setText(aText + df.format(aValue));
  }

  private void setText(
      Label aLabel, String aText, double aValue, String aNextText, double aNextValue) {
    aLabel.setText(aText + df.format(aValue) + aNextText + df.format(aNextValue));
  }
}
