package cn.junf.crypto;

import java.math.BigInteger;

public class RSAAccumulator {
    private BigInteger g; // Base
    private BigInteger p; // Prime number p
    private BigInteger q; // Prime number q
    private BigInteger N; // N = p * q
    private BigInteger root; // Accumulated value

    private BigInteger ex; //指数

    public RSAAccumulator(BigInteger g, BigInteger p, BigInteger q) {
        this.g = g;
        this.p = p;
        this.q = q;
        this.N = p.multiply(q);
        this.ex = BigInteger.ONE;
        this.root = g.modPow(ex, N); // root = g^1 mod N
    }

    public void addElement(BigInteger element) {
        ex = ex.multiply(element); //当前所有指数的累乘
        root = root.modPow(element,N);
    }

    public boolean verifyElement(BigInteger element) {
        BigInteger ex1 = this.ex.divide(element);   //ex1 = ex / a2
        BigInteger w = g.modPow(ex1,N); //w = root * (g^a2)^(-1) mod N
        BigInteger rootPrime = w.modPow(element, N); // root' = w^a2 mod N

        return rootPrime.equals(root);
    }


    public static void main(String[] args) {
        BigInteger g = BigInteger.valueOf(2); // Choose a base g
        BigInteger p = BigInteger.valueOf(17); // Choose a prime number p
        BigInteger q = BigInteger.valueOf(23); // Choose a prime number q

        RSAAccumulator accumulator = new RSAAccumulator(g, p, q);

        BigInteger element1 = BigInteger.valueOf(5);
        accumulator.addElement(element1);

        BigInteger element2 = BigInteger.valueOf(7);
        accumulator.addElement(element2);

        BigInteger element3 = BigInteger.valueOf(13);

        boolean b = accumulator.verifyElement(element1);
        boolean b1 = accumulator.verifyElement(element2);
        boolean b2 = accumulator.verifyElement(element3);
        System.out.println("Element 1: " + b); // true`
        System.out.println("Element 2: " + b1); // true
        System.out.println("Element 3: " + b2); // false
    }
}

