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
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutMessage;
import org.lisoft.lsml.model.loadout.LoadoutMessage.Type;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.util.message.Message;
import org.lisoft.lsml.util.message.MessageXBar;
import org.lisoft.lsml.view.graphs.DamageGraphPanel;
import org.lisoft.lsml.view.render.StyleManager;

/**
 * This panel will show detailed statistics about a BattleMech's performance.
 * 
 * @author Emily Björk
 */
public class MechStatisticsPanel extends JPanel implements Message.Recipient {
    private final LoadoutPanel        loadoutPanel;
    private final WeaponGroupingPanel weaponGroups;
    private final WeaponGroupStats    weaponGroupStats[] = new WeaponGroupStats[WeaponGroups.MAX_GROUPS];
    private final LoadoutBase<?>      loadout;

    private final AngleDisplay torsoYawDisplay   = new AngleDisplay(90.0);
    private final AngleDisplay torsoPitchDisplay = new AngleDisplay(0.0);
    private final JLabel       torsoYawAngle     = new JLabel();
    private final JLabel       torsoPitchAngle   = new JLabel();
    private final JLabel       torsoYawSpeed     = new JLabel();
    private final JLabel       torsoPitchSpeed   = new JLabel();
    private final JLabel       armYawAngle       = new JLabel();
    private final JLabel       armPitchAngle     = new JLabel();
    private final JLabel       armYawSpeed       = new JLabel();
    private final JLabel       armPitchSpeed     = new JLabel();

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
    public MechStatisticsPanel(LoadoutBase<?> aLoadout, MessageXBar aXBar, LoadoutMetrics aMetrics,
            LoadoutPanel aLoadoutPanel) {
        loadout = aLoadout;
        loadoutPanel = aLoadoutPanel;
        weaponGroups = new WeaponGroupingPanel(aLoadout.getWeaponGroups(), aLoadout, aXBar);
        aXBar.attach(this);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        add(makeLeftPanel(aXBar, aMetrics));
        add(makeRightPanel(aLoadout, aMetrics, aXBar));
        updateWeaponGroups();
        updateMovement();
    }

    private Component makeRightPanel(LoadoutBase<?> aLoadout, LoadoutMetrics aMetrics, MessageXBar aXBar) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        {
            JPanel armsAndTorso = new JPanel(new BorderLayout());
            armsAndTorso.setBorder(StyleManager.sectionBorder("Arms & Torso"));

            JPanel diagrams = new JPanel();
            diagrams.add(torsoYawDisplay);
            diagrams.add(torsoPitchDisplay);

            JPanel numbers = new JPanel();
            numbers.setLayout(new BoxLayout(numbers, BoxLayout.PAGE_AXIS));
            numbers.add(torsoYawAngle);
            numbers.add(torsoPitchAngle);
            numbers.add(torsoYawSpeed);
            numbers.add(torsoPitchSpeed);
            numbers.add(armYawAngle);
            numbers.add(armPitchAngle);
            numbers.add(armYawSpeed);
            numbers.add(armPitchSpeed);

            armsAndTorso.add(numbers, BorderLayout.WEST);
            armsAndTorso.add(diagrams, BorderLayout.EAST);
            panel.add(armsAndTorso);
        }

        DamageGraphPanel sustainedDps = new DamageGraphPanel(aLoadout, aXBar,
                new SustainedDpsGraphModel(aMetrics, aLoadout), "Sustained DPS", "Range [m]", "DPS");
        DamageGraphPanel alphaStrike = new DamageGraphPanel(aLoadout, aXBar,
                new AlphaStrikeGraphModel(aMetrics, aLoadout), "Alpha Strike", "Range [m]", "Damage");
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

        if (aMsg instanceof LoadoutMessage) {
            LoadoutMessage msg = (LoadoutMessage) aMsg;
            if (msg.type == Type.WEAPON_GROUPS_CHANGED) {
                updateWeaponGroups();
            }
            else if (msg.type == Type.MODULES_CHANGED || msg.type == Type.UPDATE) {
                updateMovement();
            }
        }
    }

    private void updateWeaponGroups() {
        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            boolean hasGroup = loadout.getWeaponGroups().getWeapons(i).size() > 0;
            weaponGroupStats[i].setEnabled(hasGroup);
        }
    }

    private void updateMovement() {
        final MovementProfile mp = loadout.getMovementProfile();
        final Engine engine = loadout.getEngine();
        int rating = 0;
        if (engine != null)
            rating = engine.getRating();
        double mass = loadout.getChassis().getMassMax();

        // TODO: These should be metrics
        Collection<Modifier> modifiers = loadout.getModifiers();
        double torso_pitch = mp.getTorsoPitchMax(modifiers);
        double torso_yaw = mp.getTorsoYawMax(modifiers);
        double torso_pitch_speed = mp.getTorsoPitchSpeed(modifiers) * rating / mass;
        double torso_yaw_speed = mp.getTorsoYawSpeed(modifiers) * rating / mass;

        double arm_pitch = mp.getArmPitchMax(modifiers);
        double arm_yaw = mp.getArmYawMax(modifiers);
        double arm_pitch_speed = mp.getArmPitchSpeed(modifiers) * rating / mass;
        double arm_yaw_speed = mp.getArmYawSpeed(modifiers) * rating / mass;

        torsoYawDisplay.updateAngles(torso_yaw, arm_yaw);
        torsoPitchDisplay.updateAngles(torso_pitch, arm_pitch);

        torsoYawAngle.setText("Torso yaw angle: " + LoadoutInfoPanel.df1.format(torso_yaw) + "°");
        torsoPitchAngle.setText("Torso pitch angle: " + LoadoutInfoPanel.df1.format(torso_pitch) + "°");
        torsoYawSpeed.setText("Torso yaw speed: " + LoadoutInfoPanel.df1.format(torso_yaw_speed) + "°/s");
        torsoPitchSpeed.setText("Torso pitch speed: " + LoadoutInfoPanel.df1.format(torso_pitch_speed) + "°/s");

        armYawAngle.setText("Arm yaw angle: " + LoadoutInfoPanel.df1.format(arm_yaw) + "°");
        armPitchAngle.setText("Arm pitch angle: " + LoadoutInfoPanel.df1.format(arm_pitch) + "°");
        armYawSpeed.setText("Arm yaw speed: " + LoadoutInfoPanel.df1.format(arm_yaw_speed) + "°/s");
        armPitchSpeed.setText("Arm pitch speed: " + LoadoutInfoPanel.df1.format(arm_pitch_speed) + "°/s");
    }
}
