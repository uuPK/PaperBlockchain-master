package cn.junf.service.impl;

import cn.junf.entity.Transaction;
import cn.junf.entity.Utxo;
import cn.junf.entity.Wallet;
import cn.junf.repository.UtxoRepo;
import cn.junf.service.IChameleonService;
import cn.junf.service.ITransactionService;
import cn.junf.service.IwalletService;
import cn.junf.utils.LatteChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class WalletServiceImple implements IwalletService {
    private final LatteChain latteChain = LatteChain.getInstance();
    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private IChameleonService chameleonService;

    /**
     * UTXO DAO
     */
    @Autowired
    private UtxoRepo utxoDao;

    /**
     * 获取账户余额
     *
     * @param  {@link Wallet}  用户钱包
     * @return
     */
    @Override
    public float getBalance(Wallet userWallet) {
        float total = 0;
        //从全局的UTXO中收集该用户的UTXO并进行结算
        for (Utxo record : utxoDao.findAll()) {
            if (record.getRecipientString().equals(userWallet.getPublicKeyString())) {
                userWallet.getUTXO().put(record.getId(), record);
                total += record.getValue();
            }
        }
        return total;
    }

    /**
     * 获取账户余额
     *
     * @param address 用户钱包地址
     * @return 账户余额
     */
    @Override
    public float getBalance(String address) {
        address = address.replace(" ", "+");
        Wallet userWallet = latteChain.getUsers().get(address);
        return getBalance(userWallet);
    }

    /**
     * 想recipient发起一笔值为value的交易
     *
     * @param sender    发送方
     * @param recipient 接收方
     * @param value     交易值
     * @return {@link Transaction} 交易
     */
    @Override
    public Transaction sendFunds(String sender, String recipient, float value,String msg) {
        Wallet senderWallet = latteChain.getUsers().get(sender);
        Wallet recipientWallet = latteChain.getUsers().get(recipient);
        if (senderWallet == null || recipientWallet == null) {
            //请求用户不存在
            return null;
        }

        //收集用户的账户金额
        if (this.getBalance(senderWallet) < value) {
            //发起余额不足，取消交易
            return null;
        }

        //开始构造交易输入
        Set<String> inputs = new HashSet<>();
        float total = 0;

        //收集交易发起者的UTXO
        for (Utxo item : senderWallet.getUTXO().values()) {
            total += item.getValue();
            inputs.add(item.getId());
            //已经满足支出需求
            if (total >= value) {
                break;
            }
        }

        //构造新交易
        Transaction newTransaction = new Transaction(senderWallet.getPublicKey(),
                recipientWallet.getPublicKey(), value, inputs);
        newTransaction.setSenderString(sender);
        newTransaction.setRecipientString(recipient);


        System.out.println("Transaction true");

        //设置交易数据
        /*
        String mainData = sender + '-' +
                recipient + '-' +
                value + '-' +
                newTransaction.getTimeStamp() + '-' +
                newTransaction.getRegistrationMsg() + '-' +
                msg;

         */

        String mainData = msg;

        newTransaction.setData(mainData);
        //利用变色龙哈希生成交易ID
        chameleonService.generateHash(newTransaction,senderWallet);
        //用私钥 对 整个交易 生成签名  最终要变成 指定可修改，这一块内容要变一下，现在签上自己的签名代表后面只能自己修改
        transactionService.generateSignature(senderWallet.getPrivateKey(),newTransaction);

        //扣除发起者的花费的UTXO（从个人钱包里）
        for (String input :inputs){
            senderWallet.getUTXO().remove(input);
        }
        return newTransaction;
    }
}
