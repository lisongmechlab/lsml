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
package org.lisoft.lsml.model.export;

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
import java.util.Random;

import javax.inject.Named;

import org.lisoft.lsml.messages.ApplicationMessage;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view_fx.ErrorReporter;

/**
 * Will listen on a local socket for messages to open up "lsml://" links
 *
 * @author Li Song
 */
public class LsmlProtocolIPC implements Runnable {
	private static final String CHARSET_NAME = "UTF-8";
	// In the private (ephemeral) ports
	public static final int DEFAULT_PORT = 63782;

	public static final int MIN_PORT = 1025;
	public static final int MAX_PORT = 65000;

	public static int randomPort(Random aRng) {
		return aRng.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;
	}

	/**
	 * @param aLsmlUrl
	 *            The LSML URL to send.
	 * @param aPort
	 *            The port to send on.
	 * @return <code>true</code> if the message was sent (some one listened to
	 *         the socket) <code>false</code> if the message couldn't be sent.
	 */
	static public boolean sendLoadout(String aLsmlUrl, int aPort) {
		try (Socket socket = new Socket(InetAddress.getLocalHost(), aPort);
				Writer writer = new OutputStreamWriter(socket.getOutputStream(), CHARSET_NAME);
				BufferedWriter bw = new BufferedWriter(writer)) {
			bw.write(aLsmlUrl);
		} catch (final IOException e) {
			return false;
		}
		return true;
	}

	private final ServerSocket serverSocket;
	private final Thread thread;
	private final MessageXBar xBar;

	private boolean done = false;
	private final Base64LoadoutCoder coder;
	private final ErrorReporter errorReporter;

	/**
	 * Creates a new IPC server that can receive messages on the local loopback.
	 *
	 * @param aPort
	 *            The port to listen to.
	 * @param aOpenLoadoutCallback
	 *            A callback to call when a new {@link Loadout} is received.
	 * @param aErrorRerporter
	 *            An {@link ErrorReporter} to report errors to.
	 * @throws IOException
	 *             if the socket couldn't be opened.
	 */
	public LsmlProtocolIPC(int aPort, @Named("global") MessageXBar aXBar, Base64LoadoutCoder aCoder,
			ErrorReporter aErrorRerporter) throws IOException {
		serverSocket = new ServerSocket();
		serverSocket.setReuseAddress(true);
		serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), aPort));

		thread = new Thread(this);
		thread.setName("IPC THREAD");
		xBar = aXBar;
		coder = aCoder;
		errorReporter = aErrorRerporter;
	}

	public void close() {
		done = true;
		if (null != serverSocket) {
			try {
				serverSocket.close(); // Will throw an SocketException in the
										// server thread.
			} catch (final IOException e) {
				errorReporter.error("Unable to close local socket", "The local socket for IPC couldn't be closed.", e);
			}
		}

		if (thread != null) {
			thread.interrupt();
			try {
				thread.join();
			} catch (final InterruptedException e) {
				errorReporter.error("Unable to join IPC thread", "The thread running the IPC couldn't be closed.", e);
			}
		}
	}

	@Override
	public void run() {
		while (!done) {
			try (Socket client = serverSocket.accept();
					Reader reader = new InputStreamReader(client.getInputStream(), CHARSET_NAME);
					BufferedReader in = new BufferedReader(reader)) {
				final String url = in.readLine();
				try {
					xBar.post(new ApplicationMessage(coder.parse(url), ApplicationMessage.Type.OPEN_LOADOUT, null));
				} catch (final Exception e) {
					errorReporter.error("Unable to open loadout", "LSML failed to parse/open: " + url, e);
				}
			} catch (final Exception e) {
				// Quietly ignore bad data on the socket.
			}
		}
	}

	/**
	 * Starts the server listener thread.
	 */
	public void startServer() {
		thread.start();
	}
}
