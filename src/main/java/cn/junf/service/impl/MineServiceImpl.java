package cn.junf.service.impl;

import cn.junf.entity.*;
import cn.junf.enums.LatteChainConfEnum;
import cn.junf.repository.BlockRepo;
import cn.junf.repository.TransactionPoolRepo;
import cn.junf.repository.TransactionRepo;
import cn.junf.repository.UtxoRepo;
import cn.junf.service.IChameleonService;
import cn.junf.service.IMineService;
import cn.junf.service.ITransactionService;
import cn.junf.service.IUserService;
import cn.junf.utils.BeanContext;
import cn.junf.utils.CryptoUtil;
import cn.junf.utils.LatteChain;
import cn.junf.utils.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 挖矿服务类
 */
@Service
@Slf4j
public class MineServiceImpl implements IMineService {

    /**
     * 获取一个块对象
     */
    private final LatteChain latteChain = LatteChain.getInstance();

    /**
     * 用户服务
     */
    private IUserService userService;
    /**
     * 交易服务
     */
    private ITransactionService transactionService;
    /**
     * 变色龙哈希服务
     */
    private IChameleonService chameleonService;

    /**
     * 数据库区块DAO对象
     */
    private BlockRepo blockDao;

    /**
     * 交易DAO对象
     */
    private TransactionRepo transactionDao;

    /**
     * 交易池DAO对象
     */
    private TransactionPoolRepo transactionPoolDao;
    /**
     * 全局UTXO DAO对象
     */
    private UtxoRepo utxoDao;

    /**
     * 是否在挖矿
     */
    private boolean isMin = true;

    /**
     * 初始化区块链系统
     *
     * @return boolean
     */
    @Override
    public boolean initChain() {
        if (latteChain.isInit()) {
            return false;//表示已经初始化了
        }
        userService = BeanContext.getApplicationContext().getBean(UserServiceImpl.class);
        transactionService = BeanContext.getApplicationContext().getBean(TransactionServiceImpl.class);
        transactionDao = BeanContext.getApplicationContext().getBean(TransactionRepo.class);
        utxoDao = BeanContext.getApplicationContext().getBean(UtxoRepo.class);
        chameleonService = BeanContext.getApplicationContext().getBean(ChameleonServiceImpl.class);

        //初始化系统预置用户信息
        userService.initUser();
        PublicKey coinbasePublicKey = userService.getUserPublicKey("admin");

        long chameleonSetupStartTime = System.currentTimeMillis();  //获取变色龙初始化开始时间

        //初始化变色龙哈希服务
        chameleonService.setup();

        long chameleonSetupEndTime = System.currentTimeMillis();  //获取变色龙初始化开始时间

        System.out.println("初始化变色龙init的时间：" + (chameleonSetupEndTime - chameleonSetupStartTime) + "ms");

        long distributeStartTime = System.currentTimeMillis(); //获取秘钥分发开始时间
        //设置每一个用户的变色龙哈希秘钥
        for (Wallet user : latteChain.getUsers().values()) {
            chameleonService.SetUserSecret(user);
        }

        long distributeEndTime = System.currentTimeMillis(); //获取秘钥分发结束时间
        System.out.println("变色龙初始化秘钥分发：" + (distributeEndTime - distributeStartTime) + "ms");

        //初始块奖励
        Utxo output = new Utxo(coinbasePublicKey, LatteChainConfEnum.BLOCK_SUBSIDY);
        utxoDao.save(output);
        Block genesisBlock = new Block("0", "The Times 03/Feb/2023 Chancellor on brink of second bailout for banks");
        this.mineNewBlock(genesisBlock);
        // 将创世快添加到区块链上
        this.addBlock(genesisBlock);
        for (Wallet user : latteChain.getUsers().values()) {
            user.getWorkerThread();
        }
        latteChain.setInit(true);

        return true;
    }

//    @Override
//    public void mineNewBlock(Block block) {//不是很懂这个挖矿流程
//        String difficultyString = LatteChainConfEnum.TARGET_HASH;//挖矿难度值
//        block.setMerkleRoot(CryptoUtil.calculateMerkleRoot((ArrayList<Transaction>) block.getTransactions()));//计算 默克尔树 根值
//        String hash = this.calculateBlockHash(block);  //该 区块 的哈希值
//        while (!hash.substring(0, LatteChainConfEnum.DIFFICULTY).equals(difficultyString)) {
//            block.setNonce(block.getNonce() + 1);
//            hash = this.calculateBlockHash(block);
//        }
//        block.setHash(hash);
//
//    }

