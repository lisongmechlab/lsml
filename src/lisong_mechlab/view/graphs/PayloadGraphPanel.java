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
package lisong_mechlab.view.graphs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JCheckBox;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.loadout.Efficiencies;
import lisong_mechlab.model.loadout.metrics.PayloadStatistics;
import lisong_mechlab.model.loadout.metrics.TopSpeed;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * Will draw a payload over speed graph for selected chassis.
 * 
 * @author Emily Björk
 */
public class PayloadGraphPanel extends ChartPanel{
   public static class Entry{
      private final String name;
      private final Chassi representant;

      public Entry(Collection<Chassi> aCollection){
         Iterator<Chassi> iterator = aCollection.iterator();
         String series = iterator.next().getNameShort();
         while( iterator.hasNext() ){
            series += ",";
            Chassi chassi = iterator.next();
            series += chassi.getNameShort().split("-")[1];
         }
         name = series;
         representant = aCollection.iterator().next();
      }

      @Override
      public String toString(){
         return name;
      }
   }

   private static final long       serialVersionUID = -5907483118809173045L;
   private final PayloadStatistics payloadStatistics;
   private final Efficiencies      efficiencies     = new Efficiencies(null);
   private Collection<Entry>       chassis;

   public PayloadGraphPanel(PayloadStatistics aPayloadStatistics, final JCheckBox aSpeedTweak){
      super(makeChart(new DefaultTableXYDataset()));
      aSpeedTweak.addActionListener(new ActionListener(){
         @Override
         public void actionPerformed(ActionEvent aArg0){
            efficiencies.setSpeedTweak(aSpeedTweak.isSelected());
            updateGraph();
         }
      });
      payloadStatistics = aPayloadStatistics;
   }

   public void selectChassis(Collection<Entry> aChassisCollection){
      chassis = aChassisCollection;
   }

   public void updateGraph(){
      DefaultTableXYDataset dataset = new DefaultTableXYDataset();
      for(Entry entry : chassis){
         XYSeries series = new XYSeries(entry.name, false, false);
         for(int rating = entry.representant.getEngineMin(); rating <= entry.representant.getEngineMax(); rating += 5){
            if( rating < 100 ){
               continue; // TODO: Remove this when they remove the engine limit.
            }
            double speed = TopSpeed.calculate(rating, entry.representant, efficiencies.getSpeedModifier());
            series.add(speed, payloadStatistics.calculate(entry.representant, rating));
         }
         dataset.addSeries(series);
      }
      setChart(makeChart(dataset));
      XYPlot plot = (XYPlot)getChart().getPlot();
      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
      renderer.setBaseShapesFilled(false);
      plot.setRenderer(renderer);
   }

   private static JFreeChart makeChart(XYDataset aDataset){
      return ChartFactory.createXYLineChart("Comparing payload tonnage for given speeds", "km/h", "payload tons", aDataset, PlotOrientation.VERTICAL,
                                            true, false, false);
   }
}
