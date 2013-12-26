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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import lisong_mechlab.view.action.ImportMechAction;
import lisong_mechlab.view.action.OpenHelp;
import lisong_mechlab.view.action.OpenPreferences;
import lisong_mechlab.view.action.UndoGarageAction;

public class MenuBar extends JMenuBar{
   private static final long serialVersionUID = -8841283911101837906L;

   public MenuBar(final LSML application){
      super();

      {
         JMenu menu = new JMenu("Program");
         menu.setMnemonic(KeyEvent.VK_P);
         menu.getAccessibleContext().setAccessibleDescription("Actions relating to the program");
         add(menu);

         menu.add(new JMenuItem(new OpenPreferences("Preferences", KeyStroke.getKeyStroke('p'))));
         menu.add(new JMenuItem(new OpenHelp("About", "About", KeyStroke.getKeyStroke('a'))));
         menu.add(new JMenuItem(new OpenHelp("User Manual", "User-Manual", KeyStroke.getKeyStroke('m'))));
         menu.add(new JMenuItem(new OpenHelp("Legal", "Legal", KeyStroke.getKeyStroke('m'))));

         {
            JMenuItem item = new JMenuItem("Quit", KeyEvent.VK_Q);
            item.addActionListener(new ActionListener(){
               @Override
               public void actionPerformed(ActionEvent arg0){
                  // TODO: make an action out of this
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

         menu.add(new JMenuItem(new ImportMechAction("Import...", KeyStroke.getKeyStroke('i'))));
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
                  // TODO: make an action out of this
                  application.newGarage();
               }
            });

            menu.add(item);
         }
         menu.add(new UndoGarageAction(application.xBar));
         {
            JMenuItem item = new JMenuItem("Open", KeyEvent.VK_O);
            item.addActionListener(new ActionListener(){
               @Override
               public void actionPerformed(ActionEvent aArg0){
                  // TODO: make an action out of this
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
                  // TODO: make an action out of this
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
                  // TODO: make an action out of this
                  application.saveGarageAs();
               }
            });

            menu.add(item);
         }
      }

      setEnabled(true);
   }
}
