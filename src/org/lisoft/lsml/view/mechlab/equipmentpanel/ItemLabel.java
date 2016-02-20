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
package org.lisoft.lsml.view.mechlab.equipmentpanel;

import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;

import org.lisoft.lsml.command.CmdAutoAddItem;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.metrics.TopSpeed;
import org.lisoft.lsml.view.ItemTransferHandler;
import org.lisoft.lsml.view.ProgramInit;
import org.lisoft.lsml.view.mechlab.loadoutframe.LoadoutFrame;

/**
 * This class implements a JLabel to render an item that can be dragged onto a loadout.
 * 
 * @author Li Song
 */
public class ItemLabel extends JLabel {
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###");
    private final String               baseText;
    private final String               defaultText;
    private final Item                 item;
    private final int                  gradientOffset = 60;
    private final GradientPaint        gradientPaint;
    private boolean                    smartPlace     = false;

    private static class ProgressDialog extends JDialog {
        private static final long serialVersionUID = -6084430266229568009L;
        SwingWorker<Void, Void>   task;

        public ProgressDialog() {
            super(ProgramInit.lsml(), "SmartPlace in progress...", ModalityType.APPLICATION_MODAL);
            setLocationRelativeTo(ProgramInit.lsml());

            JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            panel.add(new JLabel("That's a tricky proposition cap'n but I'll see what I can do..."));
            panel.add(progressBar);
            panel.add(new JButton(new AbstractAction("Abort") {
                private static final long serialVersionUID = 2384981612883023314L;

                @Override
                public void actionPerformed(ActionEvent aE) {
                    if (task != null)
                        task.cancel(true);
                }
            }));
            setContentPane(panel);
            setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            pack();
        }

        void setTask(SwingWorker<Void, Void> aTask) {
            task = aTask;
        }
    }

    private static class AutoPlaceTask extends SwingWorker<Void, Void> {
        private CmdAutoAddItem operation;
        private JDialog        dialog;
        private LoadoutFrame   loadoutFrame;
        private MessageXBar    xBar;
        private Item           itemToPlace;

        public AutoPlaceTask(JDialog aDialog, LoadoutFrame aLoadoutFrame, MessageXBar anXBar, Item aItem) {
            dialog = aDialog;
            loadoutFrame = aLoadoutFrame;
            xBar = anXBar;
            itemToPlace = aItem;
        }

        @Override
        public Void doInBackground() {
            try {
                operation = new CmdAutoAddItem(loadoutFrame.getLoadout(), xBar, itemToPlace);
                operation.prepareCommandAheadOfTime();
            }
            catch (Throwable e) { // Yeah anything thrown is a failure.
                operation = null;
            }
            return null;
        }

        @Override
        public void done() {
            // In EDT
            if (!isCancelled()) {
                if (operation == null) {
                    JOptionPane.showMessageDialog(dialog, "No can do cap'n!", "Not possible", JOptionPane.OK_OPTION);
                }
                else {
                    try {
                        loadoutFrame.getOpStack().pushAndApply(operation);
                    }
                    catch (Exception e) {

                        JOptionPane.showMessageDialog(null, "Failed to add item to loadout.\nError: " + e.getMessage());
                    }
                }
            }
            dialog.dispose();
        }
    }

