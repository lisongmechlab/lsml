package lisong_mechlab.model.mwo_parsing.helpers;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("JumpJetStats")
public class ItemStatsJumpJetStats{
   @XStreamAlias("boost_z")
   @XStreamAsAttribute
   public double boost;
   
   @XStreamAsAttribute
   public double duration;
   @XStreamAsAttribute
   public double heat;
   @XStreamAsAttribute
   public int    minTons;
   @XStreamAsAttribute
   public int    maxTons;
}
