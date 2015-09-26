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
package org.lisoft.lsml.view.graphs;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.view.render.StyleManager;

/**
 * This class provides colors for the drawing of weapon graphs.
 * 
 * @author Emily Björk
 *
 */
public class WeaponColouredDrawingSupplier extends DefaultDrawingSupplier {
    private final List<Paint> colours     = new ArrayList<>();
    private int               colourIndex = 0;

    private Color colorShift(Color aColour, int aMax, int aCurr) {
        float hsb[] = new float[3];
        Color.RGBtoHSB(aColour.getRed(), aColour.getGreen(), aColour.getBlue(), hsb);

        float blend = aMax == 1 ? 1.0f : (float) aCurr / (aMax - 1);
        float range_min = 0.55f;
        float range_max = 1.0f;
        float saturation = (range_max - range_min) * blend + range_min;

        hsb[1] *= saturation;
        hsb[2] *= saturation;

        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    public void updateColoursToMatch(Collection<Weapon> aWeapons) {
        int energy = 0;
        int ballistic = 0;
        int missile = 0;
        for (Weapon w : aWeapons) {
            if (w instanceof EnergyWeapon)
                energy++;
            else if (w instanceof BallisticWeapon)
                ballistic++;
            else if (w instanceof MissileWeapon)
                missile++;
        }

        int m = 0;
        int b = 0;
        int e = 0;
        colours.clear();
        for (Weapon w : aWeapons) {
            if (w instanceof EnergyWeapon) {
                Color c = StyleManager.getBgColorFor(HardPointType.ENERGY);
                colours.add(colorShift(c, energy, e++));
            }
            else if (w instanceof BallisticWeapon) {
                Color c = StyleManager.getBgColorFor(HardPointType.BALLISTIC);
                colours.add(colorShift(c, ballistic, b++));
            }
            else if (w instanceof MissileWeapon) {
                Color c = StyleManager.getBgColorFor(HardPointType.MISSILE);
                colours.add(colorShift(c, missile, m++));
            }
        }
        colourIndex = 0;
    }

    @Override
    public Paint getNextPaint() {
        Paint result = colours.get(colourIndex % colours.size());
        colourIndex++;
        return result;
    }

    @Override
    public Paint getNextFillPaint() {
        Paint result = colours.get(colourIndex % colours.size());
        colourIndex++;
        return result;
    }
}
