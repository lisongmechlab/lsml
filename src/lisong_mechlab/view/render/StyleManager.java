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
package lisong_mechlab.view.render;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.border.Border;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.item.Weapon;

public class StyleManager{
   private static final Insets PADDING                 = new Insets(2, 5, 2, 5);
   private static final Insets THIN_PADDING            = new Insets(1, 2, 1, 2);
   private static final int    RADII                   = ItemRenderer.RADII;
   private static final int    MARGIN                  = 1;
   private static final Border thinItemBorder          = new RoundedBorders(new Insets(0, MARGIN, 0, MARGIN), THIN_PADDING, RADII, false, false);
   private static final Border topBorder               = new RoundedBorders(new Insets(MARGIN, MARGIN, 0, MARGIN), PADDING, RADII, false, true);
   private static final Border middleBorder            = new RoundedBorders(new Insets(0, MARGIN, 0, MARGIN), PADDING, RADII, true, true);
   private static final Border bottomBorder            = new RoundedBorders(new Insets(0, MARGIN, MARGIN, MARGIN), PADDING, RADII, true, false);
   private static final Border singleBorder            = new RoundedBorders(new Insets(MARGIN, MARGIN, MARGIN, MARGIN), PADDING, RADII, false, false);

   // Weapons
   private static final Color  COLOR_FG_ENERGY         = Color.WHITE;
   private static final Color  COLOR_BG_ENERGY         = new Color(0xa40000);
   private static final Color  COLOR_FG_ENERGY_ALT     = Color.WHITE;
   private static final Color  COLOR_BG_ENERGY_ALT     = new Color(0xd40000);
   private static final Color  COLOR_FG_MISSILE        = Color.WHITE;
   private static final Color  COLOR_BG_MISSILE        = new Color(0x4e9a06);
   private static final Color  COLOR_FG_MISSILE_AMMO   = Color.BLACK;
   private static final Color  COLOR_BG_MISSILE_AMMO   = new Color(0x8ae234);
   private static final Color  COLOR_FG_BALLISTIC      = Color.WHITE;
   private static final Color  COLOR_BG_BALLISTIC      = new Color(0xc4a000);
   private static final Color  COLOR_FG_BALLISTIC_AMMO = Color.BLACK;
   private static final Color  COLOR_BG_BALLISTIC_AMMO = new Color(0xfce94f);
   
   // Engine/Propulsion
   private static final Color  COLOR_FG_JJ             = Color.WHITE;
   private static final Color  COLOR_BG_JJ             = new Color(0x8d6094);
   private static final Color  COLOR_FG_ENGINE         = Color.WHITE;
   private static final Color  COLOR_BG_ENGINE         = new Color(0x5c3566);
   private static final Color  COLOR_FG_HS             = Color.WHITE;
   private static final Color  COLOR_BG_HS             = new Color(0xad7fa8);

   // Structure/Internal
   private static final Color  COLOR_FG_DYNAMIC        = (new Color(0xe1e6dd)).darker();
   private static final Color  COLOR_BG_DYNAMIC        = new Color(0xe1e6dd);
   private static final Color  COLOR_FG_INTERNAL       = Color.GRAY.darker();
   private static final Color  COLOR_BG_INTERNAL       = new Color(0xd3d7cf);

   // Counter measures
   private static final Color  COLOR_FG_AMS            = Color.WHITE;
   private static final Color  COLOR_BG_AMS            = new Color(0x2e3436);
   private static final Color  COLOR_FG_AMS_AMMO       = Color.WHITE;
   private static final Color  COLOR_BG_AMS_AMMO       = new Color(0x65676b);
   private static final Color  COLOR_FG_ECM            = Color.WHITE;
   private static final Color  COLOR_BG_ECM            = new Color(0x204a87);

   // Others
   private static final Color  COLOR_BG_MISC           = new Color(0x729fcf);

   public static void styleItem(JComponent aComponent){
      Item item = null;
      styleItem(aComponent, item);
   }

   static public void styleItem(JComponent aComponent, Item anItem){
      aComponent.setOpaque(true);
      aComponent.setBorder(singleBorder);
      aComponent.setBackground(getBgColorFor(anItem));
      aComponent.setForeground(getFgColorFor(anItem));
   }
   
   public static void styleThinItem(JComponent aComponent, Item anItem){
      aComponent.setOpaque(true);
      aComponent.setBorder(thinItemBorder);
      aComponent.setBackground(getBgColorFor(anItem));
      aComponent.setForeground(getFgColorFor(anItem));
   }
   
   static public void styleItemTop(JComponent aComponent, Item anItem){
      aComponent.setOpaque(true);
      aComponent.setBorder(topBorder);
      aComponent.setBackground(getBgColorFor(anItem));
      aComponent.setForeground(getFgColorFor(anItem));
   }

   static public void styleItemMiddle(JComponent aComponent, Item anItem){
      aComponent.setOpaque(true);
      aComponent.setBorder(middleBorder);
      aComponent.setBackground(getBgColorFor(anItem));
      aComponent.setForeground(getFgColorFor(anItem));
   }

