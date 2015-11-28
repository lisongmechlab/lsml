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
package org.lisoft.lsml.view.mechlab.loadoutframe;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.LoadoutMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.graphs.AlphaStrikeGraphModel;
import org.lisoft.lsml.model.graphs.SustainedDpsGraphModel;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.view.MetricDisplay;
import org.lisoft.lsml.view.ProgramInit;
import org.lisoft.lsml.view.graphs.DamageGraphPanel;
import org.lisoft.lsml.view.mechlab.AngleDisplay;
import org.lisoft.lsml.view.models.EfficiencyModel;
import org.lisoft.lsml.view.render.ScrollablePanel;
import org.lisoft.lsml.view.render.StyleManager;

/**
 * This panel will show detailed statistics about a BattleMech's performance.
 * 
 * @author Emily Björk
 */
public class MechStatisticsPanel extends JPanel implements MessageReceiver {
    private final LoadoutPanel        loadoutPanel;
    private final WeaponGroupingPanel weaponGroups;
    private final WeaponGroupStats    weaponGroupStats[] = new WeaponGroupStats[WeaponGroups.MAX_GROUPS];
    private final LoadoutBase<?>      loadout;

    private final AngleDisplay        torsoYawDisplay    = new AngleDisplay(90.0);
    private final AngleDisplay        torsoPitchDisplay  = new AngleDisplay(0.0);
    private final MetricDisplay       torsoYawSpeed;
    private final MetricDisplay       torsoPitchSpeed;
    private final MetricDisplay       armYawSpeed;
    private final MetricDisplay       armPitchSpeed;
    private final JCheckBox           twistX;
    private final JCheckBox           twistSpeed;
    private final JCheckBox           armReflex;

    /**
     * @param aLoadout
     *            The {@link LoadoutBase} to show statistics for.
     * @param aXBar
     *            A {@link MessageXBar} to listen to changes to the loadout on. Will update statistics.
     * @param aMetrics
     *            A {@link LoadoutMetrics} objects for the loadout from which necessary metrics can be extracted.
     * @param aLoadoutPanel
     *            The loadout panel will define the size of the statistics panel.
     * 
     */
    public MechStatisticsPanel(LoadoutBase<?> aLoadout, final MessageXBar aXBar, LoadoutMetrics aMetrics,
            LoadoutPanel aLoadoutPanel) {
        loadout = aLoadout;
        loadoutPanel = aLoadoutPanel;
        weaponGroups = new WeaponGroupingPanel(aLoadout.getWeaponGroups(), aLoadout, aXBar);
        aXBar.attach(this);
        torsoYawSpeed = new MetricDisplay(aMetrics.torsoYawSpeed, "Torso yaw speed: %.0f °/s",
                "How fast the 'Mech can turn its torso sideways.", aXBar, loadout);
        torsoPitchSpeed = new MetricDisplay(aMetrics.torsoPitchSpeed, "Torso pitch speed: %.0f °/s",
                "How fast the 'Mech can tilt its torso vertically.", aXBar, loadout);
        armYawSpeed = new MetricDisplay(aMetrics.armYawSpeed, "Arm yaw speed: %.0f °/s",
                "How fast the 'Mech can move its arms sideways.", aXBar, loadout);
        armPitchSpeed = new MetricDisplay(aMetrics.armPitchSpeed, "Arm pitch speed: %.0f °/s",
                "How fast the 'Mech can move its arms vertically.", aXBar, loadout);

        twistX = new JCheckBox("Twist X");
        twistX.setModel(new EfficiencyModel(aXBar, MechEfficiencyType.TWIST_X, loadout.getEfficiencies()));
        twistSpeed = new JCheckBox("Twist Speed");
        twistSpeed.setModel(new EfficiencyModel(aXBar, MechEfficiencyType.TWIST_SPEED, loadout.getEfficiencies()));

        armReflex = new JCheckBox("Arm Reflex");
        armReflex.setModel(new EfficiencyModel(aXBar, MechEfficiencyType.ARM_REFLEX, loadout.getEfficiencies()));

        if (ProgramInit.lsml().preferences.uiPreferences.getCompactMode()) {
            setLayout(new BorderLayout());
            JPanel content = new ScrollablePanel();
            content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
            content.add(makeLeftPanel(aXBar, aMetrics));
            content.add(makeRightPanel(aLoadout, aMetrics, aXBar));
            JScrollPane scrollPane = new JScrollPane(content);
            add(scrollPane, BorderLayout.CENTER);
        }
        else {
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            add(makeLeftPanel(aXBar, aMetrics));
            add(makeRightPanel(aLoadout, aMetrics, aXBar));
        }
    }

