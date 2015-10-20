package org.lisoft.lsml.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.lisoft.lsml.view.UpdateChecker.ReleaseData;
import org.lisoft.lsml.view.UpdateChecker.UpdateCallback;

public class UpdateCheckerTest {

    ReleaseData r = null;

    @SuppressWarnings("unused")
    @Test
    public void testParse() throws IOException, InterruptedException {
        URL url = UpdateChecker.class.getResource("/resources/githubapitest.txt");
        new UpdateChecker(url, "1.6.5", new UpdateCallback() {
            @Override
            public void run(ReleaseData aReleaseData) {
                r = aReleaseData;
                synchronized (UpdateCheckerTest.this) {
                    UpdateCheckerTest.this.notify();
                }
            }
        }, false);

        synchronized (this) {
            this.wait(100000000);
        }

        assertNotNull(r);
        assertEquals("1.6.8", r.tag_name);
        assertEquals(false, r.draft);
        assertEquals(false, r.prerelease);
        assertEquals("https://github.com/lisongmechlab/lsml/releases/tag/1.6.8", r.html_url);
        assertEquals("LSML 1.6.8 Quite Quick", r.name);
    }

    @SuppressWarnings("unused")
    @Test
    public void testParse_Beta() throws IOException, InterruptedException {
        URL url = UpdateChecker.class.getResource("/resources/githubapitest.txt");
        new UpdateChecker(url, "1.6.5", new UpdateCallback() {
            @Override
            public void run(ReleaseData aReleaseData) {
                r = aReleaseData;
                synchronized (UpdateCheckerTest.this) {
                    UpdateCheckerTest.this.notify();
                }
            }
        }, true);

        synchronized (this) {
            this.wait(1000);
        }
        assertNotNull(r);
        assertEquals("1.6.9000", r.tag_name);
        assertEquals(false, r.draft);
        assertEquals(true, r.prerelease);
        assertEquals("https://github.com/lisongmechlab/lsml/releases/tag/1.7.0-develop1", r.html_url);
        assertEquals("LSML 1.7.0 Development Preview 1", r.name);
    }
    
    @SuppressWarnings("unused")
    @Test
    public void testParse_UpToDateNoBeta() throws IOException, InterruptedException {
        r = new ReleaseData();
        
        URL url = UpdateChecker.class.getResource("/resources/githubapitest.txt");
        new UpdateChecker(url, "1.6.8", new UpdateCallback() {
            @Override
            public void run(ReleaseData aReleaseData) {
                r = aReleaseData;
                synchronized (UpdateCheckerTest.this) {
                    UpdateCheckerTest.this.notify();
                }
            }
        }, false);

        synchronized (this) {
            this.wait(1000);
        }
        assertNull(r);
    }
    
    
    @SuppressWarnings("unused")
    @Test
    public void testParse_UpToDateWithBeta() throws IOException, InterruptedException {
        r = new ReleaseData();
        
        URL url = UpdateChecker.class.getResource("/resources/githubapitest.txt");
        new UpdateChecker(url, "1.6.9000", new UpdateCallback() {
            @Override
            public void run(ReleaseData aReleaseData) {
                r = aReleaseData;
                synchronized (UpdateCheckerTest.this) {
                    UpdateCheckerTest.this.notify();
                }
            }
        }, true);

        synchronized (this) {
            this.wait(1000);
        }
        assertNull(r);
    }
}
