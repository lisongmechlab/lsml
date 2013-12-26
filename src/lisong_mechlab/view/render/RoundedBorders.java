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
package lisong_mechlab.view.render;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import javax.swing.border.AbstractBorder;

public class RoundedBorders extends AbstractBorder{
   private static final long    serialVersionUID = -4151946534484412709L;
   private final Insets         insets;
   private final RenderingHints hints;
   private final int            radii;
   private final Insets         margin;
   private final boolean        noBottomBevel;
   private final boolean        noTopBevel;
   private final Area           clipFull;

   public RoundedBorders(Insets aMargin, Insets aPadding, int aRadii, boolean aNoTopBevel, boolean aNoBottomBevel){
      radii = aRadii;
      margin = aMargin;
      insets = new Insets(margin.top + aPadding.top, margin.left + aPadding.left, margin.bottom + aPadding.bottom, margin.right + aPadding.right);
      hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));

      noTopBevel = aNoTopBevel;
      noBottomBevel = aNoBottomBevel;

      clipFull = new Area(new Rectangle(0, 0, 2 * radii, 2 * radii));
      clipFull.subtract(new Area(new Ellipse2D.Float(0, 0, 2 * radii, 2 * radii)));
   }

   /**
    * Will paint the border using the given color <a href="http://www.w3.org/TR/CSS2/images/boxdim.png">box model</a>
    * 
    * @param color
    */
   public RoundedBorders(int aMargin, int aPadding, int aRadii){
      this(new Insets(aMargin, aMargin, aMargin, aMargin), new Insets(aPadding, aPadding, aPadding, aPadding), aRadii, false, false);
   }

   @Override
   public Insets getBorderInsets(Component aComponent){
      return insets;
   }

   @Override
   public Insets getBorderInsets(Component aComponent, Insets anInsets){
      return getBorderInsets(aComponent);
   }

   @Override
   public void paintBorder(Component c, Graphics g, int x, int y, int width, int height){
      if( c.getParent() == null )
         return;

      Graphics2D graphics2d = (Graphics2D)g;
      graphics2d.setRenderingHints(hints);
      graphics2d.setColor(c.getParent().getBackground());

      int padTop = margin.top;
      int padLeft = margin.left;
      int padBottom = margin.bottom;
      int padRight = margin.right;

      // Quick fill of the padding area
      graphics2d.fillRect(0, 0, width, padTop);
      graphics2d.fillRect(0, height - padBottom, width, padBottom);
      graphics2d.fillRect(0, padTop, padLeft, height - padTop - padBottom);
      graphics2d.fillRect(width - padRight, padTop, padRight, height - padTop - padBottom);

      if( !noTopBevel ){
         graphics2d.translate(padLeft, padTop);
         graphics2d.setClip(0, 0, radii, radii);
         graphics2d.fill(clipFull);

         graphics2d.translate(width - padRight - 2 * radii - padLeft, 0);
         graphics2d.setClip(radii, 0, radii, radii);
         graphics2d.fill(clipFull);

         // Restore translation
         graphics2d.translate(-width + padRight + 2 * radii, -padTop);
      }
      if( !noBottomBevel ){
         graphics2d.translate(padLeft, height - padBottom - 2 * radii);
         graphics2d.setClip(0, radii, radii, radii);
         graphics2d.fill(clipFull);

         graphics2d.translate(width - padRight - 2 * radii - padLeft, 0);
         graphics2d.setClip(radii, radii, radii, radii);
         graphics2d.fill(clipFull);

         // Restore translation
         graphics2d.translate(-width + padRight + 2 * radii, -height + padBottom + 2 * radii);
      }
   }
}
