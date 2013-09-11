package lisong_mechlab.model.loadout;

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
    */
   public byte[] encode(Loadout aLoadout);

   /**
    * Decodes a given bit stream into a {@link Loadout}.
    * 
    * @param aBitStream
    *           The bit stream to decode.
    * @return A {@link Loadout} that has been decoded.
    * @throws IllegalArgumentException
    *            If the bit stream is broken.
    */
   public Loadout decode(byte[] aBitStream) throws IllegalArgumentException;
}
