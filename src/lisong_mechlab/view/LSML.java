package lisong_mechlab.view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.loadout.MechGarage;

public class LSML extends JFrame{
   private static final String GARAGE_FILEDESCRIPTION = "Li Song Mech Lab Garage File (.xml)";

   private static final long   serialVersionUID       = -2463321343234141728L;

   private static LSML         instance;
   private MechGarage          garage;
   private final MessageXBar   xBar                   = new MessageXBar();

   public void initGarage(){
      String garageFileName = LsmlPreferences.getString(LsmlPreferences.GARAGEFILE_KEY);
      if( garageFileName.isEmpty() ){
         garageFileName = LsmlPreferences.GARAGEFILE_DEFAULT;
      }
      File garageFile = new File(garageFileName);

      if( garageFile.exists() ){
         try{
            garage = MechGarage.open(garageFile, xBar);
         }
         catch( Exception e ){
            JOptionPane.showMessageDialog(this, e.getMessage() + "\nExiting application!\nPlease check your garage file manually, it is located at: "
                                                + garageFile, "Error loading mech garage!", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
         }
      }
      else{
         garage = new MechGarage(xBar);
      }
   }

   public void openGarage(){
      JFileChooser chooser = new JFileChooser(garage.getFile());
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setMultiSelectionEnabled(false);
      chooser.setFileFilter(new FileFilter(){
         @Override
         public String getDescription(){
            return GARAGE_FILEDESCRIPTION;
         }

         @Override
         public boolean accept(File aArg0){
            return aArg0.isFile() && aArg0.getName().toLowerCase().endsWith(".xml");
         }
      });

      if( JFileChooser.APPROVE_OPTION != chooser.showOpenDialog(this) ){
         return;
      }
      try{
         garage = MechGarage.open(chooser.getSelectedFile(), xBar);
         LsmlPreferences.setString(LsmlPreferences.GARAGEFILE_KEY, chooser.getSelectedFile().getAbsolutePath());
      }
      catch( IOException e ){
         JOptionPane.showOptionDialog(this, "Error: " + e.getMessage(), "Couldn't open garage!", JOptionPane.DEFAULT_OPTION,
                                      JOptionPane.QUESTION_MESSAGE, null, null, 0);
      }
   }

   public void newGarage(){
      if( !garage.getMechs().isEmpty() ){
         try{
            garage.save();
         }
         catch( IOException e ){
            int choice = JOptionPane.showOptionDialog(this, "Error: " + e.getMessage(), "Couldn't save current garage!", JOptionPane.DEFAULT_OPTION,
                                                      JOptionPane.QUESTION_MESSAGE, null, new String[] {"Don't save", "Cancel", "Pick another file"},
                                                      0);
            if( 1 == choice ){
               return;
            }
            else if( 2 == choice ){
               saveGarageAs();
            }
         }
      }

      garage = new MechGarage(xBar);
   }

   public void saveGarageAs(){
      JFileChooser chooser = new JFileChooser(garage.getFile());
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setMultiSelectionEnabled(false);
      chooser.setFileFilter(new FileFilter(){
         @Override
         public String getDescription(){
            return GARAGE_FILEDESCRIPTION;
         }

         @Override
         public boolean accept(File aArg0){
            return aArg0.isFile() && aArg0.getName().toLowerCase().endsWith(".xml");
         }
      });

      while( true ){
         if( JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(this) ){
            break;
         }

         boolean overwrite = false;
         File file = chooser.getSelectedFile();
         if( !file.getName().toLowerCase().endsWith(".xml") ){
            file = new File(file.getParentFile(), file.getName() + ".xml");
         }

         if( file.exists() ){
            int choice = JOptionPane.showOptionDialog(this, "", "File already exists!", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                                                      null, new String[] {"Overwrite", "Cancel", "Pick another file"}, 0);
            if( 0 == choice ){
               overwrite = true;
            }
            else if( 1 == choice ){
               break;
            }
            else{
               continue;
            }
         }
         try{
            garage.saveas(file, overwrite);
            LsmlPreferences.setString(LsmlPreferences.GARAGEFILE_KEY, file.getAbsolutePath());
            break;
         }
         catch( IOException e ){
            int choice = JOptionPane.showOptionDialog(this, "Error was: " + e.getMessage(), "Couldn't save file!", JOptionPane.DEFAULT_OPTION,
                                                      JOptionPane.QUESTION_MESSAGE, null, new String[] {"Cancel", "Pick another file"}, 0);
            if( choice == 0 )
               break;
         }
      }
   }

   public void saveGarage(){
      try{
         garage.save();
      }
      catch( IOException e ){
         saveGarageAs();
      }
   }

   public void close(){
      dispose();
   }

   LSML() throws Exception{
      super("LiSong MechLab");
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      setDefaultLookAndFeelDecorated(true);
      try{
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch( Exception e ){
         System.out.println("Error setting native LAF: " + e);
      }

      setJMenuBar(new MenuBar(this));

      final LoadoutDesktop configurationsPane = new LoadoutDesktop();
      final EquipmentPane equipmentPane = new EquipmentPane(configurationsPane, this, xBar);
      final JScrollPane jScrollPane = new JScrollPane(equipmentPane);
      final JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, jScrollPane, configurationsPane);

      sp.setDividerLocation(180);
      setContentPane(sp);

      setSize(1024, 768);
      setVisible(true);

      addWindowListener(new WindowAdapter(){
         @Override
         public void windowClosing(WindowEvent e){
            configurationsPane.closeAll();
            saveGarage();
         }
      });

      initGarage();
   }

   public static void main(String[] args) throws Exception{
      SplashScreen splash = new SplashScreen();
      splash.waitUntilDone();
      
      javax.swing.SwingUtilities.invokeLater(new Runnable(){
         public void run(){
            try{
               instance = new LSML();
            }
            catch( Exception e ){
               e.printStackTrace();
            }
         }
      });
   }

   public MechGarage getGarage(){
      return garage;
   }

   public MessageXBar getXBar(){
      return xBar;
   }

   public static LSML getInstance(){
      return instance;
   }

}
