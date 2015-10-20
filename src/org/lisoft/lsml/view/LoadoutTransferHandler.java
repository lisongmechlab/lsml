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
package org.lisoft.lsml.view;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.view.mechlab.dropshipframe.DropShipFrame;
import org.lisoft.lsml.view.mechlab.garagetree.GarageTree;

/**
 * This class handles dragging and transfer of {@link LoadoutBase}s.
 * 
 * @author Li Song
 */
public class LoadoutTransferHandler extends TransferHandler {
    public static class LoadoutTransferable implements Transferable {
        public static final DataFlavor LOADOUT_DATA_FLAVOR = new DataFlavor(LoadoutBase.class, "lsml/LoadoutBase");
        private LoadoutBase<?>         loadout;

        public LoadoutTransferable(LoadoutBase<?> aLoadout) {
            loadout = aLoadout;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { LOADOUT_DATA_FLAVOR };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(LOADOUT_DATA_FLAVOR);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return loadout;
        }
    }

    @Override
    protected void exportDone(JComponent c, Transferable t, int action) {
        // The operation is finalized by the importData function()
    }

    @Override
    protected Transferable createTransferable(JComponent aComponent) {
        if (aComponent instanceof GarageTree) {
            GarageTree tree = (GarageTree) aComponent;
            Point mouse = tree.getMousePosition();
            TreePath mousePath = tree.getPathForLocation(mouse.x, mouse.y);
            Object selection = mousePath.getLastPathComponent();
            if (selection instanceof LoadoutBase<?>) {
                LoadoutBase<?> loadout = (LoadoutBase<?>) selection;
                return new LoadoutTransferable(loadout);
            }
            else if (selection instanceof ChassisBase) {
                LoadoutBase<?> loadout = DefaultLoadoutFactory.instance.produceEmpty((ChassisBase) selection);
                return new LoadoutTransferable(loadout);
            }
        }
        return null;
    }

    @Override
    public int getSourceActions(JComponent aComponent) {
        if (aComponent instanceof GarageTree) {
            GarageTree tree = (GarageTree) aComponent;
            Point mouse = tree.getMousePosition();
            TreePath mousePath = tree.getPathForLocation(mouse.x, mouse.y);
            Object selected = mousePath.getLastPathComponent();
            if (selected instanceof ChassisBase) {
                return TransferHandler.COPY;
            }
            else if (selected instanceof LoadoutBase) {
                return TransferHandler.COPY_OR_MOVE;
            }
        }

        return TransferHandler.NONE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop())
            return false;

        if (!(info.getComponent() instanceof DropShipFrame.LoadoutDisplay))
            return false;

        try {
            DropShipFrame.LoadoutDisplay target = (DropShipFrame.LoadoutDisplay) info.getComponent();
            LoadoutBase<?> loadout = (LoadoutBase<?>) info.getTransferable()
                    .getTransferData(LoadoutTransferable.LOADOUT_DATA_FLAVOR);

            LSML lsml = ProgramInit.lsml();
            final Command cmd;
            if (info.getDropAction() == MOVE) {
                cmd = target.makeMoveCommand(lsml.xBar, lsml.getGarage(), loadout);
            }
            else {
                cmd = target.makeCopyCommand(lsml.xBar, loadout);
            }
            ProgramInit.lsml().garageCmdStack.pushAndApply(cmd);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport aInfo) {
        if (!aInfo.isDataFlavorSupported(LoadoutTransferable.LOADOUT_DATA_FLAVOR))
            return false;

        Component component = aInfo.getComponent();
        while (component != null && !(component instanceof DropShipFrame)) {
            component = component.getParent();
        }

        if (null == component) {
            return false;
        }

        DropShipFrame dsf = (DropShipFrame) component;
        LoadoutBase<?> loadout;
        try {
            loadout = (LoadoutBase<?>) aInfo.getTransferable().getTransferData(LoadoutTransferable.LOADOUT_DATA_FLAVOR);
        }
        catch (UnsupportedFlavorException | IOException e) {
            return false;
        }
        if (!dsf.getDropShip().isCompatible(loadout)) {
            return false;
        }
        return true;
    }
}
