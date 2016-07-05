package org.lisoft.lsml.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ListArrayUtilsTest {
    static class A implements IfA {/* nothing */
    }

    static class B implements IfB {/* nothing */
    }

    static class C extends A implements IfB {/* nothing */
    }

    interface IfA {/* nothing */
    }

    interface IfB {/* nothing */
    }

    @Test
    public void testFilterByType() {
        final List<Object> data = new ArrayList<>();
        final A a = new A();
        final B b = new B();
        final C c = new C();
        data.add(a);
        data.add(b);
        data.add(c);

        final List<IfB> ans = ListArrayUtils.filterByType(data, IfB.class);

        assertEquals(2, ans.size());
        assertSame(b, ans.get(0));
        assertSame(c, ans.get(1));
    }
}
