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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.mwo_data.equipment.Engine;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.mechs.*;
import org.lisoft.mwo_data.modifiers.Attribute;
import org.lisoft.mwo_data.modifiers.ModifierDescription;
import org.lisoft.mwo_data.mwo_parser.WeaponDoorSet.WeaponDoor;

class MdfComponent {
  @XStreamImplicit(itemFieldName = "Internal")
  List<MdfItem> internals;

  @XStreamAsAttribute private int CanEquipECM;
  @XStreamAsAttribute private double HP;
  @XStreamAsAttribute private String Name;
  @XStreamAsAttribute private int OmniSlot;
  @XStreamAsAttribute private int Slots;

  @XStreamImplicit(itemFieldName = "Fixed")
  private List<MdfItem> fixed;

  @XStreamImplicit(itemFieldName = "Hardpoint")
  private List<MdfHardpoint> hardpoints;

  @XStreamAsAttribute
  @XStreamAlias("OmniPod")
  private int omniPod;

  public static class MdfHardpoint {
    @XStreamAsAttribute private int ID;
    @XStreamAsAttribute private int Type;
  }

  public static List<Item> getFixedItems(
      PartialDatabase aPartialDatabase, List<MdfItem> aInternals, List<MdfItem> aFixed) {
    final List<Item> ans = new ArrayList<>();
    if (null != aInternals) {
      for (final MdfItem item : aInternals) {
        if (item.Toggleable == 0) {
          ans.add(aPartialDatabase.lookupItem(item.ItemID));
        }
      }
    }
    if (null != aFixed) {
      for (final MdfItem item : aFixed) {
        if (item.Toggleable == 0) {
          ans.add(aPartialDatabase.lookupItem(item.ItemID));
        }
      }
    }
    return ans;
  }

  public static List<HardPoint> getHardPoints(
      Location aLocation,
      XMLHardpoints aHardPointsXML,
      List<MdfHardpoint> aHardPoints,
      int aCanEquipECM,
      String aChassisMwoName) {
    final List<HardPoint> ans = new ArrayList<>();
    if (null != aHardPoints) {
      for (final MdfComponent.MdfHardpoint hardpoint : aHardPoints) {
        final HardPointType hardpointType = HardPointType.fromMwoType(hardpoint.Type);

        HardPointInfo hardPointInto = null;
        for (final HardPointInfo hpi : aHardPointsXML.hardpoints) {
          if (hpi.id == hardpoint.ID) {
            hardPointInto = hpi;
          }
        }

        if (hardPointInto == null) {
          throw new NullPointerException("Found no matching hardpoint in the data files!");
        }

        boolean hasBayDoors = false;
        if (hardPointInto.NoWeaponAName != null && aHardPointsXML.weapondoors != null) {
          for (final WeaponDoorSet doorSet : aHardPointsXML.weapondoors) {
            for (final WeaponDoor weaponDoor : doorSet.weaponDoors) {
              if (hardPointInto.NoWeaponAName.equals(weaponDoor.AName)) {
                hasBayDoors = true;
                break;
              }
            }
          }
        }

        if (hardpointType == HardPointType.MISSILE) {
          final List<Integer> tubes = aHardPointsXML.tubesForId(hardpoint.ID);
          for (final Integer tube : tubes) {
            if (tube < 1) {
              ans.add(HardPointCache.getHardpoint(hardpoint.ID, aChassisMwoName, aLocation));
            } else {
              ans.add(new HardPoint(HardPointType.MISSILE, tube, hasBayDoors));
            }
          }
        } else {
          for (int i = 0; i < aHardPointsXML.slotsForId(hardpoint.ID); ++i) {
            ans.add(new HardPoint(hardpointType));
          }
        }
      }

      // For any mech with more than 2 missile hard points in CT, any launcher beyond the largest
      // one can only
      // have 5 tubes (anything else is impossible to fit)
      if (aLocation == Location.CenterTorso) {
        int missileHps = 0;
        for (final HardPoint hardPoint : ans) {
          if (hardPoint.getType() == HardPointType.MISSILE) {
            missileHps++;
          }
        }
        if (missileHps > 1) {
          int maxTubes = 0;
          for (final HardPoint hardpoint : ans) {
            maxTubes = Math.max(hardpoint.getNumMissileTubes(), maxTubes);
          }

          boolean maxAdded = false;
          for (int i = 0; i < ans.size(); ++i) {
            if (ans.get(i).getType() != HardPointType.MISSILE) {
              continue;
            }
            final int tubes = ans.get(i).getNumMissileTubes();
            if (tubes < maxTubes && tubes > 5 || tubes == maxTubes && maxAdded && tubes > 5) {
              ans.set(i, new HardPoint(HardPointType.MISSILE, 5, ans.get(i).hasMissileBayDoor()));
            }
            if (tubes == maxTubes) {
              maxAdded = true;
            }
          }
        }
      }
    }

    // Stupid PGI making hacks to put ECM on a hard point... now I have to change my code...
    if (aCanEquipECM == 1) {
      ans.add(new HardPoint(HardPointType.ECM));
    }

    return ans;
  }

