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
package org.lisoft.lsml.model.database.gamedata.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.lisoft.lsml.model.chassi.ComponentOmniMech;
import org.lisoft.lsml.model.chassi.ComponentStandard;
import org.lisoft.lsml.model.chassi.HardPoint;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.database.gamedata.HardPointCache;
import org.lisoft.lsml.model.database.gamedata.WeaponDoorSet;
import org.lisoft.lsml.model.database.gamedata.WeaponDoorSet.WeaponDoor;
import org.lisoft.lsml.model.database.gamedata.XMLHardpoints;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.ModifierDescription;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class MdfComponent {
    public static class MdfHardpoint {
        @XStreamAsAttribute
        private int ID;
        @XStreamAsAttribute
        private int Type;
        @XStreamAsAttribute
        private int Slots;
    }

    public static List<Item> getFixedItems(Map<Integer, Object> aId2obj, List<MdfItem> aInternals,
            List<MdfItem> aFixed) {
        final List<Item> ans = new ArrayList<>();
        if (null != aInternals) {
            for (final MdfItem item : aInternals) {
                if (item.Toggleable == 0) {
                    ans.add((Item) aId2obj.get(item.ItemID));
                }
            }
        }
        if (null != aFixed) {
            for (final MdfItem item : aFixed) {
                if (item.Toggleable == 0) {
                    ans.add((Item) aId2obj.get(item.ItemID));
                }
            }
        }
        return ans;
    }

    public static List<HardPoint> getHardPoints(Location aLocation, XMLHardpoints aHardPointsXML,
            List<MdfHardpoint> aHardPoints, int aCanEquipECM, String aChassiMwoName) {
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
                            }
                        }
                    }
                }

                if (hardpointType == HardPointType.MISSILE) {
                    final List<Integer> tubes = aHardPointsXML.tubesForId(hardpoint.ID);
                    for (final Integer tube : tubes) {
                        if (tube < 1) {
                            ans.add(HardPointCache.getHardpoint(hardpoint.ID, aChassiMwoName, aLocation));
                        }
                        else {
                            ans.add(new HardPoint(HardPointType.MISSILE, tube, hasBayDoors));
                        }
                    }
                }
                else {
                    for (int i = 0; i < aHardPointsXML.slotsForId(hardpoint.ID); ++i) {
                        ans.add(new HardPoint(hardpointType));
                    }
                }
            }

            // For any mech with more than 2 missile hard points in CT, any launcher beyond the largest one can only
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
                        if (tubes < maxTubes && tubes > 5 || tubes == maxTubes && maxAdded == true && tubes > 5) {
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

    public static List<Item> getToggleableItems(Map<Integer, Object> aId2obj, List<MdfItem> aInternals,
            List<MdfItem> aFixed) {
        final List<Item> ans = new ArrayList<>();
        if (null != aInternals) {
            for (final MdfItem item : aInternals) {
                if (item.Toggleable != 0) {
                    ans.add((Item) aId2obj.get(item.ItemID));
                }
            }
        }
        if (null != aFixed) {
            for (final MdfItem item : aFixed) {
                if (item.Toggleable != 0) {
                    ans.add((Item) aId2obj.get(item.ItemID));
                }
            }
        }
        return ans;
    }

    @XStreamAsAttribute
    private String Name;
    @XStreamAsAttribute
    private int Slots;
    @XStreamAsAttribute
    private double HP;
    @XStreamAsAttribute
    private int CanEquipECM;
    @XStreamAsAttribute
    private int OmniSlot;
    @XStreamAsAttribute
    @XStreamAlias("OmniPod")
    private int omniPod;

    @XStreamImplicit(itemFieldName = "Internal")
    private List<MdfItem> internals;

    @XStreamImplicit(itemFieldName = "Fixed")
    private List<MdfItem> fixed;

    @XStreamImplicit(itemFieldName = "Hardpoint")
    private List<MdfHardpoint> hardpoints;

    public ComponentOmniMech asComponentOmniMech(Map<Integer, Object> aId2obj, Engine aEngine) {
        final Location location = getLocation();
        final List<Item> fixedItems = getFixedItems(aId2obj, internals, fixed);
        final OmniPod fixedOmniPod = omniPod > 0 ? (OmniPod) aId2obj.get(omniPod) : null;

        int dynStructure = 0;
        int dynArmour = 0;
        final Iterator<Item> it = fixedItems.iterator();
        while (it.hasNext()) {
            final Item item = it.next();
            if (item.getId() == 1912) {
                it.remove();
                dynArmour++;
            }
            else if (item.getId() == 1913) {
                it.remove();
                dynStructure++;
            }
        }

        if (location == Location.LeftTorso || location == Location.RightTorso) {
            aEngine.getSide().ifPresent(side -> {
                fixedItems.add(side);
            });
        }
        final Attribute hp = new Attribute(HP, ModifierDescription.SEL_STRUCTURE, location.shortName());

        return new ComponentOmniMech(location, Slots, hp, fixedItems, fixedOmniPod, dynStructure, dynArmour);
    }

    public ComponentStandard asComponentStandard(Map<Integer, Object> aId2obj, XMLHardpoints aHardPointsXML,
            String aChassiMwoName) {
        final Location location = getLocation();
        final List<Item> fixedItems = getFixedItems(aId2obj, internals, fixed);
        final List<HardPoint> hardPoints = getHardPoints(location, aHardPointsXML, hardpoints, CanEquipECM,
                aChassiMwoName);

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
}
