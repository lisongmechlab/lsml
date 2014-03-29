/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
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
package lisong_mechlab.view.mechlab;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import lisong_mechlab.model.NotificationMessage;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.MessageXBar.Reader;

/**
 * This class implements a simple status bar that listens to messages on the message XBar and displays notify messages
 * on the bar.
 * 
 * @author Li Song
 */
public class StatusBar extends JPanel implements Reader{
   private static final long  serialVersionUID = -4434467429002792379L;
   private final LoadoutFrame frame;
   private final JLabel       statusLabel;

   public StatusBar(LoadoutFrame aLoadoutFrame, MessageXBar aXBar){
      frame = aLoadoutFrame;
      aXBar.attach(this);

      setBorder(new BevelBorder(BevelBorder.LOWERED));
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      statusLabel = new JLabel();
      statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
      
      add(statusLabel);
      add(Box.createHorizontalGlue());
      add(new JButton(new AbstractAction("Clear"){
         private static final long serialVersionUID = 5577168900405120406L;

         @Override
         public void actionPerformed(ActionEvent aArg0){
            clear();
         }
      }));
      clear();
   }
   
   public void clear(){
      statusLabel.setText("No warnings.");
      statusLabel.setForeground(Color.BLACK);
   }

   @Override
   public void receive(Message aMsg){
      if( aMsg instanceof NotificationMessage && aMsg.isForMe(frame.getLoadout()) ){
         NotificationMessage notice = (NotificationMessage)aMsg;
         statusLabel.setText(notice.severity.toString() + ": " + notice.message);
         switch(notice.severity){
            case ERROR:
               statusLabel.setForeground(Color.RED);
               break;
            case NOTICE:
               statusLabel.setForeground(Color.BLACK);
               break;
            case WARNING:
               statusLabel.setForeground(Color.ORANGE.darker());
               break;
            default:
               break;            
         }
         Toolkit.getDefaultToolkit().beep();
      }
   }

}
