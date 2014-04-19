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
package lisong_mechlab.view.mechlab;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.loadout.DistributeArmorOperation;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;
import lisong_mechlab.view.render.StyleManager;

/**
 * This panel renders a controller for the armor distribution tool.
 * 
 * @author Emily Björk
 */
public class ArmorDistributionPanel extends JPanel implements MessageXBar.Reader, ChangeListener{
   private static final long    serialVersionUID = 6835003047682738947L;

   private final Loadout        loadout;
   private final OperationStack stack;
   private final MessageXBar    xBar;
   private final JSlider        ratioSlider;
   private final JSlider        armorSlider;

   private int                  lastRatio        = 0;
   private int                  lastAmount       = 0;
   private boolean              inprogress       = false;

   class ArmorSliderOperation extends CompositeOperation{
      private final JSlider slider;
      private final int     newValue;
      private final int     oldValue;

      public ArmorSliderOperation(JSlider aSlider, int aOldValue){
         super("armor adjustment");
         slider = aSlider;
         oldValue = aOldValue;
         newValue = slider.getValue();

         addOp(new DistributeArmorOperation(loadout, armorSlider.getValue(), ratioSlider.getValue(), xBar));
      }

      @Override
      public boolean canCoalescele(Operation aOperation){
         if( aOperation != this && aOperation != null && aOperation instanceof ArmorSliderOperation ){
            ArmorSliderOperation op = (ArmorSliderOperation)aOperation;
            return slider == op.slider && oldValue == op.oldValue;
         }
         return false;
      }

      @Override
      protected void undo(){
         inprogress = true;
         slider.setValue(oldValue);
         super.undo();
         inprogress = false;
      }

      @Override
      protected void apply(){
         inprogress = true;
         slider.setValue(newValue);
         super.apply();
         inprogress = false;
      }
   }

   public ArmorDistributionPanel(final Loadout aLoadout, final OperationStack aStack, final MessageXBar aXBar){
      setBorder(StyleManager.sectionBorder("Armor distribution"));
      GroupLayout gl = new GroupLayout(this);
      setLayout(gl);

      stack = aStack;
      xBar = aXBar;
      loadout = aLoadout;

      xBar.attach(this);

      final JLabel armorLabel = new JLabel("Amount:");
      final int maxArmor = aLoadout.getChassi().getArmorMax();
      armorSlider = new JSlider(0, maxArmor, aLoadout.getArmor());
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
      if( backArmor != 0 && frontArmor != 0 ){
         initialFrontBack = (int)Math.round((double)frontArmor / backArmor);
      }

      final JLabel ratioLabel = new JLabel("Front/Back:");
      ratioSlider = new JSlider(1, 16, initialFrontBack);
      ratioSlider.setMajorTickSpacing(5);
      ratioSlider.setMinorTickSpacing(1);
      ratioSlider.setPaintTicks(true);
      ratioSlider.setSnapToTicks(true);
      ratioSlider.setLabelTable(ratioSlider.createStandardLabels(5));
      ratioSlider.setPaintLabels(true);

      gl.setHorizontalGroup(gl.createSequentialGroup().addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(armorLabel)
                                                                  .addComponent(ratioLabel))
                              .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(armorSlider).addComponent(ratioSlider)));

      gl.setVerticalGroup(gl.createSequentialGroup().addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(armorLabel)
                                                                .addComponent(armorSlider))
                            .addGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(ratioLabel).addComponent(ratioSlider)));

      lastAmount = armorSlider.getValue();
      lastRatio = ratioSlider.getValue();

      armorSlider.addChangeListener(this);
      ratioSlider.addChangeListener(this);
   }

   OperationStack privateStack = new OperationStack(0);

   @Override
   public void stateChanged(ChangeEvent aEvent){
      if( aEvent == null ){
         // From MessageXBar. Update armor in response to loadout change. Don't generate an un-doable action.
         // As the previous action is undone, it will trigger another action to redistribute armor.

         inprogress = true;
         privateStack.pushAndApply(new DistributeArmorOperation(loadout, armorSlider.getValue(), ratioSlider.getValue(), xBar));
         inprogress = false;
      }
      else{
         if( !inprogress ){
            inprogress = true;
            // A "real" stateChanged()
            if( aEvent.getSource() == ratioSlider )
               stack.pushAndApply(new ArmorSliderOperation(ratioSlider, lastRatio));
            else if( aEvent.getSource() == armorSlider )
               stack.pushAndApply(new ArmorSliderOperation(armorSlider, lastAmount));
            inprogress = false;
         }

         if( !armorSlider.getValueIsAdjusting() )
            lastAmount = armorSlider.getValue();
         if( !ratioSlider.getValueIsAdjusting() )
            lastRatio = ratioSlider.getValue();

      }
   }

   /**
    * @see lisong_mechlab.util.MessageXBar.Reader#receive(lisong_mechlab.util.MessageXBar.Message)
    */
   @Override
   public void receive(Message aMsg){
      if( aMsg.isForMe(loadout) && aMsg instanceof LoadoutPart.Message && !inprogress ){
         inprogress = true;
         SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
               stateChanged(null);
            }
         });
      }
   }
}
