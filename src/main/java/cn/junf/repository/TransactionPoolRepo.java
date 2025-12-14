package cn.junf.repository;

import cn.junf.entity.TransactionPoolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionPoolRepo extends JpaRepository<TransactionPoolEntity,String> {
    /**
     * 获取当前交易池中的交易数量
     *
     * @return  交易池中可用的交易大小
     */
    @Query(value = "select count(*) from transaction_pool",nativeQuery = true)
    long getPoolSize();

    /**
     * 从表中获取4个最早的交易
     *
     * @return  {@lick Transaction} 交易列表
     */
    @Query(value = "select * from transaction_pool order by time_stamp limit 0 , 5",nativeQuery = true)
    List<TransactionPoolEntity> getTransactions();

}
