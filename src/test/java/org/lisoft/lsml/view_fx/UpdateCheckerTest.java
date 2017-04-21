package org.lisoft.lsml.view_fx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.lisoft.lsml.view_fx.UpdateChecker.ReleaseData;

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
        assertEquals("https://github.com/EmilyBjoerk/lsml/releases/tag/1.6.8", releaseData.html_url);
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
        assertEquals("https://github.com/EmilyBjoerk/lsml/releases/tag/1.7.0-develop1", releaseData.html_url);
        assertEquals("LSML 1.7.0 Development Preview 1", releaseData.name);
    }

    @SuppressWarnings("unused")
    @Test
    public void testParse_UpToDateNoBeta() throws IOException, InterruptedException {
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

    @SuppressWarnings("unused")
    @Test
    public void testParse_UpToDateWithBeta() throws IOException, InterruptedException {
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
}
