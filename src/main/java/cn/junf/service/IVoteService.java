package cn.junf.service;

import cn.junf.entity.Vote;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author junf
 * @since 2023-06-03
 */
public interface IVoteService extends IService<Vote> {
    /**
     * 对修改请求进行投票
     * @param sender    修改者地址
     * @param id        票决ID
     * @return
     */
    Boolean vote(String sender, String id);

    /**
     * 验证是否达到秘密合成阈值
     * @param id
     * @return
     */
    Boolean voteVerify(String id) throws Exception;

}
