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
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import lisong_mechlab.model.loadout.MechGarage;
import lisong_mechlab.model.loadout.export.Base64LoadoutCoder;
import lisong_mechlab.model.loadout.export.LsmlProtocolIPC;
import lisong_mechlab.util.MessageXBar;

/**
 * This is the main program instance. It contains some program globals and startup and shutdown procedures.
 * 
 * @author Emily Björk
 */
public class LSML extends JFrame{
   public static final String      PROGRAM_FNAME          = "Li Song Mechlab";
   private static final String     VERSION_STRING         = "(develop)";
   private static final String     GARAGE_FILEDESCRIPTION = PROGRAM_FNAME + " Garage File (.xml)";
   private static final FileFilter GARAGE_FILE_FILTER     = new FileFilter(){
                                                             @Override
                                                             public String getDescription(){
                                                                return GARAGE_FILEDESCRIPTION;
                                                             }

                                                             @Override
                                                             public boolean accept(File aArg0){
                                                                return aArg0.isDirectory()
                                                                       || (aArg0.isFile() && aArg0.getName().toLowerCase().endsWith(".xml"));
                                                             }
                                                          };
   private static final long       serialVersionUID       = -2463321343234141728L;
   private LsmlProtocolIPC         lsmlProtocolIPC;
   private MechGarage              garage;
   public final MessageXBar        xBar                   = new MessageXBar();
   public final LoadoutDesktop     desktop                = new LoadoutDesktop(xBar);
   public final Base64LoadoutCoder loadoutCoder           = new Base64LoadoutCoder(xBar);

   public MechGarage getGarage(){
      return garage;
   }

   public void openLastGarage(){
      assert (SwingUtilities.isEventDispatchThread());

      String garageFileName = LsmlPreferences.getString(LsmlPreferences.GARAGEFILE_KEY, LsmlPreferences.GARAGEFILE_DEFAULT);
      File garageFile = new File(garageFileName);
      if( garageFile.exists() ){
         try{
            garage = MechGarage.open(garageFile, xBar);
         }
         catch( Exception e ){
            JOptionPane.showMessageDialog(this,
                                          "An error ocurred while opening your last saved garage!\nTo prevent further damage to the garage file, the application will close.\nPlease check your garage file manually, it is located at: "
                                                + garageFile + "\nError: " + e.getMessage(), "Error loading mech garage!", JOptionPane.ERROR_MESSAGE);
            shutdown();
         }
      }
      else{
         garage = new MechGarage(xBar);
      }
   }

   public void openGarage(){
      assert (SwingUtilities.isEventDispatchThread());

      JFileChooser chooser = new JFileChooser(garage.getFile());
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setMultiSelectionEnabled(false);
      chooser.setFileFilter(GARAGE_FILE_FILTER);

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
      assert (SwingUtilities.isEventDispatchThread());

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
      assert (SwingUtilities.isEventDispatchThread());

      JFileChooser chooser = new JFileChooser(garage.getFile());
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setMultiSelectionEnabled(false);
      chooser.setFileFilter(GARAGE_FILE_FILTER);

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
      assert (SwingUtilities.isEventDispatchThread());

      try{
         garage.save();
      }
      catch( IOException e ){
         saveGarageAs();
      }
   }

   public void shutdown(){
      dispose();
   }

   public LSML(){
      super(PROGRAM_FNAME + VERSION_STRING);

      final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      final EquipmentPane equipmentPane = new EquipmentPane(desktop, this, xBar);
      final JScrollPane jScrollPane = new JScrollPane(equipmentPane);
      final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, jScrollPane, desktop);
      splitPane.setDividerLocation(180);

      setIconImage(ProgramInit.programIcon);

      setSize((int)(screenSize.width * 0.9), (int)(screenSize.height * 0.9));
      setLocation(screenSize.width / 2 - getSize().width / 2, screenSize.height / 2 - getSize().height / 2);
      setVisible(true);
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      setJMenuBar(new MenuBar(this));
      setContentPane(splitPane);
      addWindowListener(new WindowAdapter(){
         @Override
         public void windowClosing(WindowEvent e){
            if( desktop.closeAll() ){
               saveGarage();
               if( null != lsmlProtocolIPC ){
                  lsmlProtocolIPC.close();
               }
               dispose();
            }
         }
      });

      openLastGarage();

      // Open the IPC socket first after everything else has succeeded.

      try{
         lsmlProtocolIPC = new LsmlProtocolIPC();
      }
      catch( IOException e ){
         lsmlProtocolIPC = null;
         JOptionPane.showMessageDialog(this, "Unable to startup IPC. Links with builds (lsml://...) will not work.\nError: " + e);
      }
   }
}
