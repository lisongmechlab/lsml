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
package org.lisoft.lsml.model.export;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.command.*;
import org.lisoft.lsml.model.ChassisDB;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.lsml.model.OmniPodDB;
import org.lisoft.lsml.model.UpgradeDB;
import org.lisoft.lsml.model.loadout.*;
import org.lisoft.lsml.util.DecodingException;
import org.lisoft.mwo_data.equipment.GuidanceUpgrade;
import org.lisoft.mwo_data.equipment.Internal;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.NoSuchItemException;
import org.lisoft.mwo_data.equipment.Upgrade;
import org.lisoft.mwo_data.mechs.ArmourSide;
import org.lisoft.mwo_data.mechs.Chassis;
import org.lisoft.mwo_data.mechs.Location;
import org.lisoft.mwo_data.mechs.OmniPod;
import org.lisoft.mwo_data.mechs.Upgrades;

/**
 * Implements the string encoded export format in MWO.
 *
 * @author Li Song
 */
public class MWOCoder {
  private static final int ACTUATOR_STATE_L_HA_REMOVED = 4;
  private static final int ACTUATOR_STATE_L_LAA_REMOVED = 8;
  private static final int ACTUATOR_STATE_R_HA_REMOVED = 1;
  private static final int ACTUATOR_STATE_R_LAA_REMOVED = 2;
  private static final Map<Location, Character> COMPONENT_TERMINATORS;
  private static final char ITEM_SEPARATOR = '|';
  private static final char MAGIC_V1 = 'A';
  private static final int MIN_LENGTH = 36;
  private static final int UPGRADE_CLAN_DHS = 2;
  private static final int UPGRADE_CLAN_ES_STRUCTURE = 2;
  private static final int UPGRADE_CLAN_FF_ARMOUR = 4;

  // Note: These values seem to match with the contents in
  // "GameData.pak/Libs/Items/Armour/ArmourTypes.xml"
  // However the list in that file is incomplete (Clan STD armour is missing), there is no direct
  // link
  // to the values in "GameData.pak/Libs/Items/UpgradeTypes/UpgradeTypes.xml" other than appending
  // "ArmorType"
  // at the end of the name string in ArmourTypes.xml.
  // Also matching files for the structure types are missing which leads me to believe that this
  // file is not
  // intended as a mapping for these values to their actual item IDs.
  private static final int UPGRADE_CLAN_SHS = 3;
  private static final int UPGRADE_CLAN_STD_ARMOUR = 5;
  private static final int UPGRADE_CLAN_STD_STRUCTURE = 3;
  private static final int UPGRADE_IS_DHS = 1;
  private static final int UPGRADE_IS_ES_STRUCTURE = 1;
  private static final int UPGRADE_IS_FF_ARMOUR = 1;
  private static final int UPGRADE_IS_LIGHT_FF_ARMOUR = 2;
  private static final int UPGRADE_IS_SHS = 0;
  private static final int UPGRADE_IS_STD_ARMOUR = 0;
  private static final int UPGRADE_IS_STD_STRUCTURE = 0;
  private static final int UPGRADE_IS_STEALTH_ARMOUR = 3;
  private static final int UPGRADE_OMNIMECH_BIT = 8;
  private static final Map<Upgrade, Integer> UPGRADE_TO_BITS;

