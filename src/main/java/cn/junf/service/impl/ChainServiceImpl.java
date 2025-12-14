package cn.junf.service.impl;

import cn.junf.service.IChainService;
import cn.junf.service.IMineService;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Security;

@Service
public class ChainServiceImpl implements IChainService {

    @Autowired
    private IMineService mineService;

    /**
     * 初始化一个区块链系统
     *
     * @return 成功则返回true
     */
    @Override
    public boolean initchain() {
        Security.addProvider(new BouncyCastlePQCProvider());
        return mineService.initChain();
    }
}
