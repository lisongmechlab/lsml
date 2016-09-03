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
package org.lisoft.lsml.view_fx.controls;

import java.util.Optional;

import org.lisoft.lsml.command.CmdRename;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.lsml.view_fx.style.StyleManager;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

/**
 * This control is a text field that only allows manual edit trigger. If not editing then it behaves as a label.
 *
 * @author Li Song
 */
public class NameField<T extends NamedObject> extends StackPane {
    private final TextField field = new TextField();
    private final Label label = new Label();
    private GarageDirectory<T> garageRoot;
    private T object;

    /**
     * Creates a new NameField and associates it with a {@link NamedObject}.
     *
     * @param aStack
     *            The {@link CommandStack} to use for affecting the name of the object.
     * @param aMessageDelivery
     *            A {@link MessageDelivery} to use for notifying changes of the object on.
     */
    public NameField(CommandStack aStack, MessageDelivery aMessageDelivery) {
        setAlignment(Pos.CENTER_LEFT);
        getChildren().setAll(label, field);
        FxControlUtils.fixTextField(field);
        label.textProperty().bind(field.textProperty());
        Bindings.bindContentBidirectional(label.getStyleClass(), getStyleClass());
        field.setVisible(false);
        field.prefColumnCountProperty().bind(field.textProperty().length());
        field.getStyleClass().add(StyleManager.CLASS_EDITABLE_LABEL);

        field.setOnAction(aEvent -> {
            if (!field.getText().equals(object.getName())) {

                final Optional<GarageDirectory<T>> foundDir = garageRoot.recursiveFind(object);
                GarageDirectory<T> dir = null;
                if (foundDir.isPresent()) {
                    dir = foundDir.get();
                }

                if (!LiSongMechLab.safeCommand(this, aStack,
                        new CmdRename<>(object, aMessageDelivery, field.getText(), dir), aMessageDelivery)) {
                    field.setText(object.getName());
                    label.setText(object.getName());
                }
            }
            field.setVisible(false);
        });
    }

    public void changeObject(T aObject, GarageDirectory<T> aGarageDir) {
        garageRoot = aGarageDir;
        object = aObject;
        setText(aObject.getName());
    }

    public String getText() {
        return field.getText();
    }

    public void setText(String aValue) {
        field.setText(aValue);
    }

    public void startEdit() {
        field.setVisible(true);
        field.requestFocus();
        field.selectAll();
    }

    public StringProperty textProperty() {
        return field.textProperty();
    }
}
