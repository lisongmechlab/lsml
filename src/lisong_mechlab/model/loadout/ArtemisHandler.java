package lisong_mechlab.model.loadout;

import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.MissileWeapon;

public class ArtemisHandler {
	
	private Loadout loadout;

	public ArtemisHandler(Loadout loadoutUnderTest) {
		loadout = loadoutUnderTest;
	}
	
	
	
	public boolean checkLoadoutStillValid(){
		
		for(LoadoutPart part : loadout.getPartLoadOuts()){
			if(!checkPartCanHoldArtemis(part)) {
				return false;
			}
		}
		return true;
	}



	private boolean checkPartCanHoldArtemis(LoadoutPart aPart) {
		int additionalCritSlots = 0;
		for(Item item : aPart.getItems()){
			if((item instanceof MissileWeapon) ){
				additionalCritSlots++;
			}

		}
		System.out.println(aPart.getNumCriticalSlotsFree());
		if(((aPart.getNumCriticalSlotsFree()) < additionalCritSlots) && additionalCritSlots != 0){
			return false;
		}
		return true;
		
	}

}
