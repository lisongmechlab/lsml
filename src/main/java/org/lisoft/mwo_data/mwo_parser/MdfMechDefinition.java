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
package org.lisoft.mwo_data.mwo_parser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.equipment.ArmourUpgrade;
import org.lisoft.mwo_data.equipment.Engine;
import org.lisoft.mwo_data.equipment.HeatSinkUpgrade;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.StructureUpgrade;
import org.lisoft.mwo_data.mechs.*;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * This class represents the XML content of the .mdf files.
 *
 * @author Li Song
 */
class MdfMechDefinition {
  @XmlElement private List<MdfComponent> ComponentList;
  @XmlElement private MdfMech Mech;
  @XmlElement private MdfMovementTuning MovementTuningConfiguration;
  @XmlElement private List<XMLQuirk> QuirkList;

  static MdfMechDefinition fromXml(InputStream is) {
    final XStream xstream = GameVFS.makeMwoSuitableXStream();
    xstream.alias("MechDefinition", MdfMechDefinition.class);
    xstream.alias("Mech", MdfMech.class);
    xstream.alias("Component", MdfComponent.class);
    xstream.alias("Internal", MdfItem.class);
    xstream.alias("Fixed", MdfItem.class);
    xstream.alias("MovementTuningConfiguration", MdfMovementTuning.class);
    xstream.alias("Quirk", XMLQuirk.class);
    return (MdfMechDefinition) xstream.fromXML(is);
  }

  ChassisOmniMech asChassisOmniMech(
      MechReferenceXML aMech,
      PartialDatabase aPartialDatabase,
      XMLMechIdMap aMechIdMap,
      XMLLoadout aLoadout)
      throws IOException {
    fixPGIsMistakes();
    final int baseVariant = getBaseVariant(aMechIdMap, aMech);
    final String name = aPartialDatabase.localise("@" + aMech.name);
    final String shortName = aPartialDatabase.localise("@" + aMech.name + "_short");
    final Faction faction = Faction.fromMwo(aMech.faction);

    final ComponentOmniMech[] components = new ComponentOmniMech[Location.values().length];

    // Determine engine type first
    Engine engine = null;
    for (final MdfComponent component : ComponentList) {
      if (component.getLocation() == Location.CenterTorso) {
        if (component.isRear()) {
          continue;
        }
        final ComponentOmniMech componentStandard =
            component.asComponentOmniMech(aPartialDatabase, null);
        for (final Item item : componentStandard.getFixedItems()) {
          if (item instanceof Engine) {
            engine = (Engine) item;
          }
        }
        components[componentStandard.getLocation().ordinal()] = componentStandard;
      }
    }

    if (null == engine) {
      throw new IOException("Unable to find engine for " + name);
    }

    for (final MdfComponent component : ComponentList) {
      if (component.getLocation() != Location.CenterTorso) {
        if (component.isRear()) {
          continue;
        }
        final ComponentOmniMech componentStandard =
            component.asComponentOmniMech(aPartialDatabase, engine);
        components[componentStandard.getLocation().ordinal()] = componentStandard;
      }
    }

    final StructureUpgrade structure =
        (StructureUpgrade) aPartialDatabase.lookupUpgrade(aLoadout.upgrades.structure.ItemID);
    final ArmourUpgrade armour =
        (ArmourUpgrade) aPartialDatabase.lookupUpgrade(aLoadout.upgrades.armor.ItemID);
    final HeatSinkUpgrade heatSink =
        (HeatSinkUpgrade) aPartialDatabase.lookupUpgrade(aLoadout.upgrades.heatsinks.ItemID);

    return new ChassisOmniMech(
        aMech.id,
        aMech.name,
        aMech.chassis,
        name,
        shortName,
        Mech.MaxTons,
        ChassisVariant.fromString(name, Mech.VariantType),
        baseVariant,
        MovementTuningConfiguration.asMovementProfile(),
        faction,
        components,
        structure,
        armour,
        heatSink,
        Mech.CanEquipMASC == 1);
  }

  ChassisStandard asChassisStandard(
      MechReferenceXML aMech,
      PartialDatabase aPartialDatabase,
      XMLMechIdMap aMechIdMap,
      XMLHardpoints aHardPointsXML) {
    final int baseVariant = getBaseVariant(aMechIdMap, aMech);
    final String name = aPartialDatabase.localise("@" + aMech.name);
    String shortName;
    try {
      shortName = aPartialDatabase.localise("@" + aMech.name + "_short");
    } catch (final IllegalArgumentException e) {
      shortName = name;
    }
    final Faction faction = Faction.fromMwo(aMech.faction);

    final ComponentStandard[] components = new ComponentStandard[Location.values().length];
    for (final MdfComponent component : ComponentList) {
      if (component.isRear()) {
        continue;
      }
      final ComponentStandard componentStandard =
          component.asComponentStandard(aPartialDatabase, aHardPointsXML, aMech.name);
      components[componentStandard.getLocation().ordinal()] = componentStandard;
    }

    final List<Modifier> quirkList = new ArrayList<>();
    if (null != QuirkList) {
      for (final XMLQuirk quirk : QuirkList) {
        quirkList.add(QuirkModifiers.createModifier(quirk, aPartialDatabase));
      }
    }

    return new ChassisStandard(
        aMech.id,
        aMech.name,
        aMech.chassis,
        name,
        shortName,
        Mech.MaxTons,
        ChassisVariant.fromString(name, Mech.VariantType),
        baseVariant,
        MovementTuningConfiguration.asMovementProfile(),
        faction,
        Mech.MinEngineRating,
        Mech.MaxEngineRating,
        Mech.MaxJumpJets,
        components,
        quirkList,
        Mech.CanEquipMASC == 1);
  }

