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

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
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
 * @author Li Song
 */
public class WeaponGroupStats extends JPanel {
    private static final long serialVersionUID = 3272942854807490075L;
    private final JLabel      coolingRatio;
    private final JLabel      ghostHeat;
    private final JLabel      alphaHeat;
    private final JLabel      timeToOverheat;
    private final JLabel      alphaDamage;
    private final JLabel      burstDamage;
    private final JLabel      maxDPS;
    private final JLabel      sustDPS;

    /**
     * @param aLoadout
     * @param aMetrics
     * @param aXBar
     * @param aGroup
     * 
     */
    public WeaponGroupStats(LoadoutBase<?> aLoadout, LoadoutMetrics aMetrics, MessageXBar aXBar, int aGroup) {
        setBorder(StyleManager.sectionBorder("Group " + (aGroup + 1)));
        setLayout(new GridLayout(0, 2));

        coolingRatio = new MetricDisplay(aMetrics.groupCoolingRatio[aGroup], LoadoutInfoPanel.COOLING_RATIO_TEXT,
                LoadoutInfoPanel.COOLING_RATIO_TOOLTIP, aXBar, aLoadout, true);
        timeToOverheat = new MetricDisplay(aMetrics.groupAlphaTimeToOverHeat[aGroup],
                LoadoutInfoPanel.TIME_TO_OVERHEAT_TEXT, LoadoutInfoPanel.TIME_TO_OVERHEAT_TOOLTIP, aXBar, aLoadout);
        timeToOverheat.setHorizontalAlignment(SwingConstants.RIGHT);

        alphaHeat = new JLabel("Alpha Heat: ");
        ghostHeat = new MetricDisplay(aMetrics.groupGhostHeat[aGroup], LoadoutInfoPanel.GHOST_HEAT_TEXT,
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
        ghostHeat.setHorizontalAlignment(SwingConstants.RIGHT);

        alphaDamage = new MetricDisplay(aMetrics.groupAlphaStrike[aGroup], LoadoutInfoPanel.ALPHA_DAMAGE_TEXT,
                LoadoutInfoPanel.ALPHA_DAMAGE_TOOLTIP, aXBar, aLoadout);
        burstDamage = new MetricDisplay(aMetrics.groupBurstDamageOverTime[aGroup], LoadoutInfoPanel.BURST_DAMAGE_TEXT,
                LoadoutInfoPanel.BURST_DAMAGE_TOOLTIP, aXBar, aLoadout);
        burstDamage.setHorizontalAlignment(SwingConstants.RIGHT);

        maxDPS = new MetricDisplay(aMetrics.groupMaxDPS[aGroup], LoadoutInfoPanel.MAX_DPS_TEXT,
                LoadoutInfoPanel.MAX_DPS_TOOLTIP, aXBar, aLoadout);
        sustDPS = new MetricDisplay(aMetrics.groupMaxSustainedDPS[aGroup], LoadoutInfoPanel.SUST_DPS_TEXT,
                LoadoutInfoPanel.SUST_DPS_TOOLTIP, aXBar, aLoadout);
        sustDPS.setHorizontalAlignment(SwingConstants.RIGHT);

        add(coolingRatio);
        add(timeToOverheat);
        add(alphaHeat);
        add(ghostHeat);

        add(alphaDamage);
        add(burstDamage);
        add(maxDPS);
        add(sustDPS);
    }
    
    @Override
    public void setEnabled(boolean aEnabled) {
        for(Component c : getComponents()){
            c.setEnabled(aEnabled);
        }
        super.setEnabled(aEnabled);
    }

}
