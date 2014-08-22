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
package lisong_mechlab.view.render;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

public class ScrollablePanel extends JPanel implements Scrollable{
   private static final long serialVersionUID = -5231044372862875923L;

   @Override
   public Dimension getPreferredScrollableViewportSize(){
      return null;
   }

   @Override
   public int getScrollableBlockIncrement(Rectangle aVisibleRect, int aOrientation, int aDirection){
      return 150; // Arbitrary number, works well enough.
   }

   @Override
   public boolean getScrollableTracksViewportHeight(){
      return false;
   }

   @Override
   public boolean getScrollableTracksViewportWidth(){
      return true;
   }

   @Override
   public int getScrollableUnitIncrement(Rectangle aVisibleRect, int aOrientation, int aDirection){
      return 50; // Arbitrary number, works well enough.
   }
}