  boolean isPlayableOmniMech() {
    return isUsable() && hasOmniMechComponent();
  }

  private boolean hasOmniMechComponent() {
    for (final MdfComponent component : ComponentList) {
      if (component.isOmniComponent()) {
        return true;
      }
    }
    return false;
  }

  boolean isPlayableStandardMech() {
    return isUsable() && !hasOmniMechComponent();
  }

  private boolean isUsable() {
    return 0 == Mech.UnstoppableByPlayers;
  }

  private void fixPGIsMistakes() {
    // TODO: Revisit if this hack is still necessary
    if (Mech.Variant.equalsIgnoreCase("SMN-GS") || Mech.Variant.equalsIgnoreCase("SMN-G")) {
      // As of 2022-04-03 both the SMN-G(S) and SMN-G variants have broken MDF and OmniPods
      // Compared to other Summoner variants, the SMN-G and -G(S) variants is missing upper arm
      // actuators.
      // Also, the OmniPod has toggleable states for upper arm actuators which is nonsensical
      for (MdfComponent component : ComponentList) {
        if (component.getLocation() == Location.LeftArm
            || component.getLocation() == Location.RightArm) {

          boolean hasUAA =
              component.internals.stream().anyMatch(internal -> internal.ItemID == ItemDB.UAA_ID);
          if (!hasUAA) {
            MdfItem UAA = new MdfItem();
            UAA.ItemID = ItemDB.UAA_ID;
            component.internals.add(UAA);
          }
        }
      }
    }
  }

  private int getBaseVariant(XMLMechIdMap aMechIdMap, MechReferenceXML aMech) {
    int baseVariant = -1;
    for (final XMLMechIdMap.Mech mappedMech : aMechIdMap.MechIdMap) {
      if (mappedMech.variantID == aMech.id) {
        baseVariant = mappedMech.baseID;
        break;
      }
    }
    if (Mech.VariantParent > 0) {
      if (baseVariant > 0 && Mech.VariantParent != baseVariant) {
        // Inconsistency between MechIDMap and ParentAttribute.
        throw new IllegalArgumentException(
            "MechIDMap.xml and VariantParent attribute are inconsistent for: " + aMech.name);
      }
      baseVariant = Mech.VariantParent;
    }
    return baseVariant;
  }

  private static class MdfMech {
    @XStreamAlias("CanEquipMasc")
    @XStreamAsAttribute
    int CanEquipMASC;

    @XStreamAsAttribute int MaxEngineRating;
    @XStreamAsAttribute int MaxJumpJets;
    @XStreamAsAttribute int MaxTons;
    @XStreamAsAttribute int MinEngineRating;
    @XStreamAsAttribute int UnstoppableByPlayers;
    @XStreamAsAttribute String Variant;
    @XStreamAsAttribute int VariantParent;
    @XStreamAsAttribute String VariantType;
  }

  private static class MdfMovementTuning {
    @XStreamAsAttribute double ArmTurnSpeedPitch;
    @XStreamAsAttribute double ArmTurnSpeedYaw;
    @XStreamAsAttribute double MaxArmRotationPitch;
    @XStreamAsAttribute double MaxArmRotationYaw;
    @XStreamAsAttribute double MaxMovementSpeed;
    @XStreamAsAttribute double MaxTorsoAnglePitch;
    @XStreamAsAttribute double MaxTorsoAngleYaw;
    @XStreamAsAttribute String MovementArchetype = "Huge";
    @XStreamAsAttribute double ReverseSpeedMultiplier;
    @XStreamAsAttribute double TorsoTurnSpeedPitch;
    @XStreamAsAttribute double TorsoTurnSpeedYaw;
    @XStreamAsAttribute double TurnLerpHighRate;
    @XStreamAsAttribute double TurnLerpHighSpeed;
    @XStreamAsAttribute String TurnLerpLowRate;
    @XStreamAsAttribute double TurnLerpLowSpeed;
    @XStreamAsAttribute double TurnLerpMidRate;
    @XStreamAsAttribute double TurnLerpMidSpeed;

    BaseMovementProfile asMovementProfile() {
      double TurnLerpLowRateFixForBug747 = Double.parseDouble(TurnLerpLowRate.replace("..", "."));
      return new BaseMovementProfile(
          MaxMovementSpeed,
          ReverseSpeedMultiplier,
          TorsoTurnSpeedYaw,
          TorsoTurnSpeedPitch,
          ArmTurnSpeedYaw,
          ArmTurnSpeedPitch,
          MaxTorsoAngleYaw,
          MaxTorsoAnglePitch,
          MaxArmRotationYaw,
          MaxArmRotationPitch,
          TurnLerpLowSpeed,
          TurnLerpMidSpeed,
          TurnLerpHighSpeed,
          TurnLerpLowRateFixForBug747,
          TurnLerpMidRate,
          TurnLerpHighRate,
          MovementProfile.MovementArchetype.valueOf(MovementArchetype));
    }
  }
}
