package cn.junf.utils;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MsgToHash {
    public static Element GennerateMsgToHash(String msg, Field zr){
        String cacheKey;
        try {
            //Java利用MessageDigest获取字符串MD5
            MessageDigest mDigestData = MessageDigest.getInstance("SHA-512");
            mDigestData.update(msg.getBytes());
            cacheKey = bytesToHexString(mDigestData.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(msg.hashCode());
        }

        Element msgHash = zr.newElementFromHash(cacheKey.getBytes(StandardCharsets.UTF_8),0,cacheKey.length()).getImmutable();
        return msgHash;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }




}
/**
 *     // H1 : Zq -> G1
 *     byte[] zq_bytes = zq_element.getImmutable().toCanonicalRepresentation();
 *     byte[] g1_bytes = zq_bytes;
 *         try {
 *         MessageDigest hasher = MessageDigest.getInstance("SHA-512");
 *         g1_bytes = hasher.digest(zq_bytes);   //先把Zq元素hash成512bits
 *     } catch (Exception e) {
 *         e.printStackTrace();
 *     }
 *     //再把hash后的bits映射到G1
 *     Element hash_result = pairing.getG1().newElementFromHash(g1_bytes, 0, g1_bytes.length).getImmutable();
 *         return hash_result;
 */