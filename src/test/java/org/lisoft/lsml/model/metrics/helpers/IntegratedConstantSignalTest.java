package org.lisoft.lsml.model.metrics.helpers;

import junit.framework.TestCase;

public class IntegratedConstantSignalTest extends TestCase {

    public void testIntegrateFromZeroTo() {
        IntegratedConstantSignal cut = new IntegratedConstantSignal(2.5);
        assertEquals(25.0, cut.integrateFromZeroTo(10.0));
    }
}