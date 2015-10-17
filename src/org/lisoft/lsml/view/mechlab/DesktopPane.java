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
package org.lisoft.lsml.view.mechlab;

import java.awt.Color;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.garage.DropShip;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.view.ItemTransferHandler;
import org.lisoft.lsml.view.mechlab.dropshipframe.DropShipFrame;
import org.lisoft.lsml.view.mechlab.loadoutframe.LoadoutFrame;

/**
 * This class is the {@link JDesktopPane} where all the {@link LoadoutFrame} and {@link DropShipFrame} are shown to the
 * user. It provides a method to be notified of the focus of the frames.
 * <p>
 * All methods must be called from the Swing EDT.
 * 
 * @author Li Song
 */
public class DesktopPane extends JDesktopPane implements InternalFrameListener {
    private static final int                  MAX_OPEN_WINDOWS = 10;
    private final List<InternalFrameListener> listeners        = new ArrayList<InternalFrameListener>();
    private final MessageXBar                 xBar;

    private transient int                     opened_windows;

    /**
     * Creates a new {@link DesktopPane}.
     * 
     * @param aXBar
     *            A {@link MessageXBar} to send messages to when a new loadout is opened.
     */
    public DesktopPane(MessageXBar aXBar) {
        assert (SwingUtilities.isEventDispatchThread());

        xBar = aXBar;
        setBorder(BorderFactory.createLoweredSoftBevelBorder());
        setBackground(Color.GRAY.brighter());
        setTransferHandler(new ItemTransferHandler());
        setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
    }

    public void openDropShip(DropShip aDropShip) {
        DropShipFrame frame = new DropShipFrame(xBar, xBar, aDropShip);
        frame.addInternalFrameListener(this);
        add(frame);

        frame.setLocation(20 * (opened_windows % MAX_OPEN_WINDOWS), 20 * (opened_windows % MAX_OPEN_WINDOWS));
        opened_windows++;

        try {
            frame.setVisible(true);
            frame.setFocusable(true);
            frame.setSelected(true);
        }
        catch (PropertyVetoException e) {
            // No-Op
        }
    }

    /**
     * Will open the given {@link LoadoutStandard} into the desktop pane by creating a new {@link LoadoutFrame}.
     * 
     * @param aLoadout
     *            The {@link LoadoutStandard} to create the frame for.
     * @param aDropShipMode 
     */
    public void openLoadout(LoadoutBase<?> aLoadout, boolean aDropShipMode) {
        LoadoutFrame frame = new LoadoutFrame(aLoadout, xBar, aDropShipMode);
        frame.addInternalFrameListener(this); // The desktop acts as forwarder of frame events from the frames.
        add(frame);

        frame.setLocation(20 * (opened_windows % MAX_OPEN_WINDOWS), 20 * (opened_windows % MAX_OPEN_WINDOWS));
        opened_windows++;

        try {
            frame.setVisible(true);
            frame.setFocusable(true);
            frame.setSelected(true);
        }
        catch (PropertyVetoException e) {
            // No-Op
        }
    }

    /**
     * Closes all open {@link LoadoutFrame}s. Exceptions from the frames are swallowed.
     * 
     * @return <code>true</code> if all {@link LoadoutFrame}s were closed with the user's permssion.
     */
    boolean closeAll() {
        assert (SwingUtilities.isEventDispatchThread());

        for (JInternalFrame frame : getAllFrames()) {
            try {
                frame.setClosed(true);
                frame.dispose();
            }
            catch (PropertyVetoException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Allows the given {@link InternalFrameListener} to receive {@link InternalFrameEvent}s from any subwindow of this
     * {@link DesktopPane}.
     * 
     * @param aListener
     *            The listener to send messages to. A <code>null</code> argument will cause a
     *            {@link NullPointerException} .
     */
    public void addInternalFrameListener(InternalFrameListener aListener) {
        assert (SwingUtilities.isEventDispatchThread());
        if (null == aListener)
            throw new NullPointerException("Received a null listener to addInternalFrameListener()!");
        listeners.add(aListener);
    }

    /**
     * Removes the given {@link InternalFrameListener} from this {@link DesktopPane}. No further messages will be sent.
     * No exception is thrown on a <code>null</code> argument or if the argument is not a listener of this
     * {@link DesktopPane}.
     * 
     * @param aListener
     *            The listener to remove.
     */
    public void removeInternalFrameListener(InternalFrameListener aListener) {
        listeners.remove(aListener);
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent aE) {
        assert (SwingUtilities.isEventDispatchThread());
        for (InternalFrameListener frameListener : listeners) {
            frameListener.internalFrameActivated(aE);
        }
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent aE) {
        assert (SwingUtilities.isEventDispatchThread());
        for (InternalFrameListener frameListener : listeners) {
            frameListener.internalFrameClosed(aE);
        }
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent aE) {
        assert (SwingUtilities.isEventDispatchThread());
        for (InternalFrameListener frameListener : listeners) {
            frameListener.internalFrameClosing(aE);
        }
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent aE) {
        assert (SwingUtilities.isEventDispatchThread());
        for (InternalFrameListener frameListener : listeners) {
            frameListener.internalFrameDeactivated(aE);
        }
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent aE) {
        assert (SwingUtilities.isEventDispatchThread());
        for (InternalFrameListener frameListener : listeners) {
            frameListener.internalFrameDeiconified(aE);
        }
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent aE) {
        assert (SwingUtilities.isEventDispatchThread());
        for (InternalFrameListener frameListener : listeners) {
            frameListener.internalFrameIconified(aE);
        }
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent aE) {
        assert (SwingUtilities.isEventDispatchThread());
        for (InternalFrameListener frameListener : listeners) {
            frameListener.internalFrameOpened(aE);
        }
    }
}
