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
package org.lisoft.lsml.model.modifiers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.database.ModifiersDB;

/**
 * Handles efficiencies for a mech.
 * 
 * @author Li Song
 */
public class Efficiencies {
    private final Set<MechEfficiencyType> efficiencyTypes = new HashSet<>();
    private boolean doubleBasics = false;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (doubleBasics ? 1231 : 1237);
        result = prime * result + ((efficiencyTypes == null) ? 0 : efficiencyTypes.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Efficiencies other = (Efficiencies) obj;
        if (doubleBasics != other.doubleBasics)
            return false;
        if (efficiencyTypes == null) {
            if (other.efficiencyTypes != null)
                return false;
        }
        else if (!efficiencyTypes.equals(other.efficiencyTypes))
            return false;
        return true;
    }

    /**
     * Assigns this to be equal to that.
     * 
     * @param aEfficiencies
     *            The {@link Efficiencies} to copy from.
     */
    public void assign(Efficiencies aEfficiencies) {
        efficiencyTypes.clear();
        efficiencyTypes.addAll(aEfficiencies.efficiencyTypes);
        doubleBasics = aEfficiencies.doubleBasics;
    }

    /**
     * @return A {@link List} of all the modifiers that should be applied for these efficiencies.
     */
    public List<Modifier> getModifiers() {
        List<Modifier> ans = new ArrayList<>();
        for (MechEfficiencyType type : efficiencyTypes) {
            ans.addAll(ModifiersDB.lookupEfficiencyModifiers(type, doubleBasics));
        }
        return ans;
    }

    /**
     * @return <code>true</code> if all elite skills are unlocked. Effectiveness of cool run and heat containment is
     *         doubled.
     */
    public boolean hasDoubleBasics() {
        return doubleBasics;
    }

    public boolean hasEfficiency(MechEfficiencyType aMechEfficiencyType) {
        return efficiencyTypes.contains(aMechEfficiencyType);
    }

    /**
     * Sets double basics.
     * 
     * @param aDoubleBasics
     *            The value to set.
     * @param xBar
     *            {@link MessageXBar} to signal changes on.
     */
    public void setDoubleBasics(boolean aDoubleBasics, MessageXBar xBar) {
        if (aDoubleBasics != doubleBasics) {
            doubleBasics = aDoubleBasics;
            if (xBar != null)
                xBar.post(new EfficienciesMessage(this, EfficienciesMessage.Type.Changed, true));
        }
    }

    /**
     * Changes the status of an efficiency.
     * 
     * @param aMechEfficiencyType
     *            The efficiency to change.
     * @param aValue
     *            The new value.
     * @param aXBar
     *            The {@link MessageXBar} to send change messages on.
     */
    public void setEfficiency(MechEfficiencyType aMechEfficiencyType, boolean aValue, MessageXBar aXBar) {
        if (aValue == hasEfficiency(aMechEfficiencyType))
            return;

        if (aValue)
            efficiencyTypes.add(aMechEfficiencyType);
        else
            efficiencyTypes.remove(aMechEfficiencyType);

        if (aXBar != null)
            aXBar.post(
                    new EfficienciesMessage(this, EfficienciesMessage.Type.Changed, aMechEfficiencyType.affectsHeat()));
    }
}
