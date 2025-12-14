package cn.junf.crypto;

import cn.junf.entity.Vote;
import cn.junf.entity.Wallet;
import cn.junf.utils.LatteChain;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

import cn.junf.enums.LatteChainConfEnum;

public class PBCSecretShare {

    private final LatteChain latteChain = LatteChain.getInstance();
    private Pairing pairing;
    /**
     * G1生成元
     */
    private Element P;

    private Field g1;


    private Field zr;

    private Element g;  //G1群生成元


    private Element secret;


    private static BigInteger p;

    private static Random random;

    public static PBCSecretShare pbcSecretShare;

    public static Element[] coefficients = new Element[LatteChainConfEnum.THRESHOLD];

    /**
     * 初始化方法
     */

    public static PBCSecretShare getPbcSecretShare() {
        return pbcSecretShare;
    }

    public static void setPBCSecretSetup(Pairing pairing, Field g1, Field zr, Element P, Element g) {
        PBCSecretShare pbcSecretShare = new PBCSecretShare();
        pbcSecretShare.pairing = pairing;
        pbcSecretShare.g1 = g1;
        pbcSecretShare.zr = zr;
        pbcSecretShare.P = P;
        pbcSecretShare.g = g;
        PBCSecretShare.pbcSecretShare = pbcSecretShare;
    }



    /**
     * 初始化f(x)多项式
     * 用户m  门限t
     * 用来存放t-1次多项式
     */
    public void generatePolynomial() {
        int t = LatteChainConfEnum.THRESHOLD;
        coefficients[0] = secret;
        for (int i = 1; i < t; i++) {
            coefficients[i] = generateRandomElement();
        }
    }

    /**
     * 生成小于p的随机数
     *
     * @return
     */
    public Element generateRandomElement() {
        Element result = this.zr.newRandomElement().getImmutable();
        return result;
    }

    /**
     * 计算陷门碎片
     * 为指定的用户计算秘密份额
     *
     * @param userX
     * @return
     */
    public Element computeUserShares(Element userX) {
        Element result = this.zr.newRandomElement().setToZero().getImmutable();

        for (int i = 0; i < LatteChainConfEnum.THRESHOLD; i++) {
            Element cur = coefficients[i].mul(userX.powZn(zr.newRandomElement().set(i)));
            result = result.add(cur);

        }
        return result;
    }


    /**
     * 将String转化为Element  分离出来 计算时间的时候可以不用算上类型转换的时间
     *
     * @param votes
     * @return
     */
    public List<Vote> stringConversionElement(List<Vote> votes) {
//        for (Vote vote : votes) {
//            vote.setEtrapX(this.zr.newElementFromBytes(vote.getTrapX().getBytes(StandardCharsets.UTF_8)));
//            vote.setEtrapY(this.zr.newElementFromBytes(vote.getTrapY().getBytes(StandardCharsets.UTF_8)));
//
//        }
//        Wallet senderWallet = latteChain.getUsers().get(sender);
        Wallet senderUser;
        for (Vote vote : votes){
            senderUser = latteChain.getUsers().get(vote.getUserPk());
            vote.setEtrapX(senderUser.getTrap().getX());
            vote.setEtrapY(senderUser.getTrap().getShare());

        }

        return votes;
    }

    /**
     * 秘密重建算法
     * @param votes
     * @return
     * @throws Exception
     */
    public Element reconstruction(List<Vote> votes) throws Exception {
        List<Vote> voteList = stringConversionElement(votes);
        long reconstructionStartTime = System.nanoTime();    //获取恢复秘钥 开始时间
        Element[] userX = new Element[LatteChainConfEnum.INIT_ACCOUNT_AMOUNTS];
        for (int i = 0; i < LatteChainConfEnum.THRESHOLD; i++) {
            userX[i]=voteList.get(i).getEtrapX();
        }

        Element result = this.zr.newRandomElement().setToZero().getImmutable();
        for (int i = 0; i < LatteChainConfEnum.THRESHOLD; i++) {
            result = result.add(interpolation(voteList.get(i).getEtrapY(), userX, voteList.get(i).getEtrapX(), LatteChainConfEnum.THRESHOLD));
        }
        long reconstructionEndTime = System.nanoTime();
        System.out.println("重建秘密消耗时间："+(reconstructionEndTime-reconstructionStartTime)+"ps");
        System.out.println();//获取恢复秘钥 开始时间
        return result;
    }


    /**
     * 求第i号用户(xK = i + 1)的了拉格朗日插值
     * <p>
     * //     * @param values
     *
     * @param t
     * @return 一次只算一个累加子式
     */
    public Element interpolation(Element share, Element[] userX, Element userX_i, int t) {
        Element result = this.zr.newRandomElement().setToOne().getImmutable();

        //i代表第i个用户的份额
        for (int i = 0; i < t; i++) {
            Element up = this.zr.newRandomElement().setToOne().getImmutable();
            Element down = this.zr.newRandomElement().setToOne().getImmutable();
            if (userX_i.equals(userX[i])) {
                continue;
            }
            up = up.mul(zr.newRandomElement().setToZero().sub(userX[i]));// 0-x
            down = down.mul(userX_i.sub(userX[i]));
            result = result.mul(up.mul(down.invert()));
        }

        result = result.mul(share);
        return result;
    }

    public Element getSecret() {
        return secret;
    }

    public void setSecret(Element secret) {
        this.secret = secret;
    }



}
