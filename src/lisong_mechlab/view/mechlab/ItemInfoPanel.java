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
package lisong_mechlab.view.mechlab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.upgrades.Upgrades;

/**
 * This class implements a panel that will show information about an item.
 * 
 * @author Li Song
 */
public class ItemInfoPanel extends JPanel{
   private static final long serialVersionUID = -1180217243714551398L;

   // Basic common info
   private final JLabel      slots            = new JLabel();
   private final JLabel      mass             = new JLabel();
   private final JLabel      health           = new JLabel();
   private final JTextArea   description      = new JTextArea();

   // Engine slots
   private final JLabel      engineInternalHS = new JLabel();
   private final JLabel      engineHSSlots    = new JLabel();

   // Weapon info
   private final JLabel      damage           = new JLabel();
   private final JLabel      heat             = new JLabel();
   private final JLabel      heatPerSecond    = new JLabel();
   private final JLabel      cycleTime        = new JLabel();
   private final JLabel      secondsPerShot   = new JLabel();
   private final JLabel      burntime         = new JLabel();
   private final JLabel      gh_MaxFreeAlpha  = new JLabel();
   private final JLabel      range            = new JLabel();
   private final JLabel      dps              = new JLabel();
   private final JLabel      dph              = new JLabel();
   // Ammo info
   private final JLabel      ammoperton       = new JLabel();

   public ItemInfoPanel(){
      // setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setLayout(new BorderLayout());
      setBorder(new TitledBorder("Information"));

      gh_MaxFreeAlpha.setToolTipText("The maximum number of weapons in this group that may be fired simultaneously without incurring ghost heat.");
      secondsPerShot.setToolTipText("Shows how many seconds between shots. "
                                    + "For weapons which can double fire, such as the UAC/5, this includes double fire with chance of jamming. "
                                    + "For beam weapons this incudes the beam's burn time.");
      cycleTime.setToolTipText("The time it takes for the weapon to cool down before the next shot.");
      burntime.setToolTipText("The time the beam is active and needs to be kept on the target to deal full damage. After this the weapon starts to cool down.");
     
      {
         JPanel stats = new JPanel();
         add(stats, BorderLayout.NORTH);
         stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
         
         JPanel basicInfo = new JPanel();
         basicInfo.setLayout(new BoxLayout(basicInfo, BoxLayout.X_AXIS));
         basicInfo.setAlignmentX(LEFT_ALIGNMENT);
         basicInfo.add(slots);
         basicInfo.add(Box.createHorizontalGlue());
         basicInfo.add(mass);
         basicInfo.add(Box.createHorizontalGlue());
         basicInfo.add(health);
         stats.add(basicInfo);

         JPanel heatInfo = new JPanel();
         heatInfo.setLayout(new BoxLayout(heatInfo, BoxLayout.X_AXIS));
         heatInfo.setAlignmentX(LEFT_ALIGNMENT);
         heatInfo.add(heat);
         heatInfo.add(Box.createHorizontalGlue());
         heatInfo.add(dph);
         stats.add(heatInfo);

         JPanel rangeInfo = new JPanel();
         rangeInfo.setLayout(new BoxLayout(rangeInfo, BoxLayout.X_AXIS));
         rangeInfo.setAlignmentX(LEFT_ALIGNMENT);
         rangeInfo.add(range);
         stats.add(rangeInfo);

         JPanel damageInfo = new JPanel();
         damageInfo.setLayout(new BoxLayout(damageInfo, BoxLayout.X_AXIS));
         damageInfo.setAlignmentX(LEFT_ALIGNMENT);
         damageInfo.add(damage);
         damageInfo.add(Box.createHorizontalGlue());
         damageInfo.add(burntime);
         damageInfo.add(Box.createHorizontalGlue());
         damageInfo.add(dps);
         stats.add(damageInfo);
         
         JPanel damageInfo2 = new JPanel();
         damageInfo2.setLayout(new BoxLayout(damageInfo2, BoxLayout.X_AXIS));
         damageInfo2.setAlignmentX(LEFT_ALIGNMENT);
         damageInfo2.add(cycleTime);
         damageInfo2.add(Box.createHorizontalGlue());
         damageInfo2.add(secondsPerShot);
         stats.add(damageInfo2);

         stats.add(gh_MaxFreeAlpha);
         stats.add(ammoperton);
         stats.add(engineHSSlots);
         stats.add(engineInternalHS);
      }

      description.setColumns(30);
      description.setRows(5);
      description.setWrapStyleWord(true);
      description.setEditable(false);
      description.setFocusable(false);
      description.setLineWrap(true);
      description.setMinimumSize(new Dimension(200, 50));
      description.setPreferredSize(new Dimension(400, 50));
      description.setMaximumSize(new Dimension(2000, 200));
      description.setAlignmentX(LEFT_ALIGNMENT);
      description.setFont(slots.getFont());
      description.setBackground(slots.getBackground());

      add(description, BorderLayout.CENTER);

      showItem(null, null, null);
   }

