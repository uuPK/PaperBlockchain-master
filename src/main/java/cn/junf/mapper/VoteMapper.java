package cn.junf.mapper;

import cn.junf.entity.Vote;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author junf
 * @since 2023-06-03
 */
@Mapper
public interface VoteMapper extends BaseMapper<Vote> {

    List<Vote> selectVoteList(@Param("transactionId") String transactionId);

}