  static {
    COMPONENT_TERMINATORS = new HashMap<>();
    COMPONENT_TERMINATORS.put(Location.CenterTorso, 'p');
    COMPONENT_TERMINATORS.put(Location.RightTorso, 'q');
    COMPONENT_TERMINATORS.put(Location.LeftTorso, 'r');
    COMPONENT_TERMINATORS.put(Location.LeftArm, 's');
    COMPONENT_TERMINATORS.put(Location.RightArm, 't');
    COMPONENT_TERMINATORS.put(Location.LeftLeg, 'u');
    COMPONENT_TERMINATORS.put(Location.RightLeg, 'v');
    COMPONENT_TERMINATORS.put(Location.Head, 'w');

    UPGRADE_TO_BITS = new HashMap<>();

    UPGRADE_TO_BITS.put(UpgradeDB.STD_GUIDANCE, 0);
    UPGRADE_TO_BITS.put(UpgradeDB.ARTEMIS_IV, 1);
    UPGRADE_TO_BITS.put(UpgradeDB.IS_SHS, UPGRADE_IS_SHS);
    UPGRADE_TO_BITS.put(UpgradeDB.IS_DHS, UPGRADE_IS_DHS);
    UPGRADE_TO_BITS.put(UpgradeDB.CLAN_DHS, UPGRADE_CLAN_DHS);
    UPGRADE_TO_BITS.put(UpgradeDB.CLAN_SHS, UPGRADE_CLAN_SHS);
    UPGRADE_TO_BITS.put(UpgradeDB.IS_STD_ARMOUR, UPGRADE_IS_STD_ARMOUR);
    UPGRADE_TO_BITS.put(UpgradeDB.IS_FF_ARMOUR, UPGRADE_IS_FF_ARMOUR);
    UPGRADE_TO_BITS.put(UpgradeDB.IS_LIGHT_FF_ARMOUR, UPGRADE_IS_LIGHT_FF_ARMOUR);
    UPGRADE_TO_BITS.put(UpgradeDB.IS_STEALTH_ARMOUR, UPGRADE_IS_STEALTH_ARMOUR);
    UPGRADE_TO_BITS.put(UpgradeDB.CLAN_FF_ARMOUR, UPGRADE_CLAN_FF_ARMOUR);
    UPGRADE_TO_BITS.put(UpgradeDB.CLAN_STD_ARMOUR, UPGRADE_CLAN_STD_ARMOUR);
    UPGRADE_TO_BITS.put(UpgradeDB.IS_STD_STRUCTURE, UPGRADE_IS_STD_STRUCTURE);
    UPGRADE_TO_BITS.put(UpgradeDB.IS_ES_STRUCTURE, UPGRADE_IS_ES_STRUCTURE);
    UPGRADE_TO_BITS.put(UpgradeDB.CLAN_ES_STRUCTURE, UPGRADE_CLAN_ES_STRUCTURE);
    UPGRADE_TO_BITS.put(UpgradeDB.CLAN_STD_STRUCTURE, UPGRADE_CLAN_STD_STRUCTURE);
  }

  private final BasePGICoder baseCoder;
  private final ErrorReporter errorReporter;
  private final LoadoutFactory loadoutFactory;

  /**
   * @param aBaseCoder
   * @param aLoadoutFactory
   * @param aErrorReporter
   */
  @Inject
  public MWOCoder(
      BasePGICoder aBaseCoder, LoadoutFactory aLoadoutFactory, ErrorReporter aErrorReporter) {
    baseCoder = aBaseCoder;
    loadoutFactory = aLoadoutFactory;
    errorReporter = aErrorReporter;
  }

  /**
   * Determines if this {@link LoadoutCoder} is capable of decoding the given bit stream. Usually
   * implemented by checking headers of the stream.
   *
   * @param aBitStream The stream to test for.
   * @return Returns <code>true</code> if this coder is able to decode the stream, <code>false
   *     </code> otherwise.
   */
  public boolean canDecode(String aBitStream) {
    return aBitStream.length() >= MIN_LENGTH && aBitStream.charAt(0) == MAGIC_V1;
  }

  /**
   * Decodes a given string into a {@link Loadout}.
   *
   * @param aMwoString The MWO exported string to decode.
   * @return A {@link Loadout} that has been decoded.
   * @throws DecodingException If the bit stream is broken.
   */
  public Loadout decode(String aMwoString) throws DecodingException {
    try (StringReader sr = new StringReader(aMwoString)) {
      if (MAGIC_V1 != sr.read()) {
        throw new DecodingException(
            "Magic missmatch, not a MWO loadout or newer version: " + aMwoString);
      }

      final int chassisId = baseCoder.parseExactly(sr, 2);
      final Chassis chassis = ChassisDB.lookup(chassisId);
      final Loadout loadout = loadoutFactory.produceEmpty(chassis);
      final LoadoutBuilder builder = new LoadoutBuilder();

      parseUpgrades(sr, builder, loadout);
      parseActuatorState(sr, builder, loadout);
      for (final Location location : Location.MWO_EXPORT_ORDER) {
        parseComponent(sr, builder, loadout, location);
      }
      parseBackArmour(sr, builder, loadout);

      builder.applyAll();
      builder.reportErrors(loadout, errorReporter);
      return loadout;
    } catch (final NoSuchItemException | IOException e1) {
      throw new DecodingException("Couldn't parse: " + aMwoString, e1);
    }
  }

