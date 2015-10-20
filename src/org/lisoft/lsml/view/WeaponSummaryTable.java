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
package org.lisoft.lsml.view;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This class displays a summary of weapons and ammo for a loadout in a JTable.
 * 
 * @author Emily Björk
 */
public class WeaponSummaryTable extends JTable implements MessageReceiver {
    private static final long    serialVersionUID = 868861599143353045L;
    private final LoadoutBase<?> loadout;
    private final DecimalFormat  decimalFormat    = new DecimalFormat("####");

    private static class WeaponModel extends AbstractTableModel {
        private static final long serialVersionUID = 1257566726770316140L;

        class Entry {
            private int                rounds;
            private final List<Weapon> weapons = new ArrayList<Weapon>();
            private final AmmoWeapon   weaponRepresentant;
            private final String       ammoType;

            Entry(Weapon aWeapon) {
                weapons.add(aWeapon);
                rounds = 0;
                if (aWeapon instanceof AmmoWeapon) {
                    weaponRepresentant = (AmmoWeapon) aWeapon;
                    ammoType = weaponRepresentant.getAmmoType();
                }
                else {
                    weaponRepresentant = null;
                    ammoType = null;
                }
            }

            Entry(Ammunition aItem) {
                ammoType = aItem.getAmmoType();
                rounds = aItem.getNumShots();
                weaponRepresentant = null;
            }

            boolean consume(Item anItem) {
                if (anItem instanceof Ammunition) {
                    Ammunition ammo = (Ammunition) anItem;
                    if (null != weaponRepresentant && weaponRepresentant.isCompatibleAmmo(ammo)) {
                        rounds += ammo.getNumShots();
                        return true;
                    }
                }
                else if (anItem instanceof AmmoWeapon) {
                    AmmoWeapon ammoWeapon = (AmmoWeapon) anItem;
                    if (ammoType != null && ammoWeapon.getAmmoType().equals(ammoType)) {
                        weapons.add(ammoWeapon);
                        return true;
                    }
                }
                else if (anItem instanceof Weapon) {
                    Weapon weapon = (Weapon) anItem;
                    if (weapons.contains(weapon)) {
                        weapons.add(weapon);
                        return true;
                    }
                }
                return false;
            }

            List<Weapon> getWeapons() {
                return weapons;
            }

            double getNumShots() {
                if (ammoType != null)
                    return rounds;
                return Double.POSITIVE_INFINITY;
            }

            public String getAmmoType() {
                return ammoType;
            }
        }

        List<Entry> entries = new ArrayList<>();

