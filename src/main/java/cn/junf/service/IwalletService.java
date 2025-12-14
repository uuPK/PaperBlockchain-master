package cn.junf.service;

import cn.junf.entity.Transaction;
import cn.junf.entity.Wallet;

public interface IwalletService {
    /**
     * 获取账户余额
     *
     * @param wallet    {@link Wallet}  用户钱包
     * @return  float
     */
    float getBalance(Wallet wallet);

    /**
     * 获取账户余额
     * @param address   用户钱包地址
     * @return  钱包余额
     */
    float getBalance(String address);

    /**
     * 想recipient发起一笔值为value的交易
     *
     * @param sender    发送方
     * @param recipient 接收方
     * @param value     交易值
     * @return  {@link Transaction} 交易
     */
    Transaction sendFunds(String sender,String recipient,float value,String msg);
}
