package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DynamicSlotDistributorTest{
   @Mock
   Loadout                loadout;

   @Mock
   InternalPart           ira;
   @Mock
   InternalPart           irt;
   @Mock
   InternalPart           irl;
   @Mock
   InternalPart           ihd;
   @Mock
   InternalPart           ict;
   @Mock
   InternalPart           ilt;
   @Mock
   InternalPart           ill;
   @Mock
   InternalPart           ila;

   @Mock
   LoadoutPart            ra;
   @Mock
   LoadoutPart            rt;
   @Mock
   LoadoutPart            rl;
   @Mock
   LoadoutPart            hd;
   @Mock
   LoadoutPart            ct;
   @Mock
   LoadoutPart            lt;
   @Mock
   LoadoutPart            ll;
   @Mock
   LoadoutPart            la;
   @Mock
   Upgrades               upgrades;

   List<LoadoutPart>      priorityOrder;

   @InjectMocks
   DynamicSlotDistributor cut;

   @Before
   public void setup(){
      // Priority order: RA, RT, RL, HD, CT, LT, LL, LA
      priorityOrder = Arrays.asList(ra, rt, rl, hd, ct, lt, ll, la);

      when(ira.getType()).thenReturn(Part.RightArm);
      when(irt.getType()).thenReturn(Part.RightTorso);
      when(irl.getType()).thenReturn(Part.RightLeg);
      when(ihd.getType()).thenReturn(Part.Head);
      when(ict.getType()).thenReturn(Part.CenterTorso);
      when(ilt.getType()).thenReturn(Part.LeftTorso);
      when(ill.getType()).thenReturn(Part.LeftLeg);
      when(ila.getType()).thenReturn(Part.LeftArm);

      when(ra.getInternalPart()).thenReturn(ira);
      when(rt.getInternalPart()).thenReturn(irt);
      when(rl.getInternalPart()).thenReturn(irl);
      when(hd.getInternalPart()).thenReturn(ihd);
      when(ct.getInternalPart()).thenReturn(ict);
      when(lt.getInternalPart()).thenReturn(ilt);
      when(ll.getInternalPart()).thenReturn(ill);
      when(la.getInternalPart()).thenReturn(ila);

      when(ra.toString()).thenReturn("RA");
      when(rt.toString()).thenReturn("RT");
      when(rl.toString()).thenReturn("RL");
      when(hd.toString()).thenReturn("HD");
      when(ct.toString()).thenReturn("CT");
      when(lt.toString()).thenReturn("LT");
      when(ll.toString()).thenReturn("LL");
      when(la.toString()).thenReturn("LA");
      when(loadout.getPart(Part.RightArm)).thenReturn(ra);
      when(loadout.getPart(Part.RightTorso)).thenReturn(rt);
      when(loadout.getPart(Part.RightLeg)).thenReturn(rl);
      when(loadout.getPart(Part.Head)).thenReturn(hd);
      when(loadout.getPart(Part.CenterTorso)).thenReturn(ct);
      when(loadout.getPart(Part.LeftTorso)).thenReturn(lt);
      when(loadout.getPart(Part.LeftLeg)).thenReturn(ll);
      when(loadout.getPart(Part.LeftArm)).thenReturn(la);
      when(loadout.getPartLoadOuts()).thenReturn(Arrays.asList(ra, rt, rl, hd, ct, lt, ll, la));
      when(loadout.getUpgrades()).thenReturn(upgrades);
   }

   @Test
   public void testGetDynamicStructureSlotsForComponent_NoUpgrades(){
      when(upgrades.hasEndoSteel()).thenReturn(false);
      when(upgrades.hasFerroFibrous()).thenReturn(false);

      when(ra.getNumCriticalSlotsFree()).thenReturn(12);
      when(rt.getNumCriticalSlotsFree()).thenReturn(12);
      when(rl.getNumCriticalSlotsFree()).thenReturn(12);
      when(hd.getNumCriticalSlotsFree()).thenReturn(12);
      when(ct.getNumCriticalSlotsFree()).thenReturn(12);
      when(ll.getNumCriticalSlotsFree()).thenReturn(12);
      when(lt.getNumCriticalSlotsFree()).thenReturn(12);
      when(la.getNumCriticalSlotsFree()).thenReturn(12);

      assertEquals(0, cut.getDynamicStructureSlots(ra));
      assertEquals(0, cut.getDynamicStructureSlots(rt));
      assertEquals(0, cut.getDynamicStructureSlots(rl));
      assertEquals(0, cut.getDynamicStructureSlots(hd));
      assertEquals(0, cut.getDynamicStructureSlots(ct));
      assertEquals(0, cut.getDynamicStructureSlots(lt));
      assertEquals(0, cut.getDynamicStructureSlots(ll));
      assertEquals(0, cut.getDynamicStructureSlots(la));

      assertEquals(0, cut.getDynamicArmorSlots(ra));
      assertEquals(0, cut.getDynamicArmorSlots(rt));
      assertEquals(0, cut.getDynamicArmorSlots(rl));
      assertEquals(0, cut.getDynamicArmorSlots(hd));
      assertEquals(0, cut.getDynamicArmorSlots(ct));
      assertEquals(0, cut.getDynamicArmorSlots(lt));
      assertEquals(0, cut.getDynamicArmorSlots(ll));
      assertEquals(0, cut.getDynamicArmorSlots(la));
   }

   /**
    * Calculates the cumulative number of slots that are free up until the argument according to the priority order of
    * components.
    * 
    * @param aPart
    * @return
    */
   private int cumSlotsFree(LoadoutPart aPart){
      int i = priorityOrder.indexOf(aPart);
      int sum = 0;
      while( i > 0 ){
         i--;
         sum += priorityOrder.get(i).getNumCriticalSlotsFree();
      }
      return sum;
   }

   /**
    * Calculates the number of cumulative slots that are occupied by dynamic slots given the maximum number of dynamic
    * slots that can be distributed.
    * 
    * @param aPart
    * @param slotsTotal
    * @return
    */
   private int slotsOccupied(LoadoutPart aPart, int slotsTotal){
      return Math.min(aPart.getNumCriticalSlotsFree(), Math.max(0, slotsTotal - cumSlotsFree(aPart)));
   }

   private String expectedStructure(int slotsTotal){
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      for(LoadoutPart part : priorityOrder){
         sb.append(part).append(" = ");
         sb.append(slotsOccupied(part, slotsTotal));
         sb.append(", ");
      }
      sb.append("}");
      return sb.toString();
   }

   @Test
   public void testGetDynamicStructureSlotsForComponent_Priority(){
      when(upgrades.hasEndoSteel()).thenReturn(true);
      when(upgrades.hasFerroFibrous()).thenReturn(false);

      when(ra.getNumCriticalSlotsFree()).thenReturn(12);
      when(rt.getNumCriticalSlotsFree()).thenReturn(12);
      when(rl.getNumCriticalSlotsFree()).thenReturn(12);
      when(hd.getNumCriticalSlotsFree()).thenReturn(12);
      when(ct.getNumCriticalSlotsFree()).thenReturn(12);
      when(ll.getNumCriticalSlotsFree()).thenReturn(12);
      when(lt.getNumCriticalSlotsFree()).thenReturn(12);
      when(la.getNumCriticalSlotsFree()).thenReturn(12);

      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(ra.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(rt.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(rl.getNumCriticalSlotsFree()).thenReturn(2);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(hd.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(ct.getNumCriticalSlotsFree()).thenReturn(3);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(lt.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(ll.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }

      when(la.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicStructureSlots(part));
      }
      // Slot overflow, fail graciously, no exceptions thrown
   }

   @Test
   public void testGetDynamicArmorSlotsForComponent_Priority(){
      when(upgrades.hasEndoSteel()).thenReturn(false);
      when(upgrades.hasFerroFibrous()).thenReturn(true);

      when(ra.getNumCriticalSlotsFree()).thenReturn(12);
      when(rt.getNumCriticalSlotsFree()).thenReturn(12);
      when(rl.getNumCriticalSlotsFree()).thenReturn(12);
      when(hd.getNumCriticalSlotsFree()).thenReturn(12);
      when(ct.getNumCriticalSlotsFree()).thenReturn(12);
      when(ll.getNumCriticalSlotsFree()).thenReturn(12);
      when(lt.getNumCriticalSlotsFree()).thenReturn(12);
      when(la.getNumCriticalSlotsFree()).thenReturn(12);

      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(ra.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(rt.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(rl.getNumCriticalSlotsFree()).thenReturn(2);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(hd.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(ct.getNumCriticalSlotsFree()).thenReturn(3);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(lt.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(ll.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }

      when(la.getNumCriticalSlotsFree()).thenReturn(1);
      for(LoadoutPart part : priorityOrder){
         assertEquals(expectedStructure(14), slotsOccupied(part, 14), cut.getDynamicArmorSlots(part));
      }
      // Slot overflow, fail graciously, no exceptions thrown
   }

   /**
    * Dynamic armor slots are distributed before dynamic structure (arbitrary design decision).
    */
   @Test
   public void testMixedArmorStructurePriority(){
      when(upgrades.hasEndoSteel()).thenReturn(true);
      when(upgrades.hasFerroFibrous()).thenReturn(true);

      when(ra.getNumCriticalSlotsFree()).thenReturn(4); // 4 armor
      when(rt.getNumCriticalSlotsFree()).thenReturn(4);
      when(rl.getNumCriticalSlotsFree()).thenReturn(4);
      when(hd.getNumCriticalSlotsFree()).thenReturn(4); // 2 armor 2 structure
      when(ct.getNumCriticalSlotsFree()).thenReturn(2);
      when(lt.getNumCriticalSlotsFree()).thenReturn(4);
      when(ll.getNumCriticalSlotsFree()).thenReturn(7); // 6 structure
      when(la.getNumCriticalSlotsFree()).thenReturn(1); // 0 structure

      assertEquals(0, cut.getDynamicStructureSlots(ra));
      assertEquals(4, cut.getDynamicArmorSlots(ra));

      assertEquals(0, cut.getDynamicStructureSlots(rt));
      assertEquals(4, cut.getDynamicArmorSlots(rt));

      assertEquals(0, cut.getDynamicStructureSlots(rl));
      assertEquals(4, cut.getDynamicArmorSlots(rl));

      assertEquals(2, cut.getDynamicStructureSlots(hd));
      assertEquals(2, cut.getDynamicArmorSlots(hd));

      assertEquals(2, cut.getDynamicStructureSlots(ct));
      assertEquals(0, cut.getDynamicArmorSlots(ct));

      assertEquals(4, cut.getDynamicStructureSlots(lt));
      assertEquals(0, cut.getDynamicArmorSlots(lt));

      assertEquals(6, cut.getDynamicStructureSlots(ll));
      assertEquals(0, cut.getDynamicArmorSlots(ll));

      assertEquals(0, cut.getDynamicStructureSlots(la));
      assertEquals(0, cut.getDynamicArmorSlots(la));
   }
}