  /**
   * Encodes the given {@link Loadout} to a raw bit stream.
   *
   * @param aLoadout The {@link Loadout} to encode.
   * @return A raw bit stream representing the {@link LoadoutStandard}.
   */
  public String encode(Loadout aLoadout) {
    final StringBuilder sb = new StringBuilder();
    sb.append(MAGIC_V1);
    baseCoder.append(aLoadout.getChassis().getId(), sb, 2);
    // Encode armour/structure/hs/guidance

    final boolean isOmnimech = aLoadout instanceof LoadoutOmniMech;
    final Upgrades upgrades = aLoadout.getUpgrades();
    final int structureArmour =
        (UPGRADE_TO_BITS.get(upgrades.getStructure()) << 3)
            | UPGRADE_TO_BITS.get(upgrades.getArmour());
    final int heatsinkGuidance =
        (UPGRADE_TO_BITS.get(upgrades.getHeatSink()) << 1)
            | UPGRADE_TO_BITS.get(upgrades.getGuidance())
            | (isOmnimech ? UPGRADE_OMNIMECH_BIT : 0);

    baseCoder.append(structureArmour, sb, 1);
    baseCoder.append(heatsinkGuidance, sb, 1);

    int actuatorState = 0;
    if (aLoadout instanceof final LoadoutOmniMech loadoutOmniMech) {
      final ConfiguredComponentOmniMech la = loadoutOmniMech.getComponent(Location.LeftArm);
      final ConfiguredComponentOmniMech ra = loadoutOmniMech.getComponent(Location.RightArm);

      if (!la.getToggleState(ItemDB.LAA)) {
        actuatorState |= ACTUATOR_STATE_L_LAA_REMOVED;
      } else if (!la.getToggleState(ItemDB.HA)) {
        actuatorState |= ACTUATOR_STATE_L_HA_REMOVED;
      }
      if (!ra.getToggleState(ItemDB.LAA)) {
        actuatorState |= ACTUATOR_STATE_R_LAA_REMOVED;
      } else if (!ra.getToggleState(ItemDB.HA)) {
        actuatorState |= ACTUATOR_STATE_R_HA_REMOVED;
      }
    }
    baseCoder.append(actuatorState, sb, 1);

    for (final Location location : Location.MWO_EXPORT_ORDER) {
      final ConfiguredComponent component = aLoadout.getComponent(location);
      final int frontArmour =
          component.getArmour(location.isTwoSided() ? ArmourSide.FRONT : ArmourSide.ONLY);
      baseCoder.append(frontArmour, sb, 2);

      if (location != Location.CenterTorso
          && aLoadout instanceof final LoadoutOmniMech loadoutOmniMech) {
        baseCoder.append(loadoutOmniMech.getComponent(location).getOmniPod().getId(), sb, 3);
      }
      for (final Item item : component.getItemsEquipped()) {
        if (item instanceof Internal) {
          continue;
        }
        sb.append(ITEM_SEPARATOR);
        baseCoder.append(item.getId(), sb, 1, 6);
      }
      sb.append(COMPONENT_TERMINATORS.get(location));
    }

    baseCoder.append(aLoadout.getComponent(Location.CenterTorso).getArmour(ArmourSide.BACK), sb, 2);
    baseCoder.append(aLoadout.getComponent(Location.LeftTorso).getArmour(ArmourSide.BACK), sb, 2);
    baseCoder.append(aLoadout.getComponent(Location.RightTorso).getArmour(ArmourSide.BACK), sb, 2);
    return sb.toString();
  }

