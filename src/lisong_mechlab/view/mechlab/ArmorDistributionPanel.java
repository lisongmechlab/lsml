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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
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
import lisong_mechlab.model.loadout.part.SetArmorOperation;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;
import lisong_mechlab.view.render.StyleManager;

/**
 * This panel renders a controller for the armor distribution tool.
 * 
 * @author Li Song
 */
public class ArmorDistributionPanel extends JPanel implements MessageXBar.Reader, ChangeListener{
   private static final long    serialVersionUID    = 6835003047682738947L;

   private final Loadout        loadout;
   private final OperationStack stack;
   private final MessageXBar    xBar;
   private final JSlider        ratioSlider;
   private final JSlider        armorSlider;

   private int                  lastRatio           = 0;
   private int                  lastAmount          = 0;

   boolean                      disableSliderAction = false;

   class ResetManualArmorOperation extends CompositeOperation{
      private final Loadout opLoadout = loadout;
      
      public ResetManualArmorOperation(){
         super("reset manual armor");
         for(LoadoutPart loadoutPart : loadout.getPartLoadOuts()){
            if( loadoutPart.getInternalPart().getType().isTwoSided() ){
               addOp(new SetArmorOperation(xBar, loadoutPart, ArmorSide.FRONT, loadoutPart.getArmor(ArmorSide.FRONT), false));
               addOp(new SetArmorOperation(xBar, loadoutPart, ArmorSide.BACK, loadoutPart.getArmor(ArmorSide.BACK), false));
            }
            else{
               addOp(new SetArmorOperation(xBar, loadoutPart, ArmorSide.ONLY, loadoutPart.getArmor(ArmorSide.ONLY), false));
            }
         }
      }
      
      @Override
      protected void apply() {
         super.apply();
         updateArmorDistribution();
      }
      
      @Override
      protected void undo() {
         super.undo();
         updateArmorDistribution();
      }

      @Override
      public boolean canCoalescele(Operation aOperation){
         if(aOperation != this && aOperation != null && aOperation instanceof ResetManualArmorOperation){
            ResetManualArmorOperation operation = (ResetManualArmorOperation)aOperation;
            return operation.opLoadout == opLoadout;
         }
         return false;
      }
   }

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
         disableSliderAction = true;
         slider.setValue(oldValue);
         // super.undo();
         disableSliderAction = false;
      }

      @Override
      protected void apply(){
         disableSliderAction = true;
         slider.setValue(newValue);
         super.apply();
         disableSliderAction = false;
      }
   }

   public ArmorDistributionPanel(final Loadout aLoadout, final OperationStack aStack, final MessageXBar aXBar){
      setBorder(StyleManager.sectionBorder("Automatic Armor distribution"));
      setLayout(new BorderLayout());

      stack = aStack;
      xBar = aXBar;
      loadout = aLoadout;

      xBar.attach(this);

      final JButton resetAll = new JButton(new AbstractAction("Reset manually set armor"){
         private static final long serialVersionUID = -2645636713484404605L;

         @Override
         public void actionPerformed(ActionEvent aArg0){
            stack.pushAndApply(new ResetManualArmorOperation());
         }
      });
      resetAll.setToolTipText("You can right click on the armor text on individual components.");
      add(resetAll, BorderLayout.SOUTH);

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

      JPanel sliderPanel = new JPanel();
      GroupLayout gl = new GroupLayout(sliderPanel);
      sliderPanel.setLayout(gl);
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

      add(sliderPanel, BorderLayout.CENTER);
   }

   OperationStack privateStack = new OperationStack(0);

   @Override
   public void stateChanged(ChangeEvent aEvent){
      if( disableSliderAction )
         return;
      if( aEvent.getSource() == ratioSlider )
         stack.pushAndApply(new ArmorSliderOperation(ratioSlider, lastRatio));
      else if( aEvent.getSource() == armorSlider )
         stack.pushAndApply(new ArmorSliderOperation(armorSlider, lastAmount));

      if( !armorSlider.getValueIsAdjusting() )
         lastAmount = armorSlider.getValue();
      if( !ratioSlider.getValueIsAdjusting() )
         lastRatio = ratioSlider.getValue();
   }

   public void updateArmorDistribution(){
      privateStack.pushAndApply(new DistributeArmorOperation(loadout, armorSlider.getValue(), ratioSlider.getValue(), xBar));
   }

   /**
    * @see lisong_mechlab.util.MessageXBar.Reader#receive(lisong_mechlab.util.MessageXBar.Message)
    */
   @Override
   public void receive(Message aMsg){
      if( aMsg.isForMe(loadout) && aMsg instanceof LoadoutPart.Message ){
         LoadoutPart.Message message = (LoadoutPart.Message)aMsg;
         if( message.automatic )
            return;
         updateArmorDistribution();
      }
   }
}
