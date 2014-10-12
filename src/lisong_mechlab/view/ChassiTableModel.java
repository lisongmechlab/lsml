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
package lisong_mechlab.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisClass;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.item.Faction;

public class ChassiTableModel extends AbstractTableModel {
    private static final long             serialVersionUID = -2726840937519789976L;
    private final List<ChassisBase>       mechs            = new ArrayList<>();
    private final Comparator<ChassisBase> cmp              = new Comparator<ChassisBase>() {
                                                               @Override
                                                               public int compare(ChassisBase aArg0, ChassisBase aArg1) {
                                                                   if (aArg0.getMassMax() == aArg1.getMassMax())
                                                                       return aArg0.getMwoName().compareTo(
                                                                               aArg1.getMwoName());
                                                                   return Integer.compare(aArg0.getMassMax(),
                                                                           aArg1.getMassMax());
                                                               }
                                                           };
    private final ChassisClass            chassiClass;
    private final Faction                 faction;

    public ChassiTableModel(Faction aFaction, ChassisClass aChassiClass, boolean aFilterSpecials) {
        faction = aFaction;
        chassiClass = aChassiClass;
        recreate(aFilterSpecials);
    }

    public void recreate(boolean aFilterSpecials) {
        Collection<? extends ChassisBase> all = ChassisDB.lookup(chassiClass);

        mechs.clear();
        for (ChassisBase base : all) {
            if (aFilterSpecials && base.getVariantType().isVariation()) {
                continue;
            }

            if (base.getFaction().isCompatible(faction)) {
                mechs.add(base);
            }
        }

        Collections.sort(mechs, cmp);
        fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public int getRowCount() {
        return mechs.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        return mechs.get(row);
    }
}
