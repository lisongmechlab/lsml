package org.lisoft.lsml.math;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class FastFactorial {
    private static final List<BigInteger> cache = new ArrayList<>();
    static{
        cache.add(BigInteger.valueOf(1));
    }

    public static BigInteger factorial(int n){
        while(cache.size() <= n){
            int s = cache.size();
            cache.add(cache.get(s-1).multiply(BigInteger.valueOf(s)));
        }
        return cache.get(n);
    }
}
