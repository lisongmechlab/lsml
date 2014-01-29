package lisong_mechlab.view.mechlab.equipment;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TreeModelEvent;

public abstract class FilterTreeCathegory<T> extends DefaultTreeCathegory<T>{
   protected final GarageTree garageTree;
   private String             nameFilter = "";
   private boolean            wasExpandedBeforeFilter;

   public FilterTreeCathegory(String aName, TreeCathegory aParent, GarageTreeModel aModel, final JTextField aFilterBar, GarageTree aGarageTree){
      super(aName, aParent, aModel);
      garageTree = aGarageTree;
      wasExpandedBeforeFilter = garageTree.isExpanded(getPath());
      if( aFilterBar != null ){
         aFilterBar.addCaretListener(new CaretListener(){
            @Override
            public void caretUpdate(CaretEvent aArg0){
               if( !nameFilter.equals(aFilterBar.getText()) ){
                  if( nameFilter.isEmpty() ){
                     // Starting filtering
                     wasExpandedBeforeFilter = garageTree.isExpanded(getPath());
                  }
                  else if( aFilterBar.getText().isEmpty() ){
                     // Stopping filtering
                     if( !wasExpandedBeforeFilter ){
                        garageTree.collapsePath(getPath());
                     }
                  }
                  else{
                     // Ongoing filtering
                     garageTree.expandPath(getPath());
                  }
                  
                  nameFilter = aFilterBar.getText().toLowerCase();
                  getModel().notifyTreeChange(new TreeModelEvent(this, getPath()));
               }
            }
         });
      }
   }

   protected String getFilterString(){
      return nameFilter;
   }

   private List<T> filterList(){
      if( nameFilter.isEmpty() )
         return children;

      List<T> ans = new ArrayList<>();
      for(T t : children){
         if( filter(t) )
            ans.add(t);
      }
      return ans;
   }

   abstract protected boolean filter(T t);

   @Override
   public int getChildCount(){
      return filterList().size();
   }

   @Override
   public int getIndex(Object aChild){
      return filterList().indexOf(aChild);
   }

   @Override
   public Object getChild(int anIndex){
      return filterList().get(anIndex);
   }

}
