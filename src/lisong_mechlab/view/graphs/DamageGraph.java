package lisong_mechlab.view.graphs;

import java.awt.Component;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Efficiencies;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.LoadoutPart;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.loadout.metrics.HeatDissipation;
import lisong_mechlab.model.loadout.metrics.MaxSustainedDPS;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.Pair;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.action.OpenHelp;

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
public class DamageGraph extends JFrame implements MessageXBar.Reader{
   private static final long     serialVersionUID = -8812749194029184861L;
   private final Loadout         loadout;
   private final MaxSustainedDPS maxSustainedDPS;
   private final ChartPanel      chartPanel;

   JFreeChart makechart(){
      return ChartFactory.createStackedXYAreaChart("Max Sustained DPS over range for " + loadout, "range [m]", "damage / second", getSeries(),
                                                   PlotOrientation.VERTICAL, true, true, false);
   }

   /**
    * Creates and displays the {@link DamageGraph}.
    * 
    * @param aTitle
    *           The title for the diagram.
    * @param aLoadout
    *           Which load out the diagram is for.
    */
   public DamageGraph(Loadout aLoadout, MessageXBar anXbar){
      super("Max Sustained DPS over range for " + aLoadout);

      anXbar.attach(this);

      loadout = aLoadout;
      maxSustainedDPS = new MaxSustainedDPS(loadout, new HeatDissipation(loadout));
      chartPanel = new ChartPanel(makechart());
      setContentPane(chartPanel);

      chartPanel.setLayout(new OverlayLayout(chartPanel));
      JButton button = new JButton(new OpenHelp("What is this?", "Max-sustained-dps-graph", KeyStroke.getKeyStroke('w')));
      button.setMargin(new Insets(10, 10, 10, 10));
      button.setFocusable(false);
      button.setAlignmentX(Component.RIGHT_ALIGNMENT);
      button.setAlignmentY(Component.BOTTOM_ALIGNMENT);
      chartPanel.add(button);

      setIconImage(ProgramInit.programIcon);
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
   private Double[] getRangeIntervals(){
      SortedSet<Double> ans = new TreeSet<>();

      ans.add(Double.valueOf(0.0));
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon && item != ItemDB.AMS ){
            Weapon weapon = (Weapon)item;
            ans.add(weapon.getRangeZero());
            ans.add(weapon.getRangeMin());
            ans.add(weapon.getRangeLong());
            ans.add(weapon.getRangeMax());
         }
      }
      return ans.toArray(new Double[ans.size()]);
   }

   private TableXYDataset getSeries(){
      SortedMap<Weapon, List<Pair<Double, Double>>> data = new TreeMap<Weapon, List<Pair<Double, Double>>>(new Comparator<Weapon>(){
         @Override
         public int compare(Weapon aO1, Weapon aO2){
            int comp = Double.compare(aO2.getRangeMax(), aO1.getRangeMax());
            if( comp == 0 )
               return aO1.compareTo(aO2);
            return comp;
         }
      });

      Double[] ranges = getRangeIntervals();
      for(double range : ranges){
         Set<Entry<Weapon, Double>> damageDistributio = maxSustainedDPS.getWeaponRatios(range).entrySet();
         for(Map.Entry<Weapon, Double> entry : damageDistributio){
            Weapon weapon = entry.getKey();
            double ratio = entry.getValue();
            double dps = weapon.getStat("d/s", loadout.getUpgrades());

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

   @Override
   public void receive(Message aMsg){
      if( aMsg instanceof LoadoutPart.Message ){
         LoadoutPart.Message msg = (LoadoutPart.Message)aMsg;
         if( !loadout.getPartLoadOuts().contains(msg.part) )
            return;

         if( msg.type == LoadoutPart.Message.Type.ArmorChanged )
            return;
      }
      else if( aMsg instanceof Upgrades.Message ){
         Upgrades.Message msg = (Upgrades.Message)aMsg;
         if( msg.source != loadout.getUpgrades() )
            return;
         if( msg.msg != Upgrades.Message.ChangeMsg.HEATSINKS )
            return;
      }
      else if( aMsg instanceof Efficiencies.Message ){
         Efficiencies.Message msg = (Efficiencies.Message)aMsg;
         if( msg.efficiencies != loadout.getEfficiencies() )
            return;
      }
      else{
         return;
      }

      SwingUtilities.invokeLater(new Runnable(){
         @Override
         public void run(){
            chartPanel.setChart(makechart());
         }
      });
   }
}
