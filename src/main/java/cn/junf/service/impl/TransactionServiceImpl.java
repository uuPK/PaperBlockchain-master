package cn.junf.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.junf.entity.*;
import cn.junf.enums.LatteChainConfEnum;
import cn.junf.mapper.ThresholdMapper;
import cn.junf.mapper.VoteMapper;
import cn.junf.repository.TransactionPoolRepo;
import cn.junf.repository.TransactionRepo;
import cn.junf.repository.UtxoRepo;
import cn.junf.service.IChameleonService;
import cn.junf.service.ITransactionService;
import cn.junf.service.IwalletService;
import cn.junf.utils.CryptoUtil;
import cn.junf.utils.JsonUtil;
import cn.junf.utils.LatteChain;
import cn.junf.utils.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class TransactionServiceImpl implements ITransactionService {

    private final LatteChain latteChain = LatteChain.getInstance();
    @Autowired
    private IwalletService walletService;

    @Autowired
    private IChameleonService chameleonService;

    /**
     * 交易DAO对象
     */
    @Autowired
    private TransactionRepo transactionRepo;

    /**
     * 交易池对象
     */
    @Autowired
    private TransactionPoolRepo transactionPoolRepo;

    /**
     * UTXO DAO对象
     */
    @Autowired
    private UtxoRepo utxoRepo;

    private ThresholdMapper thresholdMapper;
    /**
     * 发起一笔交易
     *
     * @param sender    交易发起方
     * @param recipient 交易接收方
     * @param value     交易金额
     * @return String 交易信息
     */
    @Override
    public Transaction createTransaction(String sender, String recipient, float value,String msg) {
        //处理网络原因导致的字符问题
        sender = sender.replace(" ", "+");
        recipient = recipient.replace(" ", "+");
        Transaction newTransaction = walletService.sendFunds(sender, recipient, value,msg);
        ReentrantLock requestLock = LockUtil.getLockUtil().getStateLock();
        Condition condition = LockUtil.getLockUtil().getWriteCondition();
        if (newTransaction != null) {
            requestLock.lock();
            try {
                transactionRepo.save(newTransaction);
                transactionPoolRepo.save(new TransactionPoolEntity(newTransaction.getId(), newTransaction.getTimeStamp()));
                log.info("新交易已提交！id: " + newTransaction.getId());
                condition.signalAll();
            } finally {
                requestLock.unlock();
            }
            return newTransaction;
        } else {
            return null;
        }

    }

    /**
     * 计算交易输出
     *
     * @param transaction {@link Transaction} 交易
     * @return 输入总值
     */
    @Override
    public boolean processTransaction(Transaction transaction) {
        //首先检查一个交易的合法性
        //验签
        if (!isValidSignature(transaction)) {
            log.warn("交易" + transaction.getId() + "签名信息异常！请审计该交易！");
            return false;
        }
        float inputValue = getInputsValue(transaction);
        //计算剩余价值
        if (inputValue == 0) {
            //输入已经被消耗
            return false;
        }

        //从全局删除交易方的UTXO
        for (String inputId : transaction.getInputUtxosId()) {
            utxoRepo.deleteById(inputId);
        }

        float leftOver = inputValue - transaction.getValue();

        PublicKey senderAddress = latteChain.getUsers().get(transaction.getSenderString()).getPublicKey();
        PublicKey recipientAddre = latteChain.getUsers().get(transaction.getRecipientString()).getPublicKey();

        //添加交易输出
        transaction.setOutputUtxos(new HashSet<>());
        Utxo sendUtxo = new Utxo(recipientAddre, transaction.getValue());
        Utxo backUtxo = new Utxo(senderAddress, leftOver);
        transaction.getOutputUtxosId().add(sendUtxo.getId());
        transaction.getOutputUtxosId().add(backUtxo.getId());

        sendUtxo.setRefTransactionId(transaction.getId());
        backUtxo.setRefTransactionId(transaction.getId());
        //将金额发送至接收方(这里不太明白）
        transaction.getOutputUtxos().add(sendUtxo);
        //将剩余金额返回至发送方
        transaction.getOutputUtxos().add(backUtxo);
        transactionRepo.saveAndFlush(transaction);
        return true;
    }

    @Override
    public float getInputsValue(Transaction transaction) {
        float total = 0;
        Utxo output;
        for (String inpunt : transaction.getInputUtxosId()) {
            output = utxoRepo.getTransactionOutputById(inpunt);
            if (output == null) {
                return 0;
            } else {
                total += output.getValue();
            }
        }
        return total;
    }

    /**
     * 为交易生成签名
     *
     * @param privateKey  {@link PrivateKey}  签名私钥
     * @param transaction {@link Transaction} 交易
     */
    @Override
    public void generateSignature(PrivateKey privateKey, Transaction transaction) {
        transaction.setSignature(CryptoUtil.applySm2Signature(privateKey, transaction.getData()));
    }

    /**
     * 验证交易签名
     *
     * @param transaction {@link Transaction} 交易
     * @return
     */
    @Override
    public boolean isValidSignature(Transaction transaction) {
        Wallet senderWallet = latteChain.getUsers().get(transaction.getSenderString());
        transaction.setSender(senderWallet.getPublicKey());
        return CryptoUtil.verifySm2Signature(transaction.getSender(), transaction.getData(), transaction.getSignature());
    }

    @Override
    public String getTransaction(String id) {
        if (!transactionRepo.existsById(id)) {
            //查询交易不存在
            return null;
        } else {
            long startTime = System.currentTimeMillis();
            Transaction transaction = transactionRepo.getTransactionById(id);
            TransactionDigest digest = new TransactionDigest(id, transaction.getSenderString(),
                    transaction.getRecipientString(), transaction.getValue().toString(),
                    transaction.getRegistrationMsg(), Long.toString(transaction.getTimeStamp()));
            long endTime = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getName() + "消耗时间：" + (endTime - startTime) + "ms");
            return JsonUtil.toJson(digest);
        }

    }

    @Override
    public void modifyTransaction(String sender, String id,String newMsg) {

        Wallet senderUser = latteChain.getUsers().get(sender);//修改者地址
        long startTime = System.currentTimeMillis();
        Transaction transaction = transactionRepo.getTransactionById(id);//查询交易是否存在，并获取交易

        String modifyMessage = chameleonService.modifyMessage(transaction.getId(), senderUser,newMsg);

//        System.out.println("——————————————"+modifyMessage+"——————————————");

        if (modifyMessage != null) {
            transaction.setData(newMsg);//区块中merkle_root不变，但底层消息已经改变
            transactionRepo.save(transaction);
            System.out.println("修改交易信息耗时： " + (System.currentTimeMillis() - startTime) + "ms");
        }else {
            System.out.println("修改失败");
        }
    }
}
