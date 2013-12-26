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
package lisong_mechlab.util;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * This class holds some static helper functions for dealing with SWING.
 * 
 * @author Emily Björk
 */
public class SwingHelpers{
   public static void bindAction(JComponent aComponent, String aCommand, Action anAction){
      Object o = anAction.getValue(Action.ACCELERATOR_KEY);
      if( o instanceof KeyStroke )
         bindAction(aComponent, aCommand, anAction, (KeyStroke)o);
      else
         throw new IllegalArgumentException("Can not bind action, it has no keystroke assigned.");
   }

   public static void bindAction(JComponent aComponent, String aCommand, Action anAction, KeyStroke aKeyStroke){
      aComponent.getInputMap(JComponent.WHEN_FOCUSED).put(aKeyStroke, aCommand);
      aComponent.getActionMap().put(aCommand, anAction);
   }
}
