package cn.junf.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.junf.entity.Threshold;
import cn.junf.entity.Transaction;
import cn.junf.entity.Wallet;
import cn.junf.mapper.ThresholdMapper;
import cn.junf.repository.TransactionRepo;
import cn.junf.service.IThresholdService;
import cn.junf.utils.LatteChain;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author junf
 * @since 2023-06-03
 */
@Service
public class ThresholdServiceImpl extends ServiceImpl<ThresholdMapper, Threshold> implements IThresholdService {
    private final LatteChain latteChain = LatteChain.getInstance();

    /**
     * 交易DAO对象
     */
    @Autowired
    private TransactionRepo transactionRepo;
    @Autowired
    private  ThresholdMapper thresholdMapper;



    /**
     * 修改前要发起修改请求，拿到足够的门限投票
     */
    @Override
    public Boolean modifyRequest(String sender, String id) {
        Wallet senderUser = latteChain.getUsers().get(sender);//修改者地址
        Transaction transaction = transactionRepo.getTransactionById(id);//查询交易是否存在，并获取交易
        if (ObjectUtil.isNull(senderUser) || ObjectUtil.isNull(transaction)) {
            return false;
        }
        //发起修改请求
        int insert = thresholdMapper.insert(new Threshold().setTransactionId(id));
        if (insert > 0){
            return true;
        }
        return false;
    }

}
