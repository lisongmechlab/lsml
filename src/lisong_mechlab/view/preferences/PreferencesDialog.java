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
package lisong_mechlab.view.preferences;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.action.SetFontSizeAction;
import lisong_mechlab.view.preferences.FontPreferences.FontSize;
import lisong_mechlab.view.render.StyleManager;

/**
 * This class contains the settings dialog for LSML.
 * 
 * @author Li Song
 */
public class PreferencesDialog extends JDialog{
   private static final long serialVersionUID = -7028706949151487418L;

   public PreferencesDialog(){
      super(ProgramInit.lsml(), "Settings", ModalityType.APPLICATION_MODAL);

      JPanel root = new JPanel();
      root.setLayout(new BoxLayout(root, BoxLayout.PAGE_AXIS));
      addAppearancePane(root);
      addUiPane(root);
      addCorePane(root);

      setContentPane(root);
      pack();
      setLocationRelativeTo(ProgramInit.lsml());
      setVisible(true);
   }

   private void addCorePane(JPanel aRoot){
      JPanel panel = new JPanel();
      panel.setBorder(StyleManager.sectionBorder("LSML Core Settings"));

      final JCheckBox useBundledData = new JCheckBox("Use bundled data",
                                                     Boolean.parseBoolean(PreferenceStore.getString(PreferenceStore.USEBUNDLED_DATA, "false")));
      useBundledData.setToolTipText("<html>If checked, LSML will quietly fallback to bundled data files if no game install is available.<br/>"
                                    + "Otherwise it will prompt you to locate the game install on next startup.</html>");
      useBundledData.addActionListener(new AbstractAction(){
         private static final long serialVersionUID = -8136020916897237506L;

         @Override
         public void actionPerformed(ActionEvent aArg0){
            PreferenceStore.setString(PreferenceStore.USEBUNDLED_DATA, Boolean.toString(useBundledData.isSelected()));
         }
      });
      panel.add(useBundledData);

      aRoot.add(panel);
   }

   private void addUiPane(JPanel aRoot){
      JPanel panel = new JPanel();
      panel.setBorder(StyleManager.sectionBorder("UI Behavior"));

      final JCheckBox smartPlace = new JCheckBox("Use SmartPlace", ProgramInit.lsml().preferences.uiPreferences.getUseSmartPlace());
      smartPlace.setToolTipText("SmartPlace allows you to place items that would not fit your current loadout by automatically moving items around.");
      smartPlace.addActionListener(new AbstractAction(){
         private static final long serialVersionUID = -8136020916897237506L;

         @Override
         public void actionPerformed(ActionEvent aArg0){
            ProgramInit.lsml().preferences.uiPreferences.setUseSmartPlace(smartPlace.isSelected());
         }
      });
      panel.add(smartPlace);

      final JCheckBox compactMode = new JCheckBox("Use Compact UI", ProgramInit.lsml().preferences.uiPreferences.getCompactMode());
      compactMode.setToolTipText("Tries to compact the UI to make it useful on smaller screens.");
      compactMode.addActionListener(new AbstractAction(){
         private static final long serialVersionUID = -8136020916897237506L;

         @Override
         public void actionPerformed(ActionEvent aArg0){
            ProgramInit.lsml().preferences.uiPreferences.setCompactMode(compactMode.isSelected());
         }
      });
      panel.add(compactMode);

      final JCheckBox hideSpecials = new JCheckBox("Hide mech variations", ProgramInit.lsml().preferences.uiPreferences.getHideSpecialMechs());
      hideSpecials.setToolTipText("<html>Will hide mech variations (champion, founders, phoenix, sarah, etc) from chassis lists.<br/>"
                                  + "Stock loadouts are still available on the \"Load stock\" menu action on relevant loadouts</html>");
      hideSpecials.addActionListener(new AbstractAction(){
         private static final long serialVersionUID = -8136020916897237506L;

         @Override
         public void actionPerformed(ActionEvent aArg0){
            ProgramInit.lsml().preferences.uiPreferences.setHideSpecialMechs(hideSpecials.isSelected());
         }
      });
      panel.add(hideSpecials);

      aRoot.add(panel);
   }

   private void addAppearancePane(JPanel aRoot){
      JPanel panel = new JPanel();
      panel.setBorder(StyleManager.sectionBorder("Appearance"));

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
      aRoot.add(panel);
   }
}
