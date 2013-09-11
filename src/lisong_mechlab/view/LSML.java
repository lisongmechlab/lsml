package lisong_mechlab.view;

import java.awt.Dimension;
import java.awt.Toolkit;
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

import lisong_mechlab.model.loadout.MechGarage;
import lisong_mechlab.model.loadout.export.ExternalLoadout;
import lisong_mechlab.model.loadout.export.LsmlProtocolIPC;
import lisong_mechlab.util.MessageXBar;

public class LSML extends JFrame{
   private static final long    serialVersionUID       = -2463321343234141728L;
   private static final String  GARAGE_FILEDESCRIPTION = "Li Song Mech Lab Garage File (.xml)";
   private static final String  VERSION_STRING         = "(development)";

   private static LSML          instance;
   private final MessageXBar    xBar                   = new MessageXBar();
   private final LoadoutDesktop desktop                = new LoadoutDesktop(xBar);
   private MechGarage           garage;
   static LsmlProtocolIPC              lsmlProtocolIPC;

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
      super("LiSong MechLab " + VERSION_STRING);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      setDefaultLookAndFeelDecorated(true);

      setJMenuBar(new MenuBar(this));

      final EquipmentPane equipmentPane = new EquipmentPane(desktop, this, xBar);
      final JScrollPane jScrollPane = new JScrollPane(equipmentPane);
      final JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, jScrollPane, desktop);

      sp.setDividerLocation(180);
      setContentPane(sp);

      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      setSize((int)(dim.width * 0.9), (int)(dim.height * 0.9));
      setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
      setVisible(true);

      addWindowListener(new WindowAdapter(){
         @Override
         public void windowClosing(WindowEvent e){
            desktop.closeAll();
            saveGarage();
         }
      });

      initGarage();
   }

   public static void main(final String[] args) throws Exception{
      // Started with an argument, it's likely a LSML:// protocol string, send it over the IPC and quit.
      if( args.length > 0 ){
         if( LsmlProtocolIPC.sendLoadout(args[0]) )
            return; // Message received we can close this program.
      }

      try{
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch( Exception e ){
         JOptionPane.showMessageDialog(null, "Unable to set default look and feel. Something is seriously wrong with your java install!\nError: " + e);
      }

      lsmlProtocolIPC = new LsmlProtocolIPC(); // FIXME: Needs to be closed!

      ProgramInit splash = new ProgramInit();
      if( !splash.waitUntilDone() ){
         System.exit(1);
      }

      javax.swing.SwingUtilities.invokeLater(new Runnable(){
         @Override
         public void run(){
            try{
               instance = new LSML();

               if( args.length > 0 )
                  instance.getDesktop().openLoadout(ExternalLoadout.parse(args[0]));
            }
            catch( Exception e ){
               JOptionPane.showMessageDialog(null, "Unable to start! Error: " + e);
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

   public LoadoutDesktop getDesktop(){
      return desktop;
   }

}
