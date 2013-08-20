package lisong_mechlab.model.item;

import lisong_mechlab.model.mwo_parsing.helpers.MdfInternal;

/**
 * Internals are special items that do not exist in the ItemDB. Instead they are created and owned by the chassii.
 * 
 * @author Emily
 */
public class Internal extends Item{
   public Internal(MdfInternal aInternal){
      super(aInternal.Name, aInternal.Desc, aInternal.Slots); // TODO: Check translation
   }

   public Internal(String aNameTag, String aDescTag, int aSlots){
      super(aNameTag, aDescTag, aSlots);
   }
}