  private void parseActuatorState(StringReader sr, LoadoutBuilder builder, final Loadout loadout)
      throws DecodingException, IOException {

    final int actuatorState = baseCoder.parseExactly(sr, 1);
    if (loadout instanceof final LoadoutOmniMech loadoutOmniMech) {

      final ConfiguredComponentOmniMech la = loadoutOmniMech.getComponent(Location.LeftArm);
      if ((actuatorState & ACTUATOR_STATE_L_LAA_REMOVED) != 0) {
        builder.push(new CmdToggleItem(null, loadoutOmniMech, la, ItemDB.LAA, false));
      } else if ((actuatorState & ACTUATOR_STATE_L_HA_REMOVED) != 0) {
        builder.push(new CmdToggleItem(null, loadoutOmniMech, la, ItemDB.HA, false));
      }

      final ConfiguredComponentOmniMech ra = loadoutOmniMech.getComponent(Location.RightArm);
      if ((actuatorState & ACTUATOR_STATE_R_LAA_REMOVED) != 0) {
        builder.push(new CmdToggleItem(null, loadoutOmniMech, ra, ItemDB.LAA, false));
      } else if ((actuatorState & ACTUATOR_STATE_R_HA_REMOVED) != 0) {
        builder.push(new CmdToggleItem(null, loadoutOmniMech, ra, ItemDB.HA, false));
      }
    }
  }

  private void parseBackArmour(StringReader sr, final LoadoutBuilder builder, final Loadout loadout)
      throws DecodingException, IOException {
    final int backArmourCT = baseCoder.parseExactly(sr, 2);
    final int backArmourLT = baseCoder.parseExactly(sr, 2);
    final int backArmourRT = baseCoder.parseExactly(sr, 2);
    builder.push(
        new CmdSetArmour(null, loadout, Location.CenterTorso, ArmourSide.BACK, backArmourCT, true));
    builder.push(
        new CmdSetArmour(null, loadout, Location.LeftTorso, ArmourSide.BACK, backArmourLT, true));
    builder.push(
        new CmdSetArmour(null, loadout, Location.RightTorso, ArmourSide.BACK, backArmourRT, true));
  }

  private void parseComponent(
      StringReader sr, final LoadoutBuilder builder, final Loadout loadout, final Location location)
      throws DecodingException, IOException {
    final ConfiguredComponent component = loadout.getComponent(location);

    final int frontArmour = baseCoder.parseExactly(sr, 2);
    builder.push(
        new CmdSetArmour(
            null,
            loadout,
            component,
            location.isTwoSided() ? ArmourSide.FRONT : ArmourSide.ONLY,
            frontArmour,
            true));

    if (location != Location.CenterTorso && loadout instanceof final LoadoutOmniMech omniMech) {
      try {
        final OmniPod omniPod = OmniPodDB.lookup(baseCoder.parseAvailable(sr, 6));
        builder.push(new CmdSetOmniPod(null, omniMech, omniMech.getComponent(location), omniPod));
      } catch (final NoSuchItemException e) {
        builder.pushError(e);
      }
    }

    char next = (char) sr.read();
    while (ITEM_SEPARATOR == next) {
      try {
        final int itemId = baseCoder.parseAvailable(sr, 6);
        builder.push(new CmdAddItem(null, loadout, component, ItemDB.lookup(itemId)));
      } catch (final NoSuchItemException e) {
        builder.pushError(e);
      }
      next = (char) sr.read();
    }

    final Character expectedTerminator = COMPONENT_TERMINATORS.get(location);
    if (expectedTerminator != next) {
      final StringBuilder sb = new StringBuilder();
      sb.append("Malformed MWO export string! ");
      sb.append(location.longName());
      sb.append(" section wasn't terminated by ");
      sb.append(expectedTerminator);
      throw new DecodingException(sb.toString());
    }
  }

