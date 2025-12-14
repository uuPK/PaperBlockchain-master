package cn.junf.service;


import cn.junf.entity.Threshold;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author junf
 * @since 2023-06-03
 */
public interface IThresholdService extends IService<Threshold> {
    /**
     * 修改前要发起修改请求，拿到足够的门限投票
     */
    Boolean modifyRequest(String sender, String id);

}
