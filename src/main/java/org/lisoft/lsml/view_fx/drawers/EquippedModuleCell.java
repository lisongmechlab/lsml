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

import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.view_fx.controls.FixedRowsListView;
import org.lisoft.lsml.view_fx.style.StyleManager;

import javafx.geometry.Pos;

/**
 * This class is responsible for rendering items on the components.
 * 
 * @author Emily Björk
 */
public class EquippedModuleCell extends FixedRowsListView.FixedListCell<PilotModule> {
    public EquippedModuleCell(FixedRowsListView<PilotModule> aItemView) {
        super(aItemView);
        setAlignment(Pos.TOP_LEFT);
        getStyleClass().add(StyleManager.CLASS_EQUIPPED);
        setRowSpan(1);
    }

    @Override
    protected void updateItem(PilotModule aModule, boolean aEmpty) {
        super.updateItem(aModule, aEmpty);

        if (null == aModule) {
            setText("EMPTY");
            setGraphic(null);
        }
        else {
            setGraphic(null);
            setText(aModule.getName());
        }
        StyleManager.changeStyle(this, aModule);
    }
}