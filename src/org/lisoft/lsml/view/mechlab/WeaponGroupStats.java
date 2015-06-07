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

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.metrics.AlphaStrike;
import org.lisoft.lsml.model.metrics.AlphaTimeToOverHeat;
import org.lisoft.lsml.model.metrics.BurstDamageOverTime;
import org.lisoft.lsml.model.metrics.CoolingRatio;
import org.lisoft.lsml.model.metrics.GhostHeat;
import org.lisoft.lsml.model.metrics.HeatCapacity;
import org.lisoft.lsml.model.metrics.HeatDissipation;
import org.lisoft.lsml.model.metrics.HeatGeneration;
import org.lisoft.lsml.model.metrics.HeatOverTime;
import org.lisoft.lsml.model.metrics.MaxDPS;
import org.lisoft.lsml.model.metrics.MaxSustainedDPS;
import org.lisoft.lsml.model.metrics.RangeMetric;
import org.lisoft.lsml.model.metrics.RangeTimeMetric;
import org.lisoft.lsml.util.message.MessageXBar;
import org.lisoft.lsml.view.MetricDisplay;
import org.lisoft.lsml.view.render.StyleManager;

/**
 * This class will visualize the interesting stats for a weapon group.
 * 
 * The stats that will be shown are:
 * <ul>
 * <li>Cooling Ratio</li>
 * <li>Ghost Heat</li>
 * <li>Seconds to Overheat</li>
 * <li>Alpha Heat %</li>
 * 
 * <li>Alpha Damage</li>
 * <li>DPS</li>
 * <li>Sustained DPS</li>
 * <li>Burst DPS</li>
 * 
 * 
 * <li>Weapons in the group</li>
 * </ul>
 * 
 * @author Emily Björk
 */
public class WeaponGroupStats extends JPanel {
    private static final long     serialVersionUID = 3272942854807490075L;
    private final JLabel          coolingRatio;
    private final JLabel          ghostHeat;
    private final JLabel          alphaHeat;
    private final JLabel          timeToOverheat;
    private final JLabel          alphaDamage;
    private final JLabel          burstDamage;
    private final JLabel          maxDPS;
    private final JLabel          sustDPS;
    private final HeatDissipation heatDissipation;
    private final HeatGeneration  heatGeneration;

    /**
     * @param aLoadout
     * @param aXBar
     * @param aHeatDissipation
     * @param aGroup
     * 
     */
    public WeaponGroupStats(LoadoutBase<?> aLoadout, MessageXBar aXBar, HeatDissipation aHeatDissipation, int aGroup) {
        setBorder(StyleManager.sectionBorder("Group " + (aGroup+1)));
        setLayout(new GridLayout(0, 2));

        heatDissipation = aHeatDissipation;
        heatGeneration = new HeatGeneration(aLoadout, aGroup);

        coolingRatio = new MetricDisplay(new CoolingRatio(heatDissipation, heatGeneration),
                LoadoutInfoPanel.COOLING_RATIO_TEXT, LoadoutInfoPanel.COOLING_RATIO_TOOLTIP, aXBar, aLoadout, true);
        ghostHeat = new MetricDisplay(new GhostHeat(aLoadout, aGroup), LoadoutInfoPanel.GHOST_HEAT_TEXT,
                LoadoutInfoPanel.GHOST_HEAT_TOOLTIP, aXBar, aLoadout) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void updateText() {
                if (metric.calculate() > 0)
                    setForeground(Color.RED);
                else
                    setForeground(coolingRatio.getForeground());
                super.updateText();
            }
        };

        final HeatCapacity heatCapacity = new HeatCapacity(aLoadout); // TODO Consolidate all metrics somewhere so we
                                                                      // don't get this duplication
        HeatOverTime heatOverTime = new HeatOverTime(aLoadout, aXBar, aGroup);
        alphaHeat = new JLabel("Alpha Heat: ");
        timeToOverheat = new MetricDisplay(new AlphaTimeToOverHeat(heatCapacity, heatOverTime, heatDissipation),
                LoadoutInfoPanel.TIME_TO_OVERHEAT_TEXT, LoadoutInfoPanel.TIME_TO_OVERHEAT_TOOLTIP, aXBar, aLoadout);

        final RangeTimeMetric metricBurstDamage = new BurstDamageOverTime(aLoadout, aXBar, aGroup);
        final RangeMetric metricAlphaStrike = new AlphaStrike(aLoadout, aGroup);
        final RangeMetric metricMaxDPS = new MaxDPS(aLoadout, aGroup);
        final RangeMetric metricSustainedDps = new MaxSustainedDPS(aLoadout, heatDissipation, aGroup);

        alphaDamage = new MetricDisplay(metricAlphaStrike, LoadoutInfoPanel.ALPHA_DAMAGE_TEXT,
                LoadoutInfoPanel.ALPHA_DAMAGE_TOOLTIP, aXBar, aLoadout);
        burstDamage = new MetricDisplay(metricBurstDamage, LoadoutInfoPanel.BURST_DAMAGE_TEXT,
                LoadoutInfoPanel.BURST_DAMAGE_TOOLTIP, aXBar, aLoadout);
        maxDPS = new MetricDisplay(metricMaxDPS, LoadoutInfoPanel.MAX_DPS_TEXT, LoadoutInfoPanel.MAX_DPS_TOOLTIP,
                aXBar, aLoadout);
        sustDPS = new MetricDisplay(metricSustainedDps, LoadoutInfoPanel.SUST_DPS_TEXT,
                LoadoutInfoPanel.SUST_DPS_TOOLTIP, aXBar, aLoadout);

        add(coolingRatio);
        add(ghostHeat);
        add(alphaHeat);
        add(timeToOverheat);

        add(alphaDamage);
        add(burstDamage);
        add(maxDPS);
        add(sustDPS);
    }

}
