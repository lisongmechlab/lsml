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

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * This class handles any exceptions that were not caught and informs the user of a potential problem.
 * 
 * @author Emily Björk
 */
public class DefaultExceptionHandler implements UncaughtExceptionHandler{

   @Override
   public void uncaughtException(final Thread aThread, final Throwable aThrowable){
      if( SwingUtilities.isEventDispatchThread() ){
         informUser(aThrowable);
      }
      else{
         try{
            SwingUtilities.invokeAndWait(new Runnable(){
               @Override
               public void run(){
                  informUser(aThrowable);
               }
            });
         }
         catch( InterruptedException ie ){
            Thread.currentThread().interrupt();
         }
         catch( InvocationTargetException ite ){
            ite.getCause().printStackTrace();
         }
      }
   }

   protected void informUser(Throwable aThrowable){
      StringBuilder builder = new StringBuilder();
      builder.append("<html><p>An error has been encountered, in most cases LSML can still continue to function normally.</p>");
      builder.append("<br><p>However as a safety precaution it is recommended to \"save as\" your garage manually from the garage menu.</p>");
      builder.append("<br><p>Please copy the following and send it to <a href=\"lisongmechlab@gmail.com\">lisongmechlab@gmail.com</a> together with an explanation of what you were doing to make us aware of the problem.</p><br>");

      StringWriter sw = new StringWriter();
      aThrowable.printStackTrace(new PrintWriter(sw));

      JPanel p = new JPanel();
      p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
      p.add(new JLabel(builder.toString()));
      JTextArea text = new JTextArea(sw.toString());
      text.setAlignmentX(Component.LEFT_ALIGNMENT);
      text.setEditable(false);
      p.add(text);
      JOptionPane.showMessageDialog(ProgramInit.lsml(), p, "LSML has encountered an error.", JOptionPane.ERROR_MESSAGE);
   }

}