        public void update(LoadoutBase<?> aLoadout) {
            entries.clear();
            List<Item> ammo = new ArrayList<>();
            for (Item item : aLoadout.items()) {
                if (item instanceof Ammunition) {
                    ammo.add(item);
                    continue;
                }

                if (item instanceof Weapon) {
                    boolean found = false;
                    Weapon weapon = (Weapon) item;
                    for (Entry entry : entries) {
                        if (entry.consume(item)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        entries.add(new Entry(weapon));
                    }
                }
            }
            for (Item item : ammo) {
                boolean found = false;
                for (Entry entry : entries) {
                    if (entry.consume(item)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    entries.add(new Entry((Ammunition) item));
                }
            }
            fireTableDataChanged();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public Object getValueAt(int aRowIndex, int aColumnIndex) {
            return entries.get(aRowIndex);
        }
    }

    private class TotalDamageColumn extends AttributeTableColumn {
        private static final long serialVersionUID = -1036416917042517947L;

        public TotalDamageColumn() {
            super("T.Dmg", 0,
                    "The total damage potential for the ammo equipped, assuming all shots hit at full damage.");
        }

        @Override
        public String valueOf(Object aSourceRowObject) {
            WeaponModel.Entry entry = (WeaponModel.Entry) aSourceRowObject;
            if (entry.getWeapons().isEmpty() || !entry.getWeapons().get(0).isOffensive())
                return decimalFormat.format(0);

            double shots = entry.getNumShots();
            Weapon protoWeapon = entry.getWeapons().get(0);
            if (protoWeapon.getDamagePerShot() == 0.0)
                return decimalFormat.format(0);
            return decimalFormat.format(shots * protoWeapon.getDamagePerShot() / protoWeapon.getAmmoPerPerShot());
        }
    }

    private class CombatSecondsColumn extends AttributeTableColumn {
        private static final long serialVersionUID = -1036416917042517947L;

        public CombatSecondsColumn() {
            super(
                    "Seconds",
                    0,
                    "<html>The amount of time to use all ammo given a constant maximum fire rate.<br>I.e. how long you can use it in sustained combat.</html>");
        }

        @Override
        public String valueOf(Object aSourceRowObject) {
            WeaponModel.Entry entry = (WeaponModel.Entry) aSourceRowObject;
            if (entry.getWeapons().isEmpty())
                return decimalFormat.format(0);

            double shots = entry.getNumShots();
            double shotsPerSecond = 0;
            Collection<Modifier> modifiers = loadout.getModifiers();
            for (Weapon weapon : entry.getWeapons()) {
                if (weapon instanceof AmmoWeapon) {
                    AmmoWeapon ammoWeapon = (AmmoWeapon) weapon;
                    shotsPerSecond += ammoWeapon.getAmmoPerPerShot() / ammoWeapon.getSecondsPerShot(modifiers);
                }
            }
            return decimalFormat.format(shots / shotsPerSecond);
        }
    }

    private class VolleyAmountColumn extends AttributeTableColumn {
        private static final long serialVersionUID = -1036416917042517947L;

        public VolleyAmountColumn() {
            super("Volleys", 0, "The number of times a weapon group can be fired.");
        }

        @Override
        public String valueOf(Object aSourceRowObject) {
            WeaponModel.Entry entry = (WeaponModel.Entry) aSourceRowObject;
            if (entry.getWeapons().isEmpty())
                return decimalFormat.format(0);

            double shots = entry.getNumShots();
            double shotsPerVolley = 0;
            for (Weapon weapon : entry.getWeapons()) {
                if (weapon instanceof AmmoWeapon)
                    shotsPerVolley += ((AmmoWeapon) weapon).getAmmoPerPerShot();
            }
            return decimalFormat.format(shots / shotsPerVolley);
        }
    }

    private class AmmoAmountColumn extends AttributeTableColumn {
        private static final long serialVersionUID = -1036416917042517947L;

        public AmmoAmountColumn() {
            super("Ammo", 0, "The amount of ammo equipped.");
        }

        @Override
        public String valueOf(Object aSourceRowObject) {
            WeaponModel.Entry entry = (WeaponModel.Entry) aSourceRowObject;
            return decimalFormat.format(entry.getNumShots());
        }
    }

    private class WeaponColumn extends AttributeTableColumn {
        private static final long serialVersionUID = -1036416917042517947L;

        public WeaponColumn() {
            super("Weapon", 0, "The weapon equipped or the ammo if only ammo is equipped.");
        }

        @Override
        public String valueOf(Object aSourceRowObject) {
            WeaponModel.Entry entry = (WeaponModel.Entry) aSourceRowObject;

            if (entry.getNumShots() == Double.POSITIVE_INFINITY) {
                // Weapon that doesn't use ammo

                // Num shots will only be positive infinity if the entry was constructed with a Weapon that is not
                // AmmoWeapon, thus the weapons list will contain at least one entry. This is safe.
                String weaponName = entry.getWeapons().get(0).getShortName();
                if (entry.getWeapons().size() > 1) {
                    return entry.getWeapons().size() + " x " + weaponName;
                }
                return weaponName;
            }
            else if (entry.getWeapons().isEmpty()) {
                // Ammo without matching weapon
                for (AmmoWeapon ammoWeapon : ItemDB.lookup(AmmoWeapon.class)) {
                    if (ammoWeapon.getAmmoType().equals(entry.getAmmoType())) {
                        return ammoWeapon.getShortName() + " AMMO";
                    }
                }
            }

            // 1 >= AmmoWeapon with 0 or more tons of ammo.
            Weapon protoWeapon = entry.getWeapons().get(0);
            String weaponName = protoWeapon.getShortName();
            if (protoWeapon.getName().toLowerCase().contains("srm")
                    || protoWeapon.getName().toLowerCase().contains("lrm")) {
                Pattern pattern = Pattern.compile("(\\D+)(\\d+).*");
                String prefix = null;
                int size = 0;
                for (Weapon weapon : entry.getWeapons()) {
                    Matcher matcher = pattern.matcher(weapon.getName());
                    if (!matcher.matches()) {
                        throw new RuntimeException("Pattern didn't match! [" + weapon.getName() + "]");
                    }
                    if (prefix == null) {
                        prefix = matcher.group(1);
                    }
                    else {
                        if (!prefix.equals(matcher.group(1))) {
                            throw new RuntimeException("Prefix missmatch! Expected [" + prefix + "] was ["
                                    + matcher.group(1) + "]");
                        }
                    }
                    size += Integer.parseInt(matcher.group(2));
                }
                return prefix + size;
            }
            if (entry.getWeapons().size() > 1) {
                return entry.getWeapons().size() + " x " + weaponName;
            }
            return weaponName;
        }
    }

    public WeaponSummaryTable(LoadoutBase<?> aLoadout, MessageXBar aXBar) {
        super(new WeaponModel());
        loadout = aLoadout;
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);
        ((WeaponModel) getModel()).update(loadout);
        aXBar.attach(this);
        setFillsViewportHeight(true);
        ((DefaultTableCellRenderer) getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        removeColumn(getColumnModel().getColumn(0));
        addColumn(new WeaponColumn());
        addColumn(new AmmoAmountColumn());
        addColumn(new VolleyAmountColumn());
        addColumn(new CombatSecondsColumn());
        addColumn(new TotalDamageColumn());
        getColumnModel().getColumn(0).setMinWidth(80);
        getColumnModel().getColumn(1).setMinWidth(30);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg.isForMe(loadout) && aMsg.affectsHeatOrDamage()) {
            ((WeaponModel) getModel()).update(loadout);
        }
    }
}
