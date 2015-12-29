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
package org.lisoft.lsml.view;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Handles showing a splash screen on program startup.
 * 
 * @author Emily Björk
 */
public class SplashScreen extends JFrame {

    private static SplashScreen instance;

    private String              progressSubText = "";
    private String              progressText    = "";

    private class BackgroundImage extends JComponent {
        private static final long serialVersionUID = 2294812231919303690L;
        private Image             image;

        public BackgroundImage(Image anImage) {
            image = anImage;
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.drawImage(image, 0, 0, this);
            int penX = 20;
            int penY = 250;
            g.setColor(Color.WHITE);
            g.drawString(progressText, penX, penY);
            penY += 15;
            g.drawString(progressSubText, penX, penY);
        }
    }

    /**
     * 
     */
    public SplashScreen() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
                Image splash = defaultToolkit.getImage(getClass().getResource("/resources/splash.png"));
                setContentPane(new BackgroundImage(splash));
                setIconImage(ProgramInit.programIcon);
                setResizable(false);
                setUndecorated(true);
                setTitle("loading...");
                setSize(560, 320);

                // This works for multi-screen configurations in linux as well.
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                DisplayMode mode = ge.getDefaultScreenDevice().getDisplayMode();

                setLocation(mode.getWidth() / 2 - getSize().width / 2, mode.getHeight() / 2 - getSize().height / 2);
                setVisible(true);
                getRootPane().setBorder(BorderFactory.createLineBorder(Color.BLACK));
                getRootPane().putClientProperty("Window.shadow", Boolean.TRUE);
            }
        });
    }

    public static void showSplash() {
        if (null == instance) {
            instance = new SplashScreen();
        }
    }

    public static void closeSplash() {
        if (null != instance) {
            instance.dispose();
            instance = null;
        }
    }

    public static void setProcessText(String aString) {
        if (null != instance) {
            instance.progressText = aString;
            instance.repaint();
        }
    }

    public static void setSubText(String aString) {
        if (null != instance) {
            instance.progressSubText = aString;
            instance.repaint();
        }
    }
}
