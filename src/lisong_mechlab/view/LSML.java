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
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import lisong_mechlab.model.loadout.MechGarage;
import lisong_mechlab.model.loadout.UndoStack;
import lisong_mechlab.model.loadout.export.Base64LoadoutCoder;
import lisong_mechlab.model.loadout.export.LsmlProtocolIPC;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.SwingHelpers;
import lisong_mechlab.view.action.UndoGarageAction;
import lisong_mechlab.view.mechlab.MechLabPane;
import lisong_mechlab.view.preferences.PreferenceStore;
import lisong_mechlab.view.preferences.Preferences;

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
   private static final String     CMD_UNDO_GARAGE        = "undo garage action";
   public final MessageXBar        xBar                   = new MessageXBar();
   public final UndoStack          undoStack              = new UndoStack(xBar, 256);
   public final Base64LoadoutCoder loadoutCoder           = new Base64LoadoutCoder(xBar, undoStack);
   public final Preferences        preferences            = new Preferences();
   public final MechLabPane        mechLabPane            = new MechLabPane(xBar, undoStack);
   public final JTabbedPane        tabbedPane             = new JTabbedPane();
   private LsmlProtocolIPC         lsmlProtocolIPC;
   private MechGarage              garage;

   public LSML(){
      super(PROGRAM_FNAME + VERSION_STRING);
      final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      setIconImage(ProgramInit.programIcon);
      setSize((int)(screenSize.width * 0.9), (int)(screenSize.height * 0.9));
      setLocation(screenSize.width / 2 - getSize().width / 2, screenSize.height / 2 - getSize().height / 2);
      setVisible(true);
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      setJMenuBar(new MenuBar(this));

      openLastGarage();

      tabbedPane.addTab("Mechlab", mechLabPane);
      tabbedPane.addTab("Mechs", new ChassiSelectionPane());
      tabbedPane.addTab("Weapons", new WeaponsListView());

      setContentPane(tabbedPane);
      addWindowListener(new WindowAdapter(){
         @Override
         public void windowClosing(WindowEvent e){
            if( mechLabPane.close() ){
               saveGarage();
               if( null != lsmlProtocolIPC ){
                  lsmlProtocolIPC.close();
               }
               dispose();
            }
         }
      });

      // Open the IPC socket first after everything else has succeeded.

      try{
         lsmlProtocolIPC = new LsmlProtocolIPC();
      }
      catch( IOException e ){
         lsmlProtocolIPC = null;
         JOptionPane.showMessageDialog(this, "Unable to startup IPC. Links with builds (lsml://...) will not work.\nError: " + e);
      }

      setupKeybindings();
   }

   private void setupKeybindings(){
      SwingHelpers.bindAction(getRootPane(), CMD_UNDO_GARAGE, new UndoGarageAction(xBar));
   }

   public MechGarage getGarage(){
      return garage;
   }

   public void openLastGarage(){
      assert (SwingUtilities.isEventDispatchThread());

      String garageFileName = PreferenceStore.getString(PreferenceStore.GARAGEFILE_KEY, PreferenceStore.GARAGEFILE_DEFAULT);
      File garageFile = new File(garageFileName);
      if( garageFile.exists() ){
         try{
            garage = MechGarage.open(garageFile, xBar, undoStack);
         }
         catch( Exception e ){
            JOptionPane.showMessageDialog(this,
                                          "An error ocurred while opening your last saved garage!\nTo prevent further damage to the garage file, the application will close.\nPlease check your garage file manually, it is located at: "
                                                + garageFile + "\nError: " + e.getMessage(), "Error loading mech garage!", JOptionPane.ERROR_MESSAGE);
            shutdown();
         }
      }
      else{
         garage = new MechGarage(xBar, undoStack);
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
         garage = MechGarage.open(chooser.getSelectedFile(), xBar, undoStack);
         PreferenceStore.setString(PreferenceStore.GARAGEFILE_KEY, chooser.getSelectedFile().getAbsolutePath());
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

      garage = new MechGarage(xBar, undoStack);
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
            PreferenceStore.setString(PreferenceStore.GARAGEFILE_KEY, file.getAbsolutePath());
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
}
