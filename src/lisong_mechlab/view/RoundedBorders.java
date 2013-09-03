package lisong_mechlab.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.AbstractBorder;

public class RoundedBorders extends AbstractBorder{
   private static final long    serialVersionUID  = -4151946534484412709L;
   private static final int     DEFAULT_RADII     = 5;
   private static final int     DEFAULT_THICKNESS = 4;

   private final Insets         insets;
   private final RenderingHints hints;
   private final int            thickness;
   private final double         radii;
   private final Color          color;
   private final BasicStroke    stroke;
   private final Insets         margin;
   private boolean              noBottomBevel;
   private boolean              noTopBevel;

   public RoundedBorders(Color aColor, Insets aMargin, Insets aPadding, int aThickness, double aRadii, boolean aNoTopBevel, boolean aNoBottomBevel){
      thickness = aThickness;
      radii = aRadii;
      color = aColor;
      margin = aMargin;
      insets = new Insets(margin.top + aPadding.top + thickness, margin.left + aPadding.left + thickness,
                          margin.bottom + aPadding.bottom + thickness, margin.right + aPadding.right + thickness);
      hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
      stroke = new BasicStroke(thickness);

      noTopBevel = aNoTopBevel;
      noBottomBevel = aNoBottomBevel;
   }

   /**
    * Will paint the border using the given color <a href="http://www.w3.org/TR/CSS2/images/boxdim.png">box model</a>
    * 
    * @param color
    */
   public RoundedBorders(Color aColor, int aMargin, int aPadding, int aThickness, double aRadii){
      this(aColor, new Insets(aMargin, aMargin, aMargin, aMargin), new Insets(aPadding, aPadding, aPadding, aPadding), aThickness, aRadii, false,
           false);
   }

   /**
    * Will paint the border using the given color
    * 
    * @param color
    */
   public RoundedBorders(Color aColor){
      this(aColor, 2, (DEFAULT_THICKNESS + 1) / 2, DEFAULT_THICKNESS, DEFAULT_RADII);
   }

   /**
    * Will paint the border using the components background color, making the component look solid with rounded corners.
    */
   public RoundedBorders(){
      this(null);
   }

   @Override
   public Insets getBorderInsets(Component c){
      return insets;
   }

   @Override
   public Insets getBorderInsets(Component c, Insets insets){
      return getBorderInsets(c);
   }

   @Override
   public void paintBorder(Component c, Graphics g, int x, int y, int width, int height){
      Graphics2D graphics2d = (Graphics2D)g;
      graphics2d.setRenderingHints(hints);

      int padTop = margin.top + thickness / 2;
      int padLeft = margin.left + thickness / 2;
      int padBottom = margin.bottom + thickness / 2;
      int padRight = margin.right + thickness / 2;

      int edgeBoxX = x + padLeft;
      int edgeBoxY = y + padTop;
      int edgeBoxWidth = width - padLeft - padRight;
      int edgeBoxHeight = height - padTop - padBottom;

      Area borderArea;
      if( noTopBevel && noBottomBevel ){
         // -1/+2 compensate for anti-aliasing by using overlap
         borderArea = new Area(new Rectangle(edgeBoxX, edgeBoxY - 1, edgeBoxWidth, edgeBoxHeight + 2));
      }
      else{
         borderArea = new Area(new RoundRectangle2D.Double(edgeBoxX, edgeBoxY, edgeBoxWidth, edgeBoxHeight, radii, radii));

         if( noTopBevel ){
            // -1/+1 compensate for anti-aliasing by using overlap
            borderArea.add(new Area(new Rectangle2D.Double(edgeBoxX, edgeBoxY - 1, edgeBoxWidth, edgeBoxHeight / 2.0 + 1)));
         }
         if( noBottomBevel ){
            borderArea.add(new Area(new Rectangle2D.Double(edgeBoxX, edgeBoxY + edgeBoxHeight / 2.0, edgeBoxWidth, edgeBoxHeight / 2.0)));
         }
      }

      if( c.getParent() != null ){
         Area clipArea = new Area(new Rectangle(width, height));
         clipArea.subtract(new Area(borderArea));
         graphics2d.setClip(clipArea);
         graphics2d.setColor(c.getParent().getBackground());
         graphics2d.fillRect(0, 0, width, height);
         graphics2d.setClip(null);
      }

      if( color == null ){
         graphics2d.setColor(c.getBackground());
      }
      else
         graphics2d.setColor(color);

      graphics2d.setStroke(stroke);
      graphics2d.draw(borderArea);
   }
}
