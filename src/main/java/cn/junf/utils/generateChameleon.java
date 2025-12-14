package cn.junf.utils;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;

public class generateChameleon {
    public static Pairing pairing;
//    public static Element p;
//    public static Element q;
    public static Element g;
    public static Field<Element> G1,G2,GT,Zq;
    public static Element sk;//私钥
    public static Element pk;//公钥



    public static void setup(){
        pairing = PairingFactory.getPairing("crypto/a.properties");
        G1= pairing.getG1();
        G2= pairing.getG2();
        GT= pairing.getGT();
        Zq= pairing.getZr();
        g=G1.newRandomElement().getImmutable();//G1上的生成
    }

    public static void generateKeyPair(){
        sk=Zq.newRandomElement().getImmutable();
        pk=g.powZn(sk).getImmutable();
    }

}
