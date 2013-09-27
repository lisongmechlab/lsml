package lisong_mechlab.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.DynamicSlotDistributor;
import lisong_mechlab.model.loadout.LoadoutPart;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.MessageXBar.Message;
import lisong_mechlab.view.render.StyleManager;

public class PartPanel extends JPanel implements MessageXBar.Reader{
   private static final int  ARMOR_LABEL_WIDTH = 30;

   private static final long serialVersionUID  = -4399442572295284661L;

   private final int         CELL_HEIGHT       = 20;
   private final int         CELL_WIDTH        = 120;

   private JLabel            frontArmorLabel;
   private JLabel            backArmorLabel;

   private final LoadoutPart loadoutPart;

   private boolean           canHaveHardpoints;

   PartPanel(LoadoutPart aLoadoutPart, MessageXBar anXBar, boolean aCanHaveHardpoints, DynamicSlotDistributor aSlotDistributor){
      super(new BorderLayout());
      anXBar.attach(this);
      loadoutPart = aLoadoutPart;
      canHaveHardpoints = aCanHaveHardpoints;

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(aLoadoutPart.getInternalPart().getType().longName()),
                                                   BorderFactory.createEmptyBorder(0, 4, 4, 8)));
      add(makeArmorPanel(anXBar));

      if( canHaveHardpoints )
         add(makeHardpointsPanel());

      // Critical slots
      PartList list = new PartList(aLoadoutPart, anXBar, aSlotDistributor);
      list.setFixedCellHeight(CELL_HEIGHT);
      list.setFixedCellWidth(CELL_WIDTH);

      add(list);
      add(Box.createRigidArea(new Dimension(0, 10)));
   }

   private JPanel makeHardpointsPanel(){
      JPanel panel = new JPanel();
      BoxLayout layoutManager = new BoxLayout(panel, BoxLayout.LINE_AXIS);
      panel.setLayout(layoutManager);
      // /panel.setBackground(Color.PINK.darker());
      panel.add(Box.createVerticalStrut(CELL_HEIGHT + CELL_HEIGHT / 2));

      for(HardpointType hp : HardpointType.values()){
         final int hardpoints = loadoutPart.getInternalPart().getNumHardpoints(hp);
         if( 1 == hardpoints ){
            JLabel label = new JLabel(hp.shortName());
            label.setBackground(StyleManager.getBgColorFor(hp));
            label.setForeground(StyleManager.getFgColorFor(hp));
            label.setBorder(new RoundedBorders());
            label.setOpaque(true);
            panel.add(label);
         }
         else if( 1 < hardpoints ){
            JLabel label = new JLabel(hardpoints + " " + hp.shortName());
            label.setBackground(StyleManager.getBgColorFor(hp));
            label.setForeground(StyleManager.getFgColorFor(hp));
            label.setBorder(new RoundedBorders());
            label.setOpaque(true);
            panel.add(label);
         }
      }

      panel.add(Box.createHorizontalGlue());
      return panel;
   }

   private JPanel makeArmorPanel(MessageXBar anXBar){
      JPanel panel = new JPanel();

      if( loadoutPart.getInternalPart().getType().isTwoSided() ){
         frontArmorLabel = new JLabel(" / " + Integer.valueOf(loadoutPart.getArmorMax(ArmorSide.FRONT)));
         frontArmorLabel.setPreferredSize(new Dimension(ARMOR_LABEL_WIDTH, CELL_HEIGHT));
         backArmorLabel = new JLabel(" / " + Integer.valueOf(loadoutPart.getArmorMax(ArmorSide.BACK)));
         backArmorLabel.setPreferredSize(new Dimension(ARMOR_LABEL_WIDTH, CELL_HEIGHT));

         JSpinner frontSpinner = new JSpinner(new ArmorSpinner(loadoutPart, ArmorSide.FRONT, anXBar));
         frontSpinner.setMaximumSize(new Dimension(ARMOR_LABEL_WIDTH, CELL_HEIGHT));

         JSpinner backSpinner = new JSpinner(new ArmorSpinner(loadoutPart, ArmorSide.BACK, anXBar));
         backSpinner.setMaximumSize(new Dimension(ARMOR_LABEL_WIDTH, CELL_HEIGHT));

         JPanel frontPanel = new JPanel();
         frontPanel.setLayout(new BoxLayout(frontPanel, BoxLayout.LINE_AXIS));
         frontPanel.add(new JLabel("Front:"));
         frontPanel.add(Box.createHorizontalGlue());
         frontPanel.add(frontSpinner);
         frontPanel.add(frontArmorLabel);

         JPanel backPanel = new JPanel();
         backPanel.setLayout(new BoxLayout(backPanel, BoxLayout.LINE_AXIS));
         backPanel.add(new JLabel("Back:"));
         backPanel.add(Box.createHorizontalGlue());
         backPanel.add(backSpinner);
         backPanel.add(backArmorLabel);

         panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
         panel.add(frontPanel);
         panel.add(backPanel);
      }
      else{
         JLabel armorLabel = new JLabel(" / " + Integer.valueOf(loadoutPart.getInternalPart().getArmorMax()));
         armorLabel.setPreferredSize(new Dimension(ARMOR_LABEL_WIDTH, 0));

         JSpinner spinner = new JSpinner(new ArmorSpinner(loadoutPart, ArmorSide.ONLY, anXBar));
         spinner.setMaximumSize(new Dimension(ARMOR_LABEL_WIDTH, CELL_HEIGHT));

         panel.add(new JLabel("Armor:"));
         panel.add(Box.createHorizontalGlue());
         panel.add(spinner);
         panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
         panel.add(armorLabel);
      }
      return panel;
   }

   @Override
   public void receive(Message aMsg){
      SwingUtilities.invokeLater(new Runnable(){
         @Override
         public void run(){
            if( backArmorLabel != null && frontArmorLabel != null ){
               frontArmorLabel.setText(" / " + Integer.valueOf(loadoutPart.getArmorMax(ArmorSide.FRONT)));
               backArmorLabel.setText(" / " + Integer.valueOf(loadoutPart.getArmorMax(ArmorSide.BACK)));
            }
         }
      });
   }
}
