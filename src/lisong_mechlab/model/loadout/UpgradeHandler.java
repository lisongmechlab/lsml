package lisong_mechlab.model.loadout;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.upgrade.Upgrade;

/**
 * This interface defines handling of one upgrade type for a {@link Loadout}; such as armor, structure and guidance.
 * 
 * @author Emily Björk
 */
public interface UpgradeHandler<T extends Upgrade> {
   /**
    * Return true if the {@link Loadout} given in the constructor can handle the upgrade in the {@link Item}.
    * 
    * @param anUpgradeItem
    *           The upgrade to apply.
    * @return <code>true</code> if the {@link Loadout} can take the upgrade and still remain in a legal state.
    */
   public boolean canApplyUpgrade(T anUpgradeItem);

   /**
    * Will apply the given upgrade to the {@link Loadout} given in the constructor. If the {@link Loadout} is unable to
    * accommodate the upgrade, an exception is thrown.
    * 
    * @param anUpgradeItem
    * @throws IllegalArgumentException
    *            Thrown if the {@link Loadout} would end up in an illegal state after the upgrade.
    */
   public void applyUpgrade(T anUpgradeItem) throws IllegalArgumentException;

   /**
    * @return The current type for
    */
   public T getUpgrade();
}