    private Component makeRightPanel(LoadoutBase<?> aLoadout, LoadoutMetrics aMetrics, MessageXBar aXBar) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        {
            JPanel armsAndTorso = new JPanel();
            armsAndTorso.setLayout(new GridBagLayout());
            armsAndTorso.setBorder(StyleManager.sectionBorder("Arms & Torso"));

            JPanel numbers = new JPanel();
            numbers.setLayout(new BoxLayout(numbers, BoxLayout.PAGE_AXIS));
            numbers.add(torsoYawSpeed);
            numbers.add(torsoPitchSpeed);
            numbers.add(armYawSpeed);
            numbers.add(armPitchSpeed);

            numbers.add(twistX);
            numbers.add(twistSpeed);
            numbers.add(armReflex);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.ipadx = 10;
            gbc.anchor = GridBagConstraints.LINE_START;
            armsAndTorso.add(numbers, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.ipadx = 0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.LINE_END;
            armsAndTorso.add(torsoYawDisplay, gbc);

            gbc.gridx = 2;
            armsAndTorso.add(torsoPitchDisplay, gbc);
            panel.add(armsAndTorso);
        }

        DamageGraphPanel sustainedDps = new DamageGraphPanel(aLoadout, aXBar,
                new SustainedDpsGraphModel(aMetrics, aLoadout));
        DamageGraphPanel alphaStrike = new DamageGraphPanel(aLoadout, aXBar,
                new AlphaStrikeGraphModel(aMetrics, aLoadout));
        panel.add(sustainedDps);
        panel.add(alphaStrike);
        return panel;
    }

    private JPanel makeLeftPanel(MessageXBar aXBar, LoadoutMetrics aMetrics) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(weaponGroups);

        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            WeaponGroupStats wgs = new WeaponGroupStats(loadout, aMetrics, aXBar, i);
            panel.add(wgs);
            weaponGroupStats[i] = wgs;
        }

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    boolean movementDirty = true;
    boolean weaponsDirty  = true;

    @Override
    public void paint(Graphics aG) {
        if (movementDirty) {
            updateMovement();
            movementDirty = false;
        }

        if (weaponsDirty) {
            updateWeaponGroups();
            weaponsDirty = false;
        }

        super.paint(aG);
    }

    @Override
    public Dimension getMaximumSize() {
        return loadoutPanel.getMaximumSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return loadoutPanel.getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return loadoutPanel.getMinimumSize();
    }

    @Override
    public void receive(Message aMsg) {
        if (!aMsg.isForMe(loadout)) {
            return;
        }

        if (aMsg instanceof EfficienciesMessage) {
            movementDirty = true;
            repaint();
        }

        if (aMsg instanceof LoadoutMessage) {
            LoadoutMessage msg = (LoadoutMessage) aMsg;
            if (msg.type == Type.WEAPON_GROUPS_CHANGED) {
                weaponsDirty = true;
                repaint();
            }
            else if (msg.type == Type.MODULES_CHANGED || msg.type == Type.UPDATE) {
                movementDirty = true;
                repaint();
            }
        }
    }

    private void updateWeaponGroups() {
        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            boolean hasGroup = loadout.getWeaponGroups().getWeapons(i, loadout).size() > 0;
            weaponGroupStats[i].setEnabled(hasGroup);
        }
    }

    private void updateMovement() {
        final MovementProfile mp = loadout.getMovementProfile();

        Collection<Modifier> modifiers = loadout.getModifiers();
        double torso_pitch = mp.getTorsoPitchMax(modifiers);
        double torso_yaw = mp.getTorsoYawMax(modifiers);
        double arm_pitch = mp.getArmPitchMax(modifiers);
        double arm_yaw = mp.getArmYawMax(modifiers);
        torsoYawDisplay.updateAngles(torso_yaw, arm_yaw);
        torsoPitchDisplay.updateAngles(torso_pitch, arm_pitch);
    }
}
