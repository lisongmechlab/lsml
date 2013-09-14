package lisong_mechlab.view.equipment;

import javax.swing.event.TreeModelEvent;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.MechGarage;
import lisong_mechlab.model.loadout.MechGarage.Message.Type;
import lisong_mechlab.util.MessageXBar;

class GarageCathegory extends AbstractTreeCathegory implements MessageXBar.Reader{
   private MechGarage garage = null;

   public GarageCathegory(String aName, TreeCathegory aParent, EquipmentTreeModel aModel, MessageXBar xbar){
      super(aName, aParent, aModel);
      xbar.attach(this);
   }

   @Override
   public void receive(MessageXBar.Message aMsg){
      if( aMsg instanceof MechGarage.Message ){
         MechGarage.Message msg = (MechGarage.Message)aMsg;
         if( msg.type == Type.NewGarage ){
            garage = msg.garage;
         }
         getModel().notifyTreeChange(new TreeModelEvent(this, getPath()));
      }
      else if( aMsg instanceof Loadout.Message){
         //Loadout.Message message = (Message)aMsg;
         
         getModel().notifyTreeChange(new TreeModelEvent(this, getPath()));
      }
   }

   @Override
   public int getChildCount(){
      return garage.getMechs().size();
   }

   @Override
   public int getIndex(Object aChild){
      return garage.getMechs().indexOf(aChild);
   }

   @Override
   public Object getChild(int aIndex){
      return garage.getMechs().get(aIndex);
   }
}
