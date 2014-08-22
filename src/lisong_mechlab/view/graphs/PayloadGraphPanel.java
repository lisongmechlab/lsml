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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JCheckBox;

import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.metrics.PayloadStatistics;
import lisong_mechlab.model.metrics.TopSpeed;

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
 * @author Li Song
 */
public class PayloadGraphPanel extends ChartPanel{
   public static class Entry{
      private final String      name;
      private final ChassisBase representant;

      public Entry(Collection<ChassisBase> aCollection){
         Iterator<ChassisBase> iterator = aCollection.iterator();
         String series = iterator.next().getNameShort();
         while( iterator.hasNext() ){
            series += ",";
            ChassisBase chassi = iterator.next();
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
   private final Efficiencies      efficiencies     = new Efficiencies();
   private Collection<Entry>       chassis;

   public PayloadGraphPanel(PayloadStatistics aPayloadStatistics, final JCheckBox aSpeedTweak){
      super(makeChart(new DefaultTableXYDataset()));
      aSpeedTweak.addActionListener(new ActionListener(){
         @Override
         public void actionPerformed(ActionEvent aArg0){
            efficiencies.setSpeedTweak(aSpeedTweak.isSelected(), null);
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
         if( entry.representant instanceof ChassisStandard ){
            ChassisStandard chassisStandard = (ChassisStandard)entry.representant;
            for(int rating = chassisStandard.getEngineMin(); rating <= chassisStandard.getEngineMax(); rating += 5){
               if( rating < 100 ){
                  continue; // TODO: Remove this when they remove the engine limit.
               }
               double speed = TopSpeed.calculate(rating, chassisStandard.getMovementProfileBase(), chassisStandard.getMassMax(),
                                                 efficiencies.getSpeedModifier());
               series.add(speed, payloadStatistics.calculate(chassisStandard, rating));
            }
         }
         else{
            // Omnimech
            ChassisOmniMech chassisOmniMech = (ChassisOmniMech)entry.representant;
            Engine engine = chassisOmniMech.getFixedEngine();

            double minSpeed = TopSpeed.calculate(engine.getRating(), chassisOmniMech.getMovementProfileMin(), chassisOmniMech.getMassMax(),
                                                 efficiencies.getSpeedModifier());
            double stockSpeed = TopSpeed.calculate(engine.getRating(), chassisOmniMech.getMovementProfileStock(), chassisOmniMech.getMassMax(),
                                                   efficiencies.getSpeedModifier());
            double maxSpeed = TopSpeed.calculate(engine.getRating(), chassisOmniMech.getMovementProfileMax(), chassisOmniMech.getMassMax(),
                                                 efficiencies.getSpeedModifier());

            double payload = payloadStatistics.calculate(chassisOmniMech);
            if( minSpeed != stockSpeed ){
               series.add(minSpeed, payload);
            }
            series.add(stockSpeed, payload);
            if( maxSpeed != stockSpeed ){
               series.add(maxSpeed, payload);
            }
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
