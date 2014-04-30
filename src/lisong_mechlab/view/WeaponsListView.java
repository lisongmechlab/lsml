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

import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import lisong_mechlab.model.chassi.HardPointType;

/**
 * This class displays a list of all weapons and relevant stats about them.
 * 
 * @author Li Song
 */
public class WeaponsListView extends JPanel{
   private static final float FONT_FACTOR      = 2.0f;
   private static final long  serialVersionUID = 1L;

   public WeaponsListView(){
      setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

      JPanel inner = new JPanel();
      inner.setLayout(new BoxLayout(inner, BoxLayout.PAGE_AXIS));
      inner.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      add(inner);

      JLabel missileLabel = new JLabel("Missile Weapons", SwingConstants.CENTER);
      missileLabel.setFont(missileLabel.getFont().deriveFont(missileLabel.getFont().getSize() * FONT_FACTOR));
      missileLabel.setAlignmentX(0.5f);
      inner.add(missileLabel);
      JScrollPane missilePane = new JScrollPane(new WeaponStatsTable(HardPointType.MISSILE));
      missilePane.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
      inner.add(missilePane);

      JLabel ballisticLabel = new JLabel("Ballistic Weapons", SwingConstants.CENTER);
      ballisticLabel.setFont(missileLabel.getFont().deriveFont(ballisticLabel.getFont().getSize() * FONT_FACTOR));
      ballisticLabel.setAlignmentX(0.5f);
      inner.add(ballisticLabel);
      JScrollPane ballisticPane = new JScrollPane(new WeaponStatsTable(HardPointType.BALLISTIC));
      ballisticPane.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
      inner.add(ballisticPane);

      JLabel energyLabel = new JLabel("Energy Weapons", SwingConstants.CENTER);
      energyLabel.setFont(missileLabel.getFont().deriveFont(energyLabel.getFont().getSize() * FONT_FACTOR));
      energyLabel.setAlignmentX(0.5f);
      inner.add(energyLabel);
      JScrollPane energyPane = new JScrollPane(new WeaponStatsTable(HardPointType.ENERGY));
      energyPane.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
      inner.add(energyPane);
   }

}
