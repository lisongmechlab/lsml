package lisong_mechlab.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.MessageXBar.Message;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.MechGarage;
import lisong_mechlab.view.action.DeleteLoadoutAction;
import lisong_mechlab.view.action.RenameLoadoutAction;
import lisong_mechlab.view.graphs.DamageGraph;

public class LoadoutFrame extends JInternalFrame implements MessageXBar.Reader{
   private static final long serialVersionUID = -9181002222136052106L;
   private static int        openFrameCount   = 0;
   private static final int  xOffset          = 30, yOffset = 30;
   private final Loadout     loadout;
   private final MessageXBar xbar;

   public LoadoutFrame(Loadout aLoadout, MessageXBar anXBar){
      super(aLoadout.toString(), true, // resizable
            true, // closable
            false, // maximizable
            true);// iconifiable

      xbar = anXBar;
      xbar.attach(this);

      // ...Create the GUI and put it in the zwindow...
      // ...Then set the window size or call pack...

      loadout = aLoadout;

      JMenuBar menuBar = new JMenuBar();
      menuBar.add(createMenuLoadout());
      menuBar.add(createMenuArmor());
      menuBar.add(createMenuGraphs());
      setJMenuBar(menuBar);

      // Set the window's location.
      setLocation(xOffset * openFrameCount, yOffset * openFrameCount);

      JPanel r = new LoadoutInfoPanel(aLoadout, anXBar);
      JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, createMechView(aLoadout, anXBar), r);

      sp.setDividerLocation(-1);
      sp.setDividerSize(0);

      setContentPane(sp);

      pack();
      setVisible(true);

      addInternalFrameListener(new InternalFrameAdapter(){
         @Override
         public void internalFrameClosing(InternalFrameEvent e){
            if( !isSaved() ){
               int ans = JOptionPane.showConfirmDialog(LoadoutFrame.this, "Would you like to save " + loadout.getName() + " to your garage?",
                                                       "Save to garage?", JOptionPane.YES_NO_OPTION);
               if( ans == JOptionPane.YES_OPTION ){
                  LSML.getInstance().getGarage().add(loadout);
               }
            }
         }
      });
   }

   public boolean isSaved(){
      return LSML.getInstance().getGarage().getMechs().contains(loadout);
   }

   public Loadout getLoadout(){
      return loadout;
   }

   private JPanel createMechView(Loadout aConfiguration, MessageXBar anXBar){
      final JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

      Dimension padding = new Dimension(5, 0);

      panel.add(Box.createRigidArea(padding));

      // Right Arm
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(Box.createVerticalStrut(50));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.RightArm), anXBar, true));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }

      panel.add(Box.createRigidArea(padding));

      // Right Torso + Leg
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.RightTorso), anXBar, true));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.RightLeg), anXBar, false));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }

      panel.add(Box.createRigidArea(padding));

      // Center Torso + Head
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.Head), anXBar, false));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.CenterTorso), anXBar, true));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }

      panel.add(Box.createRigidArea(padding));

      // Left Torso + Leg
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.LeftTorso), anXBar, true));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.LeftLeg), anXBar, false));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }

      panel.add(Box.createRigidArea(padding));

      // Left Arm
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(Box.createVerticalStrut(50));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.LeftArm), anXBar, true));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }
      // panel.setVisible(true);
      // panel.validate();
      // setMinimumSize(panel.getSize());
      // setMaximumSize(getMinimumSize());
      // setPreferredSize(getMinimumSize());
      return panel;
   }

   private JMenuItem createMenuItem(String text, ActionListener anActionListener){
      JMenuItem item = new JMenuItem(text);
      item.addActionListener(anActionListener);
      return item;
   }

   private JMenu createMenuLoadout(){
      JMenu menu = new JMenu("Loadout");

      JMenuItem save = new JMenuItem("Save to garage");
      if( isSaved() )
         save.setEnabled(false);
      else
         save.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aArg0){
               LSML.getInstance().getGarage().add(loadout);
            }
         });

      menu.add(save);
      menu.add(new JMenuItem(new RenameLoadoutAction(loadout, KeyStroke.getKeyStroke("R"))));
      menu.add(new JMenuItem(new DeleteLoadoutAction(LSML.getInstance().getGarage(), loadout, KeyStroke.getKeyStroke("D"))));

      menu.add(createMenuItem("Load stock", new ActionListener(){
         @Override
         public void actionPerformed(ActionEvent aArg0){
            try{
               loadout.loadStock();
            }
            catch( Exception e ){
               JOptionPane.showMessageDialog(LoadoutFrame.this, "Couldn't load stock loadout! Error: " + e.getMessage());
            }
         }
      }));

      menu.add(createMenuItem("Strip mech", new ActionListener(){
         @Override
         public void actionPerformed(ActionEvent aArg0){
            loadout.strip();
         }
      }));
      return menu;
   }

   private JMenu createMenuArmor(){
      JMenu menu = new JMenu("Armor");

      menu.add(createMenuItem("Strip Armor", new ActionListener(){
         @Override
         public void actionPerformed(ActionEvent aArg0){
            loadout.stripArmor();
         }
      }));

      {
         JMenu subMenu = new JMenu("Max Armor");
         menu.add(subMenu);

         subMenu.add(createMenuItem("3:1", new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aArg0){
               loadout.setMaxArmor(3);
            }
         }));

         subMenu.add(createMenuItem("5:1", new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aArg0){
               loadout.setMaxArmor(5);
            }
         }));

         subMenu.add(createMenuItem("10:1", new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aArg0){
               loadout.setMaxArmor(10);
            }
         }));

         subMenu.add(createMenuItem("Custom...", new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aArg0){
               String input = (String)JOptionPane.showInputDialog(LoadoutFrame.this,
                                                                  "Please enter the ratio between front and back armor as front:back.Example 3:1",
                                                                  "Maximizing armor...", JOptionPane.INFORMATION_MESSAGE, null, null, "3:1");
               String[] s = input.split(":");
               if( s.length == 2 ){
                  double front, back;
                  try{
                     front = Double.parseDouble(s[0]);
                     back = Double.parseDouble(s[1]);
                  }
                  catch( Exception e ){
                     JOptionPane.showMessageDialog(LoadoutFrame.this, "Error parsing ratio! Loadout was not changed!");
                     return;
                  }
                  loadout.setMaxArmor(front / back);
               }
               else
                  JOptionPane.showMessageDialog(LoadoutFrame.this, "Error parsing ratio! Loadout was not changed!");
            }
         }));
      }
      return menu;
   }

   private JMenu createMenuGraphs(){
      JMenu menu = new JMenu("Graphs");

      menu.add(createMenuItem("Damage", new ActionListener(){
         @Override
         public void actionPerformed(ActionEvent aArg0){
            new DamageGraph(loadout, xbar);
         }
      }));
      return menu;
   }

   @Override
   public void receive(Message aMsg){
      if( aMsg instanceof MechGarage.Message ){
         MechGarage.Message msg = (MechGarage.Message)aMsg;
         if( msg.loadout == loadout ){
            if( msg.type == MechGarage.Message.Type.LoadoutRemoved ){
               dispose(); // Closes frame
            }
         }
      }
      else if( aMsg instanceof Loadout.Message ){
         Loadout.Message msg = (Loadout.Message)aMsg;
         if( msg.loadout == loadout ){
            if( msg.type == Loadout.Message.Type.RENAME ){
               setTitle(loadout.toString());
            }
         }
      }
   }
}
