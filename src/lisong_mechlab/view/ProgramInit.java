package lisong_mechlab.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;

/**
 * This class shows a splash screen while the program is loading.
 * 
 * @author Emily Björk
 */
public class ProgramInit extends JFrame{
   private static final long  serialVersionUID   = -2877785947094537320L;
   private static final long  MIN_SPLASH_TIME_MS = 20;
   private static ProgramInit instance;

   private String             progressSubText    = "";
   private String             progressText       = "";

   private class BackgroundImage extends JComponent{
      private static final long serialVersionUID = 2294812231919303690L;
      private Image             image;

      public BackgroundImage(Image anImage){
         image = anImage;
      }

      @Override
      protected void paintComponent(Graphics g){
         g.drawImage(image, 0, 0, this);

         int penX = 190;
         int penY = 140;

         g.drawString(progressText, penX, penY);
         penY += 20;

         g.drawString(progressSubText, penX, penY);
      }
   }

   ProgramInit(){
      instance = this;
      SwingUtilities.invokeLater(new Runnable(){
         @Override
         public void run(){
            Image splash = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/splash.png"));
            setContentPane(new BackgroundImage(splash));
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

            setResizable(false);
            setUndecorated(true);
            setTitle("loading...");
            setSize(500, 300);
            setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
            setVisible(true);
            getRootPane().setBorder(BorderFactory.createLineBorder(Color.BLACK));
            getRootPane().putClientProperty("Window.shadow", Boolean.TRUE);
         }
      });
   }

   public static ProgramInit getInstance(){
      return instance;
   }

   public void setProcessText(String aString){
      progressText = aString;
      repaint();
   }

   public void setSubText(String aString){
      progressSubText = aString;
      repaint();
   }

   public boolean waitUntilDone(){
      long startTimeMs = new Date().getTime();

      try{
         @SuppressWarnings("unused")
         // Causes static initialization to be ran.
         Item bap = ItemDB.BAP;

         @SuppressWarnings("unused")
         // Causes static initialization to be ran.
         Chassi chassi = ChassiDB.lookup("JR7-D");
      }
      catch( Throwable e ){
         JOptionPane.showMessageDialog(this,
                                       "Unable to find game data files!\nLSML requires an up-to-date installation of MW:Online to parse data files from.");
         return false;
      }

      long endTimeMs = new Date().getTime();
      long sleepTimeMs = Math.max(0, MIN_SPLASH_TIME_MS - (endTimeMs - startTimeMs));
      try{
         Thread.sleep(sleepTimeMs);
      }
      catch( Exception e ){
         // No-Op
      }
      dispose();
      instance = null;
      return true;
   }
}