  private void parseUpgrades(StringReader sr, final LoadoutBuilder builder, final Loadout loadout)
      throws DecodingException, IOException {
    final int armourStructure = baseCoder.parseExactly(sr, 1);
    final int guidanceHeatsinks = baseCoder.parseExactly(sr, 1);
    final boolean artemis = (guidanceHeatsinks & 1) != 0;
    final GuidanceUpgrade guidanceUpgrade = artemis ? UpgradeDB.ARTEMIS_IV : UpgradeDB.STD_GUIDANCE;
    builder.push(new CmdSetGuidanceType(null, loadout, guidanceUpgrade));

    // We basically ignore the upgrades field for omnimechs. This means we will not
    // show any errors if this field is inconsistent but instead quietly ignore it.
    if (loadout instanceof final LoadoutStandard loadoutStandard) {

      // XXX: Should we check if UPGRADE_OMNIMECH_BIT is correctly set?
      // Technically it doesn't affect our ability to correctly load the loadout, but it being set
      // is an indication of a possibly corrupt loadout.

      final int heatsinkType = guidanceHeatsinks >> 1;
      switch (heatsinkType) {
        case UPGRADE_IS_SHS -> builder.push(
            new CmdSetHeatSinkType(null, loadoutStandard, UpgradeDB.IS_SHS));
        case UPGRADE_IS_DHS -> builder.push(
            new CmdSetHeatSinkType(null, loadoutStandard, UpgradeDB.IS_DHS));
        case UPGRADE_CLAN_DHS -> builder.push(
            new CmdSetHeatSinkType(null, loadoutStandard, UpgradeDB.CLAN_DHS));
        case UPGRADE_CLAN_SHS -> builder.push(
            new CmdSetHeatSinkType(null, loadoutStandard, UpgradeDB.CLAN_SHS));
        default -> builder.pushError(
            new NoSuchItemException("Unknown heatsink upgrade type: " + heatsinkType));
      }

      final int armourType = armourStructure & 0x7;
      switch (armourType) {
        case UPGRADE_IS_STD_ARMOUR -> builder.push(
            new CmdSetArmourType(null, loadoutStandard, UpgradeDB.IS_STD_ARMOUR));
        case UPGRADE_IS_FF_ARMOUR -> builder.push(
            new CmdSetArmourType(null, loadoutStandard, UpgradeDB.IS_FF_ARMOUR));
        case UPGRADE_IS_LIGHT_FF_ARMOUR -> builder.push(
            new CmdSetArmourType(null, loadoutStandard, UpgradeDB.IS_LIGHT_FF_ARMOUR));
        case UPGRADE_IS_STEALTH_ARMOUR -> builder.push(
            new CmdSetArmourType(null, loadoutStandard, UpgradeDB.IS_STEALTH_ARMOUR));
        case UPGRADE_CLAN_FF_ARMOUR -> builder.push(
            new CmdSetArmourType(null, loadoutStandard, UpgradeDB.CLAN_FF_ARMOUR));
        case UPGRADE_CLAN_STD_ARMOUR -> builder.push(
            new CmdSetArmourType(null, loadoutStandard, UpgradeDB.CLAN_STD_ARMOUR));
        default -> builder.pushError(
            new NoSuchItemException("Unknown armour upgrade type: " + armourType));
      }

      final int structureType = armourStructure >> 3;
      switch (structureType) {
        case UPGRADE_IS_STD_STRUCTURE -> builder.push(
            new CmdSetStructureType(null, loadoutStandard, UpgradeDB.IS_STD_STRUCTURE));
        case UPGRADE_IS_ES_STRUCTURE -> builder.push(
            new CmdSetStructureType(null, loadoutStandard, UpgradeDB.IS_ES_STRUCTURE));
        case UPGRADE_CLAN_ES_STRUCTURE -> builder.push(
            new CmdSetStructureType(null, loadoutStandard, UpgradeDB.CLAN_ES_STRUCTURE));
        case UPGRADE_CLAN_STD_STRUCTURE -> builder.push(
            new CmdSetStructureType(null, loadoutStandard, UpgradeDB.CLAN_STD_STRUCTURE));
        default -> builder.pushError(
            new NoSuchItemException("Unknown structure upgrade type: " + structureType));
      }
    }
  }
}
