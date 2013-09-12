package lisong_mechlab.util;

import java.io.IOException;

/**
 * This exception is thrown from various encoding process if they fail to encode their input data.
 * 
 * @author Emily Björk
 */
public class EncodingException extends IOException{
   private static final long serialVersionUID = -5553686746846136977L;

   public EncodingException(String aMessage){
      super(aMessage);
   }
}
