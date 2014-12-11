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
package lisong_mechlab.view.graphs;

import java.awt.Component;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutMessage;
import lisong_mechlab.model.metrics.MaxSustainedDPS;
import lisong_mechlab.model.modifiers.Modifier;
import lisong_mechlab.util.Pair;
import lisong_mechlab.util.WeaponRanges;
import lisong_mechlab.util.message.Message;
import lisong_mechlab.util.message.MessageXBar;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.action.OpenHelp;

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

/**
 * <p>
 * Presents a graph of damage over range for a given load out.
 * <p>
 * TODO: The calculation part should be extracted and unit tested!
 * 
 * @author Li Song
 */
public class DamageGraph extends JFrame implements Message.Recipient {
    private static final long     serialVersionUID = -8812749194029184861L;
    private final LoadoutBase<?>  loadout;
    private final MaxSustainedDPS maxSustainedDPS;
    private final ChartPanel      chartPanel;

    JFreeChart makechart() {
        return ChartFactory.createStackedXYAreaChart("Max Sustained DPS over range for " + loadout, "range [m]",
                "damage / second", getSeries(), PlotOrientation.VERTICAL, true, true, false);
    }

    /**
     * Creates and displays the {@link DamageGraph}.
     * 
     * @param aLoadout
     *            Which load out the diagram is for.
     * @param anXbar
     *            A {@link MessageXBar} to listen for changes to the loadout on.
     * @param aMaxSustainedDpsMetric
     *            A {@link MaxSustainedDPS} instance to use in calculation.
     */
    public DamageGraph(LoadoutBase<?> aLoadout, MessageXBar anXbar, MaxSustainedDPS aMaxSustainedDpsMetric) {
        super("Max Sustained DPS over range for " + aLoadout);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        anXbar.attach(this);

        loadout = aLoadout;
        maxSustainedDPS = aMaxSustainedDpsMetric;
        chartPanel = new ChartPanel(makechart());
        setContentPane(chartPanel);
        chartPanel.getChart().getLegend().setHorizontalAlignment(HorizontalAlignment.RIGHT);
        chartPanel.getChart().getLegend().setVerticalAlignment(VerticalAlignment.TOP);

        LegendTitle legendTitle = chartPanel.getChart().getLegend();
        XYTitleAnnotation titleAnnotation = new XYTitleAnnotation(0.98, 0.98, legendTitle, RectangleAnchor.TOP_RIGHT);
        titleAnnotation.setMaxWidth(0.4);
        ((XYPlot) (chartPanel.getChart().getPlot())).addAnnotation(titleAnnotation);
        chartPanel.getChart().removeLegend();

        chartPanel.setLayout(new OverlayLayout(chartPanel));
        JButton button = new JButton(new OpenHelp("What is this?", "Max-sustained-dps-graph",
                KeyStroke.getKeyStroke('w')));
        button.setMargin(new Insets(10, 10, 10, 10));
        button.setFocusable(false);
        button.setAlignmentX(Component.RIGHT_ALIGNMENT);
        button.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        chartPanel.add(button);

        setIconImage(ProgramInit.programIcon);
        setSize(800, 600);
        setVisible(true);
    }

    private TableXYDataset getSeries() {
        final Collection<Modifier> modifiers = loadout.getModifiers();
        SortedMap<Weapon, List<Pair<Double, Double>>> data = new TreeMap<Weapon, List<Pair<Double, Double>>>(
                new Comparator<Weapon>() {
                    @Override
                    public int compare(Weapon aO1, Weapon aO2) {
                        int comp = Double.compare(aO2.getRangeMax(modifiers), aO1.getRangeMax(modifiers));
                        if (comp == 0)
                            return aO1.compareTo(aO2);
                        return comp;
                    }
                });

        Double[] ranges = WeaponRanges.getRanges(loadout);
        for (double range : ranges) {
            Set<Entry<Weapon, Double>> damageDistributio = maxSustainedDPS.getWeaponRatios(range).entrySet();
            for (Map.Entry<Weapon, Double> entry : damageDistributio) {
                final Weapon weapon = entry.getKey();
                final double ratio = entry.getValue();
                final double dps = weapon.getStat("d/s", modifiers);
                final double rangeEff = weapon.getRangeEffectivity(range, modifiers);

                if (!data.containsKey(weapon)) {
                    data.put(weapon, new ArrayList<Pair<Double, Double>>());
                }
                data.get(weapon).add(new Pair<Double, Double>(range, dps * ratio * rangeEff));
            }
        }

        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        for (Map.Entry<Weapon, List<Pair<Double, Double>>> entry : data.entrySet()) {
            XYSeries series = new XYSeries(entry.getKey().getName(), true, false);
            for (Pair<Double, Double> pair : entry.getValue()) {
                series.add(pair.first, pair.second);
            }
            dataset.addSeries(series);
        }
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