    public ItemLabel(Item aItem, final EquipmentPanel aEquipmentPanel, final ItemInfoPanel aInfoPanel,
            final MessageXBar anXBar) {
        item = aItem;

        StyleManager.styleItem(this, item);
        gradientPaint = new GradientPaint(gradientOffset, 0, getBackground(), gradientOffset + 1, 1,
                StyleManager.getBgColorInvalid());
        setToolTipText("<html>" + item.getName() + "<p>" + item.getDescription() + "</html>");

        setTransferHandler(new ItemTransferHandler());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent aEvent) {
                final LoadoutFrame frame = ProgramInit.lsml().mechLabPane.getActiveLoadoutFrame();
                final Loadout<?> loadout = aEquipmentPanel.getCurrentLoadout();

                Component component = aEvent.getComponent();
                if (component instanceof ItemLabel) {
                    if (null != loadout) {
                        aInfoPanel.showItem(item, loadout.getModifiers());
                    }
                    else {
                        aInfoPanel.showItem(item, null);
                    }
                }

                ItemLabel button = (ItemLabel) aEvent.getSource();
                ItemTransferHandler handle = (ItemTransferHandler) button.getTransferHandler();
                handle.exportAsDrag(button, aEvent, TransferHandler.COPY);

                if (SwingUtilities.isLeftMouseButton(aEvent) && aEvent.getClickCount() >= 2) {
                    if (null != loadout) {
                        if (smartPlace) {
                            if (!ProgramInit.lsml().preferences.uiPreferences.getUseSmartPlace()) {
                                Object[] choices = { "Use SmartPlace", "Disable SmartPlace" };
                                Object defaultChoice = choices[0];
                                int choice = JOptionPane.showOptionDialog(ProgramInit.lsml(),
                                        "SmartPlace can re-arrange items on your loadout to make the item you're trying to equip fit.\n"
                                                + "No items will be removed, only moved.\n"
                                                + "It is not guaranteed that there exists an arrangement of items that allows the item to be added\n"
                                                + "in which case SmartPlace will try all possible combinations which might take time.\n"
                                                + "If smart place is taking too long you can safely abort it without changes to your loadout.\n\n"
                                                + "You can see if SmartPlace will be used on the item in the equipment pane if it is semi grayed out.\n",
                                        "Enable SmartPlace?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                        null, choices, defaultChoice);
                                if (choice == 0) {
                                    ProgramInit.lsml().preferences.uiPreferences.setUseSmartPlace(true);
                                }
                                else {
                                    return;
                                }
                            }

                            final ProgressDialog dialog = new ProgressDialog();
                            final AutoPlaceTask task = new AutoPlaceTask(dialog, frame, anXBar, item);
                            task.execute();
                            dialog.setTask(task);
                            dialog.addWindowListener(new WindowAdapter() {
                                @Override
                                public void windowClosed(WindowEvent e) {
                                    task.cancel(true);
                                }
                            });

                            try {
                                task.get(500, TimeUnit.MILLISECONDS);
                            }
                            catch (InterruptedException e) {
                                return; // Cancelled
                            }
                            catch (ExecutionException e) {
                                throw new RuntimeException(e); // Corblimey
                            }
                            catch (TimeoutException e) {
                                dialog.setVisible(true); // Show progress meter if it's taking time and resume EDT
                            }
                        }
                        else if (EquipResult.SUCCESS == loadout.canEquipDirectly(item)) {
                            try {
                                frame.getOpStack().pushAndApply(new CmdAutoAddItem(loadout, anXBar, item));
                            }
                            catch (Exception e) {
                                JOptionPane.showMessageDialog(null,
                                        "Failed to add item to loadout.\nError: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        });

        StringBuilder builder = new StringBuilder();
        builder.append("<html>");
        builder.append(item.getShortName());
        builder.append("<br/><span style=\"font-size:x-small;\">");
        builder.append("Tons: ").append(item.getMass()).append("<br/>Slots: ").append(item.getSlots());
        baseText = builder.toString();
        builder.append("</span></html>");
        defaultText = builder.toString();
        setText(defaultText);
        updateVisibility(null);
        updateDisplay(null);
    }

    private void updateText(Loadout<?> aLoadout) {
        if (aLoadout != null && item instanceof Engine) {
            StringBuilder builder = new StringBuilder(baseText);
            Engine engine = (Engine) item;
            double speed = TopSpeed.calculate(engine.getRating(), aLoadout.getMovementProfile(),
                    aLoadout.getChassis().getMassMax(), aLoadout.getModifiers());
            builder.append("<br/>" + DECIMAL_FORMAT.format(speed) + "kph");
            builder.append("</span></html>");
            setText(builder.toString());
        }
    }

    public Item getItem() {
        return item;
    }

    @Override
    protected void paintComponent(Graphics aGraphics) {
        if (!isOpaque() || !smartPlace) {
            super.paintComponent(aGraphics);
            return;
        }

        Graphics2D g2d = (Graphics2D) aGraphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setPaint(gradientPaint);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        setOpaque(false);
        super.paintComponent(aGraphics);
        setOpaque(true);
    }

    public void updateVisibility(Loadout<?> aLoadout) {
        if (aLoadout == null) {
            setVisible(true);
            return;
        }

        if (!aLoadout.getChassis().isAllowed(item) || !item.isCompatible(aLoadout.getUpgrades())) {
            setVisible(false);
            return;
        }

        if (item instanceof Ammunition) {
            Ammunition ammunition = (Ammunition) item;
            if (aLoadout.getHardpointsCount(ammunition.getWeaponHardpointType()) < 1) {
                setVisible(false);
            }
            else {
                boolean isUsable = false;
                for (AmmoWeapon ammoWeapon : aLoadout.items(AmmoWeapon.class)) {
                    if (ammoWeapon.isCompatibleAmmo(ammunition)) {
                        isUsable = true;
                        break;
                    }
                }
                setVisible(isUsable);
            }
        }
        else
            setVisible(true);

    }

    public void updateDisplay(Loadout<?> aLoadout) {
        if (isVisible()) {
            boolean prevSmartPlace = smartPlace;
            smartPlace = false;

            updateText(aLoadout);

            if (aLoadout != null && EquipResult.SUCCESS != aLoadout.canEquipDirectly(item)) {
                if (aLoadout.getCandidateLocationsForItem(item).isEmpty()) {
                    StyleManager.colourInvalid(this);
                }
                else {
                    StyleManager.styleItem(this, item);
                    smartPlace = true;
                }
            }
            else {
                StyleManager.styleItem(this, item);
            }

            if (prevSmartPlace != smartPlace)
                repaint();
        }
    }
}
