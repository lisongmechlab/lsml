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

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.UndoStack;
import lisong_mechlab.util.Base64;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.util.EncodingException;
import lisong_mechlab.util.MessageXBar;

/**
 * This class handles conversions of {@link Loadout}s to and from Base64 strings. It will correctly determine which
 * format the string is in and choose the right combination of decoders.
 * 
 * @author Emily Björk
 */
public class Base64LoadoutCoder{
   private static final String            LSML_PROTOCOL   = "lsml://";
   private static final String            LSML_TRAMPOLINE = "http://t.li-soft.org/?l=";
   private final transient LoadoutCoderV1 coderV1;
   private final transient Base64         base64;

   public Base64LoadoutCoder(MessageXBar anXBar, UndoStack anUndoStack){
      coderV1 = new LoadoutCoderV1(anXBar, anUndoStack);
      base64 = new Base64();
   }

   /**
    * Parses a Base64 {@link String} into a {@link Loadout}.
    * 
    * @param aUrl
    *           The string to parse.
    * @return A new {@link Loadout} object.
    * @throws DecodingException
    *            Thrown if decoding of the string failed.
    */
   public Loadout parse(String aUrl) throws DecodingException{
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
      return coderV1.decode(base64.decode(url.toCharArray()));
   }

   /**
    * Will encode a given {@link Loadout} into a LSML protocol {@link String}.
    * 
    * @param aLoadout
    *           The {@link Loadout} to encode.
    * @return A {@link String} with a Base64 encoding of the {@link Loadout}.
    * @throws EncodingException
    *            Thrown if encoding failed for some reason. Shouldn't happen.
    */
   public String encodeLSML(Loadout aLoadout) throws EncodingException{
      return LSML_PROTOCOL + String.valueOf(base64.encode(coderV1.encode(aLoadout)));
   }

   /**
    * Will encode a given {@link Loadout} into a HTTP trampoline LSML protocol {@link String}.
    * 
    * @param aLoadout
    *           The {@link Loadout} to encode.
    * @return A HTTP URI as a {@link String} with a Base64 encoding of the {@link Loadout}.
    * @throws EncodingException
    *            Thrown if encoding failed for some reason. Shouldn't happen.
    * @throws UnsupportedEncodingException
    */
   public String encodeHttpTrampoline(Loadout aLoadout) throws EncodingException, UnsupportedEncodingException{
      return LSML_TRAMPOLINE + URLEncoder.encode(String.valueOf(base64.encode(coderV1.encode(aLoadout))), "UTF-8");
   }
}
