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
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.UIManager;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Upgrades;

/**
 * This class can render any {@link Item} to an {@link Image}.
 * 
 * @author Emily Björk
 */
public class ItemRenderer{
   public static final int                    ITEM_BASE_HEIGHT = 20;                                           // [px]
   public static final int                    ITEM_BASE_WIDTH  = 100;                                          // [px]
   public static final int                    ITEM_BASE_LINE   = 13;

   private static final GraphicsConfiguration configuration    = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                                                                                    .getDefaultConfiguration();
   private static final RenderingHints        hints;
   private static final int                   PADDING          = 3;
   public static final int                    RADII            = 5;

   static{
      hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      // hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
   }

   private static void drawString(String text, int x, int y, Graphics2D g, Color bg){
      // Prepare an off-screen image to draw the string to
      Rectangle2D bounds = g.getFontMetrics().getStringBounds(text, g);
      BufferedImage image = configuration.createCompatibleImage((int)(bounds.getWidth() + 1.0f), (int)(bounds.getHeight() + 1.0f),
                                                                Transparency.OPAQUE);
      Graphics2D ig = image.createGraphics();

      // Fill the background color
      ig.setColor(bg);
      ig.fillRect(0, 0, image.getWidth(), image.getHeight());

      // Draw the string
      int x0 = 0;
      int y0 = ig.getFontMetrics().getAscent();
      ig.setColor(g.getColor());
      ig.setRenderingHints(g.getRenderingHints());
      ig.setFont(g.getFont());
      ig.drawString(text, x0, y0);
      ig.dispose();

      // Blit the image to the destination
      g.drawImage(image, x - x0, y - y0, null);
   }

   public static Image render(Item item, Upgrades aUpgrades){
      final int slots = item.getNumCriticalSlots(aUpgrades);
      final int item_w = ITEM_BASE_WIDTH - 2; // Compensate for padding added by JList in drawing the loadout
      final int item_h = ITEM_BASE_HEIGHT * slots - 2; // Compensate for padding added by JList in drawing the loadout

      final int x_offs;
      final int x_slots;
      if( item instanceof Engine && ((Engine)item).getType() == EngineType.XL ){
         x_offs = item_w + PADDING;
         x_slots = 3;
      }
      else{
         x_offs = 0;
         x_slots = 1;
      }

      final int image_w = item_w * x_slots + (x_slots - 1) * PADDING;
      BufferedImage image = configuration.createCompatibleImage(image_w, item_h, Transparency.TRANSLUCENT);
      Graphics2D g = image.createGraphics();

      g.setRenderingHints(hints);
      Object defaultHints = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
      if( defaultHints != null ){
         g.addRenderingHints((RenderingHints)defaultHints);
      }

      // Draw Item Box
      g.setColor(StyleManager.getBgColorFor(item));
      g.fillRoundRect(x_offs, 0, item_w, item_h, RADII * 2, RADII * 2);
      if( x_slots > 1 ){
         // Draw side
         final int engine_h = ITEM_BASE_HEIGHT * 3 - 2;
         g.fillRoundRect(0, 0, item_w, engine_h, RADII * 2, RADII * 2);
         g.fillRoundRect(2 * x_offs, 0, item_w, engine_h, RADII * 2, RADII * 2);
      }

      g.setFont(UIManager.getDefaults().getFont("Label.font"));
      g.setColor(StyleManager.getFgColorFor(item));
      drawString(item.getName(aUpgrades), x_offs + RADII, ITEM_BASE_LINE, g, StyleManager.getBgColorFor(item));
      if( x_slots > 1 ){
         drawString("ENGINE", RADII, ITEM_BASE_LINE, g, StyleManager.getBgColorFor(item));
         drawString("ENGINE", 2 * x_offs + RADII, ITEM_BASE_LINE, g, StyleManager.getBgColorFor(item));
      }

      g.dispose();
      return image;
   }
}
