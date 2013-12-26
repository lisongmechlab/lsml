/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */  
//@formatter:on
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
