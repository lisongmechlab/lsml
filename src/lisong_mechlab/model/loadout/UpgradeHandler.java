package lisong_mechlab.model.loadout;

import lisong_mechlab.model.item.Item;

/**
 * This interface represents a base for handling changing upgrades on a mech. Such as armor type, internal structure,
 * guidance and double heat sinks.
 * 
 * @author Emily Björk
 */
public interface UpgradeHandler{
   /**
    * Return true if the {@link Loadout} given in the constructor can handle the upgrade in the {@link Item}.
    * 
    * @param anUpgradeItem
    *           The upgrade to apply.
    * @return <code>true</code> if the {@link Loadout} can take the upgrade and still remain in a legal state.
    */
   public boolean canApplyUpgrade(Item anUpgradeItem);

   /**
    * Will apply the given upgrade to the {@link Loadout} given in the constructor. If the {@link Loadout} is unable to
    * accommodate the upgrade, an exception is thrown.
    * 
    * @param anUpgradeItem
    * @throws IllegalArgumentException
    *            Thrown if the {@link Loadout} would end up in an illegal state after the upgrade.
    */
   public void applyUpgrade(Item anUpgradeItem) throws IllegalArgumentException;
}
