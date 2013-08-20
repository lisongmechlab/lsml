package lisong_mechlab.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.MessageXBar.Message;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.LoadoutPart;

public class PartPanel extends JPanel implements MessageXBar.Reader{
   private static final long serialVersionUID = -4399442572295284661L;

   private final int         CELL_HEIGHT      = 20;
   private final int         CELL_WIDTH       = 120;

   JLabel                    lbl_armor_front;
   JLabel                    lbl_armor_back;
   JSpinner                  spinner_front;
   JSpinner                  spinner_back;

   private final LoadoutPart loadoutPart;

   PartPanel(LoadoutPart aLoadoutPart, MessageXBar anXBar){
      super(new BorderLayout());
      anXBar.attach(this);
      loadoutPart = aLoadoutPart;
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      // setBackground(Color.pink);
      setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(aLoadoutPart.getInternalPart().getType().longName()),
                                                   BorderFactory.createEmptyBorder(0, 4, 4, 8)));

      // Armor spinner
      JPanel armorPanel = new JPanel();
      {
         if( aLoadoutPart.getInternalPart().getType().isTwoSided() ){
            armorPanel.setLayout(new BoxLayout(armorPanel, BoxLayout.PAGE_AXIS));

            {
               JPanel p = new JPanel();
               p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
               spinner_front = new JSpinner(new ArmorSpinner(aLoadoutPart, ArmorSide.FRONT, anXBar));
               JLabel frontLabel = new JLabel("Front");
               frontLabel.setLabelFor(spinner_front);

               p.add(frontLabel);
               p.add(Box.createRigidArea(new Dimension(5, 0)));
               p.add(spinner_front);
               lbl_armor_front = new JLabel("/ " + Integer.valueOf(aLoadoutPart.getArmorMax(ArmorSide.FRONT)));
               p.add(lbl_armor_front);
               armorPanel.add(p);
            }

            {
               JPanel p = new JPanel();
               p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
               spinner_back = new JSpinner(new ArmorSpinner(aLoadoutPart, ArmorSide.BACK, anXBar));
               JLabel backLabel = new JLabel("Back");
               backLabel.setLabelFor(spinner_back);

               p.add(backLabel);
               p.add(Box.createRigidArea(new Dimension(5, 0)));
               p.add(spinner_back);
               lbl_armor_back = new JLabel("/ " + Integer.valueOf(aLoadoutPart.getArmorMax(ArmorSide.BACK)));
               p.add(lbl_armor_back);
               armorPanel.add(p);
            }
         }
         else{
            armorPanel.setLayout(new BoxLayout(armorPanel, BoxLayout.LINE_AXIS));

            spinner_front = new JSpinner(new ArmorSpinner(aLoadoutPart, ArmorSide.ONLY, anXBar));
            JLabel label = new JLabel("Armor");
            label.setLabelFor(spinner_front);

            armorPanel.add(label);
            armorPanel.add(Box.createRigidArea(new Dimension(5, 0)));
            armorPanel.add(spinner_front);

            JLabel armorMax = new JLabel("/ " + Integer.valueOf(aLoadoutPart.getInternalPart().getArmorMax()));
            armorPanel.add(armorMax);
         }
         // Dimension armorSpinnerSize = armorSpinner.getPreferredSize();
         // armorSpinnerSize.height = CELL_HEIGHT;
         // armorSpinnerSize.width = 80;
         // armorSpinner.setMaximumSize(armorSpinnerSize);

         Insets insets = getBorder().getBorderInsets(armorPanel);
         Dimension armorPanelDimension = armorPanel.getPreferredSize();
         armorPanelDimension.width = insets.left + insets.right + CELL_WIDTH + 2; // Magic number 2, seems to work.. :s
         armorPanel.setMaximumSize(armorPanelDimension);
         add(armorPanel);
      }

      // Hardpoints
      {
         for(HardpointType hp : HardpointType.values()){
            int hardpoints = aLoadoutPart.getInternalPart().getNumHardpoints(hp);
            if( 1 == hardpoints ){
               JLabel hardpointLabel = new JLabel(hp.shortName());
               add(hardpointLabel);
            }
            else if( 1 < hardpoints ){
               JLabel hardpointLabel = new JLabel(hardpoints + hp.shortName());
               add(hardpointLabel);
            }
         }
      }

      // Critical slots
      PartList list = new PartList(aLoadoutPart, anXBar);
      list.setFixedCellHeight(CELL_HEIGHT);
      list.setFixedCellWidth(CELL_WIDTH);

      add(list);
      add(Box.createRigidArea(new Dimension(0, 10)));

      // Formatting
      // Insets insets = getBorder().getBorderInsets(list);
      // int panelHeight = insets.bottom + insets.top + CELL_HEIGHT
      // * aConfPart.part().criticalslots() + 2 * CELL_HEIGHT;
      // int panelWidth = insets.left + insets.right + CELL_WIDTH;
      // setMinimumSize(new Dimension(panelWidth, panelHeight));
      // setMaximumSize(new Dimension(panelWidth, panelHeight));
   }

   @Override
   public void receive(Message aMsg){
      SwingUtilities.invokeLater(new Runnable(){
         @Override
         public void run(){
            if( lbl_armor_back != null && lbl_armor_front != null ){
               lbl_armor_front.setText("/ " + Integer.valueOf(loadoutPart.getArmorMax(ArmorSide.FRONT)));
               lbl_armor_back.setText("/ " + Integer.valueOf(loadoutPart.getArmorMax(ArmorSide.BACK)));
            }
/*
            if( spinner_back == null ){
               spinner_front.setValue(loadoutPart.getArmor(ArmorSide.ONLY));
            }
            else{
               spinner_front.setValue(loadoutPart.getArmor(ArmorSide.FRONT));
               spinner_back.setValue(loadoutPart.getArmor(ArmorSide.BACK));
            }*/
         }
      });
   }
}
