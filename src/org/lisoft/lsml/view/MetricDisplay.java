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

import java.util.Formatter;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.lisoft.lsml.messages.EfficienciesMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.metrics.JumpDistance;
import org.lisoft.lsml.model.metrics.Metric;
import org.lisoft.lsml.model.metrics.RangeMetric;
import org.lisoft.lsml.model.metrics.RangeTimeMetric;

/**
 * This class is a convenience for showing the results of a metric in a JLabel.
 * 
 * @author Emily Björk
 */
public class MetricDisplay extends JLabel implements MessageReceiver {
    private static final long    serialVersionUID = 4947119462839900984L;
    private final LoadoutBase<?> loadout;
    private final Formatter      formatter;
    private final StringBuilder  sb               = new StringBuilder();
    private final String         format;
    private final boolean        percent;
    protected final Metric       metric;

    public MetricDisplay(Metric aMetric, String aFormat, String aTooltip, MessageXBar aXBar, LoadoutBase<?> aLoadout) {
        this(aMetric, aFormat, aTooltip, aXBar, aLoadout, false);
    }

    public MetricDisplay(Metric aMetric, String aFormat, String aTooltip, MessageXBar aXBar, LoadoutBase<?> aLoadout,
            boolean aPercent) {
        loadout = aLoadout;
        aXBar.attach(this);
        setToolTipText("<html><p width=\"300\">" + aTooltip + "</p></html>");
        formatter = new Formatter(sb);
        format = aFormat;
        metric = aMetric;
        percent = aPercent;

        updateText();
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg.isForMe(loadout) && (aMsg.affectsHeatOrDamage() || aMsg instanceof EfficienciesMessage)) {
            updateText();
        }
    }

    protected void updateText() {
        assert(SwingUtilities.isEventDispatchThread());
        sb.setLength(0);
        double value = metric.calculate();
        if (percent)
            value *= 100.0;
        if (metric instanceof RangeTimeMetric) {
            formatter.format(format, ((RangeTimeMetric) metric).getTime(), value,
                    ((RangeTimeMetric) metric).getRange());
        }
        else if (metric instanceof RangeMetric) {
            formatter.format(format, value, ((RangeMetric) metric).getRange());
        }
        else if (metric instanceof JumpDistance) {
            formatter.format(format, loadout.getJumpJetCount(), loadout.getJumpJetsMax(), value);
        }
        else {
            formatter.format(format, value);
        }
        if (Double.isInfinite(value)) {
            int i = sb.indexOf("Infinity");
            sb.replace(i, i + "Infinity".length(), "∞");
        }
        setText(sb.toString());
    }
}
