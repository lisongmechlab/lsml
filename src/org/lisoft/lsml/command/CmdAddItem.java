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
package org.lisoft.lsml.command;

import org.lisoft.lsml.messages.ItemMessage.Type;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.NotificationMessage;
import org.lisoft.lsml.messages.NotificationMessage.Severity;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.EquipException;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentOmniMech;
import org.lisoft.lsml.util.CommandStack.Command;

/**
 * This {@link Command} adds an {@link Item} to a {@link ConfiguredComponentBase}.
 * 
 * @author Emily Björk
 */
public class CmdAddItem extends CmdItemBase {
    public static final String MANY_GAUSS_WARNING = "Only two gauss rifles can be charged simultaneously.";
    public static final String XLCASE_WARNING     = "C.A.S.E. together with XL engine has no effect.";
    private boolean            oldHAState;
    private boolean            oldLAAState;

    /**
     * Creates a new operation.
     * 
     * @param aMessageDelivery
     *            The {@link MessageDelivery} to send messages on when items are added.
     * @param aLoadout
     *            The {@link LoadoutBase} to remove the item from.
     * @param aComponent
     *            The {@link ConfiguredComponentBase} to add to.
     * @param aItem
     *            The {@link Item} to add.
     */
    public CmdAddItem(MessageDelivery aMessageDelivery, LoadoutBase<?> aLoadout, ConfiguredComponentBase aComponent,
            Item aItem) {
        super(aMessageDelivery, aLoadout, aComponent, aItem);
        if (aItem instanceof Internal)
            throw new IllegalArgumentException("Internals cannot be added!");
    }

    @Override
    public void apply() throws EquipException {
        EquipResult result = loadout.canEquipDirectly(item);
        EquipException.checkAndThrow(result);

        result = component.canEquip(item);
        EquipException.checkAndThrow(result);

        if (item instanceof Engine) {
            addXLSides((Engine) item);
        }

        applyForcedToggles(item);

        checkCaseXLWarning(item);
        checkManyGaussWarning(item);

        add(component, item);
    }

    @Override
    public String describe() {
        return "add " + item.getName() + " to " + component.getInternalComponent().getLocation();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (oldHAState ? 1231 : 1237);
        result = prime * result + (oldLAAState ? 1231 : 1237);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof CmdAddItem))
            return false;
        CmdAddItem other = (CmdAddItem) obj;
        if (oldHAState != other.oldHAState)
            return false;
        if (oldLAAState != other.oldLAAState)
            return false;
        return true;
    }

    @Override
    public void undo() {
        if (item instanceof Engine) {
            removeXLSides((Engine) item);
        }
        remove(component, item);
        restoreForcedToggles(item);
    }

    private void applyForcedToggles(Item aItem) {
        if (!(aItem instanceof Weapon) || !(component instanceof ConfiguredComponentOmniMech))
            return;

        Weapon weapon = (Weapon) aItem;
        if (weapon.isLargeBore()) {
            // Force toggle off on HA/LAA

            ConfiguredComponentOmniMech ccom = (ConfiguredComponentOmniMech) component;

            oldLAAState = ccom.getToggleState(ItemDB.LAA);
            oldHAState = ccom.getToggleState(ItemDB.HA);

            if (oldLAAState) {
                if (oldHAState) {
                    ccom.setToggleState(ItemDB.HA, false);
                    post(component, Type.Removed, ItemDB.HA, -1);
                }
                ccom.setToggleState(ItemDB.LAA, false);
                post(component, Type.Removed, ItemDB.LAA, -1);
            }
        }
    }

    private void checkCaseXLWarning(Item aItem) {
        if (null != messageDelivery) {
            Engine engine = loadout.getEngine();
            if (aItem == ItemDB.CASE && engine != null && engine.getType() == EngineType.XL) {
                messageDelivery.post(new NotificationMessage(Severity.WARNING, loadout, XLCASE_WARNING));
            }
        }
    }

    private void checkManyGaussWarning(Item aItem) {
        if (null != messageDelivery) {
            if (aItem instanceof BallisticWeapon && aItem.getName().contains("GAUSS")) {
                int rifles = 0;
                for (BallisticWeapon weapon : loadout.items(BallisticWeapon.class)) {
                    if (weapon.getName().contains("GAUSS")) {
                        rifles++;
                        if (rifles >= 2) {
                            messageDelivery
                                    .post(new NotificationMessage(Severity.WARNING, loadout, MANY_GAUSS_WARNING));
                            return;
                        }
                    }
                }
            }
        }
    }

    private void restoreForcedToggles(Item aItem) {
        if (!(aItem instanceof Weapon) || !(component instanceof ConfiguredComponentOmniMech))
            return;

        Weapon weapon = (Weapon) aItem;
        if (weapon.isLargeBore()) {
            ConfiguredComponentOmniMech ccom = (ConfiguredComponentOmniMech) component;
            if (oldLAAState) {
                ccom.setToggleState(ItemDB.LAA, true);
                post(component, Type.Added, ItemDB.LAA, -1);
                if (oldHAState) {
                    ccom.setToggleState(ItemDB.HA, true);
                    post(component, Type.Added, ItemDB.HA, -1);
                }
            }
        }
    }

    /**
     * @return The item that is being added in this operation.
     */
    public Item getItem() {
        return item;
    }
}
