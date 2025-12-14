package cn.junf.crypto;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class JPBCRSAAccumulator {

    private Pairing pairing;

    /**
     * G1生成元
     */
    private static Element P;

    public static Field g1; //域

    private static Field zr;

    private static Element g;  //生成元
    private static Element root; // Accumulated value

    private static BigInteger ex; //指数

    private static JPBCRSAAccumulator jpbcrsaAccumulator;

    public static JPBCRSAAccumulator getJpbcrsaAccumulator() {
        return jpbcrsaAccumulator;
    }

    public static void JPBCRSAAccumulatorSetup(Pairing pairing, Field g1, Field zr, Element g) {
        JPBCRSAAccumulator.jpbcrsaAccumulator = new JPBCRSAAccumulator();
        jpbcrsaAccumulator.pairing = pairing;
        jpbcrsaAccumulator.g1 = g1;
        jpbcrsaAccumulator.zr = zr;
        jpbcrsaAccumulator.g = g;
        jpbcrsaAccumulator.ex = BigInteger.ONE; //指数设定为1
        jpbcrsaAccumulator.root = jpbcrsaAccumulator.g.pow(ex); //第一个根是g^1

    }


    public void addElement(BigInteger element) {
        this.ex = this.ex.multiply(element); //当前所有指数的累乘
        this.root = this.root.pow(element);
    }

    public Element generateW(BigInteger element) {
        BigInteger ex1 = this.ex.divide(element);   //ex1 = ex / a2
        Element w = this.g.pow(ex1);
        return w;
    }

    //    public boolean verifyElement(Element w,BigInteger element) {
    public boolean verifyElement(byte[] memCer, BigInteger element) {
        Element w = JPBCRSAAccumulator.g1.newElementFromBytes(memCer).getImmutable();
//        Element w = JPBCRSAAccumulator.g1.newElementFromBytes(memCer.getBytes(StandardCharsets.UTF_8)).getImmutable();
        Element rootPrime = w.pow(element);
//        BigInteger w = g.modPow(ex1,N); //w = root * (g^a2)^(-1) mod N
//        BigInteger rootPrime = w.modPow(element, N); // root' = w^a2 mod N
        return rootPrime.equals(root);
    }

}
