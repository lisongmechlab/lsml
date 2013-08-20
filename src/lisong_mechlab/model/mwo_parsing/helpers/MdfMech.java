package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class MdfMech{
   @XStreamAsAttribute
   public String  Variant;
   @XStreamAsAttribute
   public int  MaxTons;
   @XStreamAsAttribute
   public double  BaseTons;
   @XStreamAsAttribute
   public int     MaxJumpJets;
   @XStreamAsAttribute
   public int     MinEngineRating;
   @XStreamAsAttribute
   public int     MaxEngineRating;
}
