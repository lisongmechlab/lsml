package lisong_mechlab.model.loadout.export;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.util.EncodingException;

/**
 * An interface that specifies a method for encoding and decoding a {@link Loadout} to a byte array.
 * 
 * @author Emily Björk
 */
public interface LoadoutCoder{
   /**
    * Encodes the given {@link Loadout} to a raw bit stream.
    * 
    * @param aLoadout
    *           The {@link Loadout} to encode.
    * @return A raw bit stream representing the {@link Loadout}.
    * @throws EncodingException
    *            If the bit stream couldn't be written.
    */
   public byte[] encode(Loadout aLoadout) throws EncodingException;

   /**
    * Decodes a given bit stream into a {@link Loadout}.
    * 
    * @param aBitStream
    *           The bit stream to decode.
    * @return A {@link Loadout} that has been decoded.
    * @throws DecodingException
    *            If the bit stream is broken.
    */
   public Loadout decode(byte[] aBitStream) throws DecodingException;

   /**
    * Determines if this {@link LoadoutCoder} is capable of decoding the given bit stream. Usually implemented by
    * checking headers of the stream.
    * 
    * @param aBitStream
    *           The stream to test for.
    * @return Returns <code>true</code> if this coder is able to decode the stream, <code>false</code> otherwise.
    */
   public boolean canDecode(byte[] aBitStream);
}
