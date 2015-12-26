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
package org.lisoft.lsml.view_fx.properties;

import org.lisoft.lsml.command.CmdSetArmor;
import org.lisoft.lsml.messages.ArmorMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.chassi.ArmorSide;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.util.CommandStack;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextFormatter;

/**
 * @author Emily
 *
 */
public class ArmorFactory extends IntegerSpinnerValueFactory implements MessageReceiver {

    private final BooleanProperty         manualSet = new SimpleBooleanProperty();
    private final ConfiguredComponentBase component;
    private final ArmorSide               side;
    private boolean                       writeBack = true;
    private final CommandStack            stack;
    private final TextFormatter<Integer>  formatter;

    public ArmorFactory(MessageXBar aMessageDelivery, LoadoutBase<?> aLoadout, ConfiguredComponentBase aComponent,
            ArmorSide aArmorSide, CommandStack aStack, Spinner<Integer> aSpinner) {
        super(0, aComponent.getInternalComponent().getArmorMax());
        aMessageDelivery.attach(this);
        setWrapAround(false);
        component = aComponent;
        side = aArmorSide;
        stack = aStack;

        setValue(component.getArmor(side));
        manualSet.set(component.hasManualArmor());

        valueProperty().addListener((aObservable, aOld, aNew) -> {
            if (writeBack) {
                try {
                    stack.pushAndApply(
                            new CmdSetArmor(aMessageDelivery, aLoadout, component, side, aNew.intValue(), true));
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
        if (aMsg instanceof ArmorMessage) {
            ArmorMessage armorMessage = (ArmorMessage) aMsg;
            if (armorMessage.component == component) {
                writeBack = false;
                setValue(component.getArmor(side));
                writeBack = true;

                manualSet.set(component.hasManualArmor());
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