    @Override
    public boolean mineNewBlock(Block block) {//不是很懂这个挖矿流程
        String difficultyString = LatteChainConfEnum.TARGET_HASH;//挖矿难度值
        block.setMerkleRoot(CryptoUtil.calculateMerkleRoot((ArrayList<Transaction>) block.getTransactions()));//计算 默克尔树 根值
        String hash = this.calculateBlockHash(block);  //该 区块 的哈希值
        while (!hash.substring(0, LatteChainConfEnum.DIFFICULTY).equals(difficultyString)) {
            block.setNonce(block.getNonce() + 1);
            hash = this.calculateBlockHash(block);
        }
        block.setHash(hash);
        return block.getHash().equals("") ? true : false;

    }

    /**
     * 执行交易并添加到区块中
     *
     * @param block        {@link Block} 区块
     * @param transactions {@link Transaction}
     * @return
     */
    @Override
    public boolean addTransaction(Block block, List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            if (!block.getPreviousHash().equals(LatteChainConfEnum.ZERO_HASH)) {
                //非初始块
                if (!transactionPoolDao.existsById(transaction.getId())) {
                    //当前交易已被消耗
                    return false;
                }
                if (!transactionService.processTransaction(transaction)) {
                    //交易不合法或当前交易已经被小号
                    return false;
                }
            }
            //将交易添加到区块中
            block.getTransactions().add(transaction);
        }
        return true;
    }

    /**
     * 发放奖励给矿工
     *
     * @param address 矿工账户
     * @param block   区块
     */
    @Override
    public void rewarMiner(String address, Block block) {
        System.out.println("address: " + address + "\n");
        float rewardValue = LatteChainConfEnum.BLOCK_SUBSIDY +
                LatteChainConfEnum.TRANSACTION_SUBSIDY * block.getTransactions().size();
        PublicKey account = userService.getUserPublicKey(address);
        System.out.println("account: " + account + "\n");
        Utxo reward = new Utxo(account, rewardValue);
        utxoDao.save(reward);
    }

    /**
     * 检查并添加新的区块到区块链中
     *
     * @param blockToAdd 带添加的区块
     */
    @Override
    public void addBlock(Block blockToAdd) {
        blockDao = BeanContext.getApplicationContext().getBean(BlockRepo.class);
        if (blockToAdd.getPreviousHash().equals(LatteChainConfEnum.ZERO_HASH)) {
            //当前待添加块为创世块
            blockToAdd.setId(0);
            //添加到数据库中
            blockDao.save(blockToAdd);
            log.info("[Initiation] 创世块已创建！LatteChain实例初始化成功");
        } else {
            blockDao.save(blockToAdd);
            log.info("[Issued Block]" + Thread.currentThread().getName() + " Mined ☺ :" + blockToAdd.getHash());
            //奖励矿工
            rewarMiner(blockToAdd.getMsg(), blockToAdd);
        }

    }


    /**
     * 计算得到合适的哈希值
     *
     * @param block 区块
     * @return
     */
    @Override
    public String calculateBlockHash(Block block) {
        return CryptoUtil.applySm3Hash(
                block.getId() +
                        block.getPreviousHash() +
                        block.getTimeStamp() +
                        block.getMerkleRoot() +
                        block.getNonce()
        );
    }

    /**
     * 挖矿函数，将构造新的区块并尝试计算其哈希值
     */
    @Override
    public void run() {


//        log.info("当前的线程名字"+Thread.currentThread().getName());

        transactionService = BeanContext.getApplicationContext().getBean(TransactionServiceImpl.class);
        userService = BeanContext.getApplicationContext().getBean(UserServiceImpl.class);
        blockDao = BeanContext.getApplicationContext().getBean(BlockRepo.class);
        transactionDao = BeanContext.getApplicationContext().getBean(TransactionRepo.class);
        transactionPoolDao = BeanContext.getApplicationContext().getBean(TransactionPoolRepo.class);
        utxoDao = BeanContext.getApplicationContext().getBean(UtxoRepo.class);
        chameleonService = BeanContext.getApplicationContext().getBean(ChameleonServiceImpl.class);

        ReentrantLock stateLock = LockUtil.getLockUtil().getStateLock();
        Condition condition = LockUtil.getLockUtil().getWriteCondition();


        while (true) {
            stateLock.lock();
            try {
                if (transactionPoolDao.getPoolSize() == 0) {
                    condition.await();
                } else {
                    log.info("当前的线程名字" + Thread.currentThread().getName());
                    //并行读取当前链的高度信息、前一区块的哈希值信息、当前交易池中的交易信息
                    long currenHeight = blockDao.getHeight();
                    //为啥减一：当前区块未上链，-1就能拿到上链的最后一个区块的哈希值
                    String preHash = blockDao.getBlockById(currenHeight - 1).getHash();
                    List<TransactionPoolEntity> transactionPool = transactionPoolDao.getTransactions();
                    List<Transaction> transactions = new ArrayList<>(LatteChainConfEnum.MAX_TRANSACTION_AMOUNT);
                    for (TransactionPoolEntity entity : transactionPool) {
                        transactions.add(transactionDao.getTransactionById(entity.getTransactionIndex()));
                    }
                    //构造区块
                    Block newBlock = new Block(preHash, Thread.currentThread().getName());
                    newBlock.setId(currenHeight);

                    //将所有交易池中获取的交易信息都添加到当前行构造的区块中
                    if (!addTransaction(newBlock, transactions)) {
                        //交易信息无效（签名信息错误、不满足最低金额、交易已被计算）
                        //挖矿失败，重新获取交易并创建、挖掘区块
                        continue;
                    }

                    //计算行区块的哈希值
                    mineNewBlock(newBlock);
                    //提交新的区块并获取奖励
                    addBlock(newBlock);
                    //将当前交易记在系统全局UTXO中并从池中删除已经消耗的交易
                    for (Transaction transaction : transactions) {
                        //将交易输出到全局
                        addToGlobalUtxo(transaction);
                        transactionPoolDao.deleteById(transaction.getId());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                stateLock.unlock();
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

/*        while (true) {
            try {
                if (transactionPoolDao.getPoolSize() == 0) {
                    continue;
                } else {

                    //并行读取当前链的高度信息、前一区块的哈希值信息、当前交易池中的交易信息
                    long currenHeight = blockDao.getHeight();
                    //为啥减一：当前区块未上链，-1就能拿到上链的最后一个区块的哈希值
                    String preHash = blockDao.getBlockById(currenHeight - 1).getHash();
                    List<TransactionPoolEntity> transactionPool = transactionPoolDao.getTransactions();
                    List<Transaction> transactions=new ArrayList<>(LatteChainConfEnum.MAX_TRANSACTION_AMOUNT);
                    for (TransactionPoolEntity entity:transactionPool){
                        transactions.add(transactionDao.getTransactionById(entity.getTransactionIndex()));
                    }
                    //构造区块
                    Block newBlock = new Block(preHash,Thread.currentThread().getName());
                    newBlock.setId(currenHeight);

                    //将所有交易池中获取的交易信息都添加到当前行构造的区块中
                    if (!addTransaction(newBlock,transactions)){
                        //交易信息无效（签名信息错误、不满足最低金额、交易已被计算）
                        //挖矿失败，重新获取交易并创建、挖掘区块
                        continue;
                    }
//                    boolean mineSuccess=mineNewBlock(newBlock);
//                    if (mineSuccess) {
//                        isMin = false;
//                    }
                    //计算行区块的哈希值
                    if(mineNewBlock(newBlock) && isMin){
                        stateLock.lock();
                        currenHeight = blockDao.getHeight();
                        //挖出相同块继续循环
                        if (blockDao.getBlockById(currenHeight-1).getPreviousHash().equals(newBlock.getPreviousHash())) continue;
                        isMin =false;
                        //提交新的区块并获取奖励
                        addBlock(newBlock);
                        //将当前交易记在系统全局UTXO中并从池中删除已经消耗的交易
                        for (Transaction transaction:transactions){
                            //将交易输出到全局
                            addToGlobalUtxo(transaction);
                            transactionPoolDao.deleteById(transaction.getId());
                        }
                        log.info("当前的线程名字"+Thread.currentThread().getName());
                        isMin=true;
                        stateLock.unlock();

                    }else continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            finally {
//
//                stateLock.unlock();
//            }
        }*/
    }

    /**
     * 将交易输出添加到全局UTXO
     *
     * @param transaction
     */
    public void addToGlobalUtxo(Transaction transaction) {
        for (Utxo output : transaction.getOutputUtxos()) {
            utxoDao.save(output);
        }
    }
}
