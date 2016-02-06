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
package org.lisoft.lsml.view.graphs;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.VerticalAlignment;
import org.lisoft.lsml.messages.LoadoutMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.graphs.DamageGraphModel;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.Pair;

/**
 * This panel shows a graph with a stacked graph
 * 
 * @author Li Song
 */
public class DamageGraphPanel extends ChartPanel implements MessageReceiver {
    private final Loadout<?>                    loadout;
    private final WeaponColouredDrawingSupplier colours = new WeaponColouredDrawingSupplier();
    private final DamageGraphModel              model;

    /**
     * Creates and displays the {@link DamageGraphWindow}.
     * 
     * @param aLoadout
     *            Which load out the diagram is for.
     * @param aXBar
     *            A {@link MessageXBar} to listen for changes to the loadout on.
     * @param aModel
     *            The model to use for drawing the graph.
     */
    public DamageGraphPanel(Loadout<?> aLoadout, MessageXBar aXBar, DamageGraphModel aModel) {
        super(ChartFactory.createStackedXYAreaChart(aModel.getTitle(), aModel.getXAxisLabel(), aModel.getYAxisLabel(),
                new DefaultTableXYDataset(), PlotOrientation.VERTICAL, true, true, false));
        aXBar.attach(this);
        loadout = aLoadout;
        model = aModel;

        LegendTitle legendTitle = getChart().getLegend();
        legendTitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        legendTitle.setVerticalAlignment(VerticalAlignment.TOP);

        getChart().getPlot().setDrawingSupplier(colours);

        XYTitleAnnotation titleAnnotation = new XYTitleAnnotation(0.98, 0.98, legendTitle, RectangleAnchor.TOP_RIGHT);
        titleAnnotation.setMaxWidth(0.4);
        ((XYPlot) (getChart().getPlot())).addAnnotation(titleAnnotation);
        getChart().removeLegend();

        StyleManager.styleSmallGraph(getChart(), getBackground());
    }

    private void update() {
        Map<Weapon, List<Pair<Double, Double>>> data = model.getData();
        List<Weapon> orderedWeapons = new ArrayList<>();
        DefaultTableXYDataset dataset = (DefaultTableXYDataset) getChart().getXYPlot().getDataset();
        dataset.removeAllSeries();
        for (Map.Entry<Weapon, List<Pair<Double, Double>>> entry : data.entrySet()) {
            XYSeries series = new XYSeries(entry.getKey().getName(), true, false);
            for (Pair<Double, Double> pair : entry.getValue()) {
                series.add(pair.first, pair.second);
            }
            dataset.addSeries(series);
            orderedWeapons.add(entry.getKey());
        }

        Collections.reverse(orderedWeapons);
        colours.updateColoursToMatch(orderedWeapons);
    }

    boolean dirty = true;

    @Override
    public void paint(Graphics aG) {
        if (dirty) {
            update();
            dirty = false;
        }
        super.paint(aG);
    }

    @Override
    public void receive(Message aMsg) {
        if (!aMsg.isForMe(loadout))
            return;

        boolean needsUpdate = aMsg.affectsHeatOrDamage();

        if (aMsg instanceof LoadoutMessage) {
            LoadoutMessage msg = (LoadoutMessage) aMsg;
            needsUpdate |= msg.affectsRange();
        }

        if (needsUpdate) {
            dirty = true;
            repaint();
        }
    }
}
