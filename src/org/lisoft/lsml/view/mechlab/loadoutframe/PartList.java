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
package org.lisoft.lsml.view.mechlab.loadoutframe;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.lisoft.lsml.command.CmdAddItem;
import org.lisoft.lsml.command.CmdRemoveItem;
import org.lisoft.lsml.messages.ArmorMessage;
import org.lisoft.lsml.messages.Message;
import org.lisoft.lsml.messages.MessageReceiver;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.messages.UpgradesMessage;
import org.lisoft.lsml.model.DynamicSlotDistributor;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.EquipResult;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.metrics.CriticalStrikeProbability;
import org.lisoft.lsml.model.metrics.ItemEffectiveHP;
import org.lisoft.lsml.model.metrics.helpers.ComponentDestructionSimulator;
import org.lisoft.lsml.util.CommandStack;
import org.lisoft.lsml.view.ItemTransferHandler;
import org.lisoft.lsml.view.ProgramInit;
import org.lisoft.lsml.view.action.AddItem;
import org.lisoft.lsml.view.action.ChangeEngine;
import org.lisoft.lsml.view.render.ComponentRenderer;
import org.lisoft.lsml.view.render.ComponentRenderer.RenderState;
import org.lisoft.lsml.view.render.StyleManager;

public class PartList extends JList<Item> {
    private static final long                   serialVersionUID = 5995694414450060827L;
    private final ConfiguredComponentBase       component;
    private final DynamicSlotDistributor        slotDistributor;
    private CommandStack                        cmdStack;

    private final DecimalFormat                 df               = new DecimalFormat("###.#");
    private final DecimalFormat                 df2              = new DecimalFormat("###.##");
    private final ItemEffectiveHP               effectiveHP;
    private final CriticalStrikeProbability     criticalStrikeProbability;
    private final LoadoutBase<?>                loadout;

    private final ComponentRenderer             componentRenderer;
    private final MessageXBar                   xBar;
    private final ComponentDestructionSimulator cds;
    private final boolean                       isCompact        = ProgramInit.lsml().preferences.uiPreferences
            .getCompactMode();

    private class Renderer extends JLabel implements ListCellRenderer<Object> {
        private static final long serialVersionUID = -8157859670319431469L;

        void setTooltipForItem(Item aItem) {
            if (aItem == null || aItem instanceof Internal) {
                setToolTipText(null);
                return;
            }

            StringBuilder sb = new StringBuilder();

            sb.append("<html>");
            sb.append("<b>");
            sb.append(aItem.getName());
            if (!aItem.getName().equals(aItem.getShortName())) {
                sb.append(" (").append(aItem.getShortName()).append(")");
            }
            sb.append("</b>");

            sb.append("<table width=\"100%\" cellspacing=\"1\" border=\"0\" cellpadding=\"0\">");
            sb.append("<tr><td width=\"30%\">Critical hit:</td><td> ")
                    .append(df.format(100 * criticalStrikeProbability.calculate(aItem))).append("%</td></tr>");
            sb.append("<tr><td>Destroyed:</td><td> ").append(df2.format(100 * cds.getProbabilityOfDestruction(aItem)))
                    .append("%</td></tr>");
            sb.append("<tr><td>HP:</td><td> ").append(aItem.getHealth()).append("</td></tr>");
            sb.append("<tr><td>SIE-HP:</td><td> ").append(df.format(effectiveHP.calculate(aItem))).append("</td></tr>");
            sb.append("</table>");
            sb.append("<br/>");

            sb.append("<div style='width:300px'>").append("<p>")
                    .append("<b>Critical hit</b> is the probability that a shot on this component's internal structure will deal damage to this item. "
                            + "When other items break, the crit % increases as it's more likely this item will be hit. "
                            + "If the weapon dealing damage does equal to or more damage than the HP of this item, it will break in one shot.")
                    .append("</p><p>")
                    .append("<b>Destroyed</b> is the probability that this item will be destroyed before the component is destroyed. "
                            + "A high value indicates that the item is poorly buffered and can be expected to be lost soon after the internal structure is exposed. "
                            + "A low value means that the item is likely to survive until the component is completely destroyed.")
                    .append("</p><p>")
                    .append("<b>SIE-HP</b> is the Statistical, Infinitesmal Effective-HP of this component. Under the assumption that damage is ")
                    .append("applied in small chunks (lasers) this is how much damage the component can take before this item breaks on average. "
                            + "For MG, LB 10-X AC and flamers this is lower as they have higher chance to crit and higher crit multiplier.")
                    .append("</p>").append("</div>");

            sb.append("</html>");
            setToolTipText(sb.toString());
        }

