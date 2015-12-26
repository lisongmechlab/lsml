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
package org.lisoft.lsml.view_fx.drawers;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.view_fx.style.HardPointFormatter;
import org.lisoft.lsml.view_fx.style.ModifierFormatter;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * This class implements {@link ListCell} for {@link OmniPod}s to show hard points and quirks for the pod.
 * 
 * @author Emily Björk
 */
public class OmniPodListCell extends ListCell<OmniPod> {
    private final HardPointFormatter hardPointFormatter = new HardPointFormatter();
    private final ModifierFormatter  modifierFormatter  = new ModifierFormatter();

    @Override
    protected void updateItem(OmniPod aItem, boolean aEmpty) {
        super.updateItem(aItem, aEmpty);
        if (null == aItem) {
            setGraphic(null);
            return;
        }

        VBox root = new VBox();
        HBox box = new HBox();

        for (HardPointType hardPointType : HardPointType.values()) {
            int num = aItem.getHardPointCount(hardPointType);
            if (num > 0) {
                box.getChildren().add(hardPointFormatter.format(num, hardPointType));
            }
        }

        root.getChildren().add(new Label(aItem.getChassisName()));
        root.getChildren().add(box);
        root.getStyleClass().add("omni-pod-cell");
        root.getStyleClass().add(StyleManager.CSS_CLASS_EQUIPPED);

        modifierFormatter.format(aItem.getQuirks(), root.getChildren());
        setGraphic(root);
    }

}
