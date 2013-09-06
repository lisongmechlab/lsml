package lisong_mechlab.view;

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
import lisong_mechlab.model.item.Weapon;

public class StyleManager{
   private static final Insets PADDING                 = new Insets(2, 5, 2, 5);
   private static final int    THICKNESS               = 2;
   private static final int    RADII                   = 10;
   private static final int    MARGIN                  = 2;
   private static final Border topBorder               = new RoundedBorders(null, new Insets(MARGIN, MARGIN, 0, MARGIN), PADDING, THICKNESS, RADII,
                                                                            false, true);
   private static final Border middleBorder            = new RoundedBorders(null, new Insets(0, MARGIN, 0, MARGIN), PADDING, THICKNESS, RADII, true,
                                                                            true);
   private static final Border bottomBorder            = new RoundedBorders(null, new Insets(0, MARGIN, MARGIN, MARGIN), PADDING, THICKNESS, RADII,
                                                                            true, false);
   private static final Border singleBorder            = new RoundedBorders(null, new Insets(MARGIN, MARGIN, MARGIN, MARGIN), PADDING, THICKNESS,
                                                                            RADII, false, false);

   private static Color        COLOR_BG_AMS            = new Color(0x2e3436);
   private static Color        COLOR_BG_AMS_AMMO       = new Color(0x888a85);
   private static Color        COLOR_BG_BALLISTIC      = new Color(0xc4a000);
   private static Color        COLOR_BG_BALLISTIC_AMMO = new Color(0xfce94f);
   private static Color        COLOR_BG_ECM            = new Color(0x204a87);
   private static Color        COLOR_BG_ENERGY         = new Color(0xa40000);
   private static Color        COLOR_BG_ENERGY_ALT     = new Color(0xef2929);
   private static Color        COLOR_BG_MISSILE        = new Color(0x4e9a06);
   private static Color        COLOR_BG_MISSILE_AMMO   = new Color(0x8ae234);

   private static Color        COLOR_BG_INTERNAL       = new Color(0xd3d7cf);
   private static Color        COLOR_BG_ENGINE         = new Color(0x5c3566);
   private static Color        COLOR_BG_HS             = new Color(0xad7fa8);
   private static Color        COLOR_BG_MISC           = new Color(0x729fcf);

   static public void styleItem(JComponent aComponent, Item anItem){
      aComponent.setOpaque(true);
      aComponent.setBorder(singleBorder);
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
      if( anItem instanceof Internal ){
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
      else if( anItem.getName().equals("TAG") ){
         return COLOR_BG_ENERGY_ALT;
      }
      else if( anItem instanceof Weapon || anItem == ItemDB.ECM ){
         return getBgColorFor(anItem.getHardpointType());
      }
      return COLOR_BG_MISC;
   }

   static public Color getBgColorInvalid(){
      return Color.WHITE;
   }

   static public Color getFgColorFor(HardpointType aType){
      switch( aType ){
         case AMS:
            return Color.WHITE;
         case BALLISTIC:
            return Color.WHITE;
         case ECM:
            return Color.WHITE;
         case ENERGY:
            return Color.WHITE;
         case MISSILE:
            return Color.WHITE;
         case NONE:
         default:
            return Color.BLACK;
      }
   }

   static public Color getFgColorFor(Item anItem){
      if( anItem instanceof Internal ){
         return Color.GRAY.darker();
      }
      else if( anItem instanceof Ammunition ){
         switch( ((Ammunition)anItem).getWeaponHardpointType() ){
            case AMS:
               return Color.BLACK;
            case BALLISTIC:
               return Color.BLACK;
            case MISSILE:
               return Color.BLACK;
            default:
               break;
         }
      }
      else if( anItem instanceof HeatSink ){
         return Color.BLACK;
      }
      else if( anItem instanceof Engine ){
         return Color.WHITE;
      }
      else if( anItem.getName().contains("TAG") ){
         return Color.WHITE;
      }
      else if( anItem instanceof Weapon || anItem == ItemDB.ECM ){
         return getFgColorFor(anItem.getHardpointType());
      }
      return Color.BLACK;
   }

   static public Color getFgColorInvalid(){
      return Color.RED;
   }
}
