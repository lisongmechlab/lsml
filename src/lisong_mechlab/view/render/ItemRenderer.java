package lisong_mechlab.view.render;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Upgrades;

/**
 * This class can render any {@link Item} to an {@link Image}.
 * 
 * @author Li Song
 */
public class ItemRenderer{
   public static final int                    ITEM_BASE_HEIGHT = 20;                                           // [px]
   public static final int                    ITEM_BASE_WIDTH  = 120;                                          // [px]

   public static final int                    ITEM_BASE_LINE   = 4;

   private static final GraphicsConfiguration configuration    = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                                                                                    .getDefaultConfiguration();
   private static final RenderingHints        hints;
   private static final float                 PADDING          = 3;
   private static final float                 RADII            = 12;

   static{
      hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
   }

   public static Image render(Item item, Upgrades aUpgrades){
      final int x_offs;
      final int x_slots;
      if( item instanceof Engine && ((Engine)item).getType() == EngineType.XL ){
         x_offs = ITEM_BASE_WIDTH;
         x_slots = 3;
      }
      else{
         x_offs = 0;
         x_slots = 1;
      }
      final int slots = item.getNumCriticalSlots(aUpgrades);
      final int h = ITEM_BASE_HEIGHT * slots;
      final int w = ITEM_BASE_WIDTH;
      BufferedImage image = configuration.createCompatibleImage(ITEM_BASE_WIDTH * x_slots, ITEM_BASE_HEIGHT * slots, Transparency.TRANSLUCENT);
      Graphics2D g = image.createGraphics();

      g.setRenderingHints(hints);
      g.setColor(StyleManager.getBgColorFor(item));
      g.fill(new RoundRectangle2D.Float(x_offs + PADDING, PADDING, w - PADDING, h - PADDING, RADII, RADII));
      if( x_slots > 1 ){
         final int engine_h = ITEM_BASE_HEIGHT*3;
         g.fill(new RoundRectangle2D.Float(PADDING, PADDING, w - PADDING, engine_h - PADDING, RADII, RADII));
         g.fill(new RoundRectangle2D.Float(2*x_offs + PADDING, PADDING, w - PADDING, engine_h - PADDING, RADII, RADII));
      }

      g.setFont(g.getFont().deriveFont(11.0f));
      g.setColor(StyleManager.getFgColorFor(item));
      g.drawString(item.getName(aUpgrades), x_offs + RADII - PADDING + 1, ITEM_BASE_HEIGHT - ITEM_BASE_LINE);
      if( x_slots > 1 ){
         g.drawString("ENGINE", 0 + RADII - PADDING + 1, ITEM_BASE_HEIGHT - ITEM_BASE_LINE);
         g.drawString("ENGINE", 2*x_offs + RADII - PADDING + 1, ITEM_BASE_HEIGHT - ITEM_BASE_LINE);
      }
      
      g.dispose();
      return image;
   }
}
