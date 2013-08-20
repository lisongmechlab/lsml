package lisong_mechlab.view.equipment;

import javax.swing.event.InternalFrameListener;
import javax.swing.tree.TreePath;

interface TreeCathegory extends InternalFrameListener{

   int getChildCount();

   int getIndex(Object aChild);

   Object getChild(int aIndex);

   TreePath getPath();
   
   EquipmentTreeModel getModel();
}
