package lisong_mechlab.view.equipment;

import javax.swing.event.InternalFrameAdapter;
import javax.swing.tree.TreePath;

abstract class TreeCathegory extends InternalFrameAdapter{

   public abstract int getChildCount();

   public abstract int getIndex(Object aChild);

   public abstract Object getChild(int aIndex);

   public abstract TreePath getPath();

   public abstract EquipmentTreeModel getModel();
}
