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
package org.lisoft.lsml.view_fx;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class will connect to the github repository and see if there is an update.
 * 
 * @author Li Song
 */
public class UpdateChecker {
    public static final String GITHUB_RELEASES_ADDRESS = "https://api.github.com/repos/lisongmechlab/lsml/releases";
    private Thread             thread;

    static class ReleaseData {
        String        html_url;
        String        name;
        boolean       draft;
        boolean       prerelease;
        public String tag_name;
    }

    @FunctionalInterface
    public static interface UpdateCallback {
        void run(ReleaseData aReleaseData);
    }

    /**
     * Creates a new {@link UpdateChecker} that will check for updates in the background and call the callback when it
     * is done.
     * 
     * @param aUrl
     *            The URL to check for the releases file.
     * 
     * @param aCurrentVersion
     *            The current version string.
     * @param aCallback
     *            The {@link UpdateCallback} to call.
     * @param aAcceptBeta
     *            <code>true</code> if beta releases should be considered as an update.
     */
    public UpdateChecker(final URL aUrl, final String aCurrentVersion, final UpdateCallback aCallback,
            final boolean aAcceptBeta) {

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                ReleaseData update = null;
                try {
                    URLConnection uc = aUrl.openConnection();
                    uc.setRequestProperty("Accept", "application/vnd.github.v3+json");
                    uc.setRequestProperty("User-Agent", "Li-Song-Mechlab-LSML");

                    List<ReleaseData> releases = parse(uc.getInputStream());

                    int this_index = 0;
                    while (this_index < releases.size()) {
                        ReleaseData d = releases.get(this_index);
                        if (d.tag_name.equals(aCurrentVersion)) {
                            break;
                        }
                        this_index++;
                    }

                    int upgrade_index = 0;
                    while (upgrade_index < this_index) {
                        ReleaseData d = releases.get(upgrade_index);
                        if (!d.draft && (d.prerelease == false || aAcceptBeta)) {
                            update = d;
                            break;
                        }
                        upgrade_index++;
                    }
                }
                catch (IOException e) {
                    // Quietly eat errors
                    e.printStackTrace();
                }
                finally {
                    aCallback.run(update);
                }
            }
        });

        thread.setName("Update Thread");
        thread.start();
    }

    private static List<ReleaseData> parse(InputStream is) {
        Pattern tag_contents = Pattern.compile("\"(.+)\"\\s*:\\s*\"?([^,\"]+).*");
        Pattern tag_pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:(?:\"([^\"]*)\")|(?:(\\w+)))");
        Pattern tag_version_from_url = Pattern.compile(".*LSML_Setup-(.*)_32bit.msi");

        List<ReleaseData> releases = new ArrayList<>();

        try (Scanner scanner = new Scanner(is);) {
            ReleaseData r = new ReleaseData();
            String last_url = null;
            String last_name = null;
            String line;
            while (null != (line = scanner.findInLine(tag_pattern))) {
                Matcher m = tag_contents.matcher(line);
                if (m.matches()) {
                    switch (m.group(1)) {
                        case "html_url":
                            last_url = m.group(2);
                            break;
                        case "name":
                            last_name = m.group(2);
                            break;
                        case "prerelease":
                            // The prerelease tag is encountered before any other "html_url" or "name"
                            // tag, we use this as a trigger to store the correct versions of those.
                            r.name = last_name;
                            r.prerelease = Boolean.parseBoolean(m.group(2));
                            break;
                        case "draft":
                            r.html_url = last_url;
                            r.draft = Boolean.parseBoolean(m.group(2));
                            break;
                        case "browser_download_url":
                            String url = m.group(2);
                            if (url.endsWith("32bit.msi")) {
                                Matcher mm = tag_version_from_url.matcher(url);
                                if (mm.matches()) {
                                    r.tag_name = mm.group(1);
                                    releases.add(r);
                                    r = new ReleaseData();
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return releases;
    }
}
