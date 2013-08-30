package lisong_mechlab.view.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JFrame;

import lisong_mechlab.Pair;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.metrics.HeatDissipation;
import lisong_mechlab.model.loadout.metrics.MaxSustainedDPS;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * <p>
 * Presents a graph of damage over range for a given load out.
 * <p>
 * TODO: The calculation part should be extracted and unit tested!
 * 
 * @author Li Song
 */
public class DamageGraph extends JFrame{
   private static final long     serialVersionUID = -8812749194029184861L;
   private final Loadout         loadout;
   private final MaxSustainedDPS maxSustainedDPS;

   /**
    * Creates and displays the {@link DamageGraph}.
    * 
    * @param aTitle
    *           The title for the diagram.
    * @param aLoadout
    *           Which load out the diagram is for.
    */
   public DamageGraph(String aTitle, Loadout aLoadout){
      super(aTitle + " for " + aLoadout);

      loadout = aLoadout;
      maxSustainedDPS = new MaxSustainedDPS(loadout, new HeatDissipation(loadout));
      final JFreeChart chart = ChartFactory.createStackedXYAreaChart(aTitle + " for " + aLoadout, "range [m]", "damage / second", getSeries(),
                                                                     PlotOrientation.VERTICAL, true, true, false);
      final ChartPanel chartPanel = new ChartPanel(chart);
      setContentPane(chartPanel);

      setSize(800, 600);
      setVisible(true);
   }

   /**
    * <p>
    * Calculates a list of X-coordinates at which the weapon balance needs to be recalculated.
    * <p>
    * In essence, this is a unique sorted list of the union of all min/long/max ranges for the weapons.
    * 
    * @return A {@link SortedSet} with {@link Double}s for the ranges.
    */
   private SortedSet<Double> getRangeIntervals(){
      SortedSet<Double> ans = new TreeSet<>();

      ans.add(Double.valueOf(0.0));
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon && item != ItemDB.lookup("ANTI-MISSILE SYSTEM") ){
            Weapon weapon = (Weapon)item;
            ans.add(weapon.getRangeMin());
            ans.add(weapon.getRangeLong());
            ans.add(weapon.getRangeMax());
         }
      }
      return ans;
   }

   private TableXYDataset getSeries(){
      Map<Weapon, List<Pair<Double, Double>>> data = new HashMap<Weapon, List<Pair<Double, Double>>>();
      for(Double range : getRangeIntervals()){
         for(Map.Entry<Weapon, Double> entry : maxSustainedDPS.getDamageDistribution(range).entrySet()){
            Weapon weapon = entry.getKey();
            double ratio = entry.getValue();
            double dps = weapon.getStat("d/s");

            if( !data.containsKey(weapon) ){
               data.put(weapon, new ArrayList<Pair<Double, Double>>());
            }
            data.get(weapon).add(new Pair<Double, Double>(range, dps * ratio));
         }
      }

      DefaultTableXYDataset dataset = new DefaultTableXYDataset();
      for(Map.Entry<Weapon, List<Pair<Double, Double>>> entry : data.entrySet()){
         XYSeries series = new XYSeries(entry.getKey().getName(loadout.getUpgrades()), true, false);
         for(Pair<Double, Double> pair : entry.getValue()){
            series.add(pair.first, pair.second);
         }
         dataset.addSeries(series);
      }
      return dataset;
   }
}
