package cn.junf.service.impl;

import cn.junf.entity.Wallet;
import cn.junf.enums.LatteChainConfEnum;
import cn.junf.service.IUserService;
import cn.junf.service.IwalletService;
import cn.junf.utils.LatteChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Map;

@Service
public class UserServiceImpl implements IUserService {

    //拿到一个区块
    private final LatteChain latteChain = LatteChain.getInstance();

    @Autowired
    private IwalletService walletService;

    /**
     * 初始化区块链系统中预置账户信息
     */
    @Override
    public void initUser() {
        //添加并初始化所有账户
        Wallet newUser = new Wallet("admin");

        newUser.setName("admin");
        //设置系统管理员公钥地址
        latteChain.setAdminPublicKey(newUser.getPublicKey());
        latteChain.getUsers().put("admin",newUser);
        for (int i = 1; i < LatteChainConfEnum.INIT_ACCOUNT_AMOUNTS; i++) {
            newUser = new Wallet();
            latteChain.getUsers().put(newUser.getName(),newUser);
        }

        System.out.println(latteChain.getUsers());
    }

    /**
     * 返回查询用户PublicKey信息
     *
     * @param address   String  账户地址信息
     * @return {@link PublicKey}
     */
    @Override
    public PublicKey getUserPublicKey(String address) {
        return latteChain.getUsers().get(address).getPublicKey();
    }

    @Override
    public Map<String, Wallet> getAllUsersInfo() {
        //刷新所有用户的信息
        for (String address : latteChain.getUsers().keySet()){
            latteChain.getUsers().get(address).setBalance(walletService.getBalance(address));
        }
        return latteChain.getUsers();
    }
}
