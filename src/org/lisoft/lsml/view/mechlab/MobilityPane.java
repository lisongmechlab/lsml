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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.metrics.ReverseSpeed;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.model.metrics.TurningSpeed;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.util.message.Message;
import org.lisoft.lsml.util.message.MessageXBar;
import org.lisoft.lsml.view.render.StyleManager;

/**
 * This {@link JPanel} will render all mobility information about a loadout.
 * 
 * @author Li Song
 *
 */
public class MobilityPane extends JPanel implements Message.Recipient {
    private static final long    serialVersionUID  = -3878482179163239207L;

    private final AngleDisplay   torsoYawDisplay   = new AngleDisplay(90.0);
    private final AngleDisplay   torsoPitchDisplay = new AngleDisplay(0.0);

    private final JLabel         torsoYawAngle     = new JLabel();
    private final JLabel         torsoPitchAngle   = new JLabel();
    private final JLabel         torsoYawSpeed     = new JLabel();
    private final JLabel         torsoPitchSpeed   = new JLabel();

    private final JLabel         armYawAngle       = new JLabel();
    private final JLabel         armPitchAngle     = new JLabel();
    private final JLabel         armYawSpeed       = new JLabel();
    private final JLabel         armPitchSpeed     = new JLabel();

    private final JLabel         speedMax          = new JLabel();
    private final JLabel         speedReverse      = new JLabel();
    private final JLabel         timeToFullSpeed   = new JLabel();
    private final JLabel         timeToFullStop    = new JLabel();

    private final LoadoutBase<?> loadout;
    private ChartPanel           turnSpeedChartPanel;

    public MobilityPane(LoadoutBase<?> aLoadout, MessageXBar aXBar, int parentWidth) {
        setLayout(new BorderLayout());

        aXBar.attach(this);
        loadout = aLoadout;
        
        add(makeTorsoPanel(), BorderLayout.NORTH);
        add(makeMovementPanel(parentWidth), BorderLayout.SOUTH);

        updatePanels();
    }

    private JPanel makeMovementPanel(int aParentWidth) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(StyleManager.sectionBorder("Movement"));

        XYSeriesCollection dataset = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart("Turn speed", "Speed [km/h]", "Turn rate [°/s]", dataset,
                PlotOrientation.VERTICAL, true, false, false);
        turnSpeedChartPanel = new ChartPanel(chart);
        turnSpeedChartPanel.setPreferredSize(new Dimension(aParentWidth/2, 200));        

        root.add(turnSpeedChartPanel, BorderLayout.EAST);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
        left.add(speedMax);
        left.add(speedReverse);
        left.add(timeToFullSpeed);
        left.add(timeToFullStop);

        root.add(left, BorderLayout.WEST);
        return root;
    }

    private JPanel makeTorsoPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(StyleManager.sectionBorder("Arms & Torso"));

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

        root.add(numbers, BorderLayout.WEST);
        root.add(diagrams, BorderLayout.EAST);
        return root;
    }

    private void updatePanels() {
        final MovementProfile mp = loadout.getMovementProfile();
        final Engine engine = loadout.getEngine();
        int rating = 0;
        if (engine != null)
            rating = engine.getRating();
        double mass = loadout.getChassis().getMassMax();

        Collection<Modifier> modifiers = loadout.getModifiers();
        double torso_pitch = mp.getTorsoPitchMax(modifiers);
        double torso_yaw = mp.getTorsoYawMax(modifiers);
        double torso_pitch_speed = mp.getTorsoPitchSpeed(modifiers) * rating / mass;
        double torso_yaw_speed = mp.getTorsoYawSpeed(modifiers) * rating / mass;

        double arm_pitch = mp.getArmPitchMax(modifiers);
        double arm_yaw = mp.getArmYawMax(modifiers);
        double arm_pitch_speed = mp.getArmPitchSpeed(modifiers) * rating / mass;
        double arm_yaw_speed = mp.getArmYawSpeed(modifiers) * rating / mass;

        double speed_max = TopSpeed.calculate(rating, mp, mass, modifiers);
        double speed_rev = ReverseSpeed.calculate(rating, mp, mass, modifiers);

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

        XYSeriesCollection turnSpeedGraph = new XYSeriesCollection();
        XYSeries turnSpeed = new XYSeries("Turn speed", true, false);

        if (rating > 0) {
            double topSpeed = TopSpeed.calculate(rating, mp, mass, modifiers);
            double lowSpeed = mp.getTurnLerpLowSpeed(modifiers);
            double midSpeed = mp.getTurnLerpMidSpeed(modifiers);
            double highSpeed = mp.getTurnLerpHighSpeed(modifiers);
            turnSpeed.add(topSpeed * lowSpeed,
                    TurningSpeed.getTurnRateAtThrottle(lowSpeed, rating, mass, mp, modifiers));
            turnSpeed.add(topSpeed * midSpeed,
                    TurningSpeed.getTurnRateAtThrottle(midSpeed, rating, mass, mp, modifiers));
            turnSpeed.add(topSpeed * highSpeed,
                    TurningSpeed.getTurnRateAtThrottle(highSpeed, rating, mass, mp, modifiers));
        }
        turnSpeedGraph.addSeries(turnSpeed);

        JFreeChart chart = ChartFactory.createXYLineChart("Turn speed", "Speed [km/h]", "Turn rate [°/s]",
                turnSpeedGraph, PlotOrientation.VERTICAL, true, false, false);
        //chart.getPlot().setBackgroundPaint(getBackground());
        chart.setBackgroundPaint(getBackground());
        turnSpeedChartPanel.setChart(chart);

        speedMax.setText("Top speed: " + LoadoutInfoPanel.df1.format(speed_max) + "m/s");
        speedReverse.setText("Rev. speed: " + LoadoutInfoPanel.df1.format(speed_rev) + "m/s");
        timeToFullSpeed.setText("Time to full speed: TBD");
        timeToFullStop.setText("Time to full stop: TBD");
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg.isForMe(loadout))
            updatePanels();
    }
}
