package cn.junf.entity;

import lombok.Data;

/**
 * 交易摘要信息
 */
@Data
public class TransactionDigest {
    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 交易发起方地址
     */
    private String senderAddress;

    /**
     * 交易接受方地址
     */
    private String recipientAddress;

    /**
     * 交易金额
     */
    private String value;

    /**
     * 注册信息
     */
    private String registrationMsg;

    /**
     * 时间戳
     */
    private String timeStamp;

    /**
     *
     * @param transactionId         交易ID
     * @param senderAddress         交易发起方地址
     * @param recipientAddress      交易接受方地址
     * @param registrationMsg       注册信息
     * @param value                 交易金额
     * @param timeStamp             时间戳
     */
    public TransactionDigest(String transactionId, String senderAddress, String recipientAddress, String registrationMsg, String value, String timeStamp) {
        this.transactionId=transactionId;
        this.senderAddress=senderAddress;
        this.recipientAddress=recipientAddress;
        this.value=value;
        this.registrationMsg=registrationMsg;
        this.timeStamp=timeStamp;
    }
}
