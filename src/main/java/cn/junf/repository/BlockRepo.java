package cn.junf.repository;

import cn.junf.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


public interface BlockRepo extends JpaRepository<Block,String> {
    /**
     * 获取链上最后一条记录
     * @param id    区块id
     * @return  Block
     */
   Block getBlockById(long id);

    /**
     * 获取当前区块链高度
     * @return  当前区块链的高度
     */
   @Transactional(timeout = 5,propagation = Propagation.NOT_SUPPORTED)
   @Query(value = "select count(*) from blocks",nativeQuery = true)
    long getHeight();
}
