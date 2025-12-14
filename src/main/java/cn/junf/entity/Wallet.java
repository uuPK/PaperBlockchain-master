package cn.junf.entity;

import cn.hutool.crypto.SecureUtil;
import cn.junf.utils.CryptoUtil;
import cn.junf.service.impl.MineServiceImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Setter;
import lombok.Getter;


import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

/**
 * 钱包类
 */
public class Wallet {

    /**
     * 用户名
     */
    @Getter
    @Setter
    private String name;

    /**
     * 用户私钥信息
     */
    @Getter
    @JsonIgnore
    private final PrivateKey privateKey;

    @Getter
    @JsonIgnore
    private final String privateKeyString;


    /**
     * 用户公钥信息
     */
    @Getter
    @JsonIgnore
    private final PublicKey publicKey;

    @Getter
    @JsonIgnore
    private final String publicKeyString;


    /**
     * 用户变色龙哈希陷门
     */
    @Getter
    @Setter
    @JsonIgnore
    private UserChameleonTrap trap;

    /**
     * 用户挖矿线程
     */
    @Getter
    @JsonIgnore
    private final Thread workerThread;

    /**
     * 用户账户金额
     */
    @Getter
    @JsonIgnore
    private HashMap<String,Utxo> UTXO = new HashMap<>();

    /**
     * balance  不知道干嘛用的
     */
    @Setter
    @Getter
    private float balance;


    public Wallet(){
        //非对称加密工具类，pair映射  国密SM2
        KeyPair keyPair= SecureUtil.generateKeyPair("SM2");

        //创建账户公私钥
        privateKey=keyPair.getPrivate();
        publicKey=keyPair.getPublic();
        publicKeyString= CryptoUtil.getStringFromKey(publicKey);
        privateKeyString=CryptoUtil.getStringFromKey(privateKey);
        String publickKeyString = CryptoUtil.getStringFromKey(publicKey);

        name=publickKeyString.substring(110);
        balance=0;  //balance不知道干什么用的

        System.out.println("Wallet name : " + name);


        MineServiceImpl mineService = new MineServiceImpl();
        workerThread=new Thread(mineService,name);
        workerThread.start();

    }

    public Wallet(String address){
        //非对称加密工具类，pair映射
        KeyPair keyPair= SecureUtil.generateKeyPair("SM2");

        //创建账户公私钥
        privateKey=keyPair.getPrivate();
        publicKey=keyPair.getPublic();
        publicKeyString= CryptoUtil.getStringFromKey(publicKey);
        privateKeyString=CryptoUtil.getStringFromKey(privateKey);
        String publickKeyString = CryptoUtil.getStringFromKey(publicKey);

        name=publickKeyString.substring(94);
        balance=0;  //balance不知道干什么用的

        MineServiceImpl mineService = new MineServiceImpl();
        workerThread=new Thread(mineService,address);
        System.out.println("Wallet name : " + workerThread.getName());
        workerThread.start();

    }


}
