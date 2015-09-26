/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
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
package org.lisoft.lsml.parsing.export;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.SwingUtilities;

import org.lisoft.lsml.view.ProgramInit;

/**
 * Will listen on a local socket for messages to open up "lsml://" links
 * 
 * @author Li Song
 */
public class LsmlProtocolIPC implements Runnable {
    // In the private (ephemeral) ports
    public static final int    DEFAULT_PORT = 63782;
    private final ServerSocket serverSocket;
    private final Thread       thread;
    private transient boolean  done         = false;

    /**
     * Creates a new IPC server that can receive messages on the local loopback.
     * 
     * @param aPort
     *            The port to listen to.
     * 
     * @throws UnknownHostException
     * @throws IOException
     */
    public LsmlProtocolIPC(int aPort) throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), aPort));

        thread = new Thread(this);
        thread.setName("IPC THREAD");
        thread.start();
    }

    public void close() {
        done = true;
        if (null != serverSocket) {
            try {
                serverSocket.close(); // Will throw an SocketException in the
                                      // server thread.
            }
            catch (IOException e) {
                System.err.println(e);
            }
        }

        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            }
            catch (InterruptedException e) {
                System.err.println(e);
            }
        }
    }

    /**
     * @param aLsmlUrl
     *            The LSML URL to send.
     * @param aPort
     *            The port to send on.
     * @return <code>true</code> if the message was sent (some one listened to the socket) <code>false</code> if the
     *         message couldn't be sent.
     */
    static public boolean sendLoadout(String aLsmlUrl, int aPort) {
        try (Socket socket = new Socket(InetAddress.getLocalHost(), aPort);
                Writer writer = new OutputStreamWriter(socket.getOutputStream());
                BufferedWriter bw = new BufferedWriter(writer)) {
            bw.write(aLsmlUrl);
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        while (!done) {
            try (Socket client = serverSocket.accept();
                    Reader reader = new InputStreamReader(client.getInputStream());
                    BufferedReader in = new BufferedReader(reader)) {
                final String url = in.readLine();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ProgramInit.lsml().mechLabPane.openLoadout(url);
                    }
                });
            }
            catch (Exception e) {
                // Unknown error, probably some random program sending data to
                // us.
            }
        }
    }
}
