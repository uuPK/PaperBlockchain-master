package cn.junf.repository;

import cn.junf.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepo extends JpaRepository<Transaction,String> {
    /**
     * 获取指定id的交易类
     * @param id    索引
     * @return  Transaction
     */
    Transaction getTransactionById(String id);
}
