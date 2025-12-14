package cn.junf.service;

import cn.junf.entity.Transaction;

import java.security.PrivateKey;

public interface ITransactionService {

    /**
     * 发起一笔交易
     *
     * @param sender    交易发起方
     * @param recipient 交易接收方
     * @param value     交易金额
     * @return Transaction {@link Transaction} 交易信息
     */
    Transaction createTransaction(String sender, String recipient, float value, String msg);

    /**
     * 计算交易的输出
     *
     * @param transaction {@link Transaction} 交易
     * @return 交易成功则返回true
     */
    boolean processTransaction(Transaction transaction);

    /**
     * 获取交易输入的总值
     *
     * @param transaction {@link Transaction} 交易
     * @return 输入总值
     */
    float getInputsValue(Transaction transaction);

    /**
     * 为一个交易生成签名
     *
     * @param privateKey  {@link PrivateKey}  签名私钥
     * @param transaction {@link Transaction} 交易
     */
    void generateSignature(PrivateKey privateKey, Transaction transaction);

    /**
     * 验证交易签名
     *
     * @param transaction {@link Transaction} 交易
     * @return boolean
     */
    boolean isValidSignature(Transaction transaction);

    /**
     * 获取指定ID的交易信息
     *
     * @param id 交易ID
     * @return 交易信息
     */
    String getTransaction(String id);


    /**
     * 修改交易
     *
     * @param sender
     * @param id
     * @param newMsg
     */
    void modifyTransaction(String sender, String id, String newMsg);
}
