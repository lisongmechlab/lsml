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

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.loadout.Loadout;

public class LoadoutFrame extends JInternalFrame{
   private static final long serialVersionUID = -9181002222136052106L;
   static int                openFrameCount   = 0;
   static final int          xOffset          = 30, yOffset = 30;
   final private Loadout     loadout;

   public LoadoutFrame(Loadout aLoadout, MessageXBar anXBar){
      super(aLoadout.toString(), true, // resizable
            true, // closable
            false, // maximizable
            true);// iconifiable
      // ...Create the GUI and put it in the window...
      // ...Then set the window size or call pack...

      loadout = aLoadout;

      JMenuBar menuBar = new JMenuBar();
      {
         JMenu menu = new JMenu("Loadout");
         menuBar.add(menu);
         {
            JMenuItem item = new JMenuItem("Load stock");
            item.addActionListener(new ActionListener(){
               @Override
               public void actionPerformed(ActionEvent aArg0){
                  try{
                     loadout.loadStock();
                  }
                  catch( Exception e ){
                     JOptionPane.showMessageDialog(LoadoutFrame.this, "Couldn't load stock loadout! Error: " + e.getMessage());
                  }
               }
            });
            menu.add(item);
         }
         {
            JMenuItem item = new JMenuItem("Rename...");
            item.addActionListener(new ActionListener(){
               @Override
               public void actionPerformed(ActionEvent aArg0){
                  String name = JOptionPane.showInputDialog("Give a name");
                  loadout.rename(name);
                  setTitle(loadout.toString());
                  // TODO: Trigger name change to garage view
               }
            });
            menu.add(item);
         }
         {
            JMenuItem item = new JMenuItem("Strip mech");
            item.addActionListener(new ActionListener(){
               @Override
               public void actionPerformed(ActionEvent aArg0){
                  loadout.strip();
               }
            });
            menu.add(item);
         }
         {
            JMenu subMenu = new JMenu("Max Armor");
            menu.add(subMenu);
            {
               JMenuItem item = new JMenuItem("3:1");
               item.addActionListener(new ActionListener(){
                  @Override
                  public void actionPerformed(ActionEvent aArg0){
                     loadout.setMaxArmor(3);
                  }
               });
               subMenu.add(item);
            }
            {
               JMenuItem item = new JMenuItem("5:1");
               item.addActionListener(new ActionListener(){
                  @Override
                  public void actionPerformed(ActionEvent aArg0){
                     loadout.setMaxArmor(5);
                  }
               });
               subMenu.add(item);
            }
            {
               JMenuItem item = new JMenuItem("10:1");
               item.addActionListener(new ActionListener(){
                  @Override
                  public void actionPerformed(ActionEvent aArg0){
                     loadout.setMaxArmor(10);
                  }
               });
               subMenu.add(item);
            }
            {
               JMenuItem item = new JMenuItem("Custom...");
               item.addActionListener(new ActionListener(){
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
               });
               subMenu.add(item);
            }
         }
      }

      setJMenuBar(menuBar);

      // Set the window's location.
      setLocation(xOffset * openFrameCount, yOffset * openFrameCount);

      JPanel r = new LoadoutInfoPanel(aLoadout, anXBar);
      JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, mechView(aLoadout, anXBar), r);

      sp.setDividerLocation(-1);
      sp.setDividerSize(0);

      setContentPane(sp);

      pack();
      setVisible(true);
   }

   private JPanel mechView(Loadout aConfiguration, MessageXBar anXBar){
      final JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

      Dimension padding = new Dimension(5, 0);

      panel.add(Box.createRigidArea(padding));

      // Right Arm
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.RightArm), anXBar));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }

      panel.add(Box.createRigidArea(padding));

      // Right Torso + Leg
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.RightTorso), anXBar));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.RightLeg), anXBar));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }

      panel.add(Box.createRigidArea(padding));

      // Center Torso + Head
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.Head), anXBar));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.CenterTorso), anXBar));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }

      panel.add(Box.createRigidArea(padding));

      // Left Torso + Leg
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.LeftTorso), anXBar));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.LeftLeg), anXBar));
         subPanel.add(Box.createVerticalGlue());
         panel.add(subPanel);
      }

      panel.add(Box.createRigidArea(padding));

      // Left Arm
      {
         final JPanel subPanel = new JPanel();
         subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
         subPanel.add(new PartPanel(aConfiguration.getPart(Part.LeftArm), anXBar));
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

   public boolean isSaved(){
      return false;
   }

   public Loadout getLoadout(){
      return loadout;
   }
}
