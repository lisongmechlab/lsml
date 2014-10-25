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
package lisong_mechlab.view.mechlab;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import lisong_mechlab.model.DynamicSlotDistributor;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.EquipResult;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.OpAddItem;
import lisong_mechlab.model.loadout.component.OpRemoveItem;
import lisong_mechlab.model.metrics.CriticalStrikeProbability;
import lisong_mechlab.model.metrics.ItemEffectiveHP;
import lisong_mechlab.model.metrics.helpers.ComponentDestructionSimulator;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.OperationStack;
import lisong_mechlab.util.message.Message;
import lisong_mechlab.util.message.MessageXBar;
import lisong_mechlab.view.ItemTransferHandler;
import lisong_mechlab.view.ProgramInit;
import lisong_mechlab.view.render.ComponentRenderer;
import lisong_mechlab.view.render.ComponentRenderer.RenderState;
import lisong_mechlab.view.render.StyleManager;

public class PartList extends JList<Item> {
    private static final long                   serialVersionUID = 5995694414450060827L;
    private final ConfiguredComponentBase       component;
    private final DynamicSlotDistributor        slotDistributor;
    private OperationStack                      opStack;

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

            sb.append("<div style='width:300px'>")
                    .append("<p>")
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
                    setText(isCompact ? Model.HEATSINKS_COMPACT_STRING : Model.HEATSINKS_STRING
                            + component.getEngineHeatsinks() + "/" + component.getEngineHeatsinksMax());
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

    private class Model extends AbstractListModel<Item> implements Message.Recipient {
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
            if (aMsg instanceof ConfiguredComponentBase.ComponentMessage || aMsg instanceof Upgrades.UpgradesMessage) {
                fireContentsChanged(this, 0, component.getInternalComponent().getSlots());
            }
        }
    }

    PartList(OperationStack aStack, final LoadoutBase<?> aLoadout, final ConfiguredComponentBase aComponent,
            final MessageXBar aXBar, DynamicSlotDistributor aSlotDistributor) {
        slotDistributor = aSlotDistributor;
        opStack = aStack;
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
                    removeSelected(aXBar);
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
                    removeSelected(aXBar);
                }
            }
        });
    }

    public Item removeSelected(MessageXBar aXBar) {
        RenderState state = componentRenderer.getRenderState(getSelectedIndex());
        Item item = state.getItem();
        if (component.canRemoveItem(state.getItem())) {
            opStack.pushAndApply(new OpRemoveItem(aXBar, loadout, component, state.getItem()));
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

    public void putElement(Item aItem, int aDropIndex, boolean aShouldReplace) {
        RenderState state = componentRenderer.getRenderState(aDropIndex);

        switch (state.getRenderType()) {
            case EngineHeatSink: {
                if (aItem instanceof HeatSink && EquipResult.SUCCESS == loadout.canEquip(aItem) && EquipResult.SUCCESS == component.canAddItem(aItem)) {
                    opStack.pushAndApply(new OpAddItem(xBar, loadout, component, aItem));
                }
            }
            case LastSlot: // Fall through
            case Item: // Fall through
            case MultiSlot: {
                // Drop on existing component, try to replace it if we should, otherwise just add it to the component.
                if (aShouldReplace && component.canRemoveItem(aItem)
                        && !(aItem instanceof HeatSink && state.getItem() instanceof Engine)) {
                    opStack.pushAndApply(new OpRemoveItem(xBar, loadout, component, state.getItem()));
                }
                // Fall through
            }
            case Empty: {
                if (EquipResult.SUCCESS == loadout.canEquip(aItem) && EquipResult.SUCCESS == component.canAddItem(aItem)) {
                    opStack.pushAndApply(new OpAddItem(xBar, loadout, component, aItem));
                }
            }
            default:
                break;
        }
    }
}
