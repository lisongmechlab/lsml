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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.VerticalAlignment;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutMessage;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.util.WeaponRanges;
import org.lisoft.lsml.util.message.Message;
import org.lisoft.lsml.util.message.MessageXBar;
import org.lisoft.lsml.view.ProgramInit;

/**
 * <p>
 * Presents a graph of damage over range for a given load out.
 * <p>
 * TODO: The calculation part should be extracted and unit tested!
 * 
 * @author Li Song
 */
public class DpsGraph extends JFrame implements Message.Recipient {
    private static final long    serialVersionUID = -8812749194029184861L;
    private final LoadoutBase<?> loadout;
    private final ChartPanel     chartPanel;
    private final WeaponColouredDrawingSupplier colours          = new WeaponColouredDrawingSupplier();

    JFreeChart makechart() {
        JFreeChart chart =  ChartFactory.createStackedXYAreaChart("Max DPS over range for " + loadout, "range [m]",
                "damage / second", getSeries(), PlotOrientation.VERTICAL, true, true, false);
        chart.getPlot().setDrawingSupplier(colours);

        chart.getLegend().setHorizontalAlignment(HorizontalAlignment.RIGHT);
        chart.getLegend().setVerticalAlignment(VerticalAlignment.TOP);
        
        LegendTitle legendTitle = chart.getLegend();
        XYTitleAnnotation titleAnnotation = new XYTitleAnnotation(0.98, 0.98, legendTitle, RectangleAnchor.TOP_RIGHT);
        titleAnnotation.setMaxWidth(0.4);
        ((XYPlot) (chart.getPlot())).addAnnotation(titleAnnotation);
        chart.removeLegend();

        return chart;
    }

    /**
     * Creates and displays the {@link DpsGraph}.
     * 
     * @param aLoadout
     *            Which load out the diagram is for.
     * @param aXbar
     *            A {@link MessageXBar} to listen for changes to the loadout on.
     */
    public DpsGraph(LoadoutBase<?> aLoadout, MessageXBar aXbar) {
        super("Max DPS over range for " + aLoadout);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        aXbar.attach(this);

        loadout = aLoadout;
        chartPanel = new ChartPanel(makechart());
        setContentPane(chartPanel);

        setIconImage(ProgramInit.programIcon);
        setSize(800, 600);
        setVisible(true);
    }

    private TableXYDataset getSeries() {
        final Collection<Modifier> modifiers = loadout.getModifiers();
        SortedMap<Weapon, Integer> multiplicity = new TreeMap<Weapon, Integer>(
                new Comparator<Weapon>() {
                    @Override
                    public int compare(Weapon aO1, Weapon aO2) {
                        int comp = Double.compare(aO2.getRangeMax(modifiers), aO1.getRangeMax(modifiers));
                        if (comp == 0)
                            return aO1.compareTo(aO2);
                        return comp;
                    }
                });
        
        for(Weapon weapon : loadout.items(Weapon.class)){
            if(!weapon.isOffensive())
                continue;
            if(!multiplicity.containsKey(weapon)){
                multiplicity.put(weapon, 0);
            }
            int v = multiplicity.get(weapon);
            multiplicity.put(weapon, v+1);
        }

        List<Weapon> orderedWeapons = new ArrayList<>();
        Double[] ranges = WeaponRanges.getRanges(multiplicity.keySet(), modifiers);
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        for(Map.Entry<Weapon, Integer> e : multiplicity.entrySet()){
            Weapon weapon = e.getKey();
            int mult = e.getValue();
            
            XYSeries series = new XYSeries(weapon.getName(), true, false);
            for (double range : ranges) {               
                final double dps = weapon.getStat("d/s", modifiers);
                final double rangeEff = weapon.getRangeEffectivity(range, modifiers);
                series.add(range, dps * rangeEff*mult);
            }
            dataset.addSeries(series);
            orderedWeapons.add(e.getKey());
        }
        Collections.reverse(orderedWeapons);        
        colours.updateColoursToMatch(orderedWeapons);
        return dataset;
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
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    chartPanel.setChart(makechart());
                }
            });
        }
    }
}
