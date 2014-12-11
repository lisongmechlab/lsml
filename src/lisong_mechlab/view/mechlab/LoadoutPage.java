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
package lisong_mechlab.view.mechlab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;

import lisong_mechlab.model.DynamicSlotDistributor;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.ModuleSlot;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.metrics.MaxSustainedDPS;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.message.MessageXBar;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.render.StyleManager;

/**
 * Draws the loadout editing page
 * 
 * @author Emily Björk
 */
public class LoadoutPage extends JPanel {
    private static final long      serialVersionUID = -3391845136603220435L;

    private static final int       ARM_OFFSET       = 60;
    private static final int       TORSO_OFFSET     = 20;
    private static final int       HEAD_OFFSET      = 0;
    private final LoadoutBase<?>   loadout;
    private final MessageXBar      xBar;
    private final OperationStack   loadoutOperationStack;
    private final LoadoutInfoPanel infoPanel;

    public LoadoutPage(LoadoutBase<?> aLoadout, OperationStack aOpStack, MessageXBar aXBar) {
        xBar = aXBar;
        loadout = aLoadout;
        loadoutOperationStack = aOpStack;
        
        setLayout(new BorderLayout());

        infoPanel = new LoadoutInfoPanel(loadout, loadoutOperationStack, xBar);

        JPanel mechview = createMechView(aLoadout, aXBar);
        add(mechview, BorderLayout.WEST);
        if (ProgramInit.lsml().preferences.uiPreferences.getCompactMode()) {
            JScrollPane scrollpane = new JScrollPane(infoPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            Dimension preferredSize = new Dimension();
            preferredSize.height = (int) (mechview.getPreferredSize().getHeight() + 1);
            preferredSize.width = (int) (infoPanel.getPreferredSize().getWidth()
                    + scrollpane.getVerticalScrollBar().getPreferredSize().getWidth() + 1);
            scrollpane.setPreferredSize(preferredSize);
            add(scrollpane, BorderLayout.EAST);
        }
        else {
            add(infoPanel, BorderLayout.EAST);
        }
        add(new StatusBar(loadout, aXBar), BorderLayout.SOUTH);
    }

    public LoadoutBase<?> getLoadout() {
        return loadout;
    }

    public OperationStack getOpStack() {
        return loadoutOperationStack;
    }
    
    MaxSustainedDPS getMaxSustainedDPS(){
        return infoPanel.getMaxSustainedDPSMetric();
    }

    private JPanel createComponentPadPanel(final int height, JComponent aChild) {
        final JPanel padPanel = new JPanel();
        padPanel.setLayout(new BoxLayout(padPanel, BoxLayout.LINE_AXIS));

        if (null != aChild) {
            JPanel content = new JPanel() {
                private static final long serialVersionUID = -7026792320508640323L;

                @Override
                public Dimension getMaximumSize() {
                    Dimension d = super.getMaximumSize();
                    d.height = height;
                    return d;
                }
            };
            content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
            content.add(aChild);
            content.add(Box.createVerticalGlue());
            padPanel.add(content);
        }
        padPanel.add(Box.createRigidArea(new Dimension(0, height)));
        return padPanel;
    }

    private JPanel createComponentPanel(JPanel aPadPanel, JPanel aContentPanel, JPanel aContentPanel2) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(aPadPanel);
        panel.add(aContentPanel);
        if (null != aContentPanel2) {
            panel.add(aContentPanel2);
        }
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createMechView(LoadoutBase<?> aLoadout, MessageXBar aXBar) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        Dimension padding = new Dimension(5, 0);

        panel.add(Box.createRigidArea(padding));

        DynamicSlotDistributor slotDistributor = new DynamicSlotDistributor(loadout);

        JCheckBox symmetricArmor;
        if (ProgramInit.lsml().preferences.uiPreferences.getCompactMode()) {
            symmetricArmor = new JCheckBox("Sym.armr", false);
        }
        else {
            symmetricArmor = new JCheckBox("Symmetric armor", false);
        }
        // symmetricArmor.setAlignmentX(LEFT_ALIGNMENT);

        // Right Arm
        {
            JPanel padPanel = createComponentPadPanel(ARM_OFFSET, symmetricArmor);
            final JPanel arm = new PartPanel(aLoadout, aLoadout.getComponent(Location.RightArm), aXBar, true,
                    slotDistributor, symmetricArmor, loadoutOperationStack);

            JPanel modulesPanel = new JPanel();
            modulesPanel.setLayout(new BoxLayout(modulesPanel, BoxLayout.PAGE_AXIS));
            for (ModuleSlot moduleSlot : ModuleSlot.values()) {
                JPanel modulePanel = new JPanel();
                modulePanel.setBorder(StyleManager.sectionBorder(moduleSlot.toString() + " Modules"));
                modulePanel.setLayout(new BoxLayout(modulePanel, BoxLayout.PAGE_AXIS));
                modulePanel.add(Box.createHorizontalGlue());
                // modulePanel.add(Box.createVerticalGlue());
                modulePanel.add(new PilotModuleList(xBar, loadoutOperationStack, loadout, moduleSlot));
                modulesPanel.add(modulePanel);
            }
            panel.add(createComponentPanel(padPanel, arm, modulesPanel));
        }

        if (!ProgramInit.lsml().preferences.uiPreferences.getCompactMode())
            panel.add(Box.createRigidArea(padding));

        // Right Torso + Leg
        {
            final JPanel torso = new PartPanel(aLoadout, aLoadout.getComponent(Location.RightTorso), aXBar, true,
                    slotDistributor, symmetricArmor, loadoutOperationStack);
            final JPanel leg = new PartPanel(aLoadout, aLoadout.getComponent(Location.RightLeg), aXBar, false,
                    slotDistributor, symmetricArmor, loadoutOperationStack);
            panel.add(createComponentPanel(createComponentPadPanel(TORSO_OFFSET, null), torso, leg));
        }

        if (!ProgramInit.lsml().preferences.uiPreferences.getCompactMode())
            panel.add(Box.createRigidArea(padding));

        // Center Torso + Head
        {
            final JPanel head = new PartPanel(aLoadout, aLoadout.getComponent(Location.Head), aXBar, true,
                    slotDistributor, symmetricArmor, loadoutOperationStack);
            final JPanel torso = new PartPanel(aLoadout, aLoadout.getComponent(Location.CenterTorso), aXBar, true,
                    slotDistributor, symmetricArmor, loadoutOperationStack);
            panel.add(createComponentPanel(createComponentPadPanel(HEAD_OFFSET, null), head, torso));
        }

        if (!ProgramInit.lsml().preferences.uiPreferences.getCompactMode())
            panel.add(Box.createRigidArea(padding));

        // Left Torso + Leg
        {
            final JPanel torso = new PartPanel(aLoadout, aLoadout.getComponent(Location.LeftTorso), aXBar, true,
                    slotDistributor, symmetricArmor, loadoutOperationStack);
            final JPanel leg = new PartPanel(aLoadout, aLoadout.getComponent(Location.LeftLeg), aXBar, false,
                    slotDistributor, symmetricArmor, loadoutOperationStack);
            panel.add(createComponentPanel(createComponentPadPanel(TORSO_OFFSET, null), torso, leg));
        }

        if (!ProgramInit.lsml().preferences.uiPreferences.getCompactMode())
            panel.add(Box.createRigidArea(padding));

        // Left Arm
        {
            final JPanel arm = new PartPanel(aLoadout, aLoadout.getComponent(Location.LeftArm), aXBar, true,
                    slotDistributor, symmetricArmor, loadoutOperationStack);

            final JLabel quirksummary = new JLabel("Quirk summary");
            quirksummary.addMouseListener(new MouseAdapter() {
                JWindow window = null;

                @Override
                public void mouseEntered(MouseEvent aE) {
                    window = new JWindow(ProgramInit.lsml());
                    JLabel text = new JLabel(loadout.getQuirkHtmlSummary());
                    JPanel textPanel = new JPanel();

                    textPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                    textPanel.add(text);
                    textPanel.setBackground(Color.WHITE);
                    window.add(textPanel);
                    window.pack();
                    window.setLocation(aE.getLocationOnScreen());
                    window.setVisible(true);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (window != null) {
                        window.dispose();
                        window = null;
                    }
                }
            });
            quirksummary.setForeground(Color.BLUE);
            Font font = quirksummary.getFont();
            Map<TextAttribute, Object> attributes = new HashMap<>();
            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            quirksummary.setFont(font.deriveFont(attributes));

            panel.add(createComponentPanel(createComponentPadPanel(ARM_OFFSET, quirksummary), arm, null));
        }
        return panel;
    }
}
