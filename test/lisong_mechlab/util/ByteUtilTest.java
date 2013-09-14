package lisong_mechlab.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * A simple test suite to make sure the {@link ByteUtil}s are working as intended.
 * 
 * @author Emily Björk
 */
public class ByteUtilTest{

   @Test
   public void testLsr(){
      byte input = (byte)0xFF;
      int shift = 2;
      byte expected = 0x3F;

      assertEquals(expected, ByteUtil.lsr(input, shift));
   }

   @Test
   public void testLsl(){
      byte input = (byte)0xFF;
      int shift = 2;
      byte expected = (byte)0xFC;

      assertEquals(expected, ByteUtil.lsl(input, shift));
   }

}
