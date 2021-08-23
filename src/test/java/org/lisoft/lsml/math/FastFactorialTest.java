package org.lisoft.lsml.math;

import junit.framework.TestCase;
import org.junit.Test;

import java.math.BigInteger;

public class FastFactorialTest extends TestCase {

    @Test
    public void testFactorial(){
        assertEquals(new BigInteger("93326215443944152681699238856266700490715968264381621468592963895217599993229915608941463976156518286253697920827223758251185210916864000000000000000000000000",10),
                FastFactorial.factorial(100));
    }

}