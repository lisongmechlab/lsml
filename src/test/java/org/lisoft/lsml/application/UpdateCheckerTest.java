/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2023  Li Song
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

import static org.junit.Assert.*;

import java.net.URL;
import org.junit.Test;
import org.lisoft.lsml.application.UpdateChecker.ReleaseData;

public class UpdateCheckerTest {

  ReleaseData releaseData = null;

  @Test
  public void testParse() throws InterruptedException {
    final URL url = ClassLoader.getSystemClassLoader().getResource("githubapitest.txt");
    final UpdateChecker cut =
        new UpdateChecker(
            url,
            "1.6.5",
            aReleaseData -> {
              releaseData = aReleaseData;
              synchronized (UpdateCheckerTest.this) {
                UpdateCheckerTest.this.notify();
              }
            },
            false);

    cut.run();

    synchronized (this) {
      this.wait(100000000);
    }

    assertNotNull(releaseData);
    assertEquals("1.6.8", releaseData.tag_name);
    assertEquals(false, releaseData.draft);
    assertEquals(false, releaseData.prerelease);
    assertEquals("https://github.com/lisongmechlab/lsml/releases/tag/1.6.8", releaseData.html_url);
    assertEquals("LSML 1.6.8 Quite Quick", releaseData.name);
  }

  @Test
  public void testParse_Beta() throws InterruptedException {
    final URL url = ClassLoader.getSystemClassLoader().getResource("githubapitest.txt");
    final UpdateChecker cut =
        new UpdateChecker(
            url,
            "1.6.5",
            aReleaseData -> {
              releaseData = aReleaseData;
              synchronized (UpdateCheckerTest.this) {
                UpdateCheckerTest.this.notify();
              }
            },
            true);
    cut.run();

    synchronized (this) {
      this.wait(1000);
    }
    assertNotNull(releaseData);
    assertEquals("1.6.9000", releaseData.tag_name);
    assertEquals(false, releaseData.draft);
    assertEquals(true, releaseData.prerelease);
    assertEquals(
        "https://github.com/lisongmechlab/lsml/releases/tag/1.7.0-develop1", releaseData.html_url);
    assertEquals("LSML 1.7.0 Development Preview 1", releaseData.name);
  }

  @Test
  public void testParse_DevRelease() throws InterruptedException {
    releaseData = new ReleaseData();

    final URL url = ClassLoader.getSystemClassLoader().getResource("githubapitest.txt");
    final UpdateChecker cut =
        new UpdateChecker(
            url,
            "0.0.0",
            aReleaseData -> {
              releaseData = aReleaseData;
              synchronized (UpdateCheckerTest.this) {
                UpdateCheckerTest.this.notify();
              }
            },
            true);
    cut.run();

    synchronized (this) {
      this.wait(1000);
    }
    assertNull(releaseData);
  }

  @Test
  public void testParse_UpToDateNoBeta() throws InterruptedException {
    releaseData = new ReleaseData();

    final URL url = ClassLoader.getSystemClassLoader().getResource("githubapitest.txt");
    final UpdateChecker cut =
        new UpdateChecker(
            url,
            "1.6.8",
            aReleaseData -> {
              releaseData = aReleaseData;
              synchronized (UpdateCheckerTest.this) {
                UpdateCheckerTest.this.notify();
              }
            },
            false);
    cut.run();

    synchronized (this) {
      this.wait(1000);
    }
    assertNull(releaseData);
  }

  @Test
  public void testParse_UpToDateWithBeta() throws InterruptedException {
    releaseData = new ReleaseData();

    final URL url = ClassLoader.getSystemClassLoader().getResource("githubapitest.txt");
    final UpdateChecker cut =
        new UpdateChecker(
            url,
            "1.6.9000",
            aReleaseData -> {
              releaseData = aReleaseData;
              synchronized (UpdateCheckerTest.this) {
                UpdateCheckerTest.this.notify();
              }
            },
            true);
    cut.run();

    synchronized (this) {
      this.wait(1000);
    }
    assertNull(releaseData);
  }
}
