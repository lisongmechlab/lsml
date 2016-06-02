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
package org.lisoft.lsml.view_fx.properties;

import org.lisoft.lsml.command.CmdSetArmour;
import org.lisoft.lsml.messages.ArmourMessage;
import org.lisoft.lsml.messages.ArmourMessage.Type;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ArmourSide;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.util.CommandStack;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextFormatter;

/**
 * This {@link SpinnerValueFactory} is used for setting armour values on components.
 * 
 * @author Li Song
 */
public class ArmourFactory extends IntegerSpinnerValueFactory implements MessageReceiver {

    private final BooleanProperty manualSet = new SimpleBooleanProperty();
    private final ConfiguredComponent component;
    private final ArmourSide side;
    private boolean writeBack = true;
    private final CommandStack stack;
    private final TextFormatter<Integer> formatter;

    public ArmourFactory(MessageXBar aMessageDelivery, Loadout aLoadout, ConfiguredComponent aComponent,
            ArmourSide aArmourSide, CommandStack aStack, Spinner<Integer> aSpinner) {
        super(0, aComponent.getInternalComponent().getArmourMax());
        aMessageDelivery.attach(this);
        setWrapAround(false);
        component = aComponent;
        side = aArmourSide;
        stack = aStack;

        setValue(component.getArmour(side));
        manualSet.set(component.hasManualArmour());

        valueProperty().addListener((aObservable, aOld, aNew) -> {
            if (writeBack && aNew != null) {
                try {
                    stack.pushAndApply(
                            new CmdSetArmour(aMessageDelivery, aLoadout, component, side, aNew.intValue(), true));
                    if (manualSet.get()) {
                        aMessageDelivery.post(new ArmourMessage(component, Type.ARMOUR_DISTRIBUTION_UPDATE_REQUEST));
                    }
                }
                catch (Exception e) {
                    writeBack = false;
                    valueProperty().set(aOld);
                    writeBack = true;
                }
            }
        });

        formatter = new TextFormatter<>(getConverter(), getValue());
        valueProperty().bindBidirectional(formatter.valueProperty());
        aSpinner.getEditor().setTextFormatter(formatter);
    }

    @Override
    public void receive(Message aMsg) {
        if (aMsg instanceof ArmourMessage) {
            ArmourMessage armourMessage = (ArmourMessage) aMsg;
            if (armourMessage.component == component) {
                writeBack = false;
                setValue(component.getArmour(side));
                writeBack = true;

                manualSet.set(component.hasManualArmour());
            }
        }
    }

    public boolean getManualSet() {
        return manualSet.getValue();
    }

    public void setManualSet(boolean aValue) {
        manualSet.set(aValue);
    }

    public BooleanProperty manualSetProperty() {
        return manualSet;
    }
}
