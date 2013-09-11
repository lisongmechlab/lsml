package lisong_mechlab.model.loadout;

import static org.junit.Assert.*;
import lisong_mechlab.model.loadout.Upgrades.ChangeMsg;
import lisong_mechlab.util.MessageXBar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

public class UpgradesTest{
   @Mock
   MessageXBar xbar;
   Upgrades    cut;
   
   @Before
   public void setup(){
      MockitoAnnotations.initMocks(this);
      cut = new Upgrades(xbar);
   }
   
   @Test
   public void testInitialState(){
      assertFalse(cut.hasArtemis());
      assertFalse(cut.hasDoubleHeatSinks());
      assertFalse(cut.hasEndoSteel());
      assertFalse(cut.hasFerroFibrous());
   }
   
   @Test
   public void testDHS_disable_disabled(){  
      // Execute
      cut.setDoubleHeatSinks(false);
      
      // Verify
      verifyZeroInteractions(xbar);
      assertFalse(cut.hasDoubleHeatSinks());
   }
   
   @Test
   public void testArtemis_disable_disabled(){  
      // Execute
      cut.setArtemis(false);
      
      // Verify
      verifyZeroInteractions(xbar);
      assertFalse(cut.hasArtemis());
   }
   
   @Test
   public void testFF_disable_disabled(){  
      // Execute
      cut.setFerroFibrous(false);
      
      // Verify
      verifyZeroInteractions(xbar);
      assertFalse(cut.hasFerroFibrous());
   }
   
   @Test
   public void testES_disable_disabled(){  
      // Execute
      cut.setEndoSteel(false);
      
      // Verify
      verifyZeroInteractions(xbar);
      assertFalse(cut.hasEndoSteel());
   }
   
   //--------------
   @Test
   public void testDHS_enable_disabled(){  
      // Execute
      cut.setDoubleHeatSinks(true);
      
      // Verify
      verify(xbar, times(1)).post(new Upgrades.Message(ChangeMsg.HEATSINKS, cut));
      assertTrue(cut.hasDoubleHeatSinks());
   }
   
   @Test
   public void testArtemis_enable_disabled(){  
      // Execute
      cut.setArtemis(true);
      
      // Verify
      verify(xbar, times(1)).post(new Upgrades.Message(ChangeMsg.GUIDANCE, cut));
      assertTrue(cut.hasArtemis());
   }
   
   @Test
   public void testFF_enable_disabled(){  
      // Execute
      cut.setFerroFibrous(true);
      
      // Verify
      verify(xbar, times(1)).post(new Upgrades.Message(ChangeMsg.ARMOR, cut));
      assertTrue(cut.hasFerroFibrous());
   }
   
   @Test
   public void testES_enable_disabled(){  
      // Execute
      cut.setEndoSteel(true);
      
      // Verify
      verify(xbar, times(1)).post(new Upgrades.Message(ChangeMsg.STRUCTURE, cut));
      assertTrue(cut.hasEndoSteel());
   }
   
   //--------------
   @Test
   public void testDHS_enable_enabled(){  
      // Setup
      cut.setDoubleHeatSinks(true);
      reset(xbar);
      
      // Execute
      cut.setDoubleHeatSinks(true);
      
      // Verify
      verifyZeroInteractions(xbar);
      assertTrue(cut.hasDoubleHeatSinks());
   }
   
   @Test
   public void testArtemis_enable_enabled(){  
      // Setup
      cut.setArtemis(true);
      reset(xbar);
      
      // Execute
      cut.setArtemis(true);
      
      // Verify
      verifyZeroInteractions(xbar);
      assertTrue(cut.hasArtemis());
   }
   
   @Test
   public void testFF_enable_enabled(){  
      // Setup
      cut.setFerroFibrous(true);
      reset(xbar);
      
      // Execute
      cut.setFerroFibrous(true);
      
      // Verify
      verifyZeroInteractions(xbar);
      assertTrue(cut.hasFerroFibrous());
   }
   
   @Test
   public void testES_enable_enabled(){  
      // Setup
      cut.setEndoSteel(true);
      reset(xbar);      
      
      // Execute
      cut.setEndoSteel(true);
      
      // Verify
      verifyZeroInteractions(xbar);
      assertTrue(cut.hasEndoSteel());
   }
}
