package lisong_mechlab.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.plaf.ProgressBarUI;

public class ProgressBarRenderer extends ProgressBarUI{

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
      g.setColor(new Color(0xd3d7cf));
      g.fillRect(3, 3, (int)Math.ceil(progressBar.getPercentComplete() * (w - 6)), (h - 6));

      // Draw text in bar
      Graphics2D g2d = (Graphics2D)g;
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
      g2d.setColor(Color.BLACK);
      int ascent = c.getFontMetrics(c.getFont()).getAscent();
      int stringWidth = c.getFontMetrics(c.getFont()).stringWidth(progressBar.getString());
      g2d.drawString(progressBar.getString(), (w - stringWidth) / 2, (h - 4 + ascent+1) / 2);
   }
}
