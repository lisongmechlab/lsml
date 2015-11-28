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
package org.lisoft.lsml.model.datacache.gamedata.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lisoft.lsml.model.chassi.ComponentOmniMech;
import org.lisoft.lsml.model.chassi.ComponentStandard;
import org.lisoft.lsml.model.chassi.HardPoint;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.datacache.DataCache;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.datacache.gamedata.HardPointCache;
import org.lisoft.lsml.model.datacache.gamedata.WeaponDoorSet;
import org.lisoft.lsml.model.datacache.gamedata.WeaponDoorSet.WeaponDoor;
import org.lisoft.lsml.model.datacache.gamedata.XMLHardpoints;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Item;

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

    @XStreamAsAttribute
    private String             Name;
    @XStreamAsAttribute
    private int                Slots;
    @XStreamAsAttribute
    private double             HP;
    @XStreamAsAttribute
    private int                CanEquipECM;
    @XStreamAsAttribute
    private int                OmniSlot;
    @XStreamAsAttribute
    @XStreamAlias("OmniPod")
    private int                omniPod;
    @XStreamImplicit(itemFieldName = "Internal")
    private List<MdfItem>      internals;
    @XStreamImplicit(itemFieldName = "Fixed")
    private List<MdfItem>      fixed;
    @XStreamImplicit(itemFieldName = "Hardpoint")
    private List<MdfHardpoint> hardpoints;

    public boolean isOmniComponent() {
        return OmniSlot > 0;
    }

    public boolean isRear() {
        return Location.isRear(Name);
    }

    public ComponentStandard asComponentStandard(DataCache aDataCache, XMLHardpoints aHardPointsXML,
            String aChassiMwoName) {
        Location location = getLocation();
        List<Item> fixedItems = getFixedItems(aDataCache, internals, fixed);
        List<HardPoint> hardPoints = getHardPoints(location, aHardPointsXML, hardpoints, CanEquipECM, aChassiMwoName);

        return new ComponentStandard(location, Slots, HP, fixedItems, hardPoints);
    }

    public ComponentOmniMech asComponentOmniMech(DataCache aDataCache, Engine aEngine) {
        Location location = getLocation();
        List<Item> fixedItems = getFixedItems(aDataCache, internals, fixed);
        OmniPod fixedOmniPod = (omniPod > 0) ? aDataCache.findOmniPod(omniPod) : null;

        int dynStructure = 0;
        int dynArmor = 0;
        Iterator<Item> it = fixedItems.iterator();
        while (it.hasNext()) {
            Item item = it.next();
            if (item.getMwoId() == 1912) {
                it.remove();
                dynArmor++;
            }
            else if (item.getMwoId() == 1913) {
                it.remove();
                dynStructure++;
            }
        }

        if (location == Location.LeftTorso || location == Location.RightTorso) {
            if (aEngine.getType() == EngineType.XL) {
                Item xlSide = DataCache.findItem(ItemDB.ENGINE_INTERNAL_CLAN_ID, aDataCache.getItems());
                fixedItems.add(xlSide);
            }
        }

        return new ComponentOmniMech(location, Slots, HP, fixedItems, fixedOmniPod, dynStructure, dynArmor);
    }

    public static List<Item> getFixedItems(DataCache aDataCache, List<MdfItem> aInternals, List<MdfItem> aFixed) {
        List<Item> ans = new ArrayList<>();
        if (null != aInternals) {
            for (MdfItem item : aInternals) {
                if (item.Toggleable == 0)
                    ans.add(DataCache.findItem(item.ItemID, aDataCache.getItems()));
            }
        }
        if (null != aFixed) {
            for (MdfItem item : aFixed) {
                if (item.Toggleable == 0)
                    ans.add(DataCache.findItem(item.ItemID, aDataCache.getItems()));
            }
        }
        return ans;
    }

    public static List<Item> getToggleableItems(DataCache aDataCache, List<MdfItem> aInternals, List<MdfItem> aFixed) {
        List<Item> ans = new ArrayList<>();
        if (null != aInternals) {
            for (MdfItem item : aInternals) {
                if (item.Toggleable != 0)
                    ans.add(DataCache.findItem(item.ItemID, aDataCache.getItems()));
            }
        }
        if (null != aFixed) {
            for (MdfItem item : aFixed) {
                if (item.Toggleable != 0)
                    ans.add(DataCache.findItem(item.ItemID, aDataCache.getItems()));
            }
        }
        return ans;
    }

    public static List<HardPoint> getHardPoints(Location aLocation, XMLHardpoints aHardPointsXML,
            List<MdfHardpoint> aHardPoints, int aCanEquipECM, String aChassiMwoName) {
        List<HardPoint> ans = new ArrayList<>();
        if (null != aHardPoints) {
            for (MdfComponent.MdfHardpoint hardpoint : aHardPoints) {
                final HardPointType hardpointType = HardPointType.fromMwoType(hardpoint.Type);

                HardPointInfo hardPointInto = null;
                for (HardPointInfo hpi : aHardPointsXML.hardpoints) {
                    if (hpi.id == hardpoint.ID) {
                        hardPointInto = hpi;
                    }
                }

                if (hardPointInto == null) {
                    throw new NullPointerException("Found no matching hardpoint in the data files!");
                }

                boolean hasBayDoors = false;
                if (hardPointInto.NoWeaponAName != null && aHardPointsXML.weapondoors != null) {
                    for (WeaponDoorSet doorSet : aHardPointsXML.weapondoors) {
                        for (WeaponDoor weaponDoor : doorSet.weaponDoors) {
                            if (hardPointInto.NoWeaponAName.equals(weaponDoor.AName)) {
                                hasBayDoors = true;
                            }
                        }
                    }
                }

                if (hardpointType == HardPointType.MISSILE) {
                    List<Integer> tubes = aHardPointsXML.tubesForId(hardpoint.ID);
                    for (Integer tube : tubes) {
                        if (tube < 1) {
                            ans.add(HardPointCache.getHardpoint(hardpoint.ID, aChassiMwoName, aLocation));
                        }
                        else {
                            ans.add(new HardPoint(HardPointType.MISSILE, tube, hasBayDoors));
                        }
                    }
                }
                else {
                    for (int i = 0; i < aHardPointsXML.slotsForId(hardpoint.ID); ++i)
                        ans.add(new HardPoint(hardpointType));
                }
            }

            // For any mech with more than 2 missile hard points in CT, any launcher beyond the largest one can only
            // have 5 tubes (anything else is impossible to fit)
            if (aLocation == Location.CenterTorso) {
                int missileHps = 0;
                for (HardPoint hardPoint : ans) {
                    if (hardPoint.getType() == HardPointType.MISSILE)
                        missileHps++;
                }
                if (missileHps > 1) {
                    int maxTubes = 0;
                    for (HardPoint hardpoint : ans) {
                        maxTubes = Math.max(hardpoint.getNumMissileTubes(), maxTubes);
                    }

                    boolean maxAdded = false;
                    for (int i = 0; i < ans.size(); ++i) {
                        if (ans.get(i).getType() != HardPointType.MISSILE)
                            continue;
                        int tubes = ans.get(i).getNumMissileTubes();
                        if ((tubes < maxTubes && tubes > 5) || (tubes == maxTubes && maxAdded == true && tubes > 5)) {
                            ans.set(i, new HardPoint(HardPointType.MISSILE, 5, ans.get(i).hasMissileBayDoor()));
                        }
                        if (tubes == maxTubes)
                            maxAdded = true;
                    }
                }
            }
        }

        // Stupid PGI making hacks to put ECM on a hard point... now I have to change my code...
        if (aCanEquipECM == 1)
            ans.add(new HardPoint(HardPointType.ECM));

        return ans;
    }

    /**
     * @return The location of this MdfComponent
     */
    public Location getLocation() {
        return Location.fromMwoName(Name);
    }
}
