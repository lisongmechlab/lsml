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
package lisong_mechlab.view.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.preferences.FontPreferences;
import lisong_mechlab.view.preferences.FontPreferences.FontSize;

/**
 * Sets the application font size.
 * 
 * @author Emily Björk
 */
public class SetFontSizeAction extends AbstractAction{
   private static final long        serialVersionUID = -8145859026953437134L;
   private FontPreferences.FontSize fontSize;

   public SetFontSizeAction(String aTitle, KeyStroke aKeyStroke, FontSize aFontSize){
      super(aTitle);
      putValue(Action.ACCELERATOR_KEY, aKeyStroke);
      fontSize = aFontSize;
   }

   @Override
   public void actionPerformed(ActionEvent aArg0){
      ProgramInit.lsml().preferences.fontPreferences.setFontSize(fontSize);
   }

}
