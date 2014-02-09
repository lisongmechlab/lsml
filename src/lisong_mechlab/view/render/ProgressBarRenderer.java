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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.plaf.ProgressBarUI;

public class ProgressBarRenderer extends ProgressBarUI{

   final RenderingHints hints;

   public ProgressBarRenderer(){
      Toolkit tk = Toolkit.getDefaultToolkit();
      hints = (RenderingHints)tk.getDesktopProperty("awt.font.desktophints");
   }

   @Override
   public Dimension getMinimumSize(JComponent c){
      return new Dimension(0, c.getFontMetrics(c.getFont()).getHeight() + 4);
   }

   @Override
   public Dimension getMaximumSize(JComponent c){
      return new Dimension(200, c.getFontMetrics(c.getFont()).getHeight() + 5);
   }

   @Override
   public void paint(Graphics g, JComponent c){
      JProgressBar progressBar = (JProgressBar)c;
      int w = c.getWidth();
      int h = c.getHeight();

      // Draw frame
      g.setColor(new Color(0x8e8f8f).brighter());
      g.drawRoundRect(0, 0, w - 1, h - 1, 3, 3);
      g.setColor(new Color(0xf4f4f4).brighter());
      g.drawRect(1, 1, w - 3, h - 3);

      // Draw bar
      g.setColor(new Color(0xb7ceeb));
      g.fillRect(3, 3, (int)Math.ceil(progressBar.getPercentComplete() * (w - 6)), (h - 6));

      // Draw text in bar
      Graphics2D g2d = (Graphics2D)g;

      g2d.setRenderingHints(hints);

      g2d.setColor(Color.BLACK);
      int ascent = c.getFontMetrics(c.getFont()).getAscent();
      int stringWidth = c.getFontMetrics(c.getFont()).stringWidth(progressBar.getString());
      g2d.drawString(progressBar.getString(), (w - stringWidth) / 2, (h - 4 + ascent + 1) / 2);
   }
}
