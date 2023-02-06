/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.application;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.lisoft.lsml.view_fx.LiSongMechLab;

/**
 * This class will connect to the GitHub repository and see if there is an update.
 *
 * @author Li Song
 */
public class UpdateChecker {
  public static class ReleaseData {
    public boolean draft;
    public String html_url;
    public String name;
    public boolean prerelease;
    public String tag_name;
  }

  public static final String GITHUB_RELEASES_ADDRESS =
      "https://api.github.com/repos/lisongmechlab/lsml/releases";
  private final Thread thread;

  /**
   * Creates a new {@link UpdateChecker} that will check for updates in the background and call the
   * callback when it is done.
   *
   * @param aUrl The URL to check for the releases file.
   * @param aCurrentVersion The current version string.
   * @param aCallback The {@link UpdateCallback} to call.
   * @param aAcceptBeta <code>true</code> if beta releases should be considered as an update.
   */
  public UpdateChecker(
      final URL aUrl,
      final String aCurrentVersion,
      final UpdateCallback aCallback,
      final boolean aAcceptBeta) {

    thread =
        new Thread(
            () -> {
              if (LiSongMechLab.DEVELOP_VERSION.equalsIgnoreCase(aCurrentVersion)) {
                aCallback.run(null);
                return;
              }

              ReleaseData update = null;
              try {
                final URLConnection uc = aUrl.openConnection();
                uc.setRequestProperty("Accept", "application/vnd.github.v3+json");
                uc.setRequestProperty("User-Agent", "Li-Song-Mechlab-LSML");

                final List<ReleaseData> releases = parse(uc.getInputStream());

                int this_index = 0;
                while (this_index < releases.size()) {
                  final ReleaseData d1 = releases.get(this_index);
                  if (d1.tag_name.equals(aCurrentVersion)) {
                    break;
                  }
                  this_index++;
                }

                int upgrade_index = 0;
                while (upgrade_index < this_index) {
                  final ReleaseData d2 = releases.get(upgrade_index);
                  if (!d2.draft && (!d2.prerelease || aAcceptBeta)) {
                    update = d2;
                    break;
                  }
                  upgrade_index++;
                }
              } catch (final IOException e) {
                // Quietly eat errors, no need to make a scene if the update
                // check didn't succeed.
              } finally {
                aCallback.run(update);
              }
            });

    thread.setName("Update Thread");
  }

  public void run() {
    thread.start();
  }

  private static List<ReleaseData> parse(InputStream is) {
    final Pattern tag_contents = Pattern.compile("\"(.+)\"\\s*:\\s*\"?([^,\"]+).*");
    final Pattern tag_pattern =
        Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:(?:\"([^\"]*)\")|(?:(\\w+)))");
    final Pattern tag_version_from_url = Pattern.compile(".*LSML_Setup-(.*)_32bit.msi");

    final List<ReleaseData> releases = new ArrayList<>();

    try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
      ReleaseData r = new ReleaseData();
      String last_url = null;
      String last_name = null;
      String line;
      while (null != (line = scanner.findInLine(tag_pattern))) {
        final Matcher m = tag_contents.matcher(line);
        if (m.matches()) {
          switch (m.group(1)) {
            case "html_url":
              last_url = m.group(2);
              break;
            case "name":
              last_name = m.group(2);
              break;
            case "prerelease":
              // The "prerelease" tag is encountered before any other
              // "html_url" or "name"
              // tag, we use this as a trigger to store the correct
              // versions of those.
              r.name = last_name;
              r.prerelease = Boolean.parseBoolean(m.group(2));
              break;
            case "draft":
              r.html_url = last_url;
              r.draft = Boolean.parseBoolean(m.group(2));
              break;
            case "browser_download_url":
              final String url = m.group(2);
              if (url.endsWith("32bit.msi")) {
                final Matcher mm = tag_version_from_url.matcher(url);
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

  @FunctionalInterface
  public interface UpdateCallback {
    void run(ReleaseData aReleaseData);
  }
}
