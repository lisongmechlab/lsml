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
package lisong_mechlab.view;

import java.util.Formatter;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.metrics.Metric;
import lisong_mechlab.model.loadout.metrics.RangeMetric;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.MessageXBar.Reader;

/**
 * This class is a convenience for showing the results of a metric in a JLabel.
 * 
 * @author Li Song
 */
public class MetricDisplay extends JLabel implements Reader{
   private static final long   serialVersionUID = 4947119462839900984L;
   private final Loadout       loadout;
   private final Formatter     formatter;
   private final StringBuilder sb               = new StringBuilder();
   private final String        format;
   private final boolean       percent;
   protected final Metric      metric;

   public MetricDisplay(Metric aMetric, String aFormat, String aTooltip, MessageXBar anXBar, Loadout aLoadout){
      this(aMetric, aFormat, aTooltip, anXBar, aLoadout, false);
   }

   public MetricDisplay(Metric aMetric, String aFormat, String aTooltip, MessageXBar anXBar, Loadout aLoadout, boolean aPercent){
      loadout = aLoadout;
      anXBar.attach(this);
      setToolTipText(aTooltip);
      formatter = new Formatter(sb);
      format = aFormat;
      metric = aMetric;
      percent = aPercent;

      updateText();
   }

   @Override
   public void receive(Message aMsg){
      if( aMsg.isForMe(loadout) ){
         updateText();
      }
   }

   protected void updateText(){
      assert (SwingUtilities.isEventDispatchThread());
      sb.setLength(0);
      double value = metric.calculate();
      if( percent )
         value *= 100.0;
      if( metric instanceof RangeMetric ){
         formatter.format(format, value, ((RangeMetric)metric).getRange());
      }
      else{
         formatter.format(format, value);
      }
      setText(sb.toString());
   }
}
