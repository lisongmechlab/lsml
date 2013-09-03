package lisong_mechlab.view;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.border.Border;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;

public class StyleManager{
   private static final Insets PADDING      = new Insets(2, 5, 2, 5);
   private static final int    THICKNESS    = 2;
   private static final int    RADII        = 10;
   private static final int    MARGIN       = 2;
   private static final Border topBorder    = new RoundedBorders(null, new Insets(MARGIN, MARGIN, 0, MARGIN), PADDING, THICKNESS, RADII, false, true);
   private static final Border middleBorder = new RoundedBorders(null, new Insets(0, MARGIN, 0, MARGIN), PADDING, THICKNESS, RADII, true, true);
   private static final Border bottomBorder = new RoundedBorders(null, new Insets(0, MARGIN, MARGIN, MARGIN), PADDING, THICKNESS, RADII, true, false);
   private static final Border singleBorder = new RoundedBorders(null, new Insets(MARGIN, MARGIN, MARGIN, MARGIN), PADDING, THICKNESS, RADII, false,
                                                                 false);
   
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
            return Color.GRAY.brighter();
         case BALLISTIC:
            return Color.YELLOW.darker();
         case ECM:
            return Color.GRAY.darker();
         case ENERGY:
            return Color.RED.darker();
         case MISSILE:
            return Color.GREEN.darker();
         case NONE:
         default:
            return Color.WHITE;
      }
   }

   static public Color getBgColorFor(Item anItem){
      if( anItem instanceof Internal ){
         return Color.BLUE.brighter().brighter().brighter();
      }
      else if( anItem instanceof Ammunition ){
         return getBgColorFor(((Ammunition)anItem).getWeaponHardpointType()).darker();
      }
      return getBgColorFor(anItem.getHardpointType());
   }

   static public Color getBgColorInvalid(){
      return Color.WHITE;
   }

   static public Color getFgColorFor(HardpointType aType){
      switch( aType ){
         case AMS:
            return Color.BLACK;
         case BALLISTIC:
            return Color.BLACK;
         case ECM:
            return Color.WHITE;
         case ENERGY:
            return Color.BLACK;
         case MISSILE:
            return Color.BLACK;
         case NONE:
         default:
            return Color.BLACK;
      }
   }

   static public Color getFgColorFor(Item anItem){
      if( anItem instanceof Internal ){
         return Color.WHITE;
      }
      return getFgColorFor(anItem.getHardpointType());
   }

   static public Color getFgColorInvalid(){
      return Color.RED;
   }
}
