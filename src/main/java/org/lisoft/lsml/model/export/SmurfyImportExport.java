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

import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;

import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles data exchange with smurfy's website.
 *
 * @author Li Song
 */
public class SmurfyImportExport {
    public final static String CREATE_API_KEY_URL = "https://mwo.smurfy-net.de/change-password";
    private final static int API_NUM_CHARS = 40;
    private final static String API_VALID_CHARS = "0123456789abcdefABCDEF";
    private final Base64LoadoutCoder coder;
    private final ErrorReporter errorReporter;
    private final SSLSocketFactory sslSocketFactory;
    private final URL userMechbayUrl;
    private final String version;

    /**
     * @param aCoder            A {@link Base64LoadoutCoder} to use for encoding and decoding {@link LoadoutStandard}s.
     * @param aErrorReporter    A callback to call for reporting errors in the import/export process to the user.
     * @param aSslSocketFactory The socket factory to use for creating the secure sockets.
     * @param aVersion          The LSML version to send in the User Agent string of the HTTP request to Smurfy.
     */
    public SmurfyImportExport(Base64LoadoutCoder aCoder, ErrorReporter aErrorReporter,
                              SSLSocketFactory aSslSocketFactory, String aVersion) {
        errorReporter = aErrorReporter;
        try (InputStream keyStoreStream = ClassLoader.getSystemClassLoader().getResourceAsStream("lsml.jks")) {
            userMechbayUrl = new URL("https://mwo.smurfy-net.de/api/data/user/mechbay.xml");
            sslSocketFactory = aSslSocketFactory;
        } catch (final Exception e) {
            // Any exception thrown here is a bug, promote MalformedURLException
            // to RuntimeException.
            throw new RuntimeException(e);
        }
        coder = aCoder;
        version = aVersion;
    }

    /**
     * @param aCoder         A {@link Base64LoadoutCoder} to use for encoding and decoding {@link LoadoutStandard}s.
     * @param aErrorReporter A callback to call for reporting errors in the import/export process to the user.
     * @param aVersion       The LSML version to send in the User Agent string of the HTTP request to Smurfy.
     */
    @Inject
    public SmurfyImportExport(Base64LoadoutCoder aCoder, ErrorReporter aErrorReporter,
                              @Named("version") String aVersion) {
        this(aCoder, aErrorReporter, createSocketFactory(), aVersion);
    }

    /**
     * Checks if the given API key is a valid key.
     *
     * @param aApiKey The API key to test.
     * @return <code>true</code> if the key is a valid key, false otherwise.
     */
    public static boolean isValidApiKey(String aApiKey) {
        if (aApiKey.length() != API_NUM_CHARS) {
            return false;
        }
        int offset = 0;
        final int len = aApiKey.length();
        while (offset < len) {
            final int c = aApiKey.codePointAt(offset);
            offset += Character.charCount(c);

            if (-1 == API_VALID_CHARS.indexOf(c)) {
                return false;
            }
        }
        return true;
    }

    public List<Loadout> listMechBay(String aApiKey) throws IOException {
        final List<Loadout> ans = new ArrayList<>();
        final String apiKey = aApiKey.toLowerCase(Locale.ENGLISH); // It's case
        // sensitive

        final HttpURLConnection connection = connect(userMechbayUrl);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "text/html;charset=UTF-8");
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Authorization", "APIKEY " + apiKey);

        try (InputStream is = connection.getInputStream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader in = new BufferedReader(isr)) {
            String line;
            final Pattern namePattern = Pattern.compile("\\s*<name>.*CDATA\\[([^\\]]+).*");
            final Pattern lsmlPattern = Pattern.compile("\\s*<lsml>.*CDATA\\[lsml://([^\\]]+).*");

            int lines = 0;
            String name = null;
            while (null != (line = in.readLine())) {
                // System.out.println(line);
                final Matcher nameMatcher = namePattern.matcher(line);
                final Matcher lsmlMatcher = lsmlPattern.matcher(line);
                lines++;
                if (nameMatcher.matches() && name == null) {
                    name = nameMatcher.group(1);
                }

                if (lsmlMatcher.matches()) {
                    if (name == null) {
                        throw new IOException("Parse error, expected <name> tag before <lsml> tag!");
                    }
                    final String lsml = lsmlMatcher.group(1);
                    try {
                        final Loadout loadout = coder.parse(lsml);
                        loadout.setName(name);
                        ans.add(loadout);
                    } catch (final Exception e) {
                        errorReporter.error("Smurfy Import Error", "Failure while reading line: " + line, e);
                    }

                    name = null;
                }
            }

            if (ans.isEmpty()) {
                if (lines > 3) {
                    throw new IOException("No data! LSML link generation is disabled on mwo.smurfy-net.de.");
                } else if (lines > 0) {
                    throw new IOException("Mechbay contained no loadouts.");
                }
                throw new IOException("No data received from mwo.smurfy-net.de.");
            }
        } catch (final IOException e) {
            if (401 == connection.getResponseCode()) {
                throw new AccessDeniedException("");
            }
            throw e;
        }
        return ans;
    }

    public String sendLoadout(Loadout aLoadout) throws IOException {
        final int mechId = aLoadout.getChassis().getId();
        final URL loadoutUploadUrlXml = new URL("https://mwo.smurfy-net.de/api/data/mechs/" + mechId + "/loadouts.xml");

        final String data = SmurfyXML.toXml(aLoadout);
        final byte[] rawData = data.getBytes(StandardCharsets.UTF_8);

        final HttpURLConnection connection = connect(loadoutUploadUrlXml);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "LSML/" + version);
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestProperty("Content-Length", String.valueOf(rawData.length));

        connection.setDoOutput(true);

        try (OutputStream wr = connection.getOutputStream()) {
            wr.write(rawData);
        }

        try (InputStream is = connection.getInputStream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader rd = new BufferedReader(isr)) {
            final Pattern pattern = Pattern.compile(
                    ".*mwo.smurfy-net.de/api/data/mechs/" + mechId + "/loadouts/([^.]{40})\\..*");
            for (String line = rd.readLine(); line != null; line = rd.readLine()) {
                final Matcher m = pattern.matcher(line);
                if (m.matches()) {
                    final String sha = m.group(1);
                    if (sha != null && sha.length() == 40) {
                        return "http://mwo.smurfy-net.de/mechlab#i=" + mechId + "&l=" + sha;
                    }
                }
            }
        }
        throw new IOException("Unable to determine uploaded URL... oops!");
    }

    private static SSLSocketFactory createSocketFactory() {
        try (InputStream keyStoreStream = ClassLoader.getSystemClassLoader().getResourceAsStream("lsml.jks")) {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreStream, "E6JhYSizAnzEyFSEaD5m".toCharArray());
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            final SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tmf.getTrustManagers(), null);
            return ctx.getSocketFactory();
        } catch (final Exception e) {
            // Any exception thrown here is a bug, promote MalformedURLException
            // to RuntimeException.
            throw new RuntimeException(e);
        }
    }

    private HttpURLConnection connect(URL aUrl) throws IOException {
        final HttpsURLConnection connection = (HttpsURLConnection) aUrl.openConnection();
        connection.setSSLSocketFactory(sslSocketFactory);
        return connection;
    }
}
