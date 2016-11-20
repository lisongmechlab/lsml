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
package org.lisoft.lsml.view_fx.controls;

import org.lisoft.lsml.command.CmdGarageRename;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.garage.GaragePath;
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
 * @author Emily Björk
 */
public class NameField<T extends NamedObject> extends StackPane {
    private final TextField field = new TextField();
    private final Label label = new Label();
    private GaragePath<T> path;
    private T object;

    /**
     * Creates a new NameField and associates it with a {@link NamedObject}.
     *
     * @param aStack
     *            The {@link CommandStack} to use for affecting the name of the object.
     * @param aMD
     *            A {@link MessageDelivery} to use for notifying changes of the object on.
     */
    public NameField(CommandStack aStack, MessageDelivery aMD) {
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

                if (!LiSongMechLab.safeCommand(this, aStack, new CmdGarageRename<>(aMD, path, field.getText()), aMD)) {
                    field.setText(object.getName());
                    label.setText(object.getName());
                }
            }
            field.setVisible(false);
        });
    }

    /**
     * Changes the object that is represented by this {@link NameField}.
     *
     * If the object changes location in the garage, this method must be called to update.
     *
     * @param aObject
     *            The object to match the name to.
     * @param aGaragePath
     *            The path of the object in the garage. May be <code>null</code> if the object is not in a garage.
     */
    public void changeObject(T aObject, GaragePath<T> aGaragePath) {
        path = aGaragePath;
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