        @Override
        public Component getListCellRendererComponent(JList<?> aList, Object aValue, int aIndex, boolean aIsSelected,
                boolean aHasFocus) {
            JList.DropLocation dropLocation = aList.getDropLocation();
            if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == aIndex) {
                setCursor(null);
            }

            RenderState state = componentRenderer.getRenderState(aIndex);
            Item item = state.getItem();

            setBorder(BorderFactory.createEmptyBorder());

            switch (state.getRenderType()) {
                case Empty: {
                    if (isDynArmor(aIndex)) {
                        StyleManager.styleDynamicEntry(this);
                        if (loadout instanceof LoadoutStandard)
                            setText(Model.DYN_ARMOR);
                        else
                            setText(Model.FIX_ARMOR);
                    }
                    else if (isDynStructure(aIndex)) {
                        StyleManager.styleDynamicEntry(this);
                        if (loadout instanceof LoadoutStandard)
                            setText(Model.DYN_STRUCT);

                        else
                            setText(Model.FIX_STRUCT);
                    }
                    else {
                        StyleManager.styleItem(this);
                        setText(Model.EMPTY);
                    }
                    setToolTipText(null);
                    break;
                }
                case Item: {
                    setTooltipForItem(item);
                    setText(isCompact ? item.getShortName() : item.getName());

                    if (item.getNumCriticalSlots() == 1) {
                        StyleManager.styleItem(this, item);
                    }
                    else {
                        StyleManager.styleItemTop(this, item);
                    }
                    break;
                }
                case LastSlot: {
                    setText(Model.MULTISLOT);
                    setTooltipForItem(item);
                    StyleManager.styleItemBottom(this, item);
                    break;
                }
                case MultiSlot: {
                    setText(Model.MULTISLOT);
                    setTooltipForItem(item);
                    StyleManager.styleItemMiddle(this, item);
                    break;
                }
                case EngineHeatSink: {
                    setTooltipForItem(item);
                    setText(isCompact ? Model.HEATSINKS_COMPACT_STRING
                            : Model.HEATSINKS_STRING + component.getEngineHeatSinks() + "/"
                                    + component.getEngineHeatSinksMax());
                    StyleManager.styleItemBottom(this, item);
                    break;
                }
                default:
                    throw new IllegalStateException("Unknown render state: " + state.getRenderType() + "!");
            }

