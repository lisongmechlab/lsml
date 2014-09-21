package lisong_mechlab.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ListArrayUtilsTest {
	interface IfA {/* nothing */
	}

	interface IfB {/* nothing */
	}

	class A implements IfA {/* nothing */
	}

	class B implements IfB {/* nothing */
	}

	class C extends A implements IfB {/* nothing */
	}

	@Test
	public void testFilterByType() {
		List<Object> data = new ArrayList<>();
		A a = new A();
		B b = new B();
		C c = new C();
		data.add(a);
		data.add(b);
		data.add(c);

		List<IfB> ans = ListArrayUtils.filterByType(data, IfB.class);

		assertEquals(2, ans.size());
		assertSame(b, ans.get(0));
		assertSame(c, ans.get(1));
	}
}
