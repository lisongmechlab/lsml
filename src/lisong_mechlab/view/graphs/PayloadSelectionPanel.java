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

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.loadout.metrics.PayloadStatistics;

/**
 * Draws the panel where one can select chassis by payload tonnage.
 * 
 * @author Li Song
 */
public class PayloadSelectionPanel extends JPanel{
   private static final long       serialVersionUID = 1L;

   private final Upgrades upgrades;
   private final PayloadGraphPanel graphPanel;
   private final PayloadStatistics payloadStatistics;

   public PayloadSelectionPanel(){
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      upgrades = new Upgrades(null);
      payloadStatistics = new PayloadStatistics(true, true, upgrades);
      graphPanel = new PayloadGraphPanel(payloadStatistics);

      Collection<Chassi> chassis = new ArrayList<Chassi>();
      chassis.add(ChassiDB.lookup("JR7-D"));
      chassis.add(ChassiDB.lookup("AS7-D-DC"));
      chassis.add(ChassiDB.lookup("HGN-733C"));
      chassis.add(ChassiDB.lookup("HBK-4P"));
      chassis.add(ChassiDB.lookup("DRAGON SLAYER"));
      chassis.add(ChassiDB.lookup("QKD-4G"));
      chassis.add(ChassiDB.lookup("ILYA MUROMETS"));
      
      graphPanel.selectChassis(chassis);
      
      add(graphPanel);
   }
}
