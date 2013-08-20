package lisong_mechlab.model.mwo_parsing.helpers;

import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsAmmoType.AmmoTypeStats;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ItemStatsModule extends ItemStats{
   @XStreamAsAttribute
   public String                 CType;

   public ItemStatsModuleStats   ModuleStats;
   public ItemStatsJumpJetStats  JumpJetStats;
   public ItemStatsHeatSinkStats HeatSinkStats;
   public ItemStatsEngineStats   EngineStats;
   public AmmoTypeStats          AmmoTypeStats;
}
