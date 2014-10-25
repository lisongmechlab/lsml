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

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.EquipResult;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.view.mechlab.ItemLabel;
import lisong_mechlab.view.mechlab.PartList;
import lisong_mechlab.view.mechlab.equipment.GarageTree;
import lisong_mechlab.view.render.ItemRenderer;

public class ItemTransferHandler extends TransferHandler {
    private static final long              serialVersionUID = -8109855943478269304L;
    private static ConfiguredComponentBase sourcePart       = null;

    @Override
    public int getSourceActions(JComponent aComponent) {
        return TransferHandler.COPY_OR_MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent aComponent) {
        assert (SwingUtilities.isEventDispatchThread());
        if (aComponent instanceof PartList) {
            PartList partList = (PartList) aComponent;

            Item sourceItems = partList.removeSelected(ProgramInit.lsml().xBar);
            if (sourceItems == null)
                return null;

            sourcePart = partList.getPart();
            setPreview(sourceItems);
            return new StringSelection(sourceItems.getName());
        }
        else if (aComponent instanceof GarageTree) {
            sourcePart = null;
            GarageTree equipmentPane = (GarageTree) aComponent;

            if (equipmentPane.getSelectionPath() == null)
                return null;

            Object dragged = equipmentPane.getSelectionPath().getLastPathComponent();
            Item item = null;
            if (dragged instanceof String) {
                item = ItemDB.lookup((String) dragged);
            }
            else if (dragged instanceof Item) {
                item = (Item) dragged;
            }
            else {
                return null;
            }
            setPreview(item);
            return new StringSelection(item.getName());
        }
        else if (aComponent instanceof ItemLabel) {
            Item item = ((ItemLabel) aComponent).getItem();
            setPreview(item);
            return new StringSelection(item.getName());
        }
        return null;
    }

    private void setPreview(Item anItem) {
        Image preview = ItemRenderer.render(anItem);
        setDragImage(preview);
        Point mouse = new Point(getDragImage().getWidth(null) / 2, ItemRenderer.getItemHeight() / 2);
        setDragImageOffset(mouse);
    }

    @Override
    protected void exportDone(JComponent c, Transferable t, int action) {
        // NO-OP
        // The items are removed during the export, otherwise the drop
        // may fail because of loadout tonnage limits etc.
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport aInfo) {
        Component uiComponent = aInfo.getComponent();
        if (uiComponent instanceof PartList) {
            List<Item> items = parseItems(aInfo);
            if (null == items)
                return false;

            LoadoutBase<?> loadout = ((PartList) uiComponent).getLoadout();
            ConfiguredComponentBase component = ((PartList) uiComponent).getPart();
            for (Item item : items) {
                if (EquipResult.SUCCESS != loadout.canEquip(item) || EquipResult.SUCCESS != component.canAddItem(item))
                    return false;
            }
            return true;
        }
        return true;
    }

    /**
     * Perform the actual import. This only supports drag and drop.
     */
    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }

        if (null != sourcePart && info.getDropAction() != COPY) {
            sourcePart = null;
        }

        Component component = info.getComponent();
        if (component instanceof PartList) {
            PartList model = (PartList) component;
            int dropIndex = ((JList.DropLocation) info.getDropLocation()).getIndex();
            try {
                boolean first = true;
                List<Item> items = parseItems(info);
                if (null == items)
                    return false;
                for (Item item : items) {
                    model.putElement(item, dropIndex, first);
                    dropIndex++;
                    first = false;
                }
            }
            catch (Exception e) {
                return false;
            }
        }
        // Allow the user to drop the item to get it removed
        return true;
    }

    private List<Item> parseItems(TransferHandler.TransferSupport aInfo) {
        if (!aInfo.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return null;
        }
        List<Item> items = new ArrayList<>();
        try {
            for (String itemName : ((String) aInfo.getTransferable().getTransferData(DataFlavor.stringFlavor))
                    .split("\n")) {
                items.add(ItemDB.lookup(itemName));
            }
        }
        catch (Exception e) {
            return null;
        }
        return items;
    }
}
