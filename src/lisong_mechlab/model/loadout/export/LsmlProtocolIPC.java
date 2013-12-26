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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.SwingUtilities;

import lisong_mechlab.view.ProgramInit;

/**
 * Will listen on a local socket for messages to open up "lsml://" links
 * 
 * @author Emily Björk
 */
public class LsmlProtocolIPC implements Runnable{
   // In the private (ephemeral) ports
   private static final int   PORT = 63782;
   private final ServerSocket serverSocket;
   private final Thread       thread;
   private transient boolean  done = false;

   /**
    * Creates a new IPC server that can receive messages on the local loopback.
    * 
    * @throws UnknownHostException
    * @throws IOException
    */
   public LsmlProtocolIPC() throws IOException{
      serverSocket = new ServerSocket();
      serverSocket.setReuseAddress(true);
      serverSocket.bind(new InetSocketAddress(PORT));

      thread = new Thread(this);
      thread.setName("IPC THREAD");
      thread.start();
   }

   public void close(){
      done = true;
      if( null != serverSocket ){
         try{
            serverSocket.close(); // Will throw an SocketException in the server thread.
         }
         catch( IOException e ){
            System.err.println(e);
         }
      }

      if( thread != null ){
         thread.interrupt();
         try{
            thread.join();
         }
         catch( InterruptedException e ){
            System.err.println(e);
         }
      }
   }

   /**
    * @param url
    * @return <code>true</code> if the message was sent (some one listened to the socket) <code>false</code> if the
    *         message couldn't be sent.
    */
   static public boolean sendLoadout(String url) throws Exception{
      Socket socket = null;
      BufferedWriter bw = null;
      try{
         socket = new Socket((String)null, PORT);
         bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
         bw.write(url);
      }
      catch( IOException e ){
         return false;
      }
      finally{
         if( bw != null )
            bw.close();
         if( socket != null )
            socket.close();
      }
      return true;
   }

   @Override
   public void run(){
      while( !done ){
         Socket client = null;
         BufferedReader in = null;
         try{
            client = serverSocket.accept();
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            final String url = in.readLine();
            SwingUtilities.invokeLater(new Runnable(){
               @Override
               public void run(){
                  ProgramInit.lsml().mechLabPane.openLoadout(url);
               }
            });
         }
         catch( Exception e ){
            // Unknown error, probably some random program sending data to us.
         }
         finally{
            try{
               if( null != client )
                  client.close();
               if( null != in )
                  in.close();
            }
            catch( Exception e ){
               System.err.println(e);
            }
         }
      }
   }
}
