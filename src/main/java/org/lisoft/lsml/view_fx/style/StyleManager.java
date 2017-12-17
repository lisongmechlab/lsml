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
package org.lisoft.lsml.view_fx.style;

import java.util.HashMap;
import java.util.Map;

import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Consumable;
import org.lisoft.lsml.model.item.ECM;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.model.item.MASC;
import org.lisoft.lsml.model.item.MwoObject;
import org.lisoft.lsml.model.item.TargetingComputer;
import org.lisoft.lsml.view_fx.util.EquipmentCategory;

import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * This class helps setting consistent CSS classes to various UI elements.
 *
 * @author Emily Björk
 *
 */
public class StyleManager {
    public static final String CLASS_ARM_STRUT = "arm-strut";
    public static final String CLASS_DEFAULT_PADDING = "default-padding";
    public static final String CLASS_DEFAULT_SPACING = "default-spacing";
    public static final String CLASS_SMALL_SPACING = "small-spacing";

    /**
     * Applied to all category rows in the equipment list.
     */
    public static final String CLASS_EQ_CAT = "equipment-category";

    /**
     * Applied to all rows in the equipment list that are not categories.
     */
    public static final String CLASS_EQ_LIST = "equipment-list-row";
    public static final String CLASS_EQUIPPED = "equipped";
    public static final String CLASS_HARDPOINT = "hard-point";
    public static final String CLASS_OVERLAY = "overlay";
    public static final String CLASS_MATERIAL = "material";
    public static final String CLASS_DECOR_ROOT = "decor-root";

    public static final String ICON_LISTING_LARGE = "svg-listing-large";
    public static final String ICON_LISTING_SMALL = "svg-listing-small";

    public static final String COLOUR_QUIRK_BAD = "quirk-bad";
    public static final String COLOUR_QUIRK_GOOD = "quirk-good";
    public static final String COLOUR_QUIRK_NEUTRAL = "quirk-neutral";

    public static final PseudoClass PC_AUTOARMOUR;
    public static final PseudoClass PC_SMARTPLACEABLE;
    public static final PseudoClass PC_UNEQUIPPABLE;

    private static final Map<EquipmentCategory, String> CATEGORY2CLASS_BASE;
    public static final String CLASS_ARMOR_FRONT = "svg-armor-front";
    public static final String CLASS_ICON_SMALL = "icon-small";
    public static final String CLASS_ICON_MEDIUM = "icon-medium";
    public static final String CLASS_ARMOR_BACK = "svg-armor-back";
    public static final String CLASS_ARMOR = "svg-armor";
    public static final String COLOUR_TEXT_ERROR = "text-error";
    public static final String COLOUR_TEXT_WARNING = "text-warning";
    public static final String COLOUR_TEXT_NOTICE = "text-notice";
    public static final String CLASS_EDITABLE_LABEL = "editable-label";
    public static final String CLASS_H2 = "h2";
    public static final String CLASS_H1 = "h1";

    static {
        PC_SMARTPLACEABLE = PseudoClass.getPseudoClass("smartplaceable");
        PC_UNEQUIPPABLE = PseudoClass.getPseudoClass("unequippable");
        PC_AUTOARMOUR = PseudoClass.getPseudoClass("autoarmour");

        CATEGORY2CLASS_BASE = new HashMap<>();
        CATEGORY2CLASS_BASE.put(EquipmentCategory.ENERGY, "equipment-energy");
        CATEGORY2CLASS_BASE.put(EquipmentCategory.BALLISTIC, "equipment-ballistic");
        CATEGORY2CLASS_BASE.put(EquipmentCategory.MISSILE, "equipment-missile");
        CATEGORY2CLASS_BASE.put(EquipmentCategory.AMS, "equipment-ams");
        CATEGORY2CLASS_BASE.put(EquipmentCategory.ECM, "equipment-ecm");
        CATEGORY2CLASS_BASE.put(EquipmentCategory.XL_ENGINE, "equipment-engine");
        CATEGORY2CLASS_BASE.put(EquipmentCategory.LE_ENGINE, "equipment-engine");
        CATEGORY2CLASS_BASE.put(EquipmentCategory.STD_ENGINE, "equipment-engine");
        CATEGORY2CLASS_BASE.put(EquipmentCategory.MISC, "equipment-misc");
        CATEGORY2CLASS_BASE.put(EquipmentCategory.COOLANT_FLUSH, "equipment-consumable");
        CATEGORY2CLASS_BASE.put(EquipmentCategory.STRATEGIC_STRIKE, "equipment-consumable");
        CATEGORY2CLASS_BASE.put(EquipmentCategory.UAV, "equipment-consumable");
    }

    public static void addClass(Node aNode, String aClass) {
        final ObservableList<String> styles = aNode.getStyleClass();
        if (aClass != null && !styles.contains(aClass)) {
            styles.add(aClass);
        }
    }

    public static void changeListStyle(Node aNode, EquipmentCategory aCategory) {
        if (aCategory != null) {
            String categoryClass = getCategoryClass(aCategory);
            aNode.getStyleClass().add(categoryClass);
            aNode.getStyleClass().add(CLASS_EQ_LIST);
            aNode.getStyleClass().removeIf(clazz -> clazz.startsWith("equipment") && !clazz.equals(CLASS_EQ_LIST)
                    && !clazz.equals(getCategoryClass(aCategory)));
        }
        else {
            aNode.getStyleClass().removeIf(clazz -> clazz.startsWith("equipment"));
        }
    }

