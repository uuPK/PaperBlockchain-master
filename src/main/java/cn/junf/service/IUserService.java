package cn.junf.service;

import cn.junf.entity.Wallet;

import java.security.PublicKey;
import java.util.Map;

public interface IUserService {
    /**
     * 初始化10个用户
     */
    void initUser();

    /**
     * 返回查询用户PublicKey信息
     * @param address   String  账户地址信息
     * @return  {@lick PublicKey}
     */
    PublicKey getUserPublicKey(String address);

    /**
     * 返回当前系统中所有用户的公钥地址和账户余额信息
     *
     * @return  String
     */
    Map<String, Wallet> getAllUsersInfo();
}
