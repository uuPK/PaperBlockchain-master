package cn.junf.repository;

import cn.junf.entity.Utxo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtxoRepo extends JpaRepository<Utxo,String> {
    /**
     * 从全局UTXO中获取一个utxo
     * @param id    UTXO中的id
     * @return  {@lick Utxo}
     *
     * 感觉这个少一个SQL语句
     */
    Utxo getTransactionOutputById(String id);
}