            if (state.isFixed()) {
                Color bg = getBackground();
                float[] hsb = Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), null);
                hsb[1] *= 0.2;
                bg = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
                setBackground(bg);
            }

            return this;
        }

        private boolean isDynStructure(int aIndex) {
            int struct = slotDistributor.getDynamicStructureSlots(component);
            int slots = componentRenderer.getFirstEmpty();
            int armor = slotDistributor.getDynamicArmorSlots(component);
            return aIndex >= slots + armor && aIndex < slots + armor + struct;
        }

        private boolean isDynArmor(int aIndex) {
            int armor = slotDistributor.getDynamicArmorSlots(component);
            int slots = componentRenderer.getFirstEmpty();
            return aIndex >= slots && aIndex < slots + armor;
        }
    }

    private class Model extends AbstractListModel<Item> implements MessageReceiver {
        private static final String HEATSINKS_STRING         = "HEAT SINKS: ";
        private static final String HEATSINKS_COMPACT_STRING = "HS: ";
        private static final String EMPTY                    = "EMPTY";
        private static final String MULTISLOT                = "";
        private static final String DYN_ARMOR                = "DYNAMIC ARMOR";
        private static final String DYN_STRUCT               = "DYNAMIC STRUCTURE";
        private static final String FIX_ARMOR                = "FIXED ARMOR";
        private static final String FIX_STRUCT               = "FIXED STRUCTURE";
        private static final long   serialVersionUID         = 2438473891359444131L;

        Model(MessageXBar aXBar) {
            aXBar.attach(this);
        }

        @Override
        public Item getElementAt(int aIndex) {
            return componentRenderer.getRenderState(aIndex).getItem();
        }

        @Override
        public int getSize() {
            return componentRenderer.getVisibleCount();
        }

        @Override
        public void receive(Message aMsg) {
            if (!aMsg.isForMe(loadout)) {
                return;
            }

            // Only update on item changes or upgrades
            if (aMsg instanceof ArmorMessage || aMsg instanceof UpgradesMessage) {
                fireContentsChanged(this, 0, component.getInternalComponent().getSlots());
            }
        }
    }

    PartList(CommandStack aStack, final LoadoutBase<?> aLoadout, final ConfiguredComponentBase aComponent,
            final MessageXBar aXBar, DynamicSlotDistributor aSlotDistributor) {
        slotDistributor = aSlotDistributor;
        cmdStack = aStack;
        component = aComponent;
        loadout = aLoadout;
        xBar = aXBar;
        componentRenderer = new ComponentRenderer(aXBar, component, isCompact);
        effectiveHP = new ItemEffectiveHP(component);
        cds = new ComponentDestructionSimulator(component, aXBar);
        cds.simulate();
        criticalStrikeProbability = new CriticalStrikeProbability(component);
        setModel(new Model(aXBar));
        setDragEnabled(true);
        setDropMode(DropMode.ON);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setTransferHandler(new ItemTransferHandler());
        setCellRenderer(new Renderer());

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                clearSelection();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent aArg0) {
                if (aArg0.getKeyCode() == KeyEvent.VK_DELETE) {
                    try {
                        removeSelected(aXBar);
                    }
                    catch (Exception e) {
                        JOptionPane.showMessageDialog(null, e);
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
                    try {
                        removeSelected(aXBar);
                    }
                    catch (Exception e1) {
                        JOptionPane.showMessageDialog(null, e);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent aE) {
                if (aE.isPopupTrigger()) {
                    doPop(aE);
                }
                else {
                    super.mousePressed(aE);
                }
            }

            @Override
            public void mouseReleased(MouseEvent aE) {
                if (aE.isPopupTrigger()) {
                    doPop(aE);
                }
                else {
                    super.mouseReleased(aE);
                }
            }

            private void doPop(MouseEvent aE) {
                RenderState state = componentRenderer.getRenderState(locationToIndex(aE.getPoint()));
                final Item item = state.getItem();
                if (item == null || state.isFixed())
                    return;

                final JPopupMenu menu = new JPopupMenu();

                menu.add(new JMenuItem(new AbstractAction("Remove " + item.getName()) {
                    @Override
                    public void actionPerformed(ActionEvent aEvent) {
                        try {
                            cmdStack.pushAndApply(new CmdRemoveItem(xBar, loadout, component, item));
                        }
                        catch (Exception e) {
                            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                        }
                    }
                }));

                if (item instanceof Engine && loadout instanceof LoadoutStandard) {
                    LoadoutStandard loadoutStandard = (LoadoutStandard) loadout;
                    menu.addSeparator();
                    final Engine engine = (Engine) item;
                    menu.add(new JMenuItem(new ChangeEngine(xBar, "Rating +5", cmdStack,
                            ItemDB.getEngine(engine.getRating() + 5, engine.getType(), engine.getFaction()),
                            loadoutStandard)));
                    menu.add(new JMenuItem(new ChangeEngine(xBar, "Rating -5", cmdStack,
                            ItemDB.getEngine(engine.getRating() - 5, engine.getType(), engine.getFaction()),
                            loadoutStandard)));
                    menu.add(new JMenuItem(new ChangeEngine(xBar, "Change to " + engine.getType().otherType(), cmdStack,
                            ItemDB.getEngine(engine.getRating(), engine.getType().otherType(), engine.getFaction()),
                            loadoutStandard)));
                    menu.addSeparator();

                    final HeatSink hs = loadout.getUpgrades().getHeatSink().getHeatSinkType();
                    JMenuItem addHeatSink = new JMenuItem(new AddItem("Add Heat Sink", cmdStack, xBar, loadout,
                            loadout.getComponent(Location.CenterTorso), hs));

                    menu.add(addHeatSink);

                    menu.add(new JMenuItem(new AbstractAction("Remove Heat Sink") {
                        @Override
                        public boolean isEnabled() {
                            return component.getItemsEquipped().contains(hs);
                        }

                        @Override
                        public void actionPerformed(ActionEvent aEvent) {
                            try {
                                cmdStack.pushAndApply(new CmdRemoveItem(xBar, loadout, component, hs));
                            }
                            catch (Exception e) {
                                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
                                        e);
                            }
                        }
                    }));
                }
                else if (item instanceof AmmoWeapon) {
                    AmmoWeapon ammoWeapon = (AmmoWeapon) item;
                    final Ammunition ammo = (Ammunition) ItemDB.lookup(ammoWeapon.getAmmoType());
                    final Ammunition ammoHalf = (Ammunition) ItemDB.lookup(ammoWeapon.getAmmoType() + "half");

                    menu.addSeparator();

                    menu.add(new JMenuItem(new AddItem("Add 1 ton ammo", cmdStack, xBar, loadout, ammo)));
                    menu.add(new JMenuItem(new AddItem("Add ½ ton ammo", cmdStack, xBar, loadout, ammoHalf)));
                    menu.add(new JMenuItem(new AbstractAction("Remove all ammo") {

                        @Override
                        public void actionPerformed(ActionEvent aEvent) {
                            for (ConfiguredComponentBase confComp : loadout.getComponents()) {
                                try {
                                    while (confComp.getItemsEquipped().contains(ammo)) {
                                        cmdStack.pushAndApply(new CmdRemoveItem(xBar, loadout, confComp, ammo));
                                    }
                                    while (confComp.getItemsEquipped().contains(ammoHalf)) {
                                        cmdStack.pushAndApply(new CmdRemoveItem(xBar, loadout, confComp, ammoHalf));
                                    }
                                }
                                catch (Exception e) {
                                    Thread.getDefaultUncaughtExceptionHandler()
                                            .uncaughtException(Thread.currentThread(), e);
                                }
                            }
                        }
                    }));
                }

                menu.show(aE.getComponent(), aE.getX(), aE.getY());
            }
        });
    }

    public Item removeSelected(MessageXBar aXBar) throws Exception {
        RenderState state = componentRenderer.getRenderState(getSelectedIndex());
        Item item = state.getItem();
        if (component.canRemoveItem(state.getItem())) {
            cmdStack.pushAndApply(new CmdRemoveItem(aXBar, loadout, component, state.getItem()));
            return item;
        }
        return null;
    }

    public ConfiguredComponentBase getPart() {
        return component;
    }

    public LoadoutBase<?> getLoadout() {
        return loadout;
    }

    public void putElement(Item aItem, int aDropIndex, boolean aShouldReplace) throws Exception {
        RenderState state = componentRenderer.getRenderState(aDropIndex);

        switch (state.getRenderType()) {
            case EngineHeatSink: {
                if (aItem instanceof HeatSink && EquipResult.SUCCESS == loadout.canEquipDirectly(aItem)
                        && EquipResult.SUCCESS == component.canEquip(aItem)) {
                    cmdStack.pushAndApply(new CmdAddItem(xBar, loadout, component, aItem));
                }
            }
            case LastSlot: // Fall through
            case Item: // Fall through
            case MultiSlot: {
                // Drop on existing component, try to replace it if we should, otherwise just add it to the component.
                if (aShouldReplace && component.canRemoveItem(aItem)
                        && !(aItem instanceof HeatSink && state.getItem() instanceof Engine)) {
                    cmdStack.pushAndApply(new CmdRemoveItem(xBar, loadout, component, state.getItem()));
                }
                // Fall through
            }
            case Empty: {
                if (EquipResult.SUCCESS == loadout.canEquipDirectly(aItem)
                        && EquipResult.SUCCESS == component.canEquip(aItem)) {
                    cmdStack.pushAndApply(new CmdAddItem(xBar, loadout, component, aItem));
                }
            }
            default:
                break;
        }
    }
}
