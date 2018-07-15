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

import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.messages.ApplicationMessage;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.export.Base64LoadoutCoder;
import org.lisoft.lsml.model.export.MWOCoder;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view_fx.util.FxControlUtils;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;

/**
 * This dialog allows the user to paste a string that will be auto detected and parsed as a LSML loadout if possible.
 *
 * @author Li Song
 */
public class ImportMechStringDialog extends LsmlAlert {

    private final TextField inputField;
    private final Base64LoadoutCoder lsmlCoder;
    private final MWOCoder mwoCoder;
    private final ErrorReporter errorReporter;
    private final MessageXBar xBar;

    public ImportMechStringDialog(Window aSource, Base64LoadoutCoder aLsmlCoder, MWOCoder aMwoCoder,
            ErrorReporter aErrorReporter, MessageXBar aGlobalXBar) {
        super(aSource, AlertType.CONFIRMATION, "");

        lsmlCoder = aLsmlCoder;
        mwoCoder = aMwoCoder;
        errorReporter = aErrorReporter;
        xBar = aGlobalXBar;

        final Text instruction = new Text(
                "Note: For smurfy import either export from smurfy or use the import/export section "
                        + "on the left side of the main window.");

        inputField = new TextField();
        inputField.setPromptText("LSML or MWO string...");
        FxControlUtils.fixTextField(inputField);

        setHeaderText("Paste the LSML or MWO export string in the box below.");
        setTitle("MWO/LSML loadout import");
        getDialogPane().setContent(new VBox(instruction, inputField));
    }

    public void showAndImport() {
        // Required to make the input text field auto-focused.
        Platform.runLater(() -> {
            inputField.requestFocus();
        });

        showAndWait().ifPresent(aButton -> {
            if (aButton != ButtonType.OK) {
                return;
            }
            String input = inputField.getText();
            if (null == input) {
                return;
            }
            input = input.trim();
            if (input.isEmpty()) {
                return;
            }
            try {
                final Loadout loadout = universalImport(input);
                xBar.post(new ApplicationMessage(loadout, ApplicationMessage.Type.OPEN_LOADOUT, getDialogPane()));
            }
            catch (final Exception e) {
                errorReporter.error(getOwner(), "Error ocurred when decoding loadout!",
                        "LSML was unable to decode the string [" + input + "].", e);
            }
        });
    }

    private Loadout universalImport(String aText) throws Exception {
        if (mwoCoder.canDecode(aText)) {
            return mwoCoder.decode(aText);
        }
        return lsmlCoder.parse(aText);
    }

}
