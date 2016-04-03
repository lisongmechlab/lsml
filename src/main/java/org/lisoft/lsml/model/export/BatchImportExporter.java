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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutBuilder.ErrorReportingCallback;
import org.lisoft.lsml.util.EncodingException;

/**
 * This class will perform a batch serialisation/deserialisation of a hierarchical structure of loadouts organised into
 * folders.
 * 
 * @author Li Song
 */
public class BatchImportExporter {
    private final Base64LoadoutCoder     coder;
    private final ErrorReportingCallback errorCallback;
    private LsmlLinkProtocol             protocol;
    private final Pattern                loadoutPattern = Pattern
            .compile("\\{\\s*(.+?)\\s*\\}\\s*((?:lsml|http)\\S+)\\s*");

    /**
     * Creates a new exporter object.
     * 
     * @param aCoder
     *            The {@link Base64LoadoutCoder} to use for encoding the loadouts.
     * @param aExportProtocol
     *            The {@link LsmlLinkProtocol} to use for the export. Import protocol is auto-detected.
     * @param aErrorReportingCallback
     *            A {@link ErrorReportingCallback} to report errors to if they occur during the import.
     */
    public BatchImportExporter(Base64LoadoutCoder aCoder, LsmlLinkProtocol aExportProtocol,
            ErrorReportingCallback aErrorReportingCallback) {
        coder = aCoder;
        protocol = aExportProtocol;
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
        StringBuilder sb = new StringBuilder();
        String path = "";
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
        String lines[] = aData.split("\n");
        GarageDirectory<Loadout> root = new GarageDirectory<>(""); // Implicit root
        GarageDirectory<Loadout> currentDir = root;

        List<Throwable> errors = new ArrayList<>();

        for (String line : lines) {
            Optional<String> dirName = parseDirectoryName(line);
            if (dirName.isPresent()) {
                currentDir = root.makeDirsRecursive(dirName.get());
            }
            else {
                try {

                    Matcher m = loadoutPattern.matcher(line);
                    if (m.matches()) {
                        Loadout loadout = coder.parse(m.group(2));
                        loadout.setName(m.group(1));
                        currentDir.getValues().add(loadout);
                    }
                    else {
                        throw new IOException("Invalid format on line: " + line);
                    }
                }
                catch (Exception e) {
                    errors.add(e);
                }
            }
        }
        if (!errors.isEmpty()) {
            errorCallback.report(Optional.empty(), errors);
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

    private String encode(Loadout l) throws EncodingException {
        if (protocol == LsmlLinkProtocol.HTTP) {
            return coder.encodeHttpTrampoline(l);
        }
        else if (protocol == LsmlLinkProtocol.LSML) {
            return coder.encodeLSML(l);
        }
        else {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }
    }

    private Optional<String> parseDirectoryName(String aLine) {
        String line = aLine.trim();
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
        for (Loadout l : aRoot.getValues()) {
            aSB.append("{").append(l.getName()).append("} ").append(encode(l)).append("\n");
        }
        for (GarageDirectory<Loadout> directory : aRoot.getDirectories()) {
            recurseAllDirs(aSB, directory, aParentPath + aRoot.getName() + '/');
        }
    }

}