    public static void changeStyle(Node aNode, EquipmentCategory aCategory) {
        aNode.getStyleClass().removeIf(clazz -> clazz.startsWith("equipment"));

        if (aCategory != null) {
            aNode.getStyleClass().add(getCategoryClass(aCategory));
            aNode.getStyleClass().add(CLASS_EQ_CAT);
        }
    }

    public static void changeStyle(Node aNode, MwoObject aEquipment) {
        aNode.getStyleClass().removeIf(clazz -> clazz.startsWith("equipment"));

        if (aEquipment != null) {
            final EquipmentCategory category = EquipmentCategory.classify(aEquipment);
            if (EquipmentCategory.MISC == category) {
                if (aEquipment instanceof JumpJet) {
                    aNode.getStyleClass().add("equipment-jj");
                }
                else if (aEquipment instanceof HeatSink) {
                    aNode.getStyleClass().add("equipment-hs");
                }
                else if (aEquipment instanceof Internal) {
                    if (aEquipment == ItemDB.DYN_ARMOUR || aEquipment == ItemDB.DYN_STRUCT
                            || aEquipment == ItemDB.FIX_ARMOUR || aEquipment == ItemDB.FIX_STRUCT) {
                        aNode.getStyleClass().add("equipment-dynamic");
                    }
                    else {
                        aNode.getStyleClass().add("equipment-internal");
                    }
                }
                else {
                    aNode.getStyleClass().add(getCategoryClass(category));
                }
            }
            else {
                if (aEquipment instanceof Ammunition) {
                    aNode.getStyleClass().add(getCategoryClass(category) + "-ammo");
                }
                else {
                    aNode.getStyleClass().add(getCategoryClass(category) + "");
                }
            }
        }
        else {
            aNode.getStyleClass().add("equipment-empty");
        }
    }

    private static String getCategoryClass(final EquipmentCategory category) {
        String cat = CATEGORY2CLASS_BASE.get(category);
        if (null == cat) {
            System.err.println("Lookup for " + category + " failed!");
            return CATEGORY2CLASS_BASE.get(EquipmentCategory.MISC);
        }
        return cat;
    }

    public static Node makeDirectoryIcon() {
        final Region r = new Region();
        r.getStyleClass().add("svg-folder");
        r.getStyleClass().add(CLASS_ICON_SMALL);
        return r;
    }

    public static Region makeIcon(MwoObject aItem) {
        final Region bg = new Region();
        addClass(bg, CLASS_ICON_SMALL);
        changeStyle(bg, aItem);

        final Region fg = new Region();
        addClass(fg, item2icon(aItem));
        addClass(fg, CLASS_ICON_SMALL);
        fg.setStyle("-fx-background-color: -fx-text-background-color;");

        return new StackPane(bg, fg);
    }

    /**
     * @return A {@link Node} that is rendered as an icon.
     */
    public static Node makeMechIcon() {
        final Region r = new Region();
        r.getStyleClass().add("svg-mech");
        r.getStyleClass().add("icon");
        r.getStyleClass().add(CLASS_ICON_SMALL);
        return r;
    }

    public static void makeOverlay(Node aNode) {
        addClass(aNode, CLASS_OVERLAY);
        addClass(aNode, CLASS_MATERIAL);
        aNode.setPickOnBounds(false);
    }

    public static void setCompactStyle(Scene aScene, boolean aCompact) {
        aScene.getRoot().getStylesheets().remove("view/CompactStyle.css");
        if (aCompact) {
            aScene.getRoot().getStylesheets().add("view/CompactStyle.css");
        }
    }

    private static String item2icon(MwoObject aItem) {
        if (aItem instanceof Engine) {
            final Engine engine = (Engine) aItem;
            return "svg-eq-engine-" + engine.getType().toString().toLowerCase();
        }
        else if (aItem instanceof HeatSink) {
            return "svg-eq-hs";
        }
        else if (aItem instanceof JumpJet) {
            return "svg-eq-jj";
        }
        else if (aItem instanceof Ammunition) {
            return "svg-eq-ammo";
        }
        else if (aItem instanceof ECM) {
            return "svg-eq-ecm";
        }
        else if (aItem instanceof TargetingComputer) {
            if (aItem.getName().contains("COMMAND")) {
                return "svg-eq-cc";
            }
            return "svg-eq-tc";
        }
        else if (aItem instanceof Consumable) {
            final Consumable pilotModule = (Consumable) aItem;
            switch (pilotModule.getType()) {
                case COOLANT_FLUSH:
                    return "svg-eq-consumable-flush";
                case STRATEGIC_STRIKE:
                    return "svg-eq-consumable-strike";
                case UAV:
                    return "svg-eq-consumable-uav";
                case UNKNOWN:
                default:
                    return "svg-eq-unknown";
            }
        }
        else if (aItem instanceof MASC) {
            return "svg-eq-masc";
        }

        String s = aItem.getName().toLowerCase().replaceAll("^c-|^clan", "");
        s = s.replaceAll("[-/.\\s]", "");
        s = s.replaceAll("\\+artemis", "");
        s = s.replaceAll("streak", "s");
        s = s.replaceAll("ultra", "u");
        s = s.replaceAll("pulse", "p");
        s = s.replaceAll("small", "s");
        s = s.replaceAll("medium", "m");
        s = s.replaceAll("large", "l");
        s = s.replaceAll("micro", "u");
        s = s.replaceAll("sml", "s");
        s = s.replaceAll("med", "m");
        s = s.replaceAll("lrg", "l");
        s = s.replaceAll("laser", "las");
        s = s.replaceAll("beagleactiveprobe", "bap");
        s = s.replaceAll("activeprobe", "bap");
        s = s.replaceAll("hvy", "heavy");
        return "svg-eq-" + s;
    }
}
