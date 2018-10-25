package org.lisoft.lsml.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URL;

import org.junit.Test;
import org.lisoft.lsml.application.UpdateChecker.ReleaseData;

public class UpdateCheckerTest {

    ReleaseData releaseData = null;

    @Test
    public void testParse() throws InterruptedException {
        final URL url = ClassLoader.getSystemClassLoader().getResource("githubapitest.txt");
        final UpdateChecker cut = new UpdateChecker(url, "1.6.5", aReleaseData -> {
            releaseData = aReleaseData;
            synchronized (UpdateCheckerTest.this) {
                UpdateCheckerTest.this.notify();
            }
        }, false);

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
        final UpdateChecker cut = new UpdateChecker(url, "1.6.5", aReleaseData -> {
            releaseData = aReleaseData;
            synchronized (UpdateCheckerTest.this) {
                UpdateCheckerTest.this.notify();
            }
        }, true);
        cut.run();

        synchronized (this) {
            this.wait(1000);
        }
        assertNotNull(releaseData);
        assertEquals("1.6.9000", releaseData.tag_name);
        assertEquals(false, releaseData.draft);
        assertEquals(true, releaseData.prerelease);
        assertEquals("https://github.com/lisongmechlab/lsml/releases/tag/1.7.0-develop1", releaseData.html_url);
        assertEquals("LSML 1.7.0 Development Preview 1", releaseData.name);
    }

    @Test
    public void testParse_UpToDateNoBeta() throws InterruptedException {
        releaseData = new ReleaseData();

        final URL url = ClassLoader.getSystemClassLoader().getResource("githubapitest.txt");
        final UpdateChecker cut = new UpdateChecker(url, "1.6.8", aReleaseData -> {
            releaseData = aReleaseData;
            synchronized (UpdateCheckerTest.this) {
                UpdateCheckerTest.this.notify();
            }
        }, false);
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
        final UpdateChecker cut = new UpdateChecker(url, "1.6.9000", aReleaseData -> {
            releaseData = aReleaseData;
            synchronized (UpdateCheckerTest.this) {
                UpdateCheckerTest.this.notify();
            }
        }, true);
        cut.run();

        synchronized (this) {
            this.wait(1000);
        }
        assertNull(releaseData);
    }

    @Test
    public void testParse_DevRelease() throws InterruptedException {
        releaseData = new ReleaseData();

        final URL url = ClassLoader.getSystemClassLoader().getResource("githubapitest.txt");
        final UpdateChecker cut = new UpdateChecker(url, "(develop)", aReleaseData -> {
            releaseData = aReleaseData;
            synchronized (UpdateCheckerTest.this) {
                UpdateCheckerTest.this.notify();
            }
        }, true);
        cut.run();

        synchronized (this) {
            this.wait(1000);
        }
        assertNull(releaseData);
    }
}
