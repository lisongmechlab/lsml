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
package lisong_mechlab.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlReader{
   private Document doc;

   public XmlReader(InputStream aFile) throws ParserConfigurationException, SAXException, IOException{
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

      doc = dBuilder.parse(aFile);
      doc.getDocumentElement().normalize();

      // final String root = doc.getDocumentElement().getNodeName();
      // if( !root.equals("lsml") ){
      // throw new IllegalArgumentException("Not an LiSongMechLab File!");
      // }
   }

   public List<Element> getElementsByTagName(String aTagName){
      List<Element> ans = new ArrayList<Element>();
      NodeList items = doc.getElementsByTagName(aTagName);
      for(int i = 0; i < items.getLength(); i++){
         Node node = items.item(i);
         if( node.getNodeType() == Node.ELEMENT_NODE ){
            ans.add((Element)items.item(i));
         }
      }
      return ans;
   }

   public Element getElementByTagName(String aTagName, Element aNode){
      List<Element> ans = getElementsByTagName(aTagName, aNode);
      if( ans.size() != 1 ){
         throw new IllegalArgumentException("Exepcted one (1) element with tag: <" + aTagName + ">!"); // TODO: Throw
                                                                                                       // something else
      }
      return ans.get(0);
   }

   public List<Element> getElementsByTagName(String aTagName, Element aNode){
      List<Element> ans = new ArrayList<Element>();
      NodeList items = aNode.getElementsByTagName(aTagName);
      for(int i = 0; i < items.getLength(); i++){
         Node node = items.item(i);
         if( node.getNodeType() == Node.ELEMENT_NODE ){
            ans.add((Element)items.item(i));
         }
      }
      return ans;
   }

   public Element getElementByTagName(String aTagName){
      List<Element> ans = getElementsByTagName(aTagName);
      if( ans.size() != 1 ){
         throw new IllegalArgumentException("Exepcted one (1) element with tag: <" + aTagName + ">!"); // TODO: Throw
                                                                                                       // something else
      }
      return ans.get(0);
   }

   public String getTagValue(String aTagName, Element anElement){
      List<String> tags = getTagValues(aTagName, anElement);

      if( tags.size() != 1 ){
         throw new IllegalArgumentException("Expected 1 tag named <" + aTagName + "> for <" + anElement.getNodeName() + ">, found: " + tags.size());
      }
      return tags.get(0);
   }

   public List<String> getTagValues(String aTagName, Element anElement){
      Node child = anElement.getFirstChild();
      List<String> tags = new ArrayList<String>();
      while( null != child ){
         if( child.getNodeType() == Node.ELEMENT_NODE ){
            Element el = (Element)child;
            if( aTagName.equals(el.getNodeName()) ){
               tags.add(el.getChildNodes().item(0).getNodeValue());
            }
         }
         child = child.getNextSibling();
      }
      return tags;
   }
}
