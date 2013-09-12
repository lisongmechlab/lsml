package lisong_mechlab.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import lisong_mechlab.view.action.OpenHelp;
import lisong_mechlab.view.action.OpenMechSelectorAction;

public class MenuBar extends JMenuBar{
   private static final long serialVersionUID = -8841283911101837906L;

   public MenuBar(final LSML application){
      super();

      {
         JMenu menu = new JMenu("Program");
         menu.setMnemonic(KeyEvent.VK_P);
         menu.getAccessibleContext().setAccessibleDescription("Actions relating to the program");
         add(menu);

         menu.add(new JMenuItem(new OpenHelp("About", "About", KeyStroke.getKeyStroke('a'))));
         menu.add(new JMenuItem(new OpenHelp("User Manual", "User-Manual", KeyStroke.getKeyStroke('m'))));
         menu.add(new JMenuItem(new OpenHelp("Legal", "Legal", KeyStroke.getKeyStroke('m'))));
         
         {
            JMenuItem item = new JMenuItem("Quit", KeyEvent.VK_Q);
            item.addActionListener(new ActionListener(){
               @Override
               public void actionPerformed(ActionEvent arg0){
                  application.shutdown();
               }
            });
            menu.add(item);
         }
      }
      
      {
         JMenu menu = new JMenu("Mech");
         menu.setMnemonic(KeyEvent.VK_M);
         menu.getAccessibleContext().setAccessibleDescription("Actions relating to mech configurations");
         add(menu);

         menu.add(new JMenuItem(new OpenMechSelectorAction("New...", KeyStroke.getKeyStroke('n'))));
      }

      {
         JMenu menu = new JMenu("Garage");
         menu.setMnemonic(KeyEvent.VK_G);
         menu.getAccessibleContext().setAccessibleDescription("Actions relating to the garage");
         add(menu);

         {
            JMenuItem item = new JMenuItem("New", KeyEvent.VK_N);
            item.addActionListener(new ActionListener(){
               @Override
               public void actionPerformed(ActionEvent aArg0){
                  application.newGarage();
               }
            });

            menu.add(item);
         }
         {
            JMenuItem item = new JMenuItem("Open", KeyEvent.VK_O);
            item.addActionListener(new ActionListener(){
               @Override
               public void actionPerformed(ActionEvent aArg0){
                  application.openGarage();
               }
            });

            menu.add(item);
         }

         {
            JMenuItem item = new JMenuItem("Save", KeyEvent.VK_S);
            item.addActionListener(new ActionListener(){
               @Override
               public void actionPerformed(ActionEvent aArg0){
                  application.saveGarage();
               }
            });

            menu.add(item);
         }
         {
            JMenuItem item = new JMenuItem("Save as...", KeyEvent.VK_A);
            item.addActionListener(new ActionListener(){
               @Override
               public void actionPerformed(ActionEvent aArg0){
                  application.saveGarageAs();
               }
            });

            menu.add(item);
         }
      }

      setEnabled(true);
   }
}
