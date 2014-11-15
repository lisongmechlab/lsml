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

import java.io.File;
import java.util.Collection;

import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ModifierEquipment;
import lisong_mechlab.model.item.ModuleSlot;
import lisong_mechlab.model.item.PilotModule;
import lisong_mechlab.model.loadout.component.ComponentBuilder;
import lisong_mechlab.model.loadout.component.ComponentBuilder.Factory;
import lisong_mechlab.model.loadout.component.ConfiguredComponentStandard;
import lisong_mechlab.model.modifiers.Modifier;
import lisong_mechlab.model.upgrades.UpgradesMutable;
import lisong_mechlab.util.OperationStack;

import com.thoughtworks.xstream.XStream;

/**
 * This class represents the complete state of a 'mechs configuration.
 * 
 * @author Emily Björk
 */
public class LoadoutStandard extends LoadoutBase<ConfiguredComponentStandard> {
    private final UpgradesMutable upgrades;

    public static LoadoutStandard load(File aFile) {
        XStream stream = loadoutXstream();
        return (LoadoutStandard) stream.fromXML(aFile);
    }

    /**
     * Will create a new, empty load out based on the given chassis.
     * 
     * @param aChassi
     *            The chassis to base the load out on.
     */
    @Deprecated
    public LoadoutStandard(ChassisStandard aChassi) {
        this(ComponentBuilder.getStandardComponentFactory(), aChassi, UpgradesMutable.standardUpgrades());
    }

    /**
     * Will create a new, empty load out based on the given chassis.
     * 
     * @param aFactory
     *            The {@link Factory} used to construct the components.
     * 
     * @param aChassi
     *            The chassis to base the load out on.
     * @param aUpgradesMutable
     *            The {@link UpgradesMutable} that will be used for this chassis.
     */
    public LoadoutStandard(Factory<ConfiguredComponentStandard> aFactory, ChassisStandard aChassi,
            UpgradesMutable aUpgradesMutable) {
        super(aFactory, aChassi);

        upgrades = aUpgradesMutable;
    }

    /**
     * Will load a stock load out for the given variation name.
     * 
     * @param aString
     *            The name of the stock variation to load.
     * @throws Exception
     */
    public LoadoutStandard(String aString) throws Exception {
        this(ComponentBuilder.getStandardComponentFactory(), (ChassisStandard) ChassisDB.lookup(aString),
                UpgradesMutable.standardUpgrades());
        OperationStack operationStack = new OperationStack(0);
        operationStack.pushAndApply(new OpLoadStock(getChassis(), this, null));
    }

    /**
     * Copy constructor.
     * 
     * @param aFactory
     *            The {@link Factory} used to construct the components.
     * @param aLoadout
     *            The {@link LoadoutStandard} to copy.
     */
    public LoadoutStandard(Factory<ConfiguredComponentStandard> aFactory, LoadoutStandard aLoadout) {
        super(aFactory, aLoadout);
        upgrades = new UpgradesMutable(aLoadout.upgrades);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + upgrades.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof LoadoutStandard))
            return false;
        LoadoutStandard other = (LoadoutStandard) obj;
        if (!upgrades.equals(other.upgrades))
            return false;
        return true;
    }

    @Override
    public ChassisStandard getChassis() {
        return (ChassisStandard) super.getChassis();
    }

    /**
     * @return The {@link Engine} equipped on this loadout, or <code>null</code> if no engine is equipped.
     */
    @Override
    public Engine getEngine() {
        // The engine is not among the fixed items for a standard loadout.
        for (Item item : getComponent(Location.CenterTorso).getItemsEquipped()) {
            if (item instanceof Engine) {
                return (Engine) item;
            }
        }
        return null;
    }

    @Override
    public int getNumCriticalSlotsUsed() {
        int ans = getUpgrades().getStructure().getExtraSlots() + getUpgrades().getArmor().getExtraSlots();
        for (ConfiguredComponentStandard component : getComponents()) {
            ans += component.getSlotsUsed();
        }
        return ans;
    }

    @Override
    public MovementProfile getMovementProfile() {
        return getChassis().getMovementProfileBase();
    }

    @Override
    public LoadoutStandard copy() {
        // TODO: Remove hard-coded factory
        return new LoadoutStandard(ComponentBuilder.getStandardComponentFactory(), this);
    }

    @Override
    public int getJumpJetsMax() {
        return getChassis().getJumpJetsMax();
    }

    @Override
    public UpgradesMutable getUpgrades() {
        return upgrades;
    }

    @Override
    public int getModulesMax(ModuleSlot aModuleSlot) {
        if (aModuleSlot == ModuleSlot.MECH) {
            return getChassis().getMechModulesMax();
        }
        else if (aModuleSlot == ModuleSlot.CONSUMABLE) {
            return getChassis().getConsumableModulesMax();
        }
        else if (aModuleSlot == ModuleSlot.WEAPON) {
            return getChassis().getWeaponModulesMax();
        }
        else if (aModuleSlot == ModuleSlot.HYBRID) {
            return 1;// 1 from mastery
        }
        else {
            throw new IllegalArgumentException("Unknown module slot type!");
        }
    }

    @Override
    public Collection<Modifier> getModifiers() {
        Collection<Modifier> ans = super.getModifiers();
        ans.addAll(getChassis().getQuirks());
        return ans;
    }

    @Override
    public String getQuirkHtmlSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<body>");

        sb.append("<p>Chassis Quirks:</p>");
        for (Modifier modifier : getChassis().getQuirks()) {
            modifier.describeToHtml(sb);
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
