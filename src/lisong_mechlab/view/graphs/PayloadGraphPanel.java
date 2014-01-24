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

import java.util.Collection;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.loadout.metrics.PayloadStatistics;
import lisong_mechlab.model.loadout.metrics.TopSpeed;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * Will draw a payload over speed graph for selected chassis.
 * 
 * @author Li Song
 */
public class PayloadGraphPanel extends ChartPanel{
   private final PayloadStatistics payloadStatistics;

   private static final long  serialVersionUID = -5907483118809173045L;
   private Collection<Chassi> chassis;

   public PayloadGraphPanel(PayloadStatistics aPayloadStatistics){
      super(makeChart(new DefaultTableXYDataset()));
      payloadStatistics = aPayloadStatistics;
   }

   public void selectChassis(Collection<Chassi> aChassisCollection){
      chassis = aChassisCollection;
      updateGraph();
   }
   
   private void updateGraph(){
      DefaultTableXYDataset dataset = new DefaultTableXYDataset();
      for(Chassi chassi: chassis){
         XYSeries series = new XYSeries(chassi.getName(), false, false);
         for(int rating = chassi.getEngineMin(); rating <= chassi.getEngineMax(); rating += 5){
            if(rating < 100){
               continue; // TODO: Remove this when they remove the engine limit.
            }
            double speed = TopSpeed.calculate(rating, chassi, 1.0);
            series.add(speed, payloadStatistics.calculate(chassi, rating));
         }
         dataset.addSeries(series);
      }
      
      setChart(makeChart(dataset));
      
   }
   
   private static JFreeChart makeChart(XYDataset aDataset){
      return ChartFactory.createXYLineChart("Comparing payload tonnage for given speeds", "km/h", "payload tons", aDataset, PlotOrientation.VERTICAL, true, false, false);
   }
}
