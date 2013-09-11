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

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.Base64;
import lisong_mechlab.util.DecodingException;
import lisong_mechlab.view.LSML;

/**
 * Will listen on a local socket for messages to open up "lsml://" links
 * 
 * @author Emily Björk
 */
public class LsmlProtocolIPC{
   // In the private (ephemeral) ports
   private static final int   port = 63782;
   private final ServerSocket socket;
   private final Thread       thread;
   private transient boolean  done = false;

   public LsmlProtocolIPC() throws UnknownHostException, IOException{
      socket = new ServerSocket();
      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress(port));

      thread = new Thread(new Runnable(){
         @Override
         public void run(){
            while( !done ){
               Socket client = null;
               BufferedReader in = null;
               try{
                  client = socket.accept();
                  in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                  final String url = in.readLine();
                  SwingUtilities.invokeLater(new Runnable(){

                     @Override
                     public void run(){
                        try{
                           LSML.getInstance().getDesktop().openLoadout(ExternalLoadout.parse(url));
                        }
                        catch( IOException e ){
                           e.printStackTrace();
                        }
                     }

                  });

               }
               catch( Exception e ){
                  System.err.println(e);
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
      });
      thread.setName("IPC THREAD");
      thread.start();
   }

   /**
    * @param url
    * @return <code>true</code> if the message was sent (some one listened to the socket) <code>false</code> if the
    *         message couldn't be sent.
    * @throws UnknownHostException
    * @throws IOException
    */
   static public boolean sendLoadout(String url) throws UnknownHostException, IOException{
      Socket socket = null;
      BufferedWriter bw = null;
      try{
         socket = new Socket((String)null, port);
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

   void close(){
      done = true;
      if( thread != null ){
         thread.interrupt();
         try{
            thread.join();
         }
         catch( InterruptedException e ){
            System.err.println(e);
         }
      }
      if( null != socket ){
         try{
            socket.close();
         }
         catch( IOException e ){
            System.err.println(e);
         }
      }
   }
}
