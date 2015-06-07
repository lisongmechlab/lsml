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
package org.lisoft.lsml.view.mechlab;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.model.metrics.HeatDissipation;
import org.lisoft.lsml.util.message.Message;
import org.lisoft.lsml.util.message.MessageXBar;

/**
 * Weapon Lab
 * 
 * @author Emily Björk
 */
public class WeaponLabPage extends JPanel implements Message.Recipient {
    private static final long         serialVersionUID   = 1954396479455254773L;

    private final WeaponGroupingPanel weaponGroups;
    private final WeaponGroupStats    weaponGroupStats[] = new WeaponGroupStats[WeaponGroups.MAX_GROUPS];
    private final LoadoutBase<?>      loadout;

    public WeaponLabPage(LoadoutBase<?> aLoadout, MessageXBar aXBar, HeatDissipation aHeatDissipation) {
        loadout = aLoadout;
        aXBar.attach(this);

        setLayout(new BorderLayout());
        JPanel weaponsPanel = new JPanel();
        weaponsPanel.setLayout(new BoxLayout(weaponsPanel, BoxLayout.PAGE_AXIS));
        weaponGroups = new WeaponGroupingPanel(aLoadout.getWeaponGroups(), aLoadout, aXBar);
        weaponsPanel.add(weaponGroups);
        weaponsPanel.add(Box.createVerticalGlue());
        add(weaponsPanel, BorderLayout.WEST);

        JPanel groupsPanel = new JPanel();
        groupsPanel.setLayout(new BoxLayout(groupsPanel, BoxLayout.PAGE_AXIS));
        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            WeaponGroupStats wgs = new WeaponGroupStats(aLoadout, aXBar, aHeatDissipation, i);
            groupsPanel.add(wgs, BorderLayout.EAST);
            weaponGroupStats[i] = wgs;
        }
        groupsPanel.add(Box.createVerticalGlue());
        add(groupsPanel, BorderLayout.EAST);
        update();
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg.isForMe(loadout) && aMsg.affectsHeatOrDamage()) {
            update();
        }
    }

    private void update() {
        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            boolean hasGroup = loadout.getWeaponGroups().getWeapons(i).size() > 0;
            weaponGroupStats[i].setVisible(hasGroup);
        }
    }
}
