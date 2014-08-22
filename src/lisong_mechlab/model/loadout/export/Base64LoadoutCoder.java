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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.util.Base64;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.util.EncodingException;

/**
 * This class handles conversions of {@link LoadoutStandard}s to and from Base64 strings. It will correctly determine
 * which format the string is in and choose the right combination of decoders.
 * 
 * @author Emily Björk
 */
public class Base64LoadoutCoder{
   private static final String            LSML_PROTOCOL   = "lsml://";
   private static final String            LSML_TRAMPOLINE = "http://t.li-soft.org/?l=";
   private final transient LoadoutCoderV1 coderV1;
   private final transient LoadoutCoderV2 coderV2;
   private final transient LoadoutCoderV3 coderV3;
   private final transient LoadoutCoder   preferredEncoder;
   private final transient Base64         base64;

   public Base64LoadoutCoder(){
      coderV1 = new LoadoutCoderV1();
      coderV2 = new LoadoutCoderV2();
      coderV3 = new LoadoutCoderV3();
      preferredEncoder = coderV3;
      base64 = new Base64();
   }

   /**
    * Parses a Base64 {@link String} into a {@link LoadoutStandard}.
    * 
    * @param aUrl
    *           The string to parse.
    * @return A new {@link LoadoutStandard} object.
    * @throws DecodingException
    *            Thrown if decoding of the string failed.
    */
   public LoadoutBase<?> parse(String aUrl) throws DecodingException{
      String url = aUrl.trim();
      if( url.toLowerCase().startsWith(LSML_PROTOCOL) ){
         url = url.substring(LSML_PROTOCOL.length());
      }
      while( url.length() % 4 != 0 ){
         // Was the offending character a trailing backslash? Remove it
         if( url.endsWith("/") )
            url = url.substring(0, url.length() - 1);
         else{
            throw new DecodingException("The string [" + aUrl + "] is invalid!");
         }
      }

      byte[] bitstream = base64.decode(url.toCharArray());

      if( coderV1.canDecode(bitstream) ){
         return coderV1.decode(bitstream);
      }
      else if( coderV2.canDecode(bitstream) ){
         return coderV2.decode(bitstream);
      }
      else if( coderV3.canDecode(bitstream) ){
         return coderV3.decode(bitstream);
      }
      else{
         throw new DecodingException("No suitable decoder found to decode [" + aUrl + "] with!");
      }
   }

   /**
    * Will encode a given {@link LoadoutBase} into a LSML protocol {@link String}.
    * 
    * @param aLoadout
    *           The {@link LoadoutBase} to encode.
    * @return A {@link String} with a Base64 encoding of the {@link LoadoutStandard}.
    * @throws EncodingException
    *            Thrown if encoding failed for some reason. Shouldn't happen.
    */
   public String encodeLSML(LoadoutBase<?> aLoadout) throws EncodingException{
      return LSML_PROTOCOL + String.valueOf(base64.encode(preferredEncoder.encode(aLoadout)));
   }

   /**
    * Will encode a given {@link LoadoutBase} into a HTTP trampoline LSML protocol {@link String}.
    * 
    * @param aLoadout
    *           The {@link LoadoutBase} to encode.
    * @return A HTTP URI as a {@link String} with a Base64 encoding of the {@link LoadoutStandard}.
    * @throws EncodingException
    *            Thrown if encoding failed for some reason. Shouldn't happen.
    * @throws UnsupportedEncodingException
    */
   public String encodeHttpTrampoline(LoadoutBase<?> aLoadout) throws EncodingException, UnsupportedEncodingException{
      return LSML_TRAMPOLINE + URLEncoder.encode(String.valueOf(base64.encode(preferredEncoder.encode(aLoadout))), "UTF-8");
   }
}
