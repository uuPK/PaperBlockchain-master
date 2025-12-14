package cn.junf.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.junf.crypto.PBCSecretShare;
import cn.junf.entity.Vote;
import cn.junf.entity.Wallet;
import cn.junf.enums.LatteChainConfEnum;
import cn.junf.mapper.VoteMapper;
import cn.junf.service.IVoteService;

import cn.junf.utils.LatteChain;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import it.unisa.dia.gas.jpbc.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author junf
 * @since 2023-06-03
 */
@Service
public class VoteServiceImpl extends ServiceImpl<VoteMapper, Vote> implements IVoteService {
    private final LatteChain latteChain = LatteChain.getInstance();

    @Autowired
    private VoteMapper voteMapper;

    @Override
    public Boolean vote(String sender, String id) {

        Wallet senderUser = latteChain.getUsers().get(sender);//投票者地址
        if (ObjectUtil.isNull(senderUser)) {
            return false;
        }

//        int insert = voteMapper.insert(new Vote().setTransactionId(id)
//                .setTrapX(senderUser.getTrap().getX().toString())
//                .setTrapY(senderUser.getTrap().getShare().toString()));
        int insert = voteMapper.insert(new Vote().setTransactionId(id).setUserPk(sender));
        if (insert > 0) return true;
        return false;
    }

    /**
     * 验证是否达到秘密合成阈值
     *
     * @param id
     * @return
     */
    @Override
    public Boolean voteVerify(String id) throws Exception {

        List<Vote> votes = voteMapper.selectVoteList(id);

        //投票已经达到门限值
        if (votes.size() >= LatteChainConfEnum.THRESHOLD) {
            Element reconstruction = PBCSecretShare.getPbcSecretShare().reconstruction(votes);
            System.out.println("reconstruction:    " + reconstruction);
            System.out.println("Secret:            " + PBCSecretShare.getPbcSecretShare().getSecret());
            if (reconstruction.equals(PBCSecretShare.getPbcSecretShare().getSecret())) {
                return true;
            }
        }
        return false;
    }
}