  public static Stream<Item> getToggleableItems(
      PartialDatabase aPartialDatabase, List<MdfItem> aItems) {
    if (null == aItems) {
      return Stream.empty();
    } else {
      return aItems.stream()
          .filter(mdfItem -> mdfItem.Toggleable != 0)
          .filter(mdfItem -> isLegalToggleable(mdfItem.ItemID))
          .map(mdfItem -> aPartialDatabase.lookupItem(mdfItem.ItemID));
    }
  }

  public ComponentOmniMech asComponentOmniMech(PartialDatabase aPartialDatabase, Engine aEngine) {
    final Location location = getLocation();
    final List<Item> fixedItems = getFixedItems(aPartialDatabase, internals, fixed);
    final OmniPod fixedOmniPod = omniPod > 0 ? aPartialDatabase.lookupOmniPod(omniPod) : null;

    int dynStructure = 0;
    int dynArmour = 0;
    final Iterator<Item> it = fixedItems.iterator();
    while (it.hasNext()) {
      final Item item = it.next();
      if (item.getId() == 1912) {
        it.remove();
        dynArmour++;
      } else if (item.getId() == 1913) {
        it.remove();
        dynStructure++;
      }
    }

    if (location == Location.LeftTorso || location == Location.RightTorso) {
      aEngine.getSide().ifPresent(fixedItems::add);
    }
    final Attribute hp = new Attribute(HP, ModifierDescription.SEL_STRUCTURE, location.shortName());

    return new ComponentOmniMech(
        location, Slots, hp, fixedItems, fixedOmniPod, dynStructure, dynArmour);
  }

  public ComponentStandard asComponentStandard(
      PartialDatabase aPartialDatabase, XMLHardpoints aHardPointsXML, String aChassisMwoName) {
    final Location location = getLocation();
    final List<Item> fixedItems = getFixedItems(aPartialDatabase, internals, fixed);
    final List<HardPoint> hardPoints =
        getHardPoints(location, aHardPointsXML, hardpoints, CanEquipECM, aChassisMwoName);

    final Attribute hp = new Attribute(HP, ModifierDescription.SEL_STRUCTURE, location.shortName());

    return new ComponentStandard(location, Slots, hp, fixedItems, hardPoints);
  }

  /**
   * @return The location of this MdfComponent
   */
  public Location getLocation() {
    return Location.fromMwoName(Name);
  }

  public boolean isOmniComponent() {
    return OmniSlot > 0;
  }

  public boolean isRear() {
    return Location.isRear(Name);
  }

  private static boolean isLegalToggleable(int aItemId) {
    return aItemId == ItemDB.LAA_ID || aItemId == ItemDB.HA_ID;
  }
}
