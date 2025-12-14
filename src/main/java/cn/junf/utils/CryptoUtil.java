package cn.junf.utils;

import cn.hutool.crypto.SmUtil;
import cn.junf.entity.Transaction;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 生成电子签名的工具类
 */
public class CryptoUtil {

    /**
     * Sm3哈希函数
     *
     * @param msg 待哈希消息
     * @return String  哈希值
     */
    public static String applySm3Hash(String msg) {
        return SmUtil.sm3(msg);
    }

    /**
     * Sha256哈希函数
     *
     * @param data 摘要信息
     * @return
     */
    public static String applySha256Hash(byte[] data) {
        long startTime = System.currentTimeMillis();
        try {
            MessageDigest msgDigest = MessageDigest.getInstance("SHA-256");
            msgDigest.update(data);
            return byte2Hex(msgDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * SM2签名函数
     *
     * @param privateKey 私钥
     * @param msg        消息
     * @return byte[]      签名信息
     */
    public static byte[] applySm2Signature(PrivateKey privateKey, String msg) {
        return SmUtil.sm2(privateKey, null).sign(msg.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * SM2签名验证函数
     *
     * @param publicKey 公钥
     * @param msg       消息
     * @param signature 签名
     * @return 是否为合法的SM2签名消息
     */
    public static boolean verifySm2Signature(PublicKey publicKey, String msg, byte[] signature) {
        return SmUtil.sm2(null, publicKey).verify(msg.getBytes(StandardCharsets.UTF_8), signature);
    }


    /**
     * 将byte转为16进制
     *
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String temp = null;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                // 1得到一位的进行补0操作（不太明白）
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString();
    }

    /**
     * Base64加密
     *
     * @param key {@link Key}
     * @return Base64加密结果
     */
    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * 构造merkle树(没有构造。。只计算出了根哈希值)  计算merkle根值
     * @param transactions  交易{@link Transaction}
     * @return
     */
    public static String calculateMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();

        List<String> previousTreeLayer = new ArrayList<>();
        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.getId());
        }
        List<String> treeLayer = previousTreeLayer;

        while(count >1){
            treeLayer = new ArrayList<>();//新链表，只记录两个叶子节点求哈希后的新值，一直循环到两边里只有一个数据，就是跟哈希值
            for (int i=1;i<previousTreeLayer.size();i+=2){
                treeLayer.add(applySm3Hash(previousTreeLayer.get(i-1)+previousTreeLayer.get(i)));
            }
            count=treeLayer.size();
            previousTreeLayer=treeLayer;
        }
        return (treeLayer.size()==1)?treeLayer.get(0):"";
    }
}
