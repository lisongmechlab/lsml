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
package lisong_mechlab.model.loadout;

import java.util.Collection;

import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.ModifierEquipment;
import lisong_mechlab.model.item.ModuleSlot;
import lisong_mechlab.model.item.PilotModule;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.loadout.component.ComponentBuilder.Factory;
import lisong_mechlab.model.loadout.component.ConfiguredComponentOmniMech;
import lisong_mechlab.model.modifiers.Modifier;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This class represents a configured loadout for an omnimech.
 * 
 * @author Li Song
 */
public class LoadoutOmniMech extends LoadoutBase<ConfiguredComponentOmniMech> {
    transient private final Upgrades               upgrades;

    /**
     * Creates a new, empty loadout.
     * 
     * @param aFactory
     *            The {@link Factory} used to construct the components.
     * @param aChassis
     *            The chassis to base this loadout on.
     */
    public LoadoutOmniMech(Factory<ConfiguredComponentOmniMech> aFactory, ChassisOmniMech aChassis) {
        super(aFactory, aChassis);
        upgrades = new Upgrades(aChassis.getFixedArmorType(), aChassis.getFixedStructureType(),
                UpgradeDB.STANDARD_GUIDANCE, aChassis.getFixedHeatSinkType());
    }

    /**
     * Copy constructor.
     * 
     * @param aFactory
     *            The {@link Factory} used to construct the components.
     * @param aLoadoutOmniMech
     *            The {@link LoadoutOmniMech} to copy.
     */
    public LoadoutOmniMech(Factory<ConfiguredComponentOmniMech> aFactory, LoadoutOmniMech aLoadoutOmniMech) {
        super(aFactory, aLoadoutOmniMech);
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
        component.setOmniPod(aOmniPod);
    }

    @Override
    public MovementProfile getMovementProfile() {
        return getChassis().getMovementProfileBase();
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
    public LoadoutOmniMech copy() {
        // TODO: Remove hard-coded factory
        return new LoadoutOmniMech(ComponentBuilder.getOmniComponentFactory(), this);
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
    public Collection<Modifier> getModifiers() {
        Collection<Modifier> ans = super.getModifiers();
        for (ConfiguredComponentOmniMech component : getComponents()) {
            ans.addAll(component.getOmniPod().getQuirks());
        }
        return ans;
    }

    @Override
    public String getQuirkHtmlSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<body>");

        sb.append("<p>Omnipod Quirks:</p>");
        for (ConfiguredComponentOmniMech component : getComponents()) {
            for (Modifier modifier : component.getOmniPod().getQuirks()) {
                modifier.describeToHtml(sb);
            }
        }

        sb.append("<p>Equipment Bonuses:</p>");
        for (ModifierEquipment me : items(ModifierEquipment.class)) {
            for (Modifier modifier : me.getModifiers()) {
                modifier.describeToHtml(sb);
            }
        }

        sb.append("<p>Module Bonuses:</p>");
        for (PilotModule me : getModules()) {
            if (me instanceof ModifierEquipment) {
                for (Modifier modifier : ((ModifierEquipment) me).getModifiers()) {
                    modifier.describeToHtml(sb);
                }
            }
        }

        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }
}
