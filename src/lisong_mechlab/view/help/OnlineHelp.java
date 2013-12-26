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
package lisong_mechlab.view.help;

import java.awt.Desktop;
import java.net.URL;

import javax.swing.JOptionPane;

import lisong_mechlab.view.ProgramInit;

public class OnlineHelp{
   public static void openHelp(String aHelpTag){
      try{
         Desktop.getDesktop().browse((new URL("https://github.com/EmilyBjoerk/lsml/wiki/" + aHelpTag).toURI()));
      }
      catch( Exception e ){
         JOptionPane.showMessageDialog(ProgramInit.lsml(), "Couldn't open help file!\n" + e);
      }
   }
}
