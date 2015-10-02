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
package org.lisoft.lsml.view.mechlab;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;

import org.lisoft.lsml.view.render.StyleManager;

/**
 * A label that displays two angles as overlaid
 * 
 * @author Emily Björk
 *
 */
public class AngleDisplay extends JLabel {
    private static final long    serialVersionUID = 8352294670775982189L;
    private final RenderingHints hints;
    private final double         base;
    private double               primary          = 0.0;
    private double               secondary        = 0.0;

    private Color borderColor = Color.BLACK;
    private Color secondaryColor = StyleManager.getColourBarSecondary();
    private Color primaryColor = StyleManager.getColourBarPrimary();
    
    public AngleDisplay(double aBase) {
        Dimension minimumSize = new Dimension(50, 50);
        setMinimumSize(minimumSize);
        Dimension preferredSize = new Dimension(100, 100);
        setPreferredSize(preferredSize);

        hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));

        base = aBase;
    }

    public void updateAngles(double aPrimary, double aSecondary) {
        primary = aPrimary;
        secondary = aSecondary;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension sz = super.getPreferredSize();
        sz.width = Math.min(sz.width, sz.height);
        sz.width = sz.height;
        return sz;
    }
    
    @Override
    public Dimension getMaximumSize() {
        Dimension sz = super.getMaximumSize();
        sz.width = Math.min(sz.width, sz.height);
        sz.width = sz.height;
        return sz;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHints(hints);

        int width = Math.min(getWidth(), getHeight()) - 1;
        int startAngle = (int) Math.round(base - (primary + secondary));
        int endAngle = (int) Math.round(2 * (primary + secondary));
        g2.setColor(secondaryColor);
        g2.fillArc(0, 0, width, width, startAngle, endAngle);

        startAngle = (int) Math.round(base - (primary));
        endAngle = (int) Math.round(2 * primary);
        g2.setColor(primaryColor);
        g2.fillArc(0, 0, width, width, startAngle, endAngle);

        g2.setColor(borderColor);
        //g2.drawOval(0, 0, width, width);
    }
}

