package lisong_mechlab.view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.view.equipment.EquipmentTreeModel;

public class EquipmentPane extends JTree{
   private static final long serialVersionUID = -8856874024057864775L;
   EquipmentTreeModel treeModel = null;
   private final MessageXBar xBar;
   
   public EquipmentPane(final LoadoutDesktop aLoadoutDesktop, final LSML aLsml, MessageXBar crossBar) throws Exception{
      treeModel = new EquipmentTreeModel(aLsml, crossBar);
      xBar = crossBar;
      
      setModel(treeModel);
      setDragEnabled(true);
      setRootVisible(false);
      setShowsRootHandles(true);
      aLoadoutDesktop.addInternalFrameListener(treeModel);

      addMouseListener(new MouseAdapter(){
         @Override
         public void mousePressed(MouseEvent e){
            if( e.getClickCount() == 2 ){
               TreePath path = getPathForLocation(e.getX(), e.getY());
               if(path == null)
                  return;
               Object clicked = path.getLastPathComponent();
               if( clicked instanceof Chassi ){
                  Chassi chassi = (Chassi)clicked;
                  Loadout loadout = new Loadout(chassi, xBar);
                  aLsml.getGarage().add(loadout);
                  aLoadoutDesktop.openLoadout(loadout, xBar);
               }
               else if(clicked instanceof Loadout){
                  aLoadoutDesktop.openLoadout((Loadout)clicked, xBar);
               }
            }
         }
      });
   }
}
