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
package org.lisoft.lsml.model.export;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.EncodingException;

/**
 * This class will perform a batch serialisation/deserialisation of a hierarchical structure of loadouts organised into
 * folders.
 *
 * @author Emily Björk
 */
public class BatchImportExporter {
    private final Base64LoadoutCoder coder;
    private final ErrorReporter errorCallback;
    private LsmlLinkProtocol protocol = LsmlLinkProtocol.LSML;
    private final Pattern loadoutPattern = Pattern.compile("\\{\\s*(.+?)\\s*\\}\\s*((?:lsml|http)\\S+)\\s*");

    /**
     * Creates a new exporter object.
     *
     * @param aCoder
     *            The {@link Base64LoadoutCoder} to use for encoding the loadouts.
     * @param aErrorReportingCallback
     *            A {@link ErrorReporter} to report errors to if they occur during the import.
     */
    @Inject
    public BatchImportExporter(Base64LoadoutCoder aCoder, ErrorReporter aErrorReportingCallback) {
        coder = aCoder;
        errorCallback = aErrorReportingCallback;
    }

    /**
     * Exports the given {@link GarageDirectory} and all its loadouts and sub directories to a human readable text.
     *
     * @param aRoot
     *            The {@link GarageDirectory} to export.
     * @return A String that can later be imported using {@link #parse(String)} to recreate the {@link GarageDirectory}
     *         passed as an argument.
     * @throws EncodingException
     *             Throw if encoding the loadout failed.
     */
    public String export(GarageDirectory<Loadout> aRoot) throws EncodingException {
        final StringBuilder sb = new StringBuilder();
        final String path = "";
        recurseAllDirs(sb, aRoot, path);
        return sb.toString();
    }

    /**
     * Reads the given string and produces a {@link GarageDirectory} matching the structure in the input data.
     *
     * @param aData
     *            The data to parse
     * @return A {@link GarageDirectory} with the parsed data.
     */
    public GarageDirectory<Loadout> parse(String aData) {
        final String lines[] = aData.split("\n");
        final GarageDirectory<Loadout> root = new GarageDirectory<>(""); // Implicit
                                                                         // root
        GarageDirectory<Loadout> currentDir = root;
        for (final String line : lines) {
            final Optional<String> dirName = parseDirectoryName(line);
            try {
                if (dirName.isPresent()) {
                    currentDir = root.makeDirsRecursive(dirName.get());
                }
                else {
                    final Matcher m = loadoutPattern.matcher(line);
                    if (m.matches()) {
                        final Loadout loadout = coder.parse(m.group(2));
                        loadout.setName(m.group(1));
                        currentDir.getValues().add(loadout);
                    }
                    else {
                        throw new IOException("Invalid format on line: " + line);
                    }
                }
            }
            catch (final Exception e) {
                errorCallback.error("Parse error", "Unable to parse line: " + line, e);
            }
        }
        return root;
    }

    /**
     * Changes the protocol used for encoding the loadouts during export.
     *
     * @param aProtocol
     *            The new protocol to use.
     */
    public void setProtocol(LsmlLinkProtocol aProtocol) {
        protocol = aProtocol;
    }

    private String encode(Loadout l) {
        if (protocol == LsmlLinkProtocol.HTTP) {
            return coder.encodeHTTPTrampoline(l);
        }
        else if (protocol == LsmlLinkProtocol.LSML) {
            return coder.encodeLSML(l);
        }
        else {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }
    }

    private Optional<String> parseDirectoryName(String aLine) {
        final String line = aLine.trim();
        if (line.startsWith("[") && line.endsWith("]")) {
            return Optional.of(line.substring(1, line.length() - 1));
        }
        return Optional.empty();
    }

    private void recurseAllDirs(StringBuilder aSB, GarageDirectory<Loadout> aRoot, String aParentPath)
            throws EncodingException {
        if (!aRoot.getValues().isEmpty()) {
            aSB.append('[').append(aParentPath).append(aRoot.getName()).append("]\n");
        }
        for (final Loadout l : aRoot.getValues()) {
            aSB.append("{").append(l.getName()).append("} ").append(encode(l)).append("\n");
        }
        for (final GarageDirectory<Loadout> directory : aRoot.getDirectories()) {
            recurseAllDirs(aSB, directory, aParentPath + aRoot.getName() + '/');
        }
    }

}