   static public void styleItemBottom(JComponent aComponent, Item anItem){
      aComponent.setOpaque(true);
      aComponent.setBorder(bottomBorder);
      aComponent.setBackground(getBgColorFor(anItem));
      aComponent.setForeground(getFgColorFor(anItem));
   }

   public static void styleThinItem(JComponent aComponent, HardpointType aType){
      aComponent.setOpaque(true);
      aComponent.setBorder(thinItemBorder);
      aComponent.setBackground(getBgColorFor(aType));
      aComponent.setForeground(getFgColorFor(aType));
   }
   
   public static void styleDynamicEntry(JComponent aComponent){
      aComponent.setOpaque(true);
      aComponent.setBorder(singleBorder);
      aComponent.setBackground(COLOR_BG_DYNAMIC);
      aComponent.setForeground(COLOR_FG_DYNAMIC);
   }

   public static void styleItem(JComponent aComponent, HardpointType aType){
      aComponent.setOpaque(true);
      aComponent.setBorder(singleBorder);
      aComponent.setBackground(getBgColorFor(aType));
      aComponent.setForeground(getFgColorFor(aType));
   }

   static public void colour(JComponent aComponent, HardpointType aType){
      aComponent.setBackground(getBgColorFor(aType));
      aComponent.setForeground(getFgColorFor(aType));
   }

   static public void colour(JComponent aComponent, Item anItem){
      aComponent.setBackground(getBgColorFor(anItem));
      aComponent.setForeground(getFgColorFor(anItem));
   }

   static public void colourInvalid(JComponent aComponent){
      aComponent.setBackground(getBgColorInvalid());
      aComponent.setForeground(getFgColorInvalid());
   }

   static public Color getBgColorFor(HardpointType aType){
      switch( aType ){
         case AMS:
            return COLOR_BG_AMS;
         case BALLISTIC:
            return COLOR_BG_BALLISTIC;
         case ECM:
            return COLOR_BG_ECM;
         case ENERGY:
            return COLOR_BG_ENERGY;
         case MISSILE:
            return COLOR_BG_MISSILE;
         case NONE:
         default:
            return Color.WHITE;
      }
   }

   static public Color getBgColorFor(Item anItem){
      if( anItem == null ){
         return Color.WHITE;
      }
      else if( anItem instanceof Internal ){
         return COLOR_BG_INTERNAL;
      }
      else if( anItem instanceof Ammunition ){
         switch( ((Ammunition)anItem).getWeaponHardpointType() ){
            case AMS:
               return COLOR_BG_AMS_AMMO;
            case BALLISTIC:
               return COLOR_BG_BALLISTIC_AMMO;
            case MISSILE:
               return COLOR_BG_MISSILE_AMMO;
            default:
               break;
         }
      }
      else if( anItem instanceof HeatSink ){
         return COLOR_BG_HS;
      }
      else if( anItem instanceof Engine ){
         return COLOR_BG_ENGINE;
      }
      else if( anItem instanceof JumpJet ){
         return COLOR_BG_JJ;
      }
      else if( anItem.getName().equals("TAG") ){
         return COLOR_BG_ENERGY_ALT;
      }
      else if( anItem instanceof Weapon || anItem == ItemDB.ECM ){
         return getBgColorFor(anItem.getHardpointType());
      }
      return COLOR_BG_MISC;
   }

   static public Color getBgColorInvalid(){
      return Color.GRAY.brighter();
   }

   static public Color getFgColorFor(HardpointType aType){
      switch( aType ){
         case AMS:
            return COLOR_FG_AMS;
         case BALLISTIC:
            return COLOR_FG_BALLISTIC;
         case ECM:
            return COLOR_FG_ECM;
         case ENERGY:
            return COLOR_FG_ENERGY;
         case MISSILE:
            return COLOR_FG_MISSILE;
         case NONE:
         default:
            return Color.BLACK;
      }
   }

   static public Color getFgColorFor(Item anItem){
      if( anItem == null ){
         return Color.BLACK;
      }
      else if( anItem instanceof Internal ){
         return COLOR_FG_INTERNAL;
      }
      else if( anItem instanceof Ammunition ){
         switch( ((Ammunition)anItem).getWeaponHardpointType() ){
            case AMS:
               return COLOR_FG_AMS_AMMO;
            case BALLISTIC:
               return COLOR_FG_BALLISTIC_AMMO;
            case MISSILE:
               return COLOR_FG_MISSILE_AMMO;
            default:
               break;
         }
      }
      else if( anItem instanceof HeatSink ){
         return COLOR_FG_HS;
      }
      else if( anItem instanceof Engine ){
         return COLOR_FG_ENGINE;
      }
      else if( anItem instanceof JumpJet ){
         return COLOR_FG_JJ;
      }
      else if( anItem.getName().contains("TAG") ){
         return COLOR_FG_ENERGY_ALT;
      }
      else if( anItem instanceof Weapon || anItem == ItemDB.ECM ){
         return getFgColorFor(anItem.getHardpointType());
      }
      return Color.BLACK;
   }

   static public Color getFgColorInvalid(){
      return Color.GRAY.darker();
   }
}
