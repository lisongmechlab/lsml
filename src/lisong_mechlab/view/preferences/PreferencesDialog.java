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
package lisong_mechlab.view.preferences;

import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.action.SetFontSizeAction;
import lisong_mechlab.view.preferences.FontPreferences.FontSize;

/**
 * This class contains the settings dialog for LSML.
 * 
 * @author Emily Björk
 */
public class PreferencesDialog extends JDialog{
   private static final long serialVersionUID = -7028706949151487418L;

   public PreferencesDialog(){
      super(ProgramInit.lsml(), "Settings", ModalityType.APPLICATION_MODAL);

      addAppearancePane();

      pack();
      setLocationRelativeTo(ProgramInit.lsml());
      setVisible(true);
   }

   private void addAppearancePane(){
      JPanel panel = new JPanel();
      panel.setBorder(new TitledBorder("Appearance"));

      JRadioButton fontVerySmall = new JRadioButton();
      JRadioButton fontSmall = new JRadioButton();
      JRadioButton fontNormal = new JRadioButton();
      JRadioButton fontLarge = new JRadioButton();
      JRadioButton fontVeryLarge = new JRadioButton();

      fontVerySmall.setAction(new SetFontSizeAction("Very Small", null, FontSize.VerySmall));
      fontSmall.setAction(new SetFontSizeAction("Small", null, FontSize.Small));
      fontNormal.setAction(new SetFontSizeAction("Normal", null, FontSize.Normal));
      fontLarge.setAction(new SetFontSizeAction("Large", null, FontSize.Large));
      fontVeryLarge.setAction(new SetFontSizeAction("Very Large", null, FontSize.VeryLarge));

      ButtonGroup fontSize = new ButtonGroup();
      fontSize.add(fontVerySmall);
      fontSize.add(fontSmall);
      fontSize.add(fontNormal);
      fontSize.add(fontLarge);
      fontSize.add(fontVeryLarge);

      switch( ProgramInit.lsml().preferences.fontPreferences.getFontSize() ){
         case Normal:
            fontNormal.setSelected(true);
            break;
         case Small:
            fontSmall.setSelected(true);
            break;
         case VerySmall:
            fontVerySmall.setSelected(true);
            break;
         case Large:
            fontLarge.setSelected(true);
            break;
         case VeryLarge:
            fontVeryLarge.setSelected(true);
            break;
      }

      JPanel buttons = new JPanel(new GridLayout(1, 0));
      buttons.add(fontVerySmall);
      buttons.add(fontSmall);
      buttons.add(fontNormal);
      buttons.add(fontLarge);
      buttons.add(fontVeryLarge);

      JLabel fontSizeLabel = new JLabel("Default Font Size:");
      fontSizeLabel.setLabelFor(buttons);

      panel.add(fontSizeLabel);
      panel.add(buttons);
      add(panel);
   }
}
