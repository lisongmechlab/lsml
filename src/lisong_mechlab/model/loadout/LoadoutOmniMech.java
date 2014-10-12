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
package lisong_mechlab.model.loadout;

import java.util.Collection;

import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.chassi.QuirkedMovementProfile;
import lisong_mechlab.model.chassi.Quirks;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ModuleSlot;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.loadout.component.ComponentBuilder.Factory;
import lisong_mechlab.model.loadout.component.ConfiguredComponentOmniMech;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.OperationStack.Operation;
import lisong_mechlab.util.message.MessageXBar;

/**
 * This class represents a configured loadout for an omnimech.
 * 
 * @author Emily Björk
 */
public class LoadoutOmniMech extends LoadoutBase<ConfiguredComponentOmniMech> {
    transient private final QuirkedMovementProfile movementProfile;
    transient private final Upgrades               upgrades;

    /**
     * @param aFactory
     * @param aChassis
     */
    public LoadoutOmniMech(Factory<ConfiguredComponentOmniMech> aFactory, ChassisOmniMech aChassis) {
        super(aFactory, aChassis);
        movementProfile = new QuirkedMovementProfile(aChassis.getMovementProfileBase());
        upgrades = new Upgrades(aChassis.getFixedArmorType(), aChassis.getFixedStructureType(),
                UpgradeDB.STANDARD_GUIDANCE, aChassis.getFixedHeatSinkType());
        for (ConfiguredComponentOmniMech component : getComponents()) {
            movementProfile.addMovementModifier(component.getOmniPod().getQuirks());
        }
    }

    /**
     * @param aOmniPodFactory
     * @param aLoadoutOmniMech
     */
    public LoadoutOmniMech(Factory<ConfiguredComponentOmniMech> aOmniPodFactory, LoadoutOmniMech aLoadoutOmniMech) {
        super(aOmniPodFactory, aLoadoutOmniMech);
        movementProfile = new QuirkedMovementProfile(getChassis().getMovementProfileBase());
        for (ConfiguredComponentOmniMech component : getComponents()) {
            movementProfile.addMovementModifier(component.getOmniPod().getQuirks());
        }
        upgrades = new Upgrades(aLoadoutOmniMech.getUpgrades());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((upgrades == null) ? 0 : upgrades.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        LoadoutOmniMech other = (LoadoutOmniMech) obj;
        if (!upgrades.equals(other.upgrades))
            return false;
        return true;
    }

    /**
     * This setter method is only intended to be used from package local {@link Operation}s. It's a raw, unchecked
     * accessor.
     * 
     * @param aOmniPod
     *            The omnipod to set, it's put in it's dedicated slot.
     */
    public void setOmniPod(OmniPod aOmniPod) {
        ConfiguredComponentOmniMech component = getComponent(aOmniPod.getLocation());
        movementProfile.removeMovementModifier(component.getOmniPod().getQuirks());
        movementProfile.addMovementModifier(aOmniPod.getQuirks());
        component.setOmniPod(aOmniPod);
    }

    @Override
    public MovementProfile getMovementProfile() {
        return movementProfile;
    }

    @Override
    public int getJumpJetsMax() {
        int ans = getChassis().getFixedJumpJets();
        for (ConfiguredComponentOmniMech component : getComponents()) {
            ans += component.getOmniPod().getJumpJetsMax();
        }
        return ans;
    }

    @Override
    public ChassisOmniMech getChassis() {
        return (ChassisOmniMech) super.getChassis();
    }

    @Override
    protected boolean canEquipGlobal(Item aItem) {
        if (aItem instanceof Engine)
            return false;
        return super.canEquipGlobal(aItem);
    }

    @Override
    public LoadoutOmniMech clone(MessageXBar aXBar) {
        return new LoadoutOmniMech(ComponentBuilder.getOmniPodFactory(), this);
    }

    @Override
    public Engine getEngine() {
        return getChassis().getFixedEngine();
    }

    /**
     * @return The number of globally used critical slots.
     */
    @Override
    public int getNumCriticalSlotsUsed() {
        int ans = 0;
        for (ConfiguredComponentOmniMech component : getComponents()) {
            ans += component.getSlotsUsed();
        }
        return ans;
    }

    @Override
    public Upgrades getUpgrades() {
        return upgrades;
    }

    @Override
    public int getModulesMax(ModuleSlot aModuleSlot) {
        if (aModuleSlot == ModuleSlot.MECH) {
            int ans = getChassis().getMechModulesMax();
            for (ConfiguredComponentOmniMech component : getComponents()) {
                ans += component.getOmniPod().getPilotModulesMax();
            }
            return ans;
        }
        else if (aModuleSlot == ModuleSlot.CONSUMABLE) {
            return getChassis().getConsumableModulesMax();
        }
        else if (aModuleSlot == ModuleSlot.WEAPON) {
            return getChassis().getWeaponModulesMax();
        }
        else if (aModuleSlot == ModuleSlot.HYBRID) {
            return 1; // +1 for mastery
        }
        else {
            throw new IllegalArgumentException("Unknown module slot type!");
        }
    }

    @Override
    public <U> Collection<U> getModifiers(Class<U> aClass) {
        Collection<U> ans = super.getModifiers(aClass);
        for (ConfiguredComponentOmniMech component : getComponents()) {
            Quirks quirks = component.getOmniPod().getQuirks();
            if (aClass.isInstance(quirks)) {
                ans.add(aClass.cast(quirks));
            }
        }
        return ans;
    }

    @Override
    public String getQuirkHtmlSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<body>");

        sb.append("<p>Quirks:</p>");
        for (ConfiguredComponentOmniMech component : getComponents()) {
            component.getOmniPod().getQuirks().describeAsHtmlWithoutHeaders(sb);
        }
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }
}
