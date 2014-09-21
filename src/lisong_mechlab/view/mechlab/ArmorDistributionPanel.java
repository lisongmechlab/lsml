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
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.OpDistributeArmor;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.OpSetArmor;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;
import lisong_mechlab.util.message.Message;
import lisong_mechlab.util.message.MessageXBar;
import lisong_mechlab.view.render.StyleManager;

/**
 * This panel renders a controller for the armor distribution tool.
 * 
 * @author Li Song
 */
public class ArmorDistributionPanel extends JPanel implements Message.Recipient, ChangeListener {
	private static final long		serialVersionUID	= 6835003047682738947L;

	private final LoadoutBase<?>	loadout;
	private final OperationStack	stack;
	private final MessageXBar		xBar;
	private final JSlider			ratioSlider;
	private final JSlider			armorSlider;
	private final OperationStack	privateStack		= new OperationStack(0);

	private boolean					disableSliderAction	= false;
	private boolean					armorOpInProgress	= false;
	private int						lastRatio			= 0;
	private int						lastAmount			= 0;

	private class ResetManualArmorOperation extends CompositeOperation {
		private final LoadoutBase<?>	opLoadout	= loadout;

		public ResetManualArmorOperation() {
			super("reset manual armor", xBar);
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
		public boolean canCoalescele(Operation aOperation) {
			if (aOperation != this && aOperation != null && aOperation instanceof ResetManualArmorOperation) {
				ResetManualArmorOperation operation = (ResetManualArmorOperation) aOperation;
				return operation.opLoadout == opLoadout;
			}
			return false;
		}

		@Override
		public void buildOperation() {
			for (ConfiguredComponentBase loadoutPart : loadout.getComponents()) {
				if (loadoutPart.getInternalComponent().getLocation().isTwoSided()) {
					addOp(new OpSetArmor(messageBuffer, loadout, loadoutPart, ArmorSide.FRONT,
							loadoutPart.getArmor(ArmorSide.FRONT), false));
					addOp(new OpSetArmor(messageBuffer, loadout, loadoutPart, ArmorSide.BACK,
							loadoutPart.getArmor(ArmorSide.BACK), false));
				} else {
					addOp(new OpSetArmor(messageBuffer, loadout, loadoutPart, ArmorSide.ONLY,
							loadoutPart.getArmor(ArmorSide.ONLY), false));
				}
			}
		}
	}

	private class ArmorSliderOperation extends CompositeOperation {
		private final JSlider	slider;
		private final int		newValue;
		private final int		oldValue;

		public ArmorSliderOperation(JSlider aSlider, int aOldValue) {
			super("armor adjustment", xBar);
			slider = aSlider;
			oldValue = aOldValue;
			newValue = slider.getValue();

			addOp(new OpDistributeArmor(loadout, armorSlider.getValue(), ratioSlider.getValue(), messageBuffer));
		}

		@Override
		public boolean canCoalescele(Operation aOperation) {
			if (aOperation != this && aOperation != null && aOperation instanceof ArmorSliderOperation) {
				ArmorSliderOperation op = (ArmorSliderOperation) aOperation;
				return slider == op.slider && oldValue == op.oldValue;
			}
			return false;
		}

		@Override
		protected void undo() {
			disableSliderAction = true;
			slider.setValue(oldValue);
			// super.undo();
			disableSliderAction = false;
		}

		@Override
		protected void apply() {
			disableSliderAction = true;
			slider.setValue(newValue);
			super.apply();
			disableSliderAction = false;
		}

		@Override
		public void buildOperation() {
			// TODO I think this operation possibly should inherit from OpDistributeArmor
		}
	}

