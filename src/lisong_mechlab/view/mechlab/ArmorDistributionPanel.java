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

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.loadout.DistributeArmorOperation;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.view.render.StyleManager;

/**
 * This panel renders a controller for the armor distribution tool.
 * 
 * @author Li Song
 */
public class ArmorDistributionPanel extends JPanel{
   private static final long serialVersionUID = 6835003047682738947L;

   public ArmorDistributionPanel(final Loadout aLoadout, final OperationStack aStack, final MessageXBar aXBar){
      setBorder(StyleManager.sectionBorder("Armor distribution"));
      GroupLayout gl = new GroupLayout(this);
      setLayout(gl);

      final JLabel armorLabel = new JLabel("Amount:");
      final int maxArmor = aLoadout.getChassi().getArmorMax();
      final JSlider armorSlider = new JSlider(0, maxArmor, aLoadout.getArmor());
      armorSlider.setMajorTickSpacing(100);
      armorSlider.setMinorTickSpacing(25);
      armorSlider.setLabelTable(armorSlider.createStandardLabels(100));
      armorSlider.setPaintTicks(true);
      armorSlider.setPaintLabels(true);
      armorSlider.setToolTipText("<html>Drag the slider to adjust armor in half-ton increments.<br/>"
                                 + "Armor will be placed automatically among components without manually set armor values.<br/>"
                                 + "You can right click on the component's armor value to reset a manually set value.</html>");

      LoadoutPart ct = aLoadout.getPart(Part.CenterTorso);
      int backArmor = ct.getArmor(ArmorSide.BACK);
      int frontArmor = ct.getArmor(ArmorSide.FRONT);
      int initialFrontBack = 5;
      if(backArmor != 0 && frontArmor != 0 ){
         initialFrontBack = (int)Math.round((double)frontArmor / backArmor);
      }
      
      final JLabel ratioLabel = new JLabel("Front/Back:");
      final JSlider ratioSlider = new JSlider(1, 16, initialFrontBack);
      ratioSlider.setMajorTickSpacing(5);
      ratioSlider.setMinorTickSpacing(1);
      ratioSlider.setPaintTicks(true);
      ratioSlider.setSnapToTicks(true);
      ratioSlider.setLabelTable(ratioSlider.createStandardLabels(5));
      ratioSlider.setPaintLabels(true);

      //gl.setAutoCreateContainerGaps(true);
      //gl.setAutoCreateGaps(true);

      gl.setHorizontalGroup(gl.createSequentialGroup()
                            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(armorLabel).addComponent(ratioLabel))
                            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(armorSlider).addComponent(ratioSlider)));
      
      gl.setVerticalGroup(gl.createSequentialGroup()
                          .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(armorLabel).addComponent(armorSlider))
                          .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(ratioLabel).addComponent(ratioSlider)));

      armorSlider.addChangeListener(new ChangeListener(){
         @Override
         public void stateChanged(ChangeEvent aArg0){
            while( aStack.nextUndo() instanceof DistributeArmorOperation ){
               aStack.undo();
            }
            aStack.pushAndApply(new DistributeArmorOperation(aLoadout, armorSlider.getValue(), ratioSlider.getValue(), aXBar));
         }
      });
      ratioSlider.addChangeListener(new ChangeListener(){
         @Override
         public void stateChanged(ChangeEvent aArg0){
            while( aStack.nextUndo() instanceof DistributeArmorOperation ){
               aStack.undo();
            }
            aStack.pushAndApply(new DistributeArmorOperation(aLoadout, armorSlider.getValue(), ratioSlider.getValue(), aXBar));
         }
      });
   }
}
