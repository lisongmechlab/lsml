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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
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
        setLayout(new GridLayout(0, 2));

        JPanel weapons = new JPanel(new GridLayout(0, 1));
        JPanel groups = new JPanel(new GridLayout(0, WeaponGroups.MAX_GROUPS));

        // Header
        for (int i = 1; i <= WeaponGroups.MAX_GROUPS; ++i) {
            JLabel label = new JLabel(Integer.toString(i), SwingConstants.CENTER);
            groups.add(label);
        }

        // Checkboxes
        weapons.add(new JLabel());
        for (int i = 0; i < WeaponGroups.MAX_WEAPONS; ++i) {
            weaponNames[i] = new JLabel("" + i * i * i * 10000);
            weapons.add(weaponNames[i]);

            for (int j = 0; j < WeaponGroups.MAX_GROUPS; ++j) {
                final int group = j;
                final int weapon = i;

                final JCheckBox checkbox = new JCheckBox();
                checkbox.setAction(new AbstractAction() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent aE) {
                        weaponGroups.setGroup(group, weapon, checkbox.isSelected());
                        aXBar.post(new LoadoutMessage(loadout, LoadoutMessage.Type.MODULES_CHANGED)); // TODO, Make a
                                                                                                      // proper message
                    }
                });

                groupSelectors[index(group, weapon)] = checkbox;
                groups.add(checkbox);
            }
        }

        add(weapons);
        add(groups);
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
    public void receive(Message aMsg) {
        if (aMsg.isForMe(loadout) && aMsg.affectsHeatOrDamage()) {
            updateTable();
        }
    }

    private int index(int group, int weapon) {
        return weapon * WeaponGroups.MAX_GROUPS + group;
    }
}