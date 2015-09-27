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
package org.lisoft.lsml.view.mechlab;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutMessage;
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.util.message.Message;
import org.lisoft.lsml.util.message.MessageXBar;
import org.lisoft.lsml.view.render.StyleManager;

public class WeaponGroupingPanel extends JPanel implements Message.Recipient {
    private static final long    serialVersionUID = -2363813549845560757L;
    private final WeaponGroups   weaponGroups;
    private final LoadoutBase<?> loadout;
    private final JCheckBox      groupSelectors[] = new JCheckBox[WeaponGroups.MAX_GROUPS * WeaponGroups.MAX_WEAPONS];
    private final JLabel         weaponNames[]    = new JLabel[WeaponGroups.MAX_WEAPONS];

    /**
     * @param aWeaponGroups
     * @param aLoadout
     * @param aXBar
     * 
     */
    public WeaponGroupingPanel(WeaponGroups aWeaponGroups, LoadoutBase<?> aLoadout, final MessageXBar aXBar) {
        weaponGroups = aWeaponGroups;
        loadout = aLoadout;
        aXBar.attach(this);

        setBorder(StyleManager.sectionBorder("Weapon Groups"));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;

        // Header
        for (int i = 1; i <= WeaponGroups.MAX_GROUPS; ++i) {
            JLabel label = new JLabel(Integer.toString(i), SwingConstants.CENTER);
            gbc.gridx = i;
            add(label, gbc);
        }

        // Checkboxes
        for (int i = 0; i < WeaponGroups.MAX_WEAPONS; ++i) {
            weaponNames[i] = new JLabel();

            gbc.gridy = i + 1;
            gbc.gridx = 0;

            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.weightx = 1.0;
            add(weaponNames[i], gbc);
            gbc.weightx = 0.0;
            gbc.anchor = GridBagConstraints.CENTER;

            for (int j = 0; j < WeaponGroups.MAX_GROUPS; ++j) {
                final int group_id = j;
                final int weapon_id = i;

                final JCheckBox checkbox = new JCheckBox();
                checkbox.setModel(new JCheckBox.ToggleButtonModel() {
                    @Override
                    public void setSelected(boolean aB) {
                        weaponGroups.setGroup(group_id, weapon_id, aB);
                        aXBar.post(new LoadoutMessage(loadout, LoadoutMessage.Type.WEAPON_GROUPS_CHANGED));
                    }

                    @Override
                    public boolean isSelected() {
                        return weaponGroups.isInGroup(group_id, weapon_id);
                    }
                });

                groupSelectors[index(group_id, weapon_id)] = checkbox;

                gbc.gridx = j + 1;
                add(checkbox, gbc);
            }
        }
        updateTable();
    }

    private void updateTable() {
        List<Weapon> weapons = weaponGroups.getWeaponOrder();
        for (int i = 0; i < weaponNames.length; ++i) {
            if (i < weapons.size()) {
                weaponNames[i].setText(weapons.get(i).getName());
                weaponNames[i].setVisible(true);
            }
            else {
                weaponNames[i].setVisible(false);
            }
        }

        for (int weapon = 0; weapon < WeaponGroups.MAX_WEAPONS; ++weapon) {
            for (int group = 0; group < WeaponGroups.MAX_GROUPS; ++group) {
                groupSelectors[index(group, weapon)].setVisible(weapon < weapons.size());
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getMinimumSize();
        d.width = 300; // Couldn't really find any other solution that worked...
        return d;
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg.isForMe(loadout) && aMsg.affectsHeatOrDamage()) {
            updateTable();
        }
    }

    private int index(int group, int weapon) {
        return weapon * WeaponGroups.MAX_GROUPS + group;
    }
}