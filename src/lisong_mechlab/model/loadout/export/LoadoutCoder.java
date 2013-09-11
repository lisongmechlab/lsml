package lisong_mechlab.model.loadout.export;

import java.io.IOException;

import lisong_mechlab.model.loadout.Loadout;

/**
 * An interface that specifies a method for encoding and decoding a {@link Loadout} to a byte array.
 * 
 * @author Li Song
 */
public interface LoadoutCoder{
   /**
    * Encodes the given {@link Loadout} to a raw bit stream.
    * 
    * @param aLoadout
    *           The {@link Loadout} to encode.
    * @return A raw bit stream representing the {@link Loadout}.
    * @throws IOException
    *            If the bit stream couldn't be written.
    */
   public byte[] encode(Loadout aLoadout) throws IOException;

   /**
    * Decodes a given bit stream into a {@link Loadout}.
    * 
    * @param aBitStream
    *           The bit stream to decode.
    * @return A {@link Loadout} that has been decoded.
    * @throws IOException
    *            If the bit stream is broken.
    */
   public Loadout decode(byte[] aBitStream) throws IOException;
}
