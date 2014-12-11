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
package lisong_mechlab.mwo_data;

import lisong_mechlab.model.DataCache;
import lisong_mechlab.model.modifiers.Modifier;
import lisong_mechlab.model.modifiers.ModifierDescription;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class XMLQuirk {
    @XStreamAsAttribute
    public String name;
    @XStreamAsAttribute
    public double value;

    public Modifier toQuirk(DataCache aDataCache) {
        for(ModifierDescription description : aDataCache.getModifierDescriptions()){
            if(description.getKey().equalsIgnoreCase(name)){
                return new Modifier(description, value);
            }
        }
        throw new IllegalArgumentException("Unknown qurk: " + name);
    }
}