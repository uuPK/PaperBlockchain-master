package cn.junf.service.impl;

import cn.junf.crypto.JPBCRSAAccumulator;
import cn.junf.crypto.PBCSecretShare;
import cn.junf.entity.Transaction;
import cn.junf.entity.UserChameleonTrap;
import cn.junf.entity.Wallet;
import cn.junf.repository.TransactionRepo;
import cn.junf.service.IChameleonService;
import cn.junf.utils.CryptoUtil;
import cn.junf.utils.MsgToHash;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.HashMap;

@Service
public class ChameleonServiceImpl implements IChameleonService {
    private Pairing pairing;

    /**
     * G1生成元
     */
    private Element P;

    private Field g1;

    private Field zr;

    private Element g;  //G1群生成元

    /**
     * 交易DAO对象
     */
    @Autowired
    private TransactionRepo transactionRepo;

    @Override
    public void setup() {
        pairing = PairingFactory.getPairing("crypto/a.properties");
        this.g1 = pairing.getG1();
        this.zr = pairing.getZr();
        this.P = g1.newRandomElement().getImmutable();
        this.g = g1.newRandomElement().getImmutable();
        PBCSecretShare.setPBCSecretSetup(pairing, g1, zr, P, g);
        JPBCRSAAccumulator.JPBCRSAAccumulatorSetup(pairing, g1, zr, g);
//        long polynomialStartTime = System.currentTimeMillis();  //生成多项式开始时间
        //生成秘密
        Element element = this.zr.newRandomElement().getImmutable();
        PBCSecretShare.getPbcSecretShare().setSecret(element);
        //生成多项式
        PBCSecretShare.getPbcSecretShare().generatePolynomial();
//        long polynomialEndTime = System.currentTimeMillis();  //生成多项式结束时间
//        System.out.println("生成多项式时间：" + (polynomialEndTime - polynomialStartTime) + "ms");

    }

    @Override
    public void SetUserSecret(Wallet user) {
        /**
         * 用户秘密
         */
        Element x = zr.newRandomElement().getImmutable();//用户陷门私钥
        Element y = g.powZn(x).getImmutable();//用户陷门公钥
        Element userShares = PBCSecretShare.getPbcSecretShare().computeUserShares(x);//陷门碎片
        user.setTrap(new UserChameleonTrap(x, y, userShares));
    }

    /**
     * 生成不用有多余的修改
     *
     * @param transaction 交易信息
     * @param user        {@link Wallet}  用户
     */
    @Override
    public void generateHash(Transaction transaction, Wallet user) {
        long startYime = System.currentTimeMillis();//获取开始时间

        Element r = zr.newRandomElement().getImmutable();

        //将msg转换成hash值，并将该hash值映射到Element上
        Element msg = MsgToHash.GennerateMsgToHash(transaction.getData(), this.zr);

        /* 理论上可行，但计算上不可行
        //gPowMsg = g ^ msg
        Element gPowMsg = this.g.powZn(msg).getImmutable();


        //yPowR = y ^ r = pk ^ r
        Element yPowR = user.getTrap().getY().powZn(r).getImmutable();

        // hashResult = gPowMsg * yPowR = (g ^ msg ) * (pk ^ r) (mod p)
        Element hashResult = gPowMsg.mulZn(yPowR).getImmutable();

         */

        //hash = g ^ ( m + xr )
        Element hashResult = this.g.powZn(msg.add(user.getTrap().getX().mulZn(r))).getImmutable();

        String hash = CryptoUtil.applySha256Hash(hashResult.toBytes());

        user.getTrap().getKMap().put(hash, r);//r值后面进行修改的时候会用到了r值，用来对应修改交易

        //设置交易ID以及交易信息
        transaction.setId(hash);
        transaction.setRegistrationMsg(String.valueOf(msg.hashCode()));

        long endTime = System.currentTimeMillis(); //获取结束时间

        System.out.println("变色龙哈希计算时间：" + (endTime - startYime) + "ms");

    }


    /**
     * 对交易信息进行修改  修改前要进行投票  投票通过才能改
     *
     * @param hash 原先的交易哈希值
     * @param user {@link Wallet} 用户
     * @return 新生成的消息
     */
    @Override
    public String modifyMessage(String hash, Wallet user, String newMsg) {
        long startTime = System.currentTimeMillis();

        Element msg = MsgToHash.GennerateMsgToHash(transactionRepo.getTransactionById(hash).getData(), this.zr);

        Element updateMsg = MsgToHash.GennerateMsgToHash(newMsg, this.zr);

        //更新RSA累加器状态
        BigInteger acc = updateMsg.toBigInteger();
        JPBCRSAAccumulator jpbcrsaAccumulator = JPBCRSAAccumulator.getJpbcrsaAccumulator();
        jpbcrsaAccumulator.addElement(acc);

        //subMsg = m - m'
        Element subMsg = msg.sub(updateMsg);

        //XmulM = subMsg * (1/x) = (m - m') / x
        Element XmulM = subMsg.mul(user.getTrap().getX().invert());

        // R是新的随机数,r是旧的随机数 R = XmulM + r
        Element R = XmulM.add(user.getTrap().getKMap().get(hash));
        long endTime = System.currentTimeMillis();

        System.out.println("计算新R的耗时：" + (endTime - startTime) + "ms");
        if (verifyHash(hash, updateMsg, user.getTrap(), R)) {
//            return String.valueOf(calM.hashCode());
            return "新算法验证成功";
        } else {
            return null;
        }

    }

    /**
     * 验证新生成的交易信息是否满足变色龙哈希特性（即哈希值不变）
     * //     * @param msg       新的交易信息
     *
     * @param hash 原Hash值
     * @param trap 用户秘钥
     *             //     * @param usingR    r'
     * @return 验证成功返回true
     */
    @Override
    public boolean verifyHash(String hash, Element updateMsg, UserChameleonTrap trap, Element R) {

        //计算行hash' = m'P +r'Y = (m' + rx)P
        long startTime = System.currentTimeMillis();

        //计算新的哈希值  newHash = g ^ (m' + xr')
        Element newHash = this.g.powZn(updateMsg.add(trap.getX().mul(R)));

        String newHashStr = CryptoUtil.applySha256Hash(newHash.toBytes());

        long endTime = System.currentTimeMillis();//获取结束时间
//        System.out.println("编辑验证时间：" + (endTime - startTime) + "ms");//输出程序运行时间

        return newHashStr.equals(hash);
    }

    @Override
    public Element generateMemCer(String newMsg) {
        Element updateMsg = MsgToHash.GennerateMsgToHash(newMsg, this.zr);
        BigInteger acc = updateMsg.toBigInteger();
        return JPBCRSAAccumulator.getJpbcrsaAccumulator().generateW(acc);
    }

    @Override
    public boolean verifyMemCer(byte[] memCer, String newMsg) {
        Element updateMsg = MsgToHash.GennerateMsgToHash(newMsg, this.zr);
        BigInteger acc = updateMsg.toBigInteger();
        long startTimeMs = System.currentTimeMillis();//获取开始时间
        long startTime = System.nanoTime();//获取开始时间
        boolean b = JPBCRSAAccumulator.getJpbcrsaAccumulator().verifyElement(memCer, acc);
        long endTime = System.nanoTime();//获取开始时间
        long endTimeMS = System.currentTimeMillis();//获取开始时间
        System.out.println("RSA验证时间：" + (endTime - startTime) + "ns");
        System.out.println("RSA验证时间：" + (endTimeMS - startTimeMs) + "ms");

        return b;
    }
}