	public ArmorDistributionPanel(final LoadoutBase<?> aLoadout, final OperationStack aStack, final MessageXBar aXBar) {
		setBorder(StyleManager.sectionBorder("Automatic Armor distribution"));
		setLayout(new BorderLayout());

		stack = aStack;
		xBar = aXBar;
		loadout = aLoadout;

		xBar.attach(this);

		final JButton resetAll = new JButton(new AbstractAction("Reset manually set armor") {
			private static final long	serialVersionUID	= -2645636713484404605L;

			@Override
			public void actionPerformed(ActionEvent aArg0) {
				stack.pushAndApply(new ResetManualArmorOperation());
			}
		});
		resetAll.setToolTipText("You can right click on the armor text on individual components.");
		add(resetAll, BorderLayout.SOUTH);

		final JLabel armorLabel = new JLabel("Amount:");
		final int maxArmor = aLoadout.getChassis().getArmorMax();
		armorSlider = new JSlider(0, maxArmor, aLoadout.getArmor());
		armorSlider.setMajorTickSpacing(100);
		armorSlider.setMinorTickSpacing(25);
		armorSlider.setLabelTable(armorSlider.createStandardLabels(100));
		armorSlider.setPaintTicks(true);
		armorSlider.setPaintLabels(true);
		armorSlider.setToolTipText("<html>Drag the slider to adjust armor in half-ton increments.<br/>"
				+ "Armor will be placed automatically among components without manually set armor values.<br/>"
				+ "You can right click on the component's armor value to reset a manually set value.</html>");

		ConfiguredComponentBase ct = aLoadout.getComponent(Location.CenterTorso);
		int backArmor = ct.getArmor(ArmorSide.BACK);
		int frontArmor = ct.getArmor(ArmorSide.FRONT);
		int initialFrontBack = 5;
		if (backArmor != 0 && frontArmor != 0) {
			initialFrontBack = (int) Math.round((double) frontArmor / backArmor);
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
		gl.setHorizontalGroup(gl
				.createSequentialGroup()
				.addGroup(
						gl.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(armorLabel)
								.addComponent(ratioLabel))
				.addGroup(
						gl.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(armorSlider)
								.addComponent(ratioSlider)));

		gl.setVerticalGroup(gl
				.createSequentialGroup()
				.addGroup(
						gl.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(armorLabel)
								.addComponent(armorSlider))
				.addGroup(
						gl.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(ratioLabel)
								.addComponent(ratioSlider)));

		lastAmount = armorSlider.getValue();
		lastRatio = ratioSlider.getValue();

		armorSlider.addChangeListener(this);
		ratioSlider.addChangeListener(this);

		add(sliderPanel, BorderLayout.CENTER);
	}

	@Override
	public void stateChanged(ChangeEvent aEvent) {
		if (disableSliderAction)
			return;
		if (aEvent.getSource() == ratioSlider)
			stack.pushAndApply(new ArmorSliderOperation(ratioSlider, lastRatio));
		else if (aEvent.getSource() == armorSlider)
			stack.pushAndApply(new ArmorSliderOperation(armorSlider, lastAmount));

		if (!armorSlider.getValueIsAdjusting())
			lastAmount = armorSlider.getValue();
		if (!ratioSlider.getValueIsAdjusting())
			lastRatio = ratioSlider.getValue();
	}

	public void updateArmorDistribution() {
		if(armorOpInProgress)
			return;
		armorOpInProgress = true;
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				privateStack.pushAndApply(new OpDistributeArmor(loadout, armorSlider.getValue(), ratioSlider.getValue(), xBar));
				armorOpInProgress = false;	
			}
		});
	}

	/**
	 * @see lisong_mechlab.util.message.Message.Recipient#receive(Message)
	 */
	@Override
	public void receive(Message aMsg) {
		if (aMsg.isForMe(loadout) && aMsg instanceof ConfiguredComponentBase.ComponentMessage) {
			ConfiguredComponentBase.ComponentMessage message = (ConfiguredComponentBase.ComponentMessage) aMsg;
			if (message.automatic)
				return;
			updateArmorDistribution();
		}
	}
}
