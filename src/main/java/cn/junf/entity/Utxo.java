package cn.junf.entity;

import cn.junf.utils.CryptoUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.security.PublicKey;

@Data
@Entity
@Table(name = "global_utxo",schema = "lattechain")
public class Utxo {

    /**
     * id
     */
    @Id
    @Column(name = "utxo_id")
    private String id;

    /**
     *  交易接收方
     */
    @Transient
    @JsonIgnore
    private PublicKey recipient;

    @Column(name = "owner")
    @JsonIgnore
    private String recipientString;

    /**
     * 交易金额
     */
    private float value;

    /**
     * 时间戳
     */
    private long timeStamp;

    /**
     * 产生该UTXO的交易
     */
    private String refTransactionId;

    protected Utxo(){}

    /**
     * 新建一个交易输出，并自动计算其交易ID
     */
    public Utxo(PublicKey recipient,float value){
        this.recipient=recipient;
        this.recipientString= CryptoUtil.getStringFromKey(recipient);
        this.value=value;
        this.timeStamp=System.currentTimeMillis();
        //id=接收方地址+钱+时间戳
        this.id=CryptoUtil.applySm3Hash(CryptoUtil.getStringFromKey(recipient)+value+timeStamp);
    }
}
