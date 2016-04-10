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
package org.lisoft.lsml.model.datacache.gamedata.helpers;

import java.util.ArrayList;
import java.util.List;

import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.Location;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsModuleStats {
    @XStreamAsAttribute
    public int slots;
    @XStreamAlias(value = "weight")
    @XStreamAsAttribute
    public double tons;
    @XStreamAlias("Health")
    @XStreamAsAttribute
    public int health;

    @XStreamAsAttribute
    public int amountAllowed;
    @XStreamAsAttribute
    public String components;
    @XStreamAsAttribute
    public String mechClass;

    @XStreamAsAttribute
    public int TonsMin; // Currently only used by MASC?
    @XStreamAsAttribute
    public int TonsMax; // Currently only used by MASC?

    public List<Location> getLocations() {
        if (null != components) {
            String[] comps = components.split("\\s*,\\s*");
            List<Location> ans = new ArrayList<>();
            for (String component : comps) {
                ans.add(Location.fromMwoName(component));
            }
            return ans;
        }
        return null;
    }

    public List<ChassisClass> getMechClasses() {
        if (null != mechClass) {
            String[] classes = mechClass.split("\\s*,\\s*");
            List<ChassisClass> ans = new ArrayList<>();
            for (String clazz : classes) {
                ans.add(ChassisClass.valueOf(clazz.toUpperCase()));
            }
            return ans;
        }
        return null;
    }
}
