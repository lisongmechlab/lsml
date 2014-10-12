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
package lisong_mechlab.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import lisong_mechlab.model.item.PilotModule;
import lisong_mechlab.model.item.PilotModuleDB;
import lisong_mechlab.view.mechlab.PilotModuleList;
import lisong_mechlab.view.mechlab.equipment.ModuleSeletionList;

/**
 * This class handles dragging and transfer of {@link PilotModule}s.
 * 
 * @author Emily Björk
 */
public class ModuleTransferHandler extends TransferHandler {
    private static final long serialVersionUID = -3237485137173997072L;

    @Override
    protected void exportDone(JComponent c, Transferable t, int action) {
        // No-Op: Modules are removed during export, otherwise drop on self may fail.
    }

    @Override
    protected Transferable createTransferable(JComponent aComponent) {
        final PilotModule module;
        if (aComponent instanceof ModuleSeletionList) {
            ModuleSeletionList modulePanel = (ModuleSeletionList) aComponent;
            module = modulePanel.getSelectedValue();
        }
        else if (aComponent instanceof PilotModuleList) {
            PilotModuleList pml = (PilotModuleList) aComponent;
            module = pml.takeCurrent();
            if (module == null)
                return null;
        }
        else {
            return null;
        }

        return new StringSelection(Integer.toString(module.getMwoId()));
    }

    @Override
    public int getSourceActions(JComponent aComponent) {
        return TransferHandler.COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop())
            return false;

        if (!(info.getComponent() instanceof PilotModuleList))
            return false;

        try {
            PilotModule module = parseModule(info);
            PilotModuleList pml = (PilotModuleList) info.getComponent();
            pml.putElement(module);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport aInfo) {
        if (!aInfo.isDataFlavorSupported(DataFlavor.stringFlavor))
            return false;

        if (!(aInfo.getComponent() instanceof PilotModuleList))
            return false;

        try {
            PilotModule module = parseModule(aInfo);
            PilotModuleList pml = (PilotModuleList) aInfo.getComponent();
            return pml.getLoadout().canAddModule(module);
        }
        catch (Exception exception) {
            return false;
        }
    }

    private static PilotModule parseModule(TransferHandler.TransferSupport aInfo) throws Exception {
        int moduleId = Integer.parseInt((String) aInfo.getTransferable().getTransferData(DataFlavor.stringFlavor));
        return PilotModuleDB.lookup(moduleId);
    }
}
