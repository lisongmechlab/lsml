package lisong_mechlab.model.mwo_parsing.helpers;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class HardPointInfo{
   @XStreamAsAttribute
   public int id;
   
   @XStreamImplicit(itemFieldName = "WeaponSlot")
   public List<HardPointWeaponSlot> weaponslots;
}
