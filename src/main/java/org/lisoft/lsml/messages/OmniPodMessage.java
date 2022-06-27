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
package org.lisoft.lsml.messages;

import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.Loadout;

public class OmniPodMessage implements Message {
    public final ConfiguredComponent component;

    public OmniPodMessage(ConfiguredComponent aComponent) {
        component = aComponent;
    }

    @Override
    public boolean affectsHeatOrDamage() {
        return true; // Quirks can change
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OmniPodMessage)) {
            return false;
        }
        OmniPodMessage other = (OmniPodMessage) obj;
        if (component == null) {
            return other.component == null;
        } else {
            return component.equals(other.component);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((component == null) ? 0 : component.hashCode());
        return result;
    }

    @Override
    public boolean isForMe(Loadout aLoadout) {
        return aLoadout.getComponents().contains(component);
    }

    @Override
    public String toString() {
        return "OmniPod changed on " + component.getInternalComponent().getLocation().toString();
    }
}