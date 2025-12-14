package cn.junf.service;

import cn.junf.entity.Transaction;
import cn.junf.entity.UserChameleonTrap;
import cn.junf.entity.Wallet;
import it.unisa.dia.gas.jpbc.Element;


public interface IChameleonService {
    /**
     * 初始化
     */
    void setup();

    /**
     * 为用户生成变色龙哈希所需的陷门和公开参数
     *
     * @param user 用户
     */
    void SetUserSecret(Wallet user);

    /**
     * 为交易生成变色龙哈希值
     *
     * @param transaction 交易信息
     * @param user        {@link Wallet}  用户
     */
    void generateHash(Transaction transaction, Wallet user);

    /**
     * 修改用户信息
     *
     * @param hash 原先的交易哈希值
     * @param user {@link Wallet} 用户
     * @return 新的交易信息
     */
    String modifyMessage(String hash, Wallet user, String newMsg);

    /**
     * 验证新生成的交易信息是否满足变色龙哈希特性（即哈希值不变）
     * <p>
     * //     * @param msg       新的交易信息
     *
     * @param hash 原Hash值
     * @param trap 用户秘钥
     *             //     * @param usingR    r'
     * @return 验证成功返回true
     */
    boolean verifyHash(String hash, Element updateMsg, UserChameleonTrap trap, Element R);
    //boolean verifyHash(Element msg, String hash, UserChameleonTrap trap, Element usingR);

    Element generateMemCer(String newMsg);

    //    boolean verifyMemCer(String memCer,String newMsg);
    boolean verifyMemCer(byte[] memCer, String newMsg);

}
