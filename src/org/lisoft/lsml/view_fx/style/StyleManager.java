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
package org.lisoft.lsml.view_fx.style;

import java.util.HashMap;
import java.util.Map;

import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.view_fx.loadout.equipment.EquipmentCategory;

import javafx.css.PseudoClass;
import javafx.scene.Node;

/**
 * This class helps setting consistent CSS classes to various UI elements.
 * 
 * @author Li Song
 *
 */
public class StyleManager {
    public static final PseudoClass                     CSS_PC_UNEQUIPPABLE      = PseudoClass
            .getPseudoClass("unequippable");
    public static final PseudoClass                     CSS_PC_SMARTPLACEABLE    = PseudoClass
            .getPseudoClass("smartplaceable");
    public static final PseudoClass                     CSS_PC_FIXED             = PseudoClass.getPseudoClass("fixed");

    public static final String                          CSS_CLASS_EQUIPPED       = "equipped";

    public static final String                          CSS_CLASS_ARM_STRUT      = "ArmStrut";
    public static final String                          CSS_CLASS_TORSO_STRUT    = "TorsoStrut";
    public static final String                          CSS_CLASS_HARDPOINT      = "HardPoint";

    private static final String                         CSS_COLOUR_MISSILE       = "item-colour-missile";
    private static final String                         CSS_COLOUR_ENERGY        = "item-colour-energy";
    private static final String                         CSS_COLOUR_ECM           = "item-colour-ecm";
    private static final String                         CSS_COLOUR_BALLISTIC     = "item-colour-ballistic";
    private static final String                         CSS_COLOUR_AMS           = "item-colour-ams";
    private static final String                         CSS_COLOUR_MISC          = "item-colour-misc";
    private static final String                         CSS_COLOUR_AMMO_SUFFIX   = "-ammo";
    private static final String                         CSS_COLOUR_INTERNAL      = "item-colour-internal";
    private static final String                         CSS_COLOUR_HEAT_SINK     = "item-colour-hs";
    private static final String                         CSS_COLOUR_ENGINE        = "item-colour-engine";
    private static final String                         CSS_COLOUR_JUMP_JET      = "item-colour-jj";
    private static final String                         CSS_COLOUR_DYNAMIC       = "item-colour-dynamic";
    private static final String                         CSS_COLOUR_DISABLED      = "item-colour-disabled";

    public static final String                          CSS_COLOUR_QUIRK_GOOD    = "quirk-good";
    public static final String                          CSS_COLOUR_QUIRK_BAD     = "quirk-bad";
    public static final String                          CSS_COLOUR_QUIRK_NEUTRAL = "quirk-neutral";

    private static final Map<HardPointType, String>     CSS_HP2COLOUR;

    private static final Map<EquipmentCategory, String> CSS_CATEGORY2CLASS_BASE;

    static {
        CSS_HP2COLOUR = new HashMap<>();
        CSS_HP2COLOUR.put(HardPointType.AMS, CSS_COLOUR_AMS);
        CSS_HP2COLOUR.put(HardPointType.BALLISTIC, CSS_COLOUR_BALLISTIC);
        CSS_HP2COLOUR.put(HardPointType.ECM, CSS_COLOUR_ECM);
        CSS_HP2COLOUR.put(HardPointType.ENERGY, CSS_COLOUR_ENERGY);
        CSS_HP2COLOUR.put(HardPointType.MISSILE, CSS_COLOUR_MISSILE);
        CSS_HP2COLOUR.put(HardPointType.NONE, CSS_COLOUR_MISC);

        CSS_CATEGORY2CLASS_BASE = new HashMap<>();
        CSS_CATEGORY2CLASS_BASE.put(EquipmentCategory.ENERGY, "equipment-energy");
        CSS_CATEGORY2CLASS_BASE.put(EquipmentCategory.BALLISTIC, "equipment-ballistic");
        CSS_CATEGORY2CLASS_BASE.put(EquipmentCategory.MISSILE, "equipment-missile");
        CSS_CATEGORY2CLASS_BASE.put(EquipmentCategory.AMS, "equipment-ams");
        CSS_CATEGORY2CLASS_BASE.put(EquipmentCategory.ECM, "equipment-ecm");
        CSS_CATEGORY2CLASS_BASE.put(EquipmentCategory.ENGINE, "equipment-engine");
        CSS_CATEGORY2CLASS_BASE.put(EquipmentCategory.MISC, "equipment-misc");

    }

