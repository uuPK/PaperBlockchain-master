package cn.junf.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 交易池
 */
@Entity
@Table(name = "transaction_pool")
public class TransactionPoolEntity {

    /**
     * 交易ID
     */
    @Id
    @Setter
    @Getter
    private String transactionIndex;

    /**
     * 时间戳
     */
    @Column
    private long timeStamp;

    protected TransactionPoolEntity(){
    }
    public TransactionPoolEntity(String index,long timeStamp){
        this.transactionIndex=index;
        this.timeStamp=timeStamp;
    }
}
