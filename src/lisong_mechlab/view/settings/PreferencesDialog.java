/*
 * @formatter:off
 * Li Song Mech Lab - A 'mech building tool for PGI's MechWarrior: Online.
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
package lisong_mechlab.view.settings;

import javax.swing.JDialog;

import lisong_mechlab.view.ProgramInit;

/**
 * This class contains the settings dialog for LSML.
 * 
 * @author Emily Björk
 */
public class PreferencesDialog extends JDialog{
   private static final long serialVersionUID = -7028706949151487418L;

   public PreferencesDialog(){
      super(ProgramInit.lsml(), "Settings", ModalityType.APPLICATION_MODAL);
      
      
   }
}