    public static void changeItemStyle(Node aNode, Item aItem) {
        aNode.getStyleClass().removeIf(clazz -> clazz.startsWith("equipment"));

        if (aItem != null) {
            EquipmentCategory category = EquipmentCategory.classify(aItem);
            if (EquipmentCategory.MISC == category) {
                if (aItem instanceof JumpJet) {
                    aNode.getStyleClass().add("equipment-jj-category");
                }
                else if (aItem instanceof HeatSink) {
                    aNode.getStyleClass().add("equipment-hs-category");
                }
                else if (aItem instanceof Internal) {
                    aNode.getStyleClass().add("equipment-internal-category");
                }
            }
            else {
                if (aItem instanceof Ammunition) {
                    aNode.getStyleClass().add(CSS_CATEGORY2CLASS_BASE.get(category) + "-ammo");
                }
                else {
                    aNode.getStyleClass().add(CSS_CATEGORY2CLASS_BASE.get(category) + "-category");
                }
            }
        }
    }

    public static void changeCategoryStyle(Node aNode, EquipmentCategory aCategory) {
        aNode.getStyleClass().removeIf(clazz -> clazz.startsWith("equipment"));

        if (aCategory != null) {
            aNode.getStyleClass().add(CSS_CATEGORY2CLASS_BASE.get(aCategory) + "-category");
        }
    }

    public static void changeEquipmentStyle(Node aNode, EquipmentCategory aCategory) {
        aNode.getStyleClass().removeIf(clazz -> clazz.startsWith("equipment"));

        if (aCategory != null) {
            aNode.getStyleClass().add(CSS_CATEGORY2CLASS_BASE.get(aCategory) + "-item");
        }
    }

    public static void changeEquipmentIcon(Node aNode, Item aItem) {
        aNode.getStyleClass().removeIf(clazz -> clazz.startsWith("equipment"));

        if (aItem != null) {
            EquipmentCategory category = EquipmentCategory.classify(aItem);
            aNode.getStyleClass().add(CSS_CATEGORY2CLASS_BASE.get(category) + "-default-icon");
        }
    }

    @Deprecated
    public static String getCssColorFor(HardPointType aHardPointType) {
        return CSS_HP2COLOUR.get(aHardPointType);
    }

    @Deprecated
    public static String getCssColorFor(Item aItem) {
        if (aItem == null)
            return CSS_COLOUR_MISC;

        if (aItem instanceof Ammunition) {
            Ammunition ammunition = (Ammunition) aItem;
            return CSS_HP2COLOUR.get(ammunition.getWeaponHardpointType()) + CSS_COLOUR_AMMO_SUFFIX;
        }

        if (aItem instanceof JumpJet) {
            return CSS_COLOUR_JUMP_JET;
        }

        if (aItem instanceof Engine) {
            return CSS_COLOUR_ENGINE;
        }

        if (aItem instanceof HeatSink) {
            return CSS_COLOUR_HEAT_SINK;
        }

        if (aItem instanceof Internal) {
            return CSS_COLOUR_INTERNAL;
        }

        return getCssColorFor(aItem.getHardpointType());
    }

    @Deprecated
    public static String getCssColorFor(EquipmentCategory aCategory) {
        switch (aCategory) {
            case ENERGY:
                return CSS_COLOUR_ENERGY.replace("item", "category");
            case BALLISTIC:
                return CSS_COLOUR_BALLISTIC.replace("item", "category");
            case MISSILE:
                return CSS_COLOUR_MISSILE.replace("item", "category");
            case ECM:
                return CSS_COLOUR_ECM.replace("item", "category");
            case AMS:
                return CSS_COLOUR_AMS.replace("item", "category");
            case ENGINE:
                return CSS_COLOUR_ENGINE.replace("item", "category");
            case MISC:
                return CSS_COLOUR_MISC.replace("item", "category");
            default:
                throw new RuntimeException("Unknown category: " + aCategory);
        }
    }

    @Deprecated
    public static String getCssColorForDisabled() {
        // TODO Auto-generated method stub
        return CSS_COLOUR_DISABLED;
    }
}