   private void showBasicInfo(Item anItem){
      if( null != anItem ){
         ((TitledBorder)getBorder()).setTitle(anItem.getName());
         repaint();
         slots.setText("Slots: " + anItem.getNumCriticalSlots(null));
         mass.setText("Tons: " + anItem.getMass(null));
         description.setText("Description:\n" + anItem.getDescription());
         health.setText("HP: " + anItem.getHealth());
      }
      else{
         ((TitledBorder)getBorder()).setTitle("No item selected");
         repaint();
         slots.setText("Slots: N/A");
         mass.setText("Tons: N/A");
         description.setText("Description:\nN/A");
         health.setText("HP: N/A");
      }
   }

   private void showEngineInfo(Engine anEngine){
      engineInternalHS.setVisible(true);
      engineInternalHS.setText("Internal heat sinks: " + anEngine.getNumInternalHeatsinks());
      engineHSSlots.setVisible(true);
      engineHSSlots.setText("Heat sink slots: " + anEngine.getNumHeatsinkSlots());
   }

   private void showAmmoInfo(Ammunition anAmmo){
      ammoperton.setVisible(true);
      ammoperton.setText("Ammo per ton: " + anAmmo.getShotsPerTon());
   }

   private void showWeaponInfo(Weapon aWeapon, Upgrades anUpgrades, Efficiencies aEfficiencies){
      DecimalFormat df0 = new DecimalFormat("###");
      DecimalFormat df1 = new DecimalFormat("###.#");
      DecimalFormat df2 = new DecimalFormat("###.##");

      damage.setVisible(true);
      damage.setText("Damage: " + aWeapon.getDamagePerShot());
      heat.setVisible(true);
      heat.setText("Heat: " + aWeapon.getHeat());
      gh_MaxFreeAlpha.setVisible(true);
      gh_MaxFreeAlpha.setText("Max free alpha: "
                              + df0.format((aWeapon.getGhostHeatGroup() >= 0) ? aWeapon.getGhostHeatMaxFreeAlpha() : Double.POSITIVE_INFINITY));

      cycleTime.setVisible(true);
      cycleTime.setText("Cycle time: " + df2.format(aWeapon.getCycleTime(aEfficiencies)));
      if( aWeapon instanceof EnergyWeapon ){
         burntime.setVisible(true);
         burntime.setText("Burn time: " + df1.format(((EnergyWeapon)aWeapon).getDuration()));
      }
      secondsPerShot.setVisible(true);
      secondsPerShot.setText("RoF: " + df2.format(aWeapon.getSecondsPerShot(aEfficiencies)) + " s/shot");

      heatPerSecond.setVisible(true);
      heatPerSecond.setText("HPS: " + df1.format(aWeapon.getStat("h/s", anUpgrades, aEfficiencies)));

      dps.setVisible(true);
      dps.setText("DPS: " + df1.format(aWeapon.getStat("d/s", anUpgrades, aEfficiencies)));

      dph.setVisible(true);
      dph.setText("DPH: " + df1.format(aWeapon.getStat("d/h", anUpgrades, aEfficiencies)));

      range.setVisible(true);
      range.setText("Range: " + ((aWeapon.getRangeMin() > 0.001) ? (df0.format(aWeapon.getRangeMin()) + " / ") : "") + df0.format(aWeapon.getRangeLong()) + " / "
                    + df0.format(aWeapon.getRangeMax()));

      if( aWeapon instanceof AmmoWeapon ){
         AmmoWeapon ammoWeapon = (AmmoWeapon)aWeapon;
         ammoperton.setVisible(true);
         ammoperton.setText("Ammo per ton: " + ammoWeapon.getAmmoType(anUpgrades).getShotsPerTon());
      }
      else{
         ammoperton.setVisible(false);
      }
   }

   private void clearDisplay(){
      engineInternalHS.setVisible(false);
      engineHSSlots.setVisible(false);

      damage.setVisible(false);
      burntime.setVisible(false);
      heat.setVisible(false);
      heatPerSecond.setVisible(false);
      cycleTime.setVisible(false);
      gh_MaxFreeAlpha.setVisible(false);
      range.setVisible(false);
      dps.setVisible(false);
      dph.setVisible(false);
      secondsPerShot.setVisible(false);

      ammoperton.setVisible(false);
   }

   public void showItem(Item anItem, Upgrades anUpgrades, Efficiencies aEfficiencies){
      clearDisplay();
      showBasicInfo(anItem);

      if( anItem instanceof Weapon ){
         showWeaponInfo((Weapon)anItem, anUpgrades, aEfficiencies);
      }
      else if( anItem instanceof Ammunition ){
         showAmmoInfo((Ammunition)anItem);
      }
      else if( anItem instanceof Engine ){
         showEngineInfo((Engine)anItem);
      }
   }
}
