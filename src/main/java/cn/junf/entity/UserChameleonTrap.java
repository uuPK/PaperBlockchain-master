package cn.junf.entity;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;

import java.util.HashMap;

/**
 * 用户哈希交易
 */
@Data
public class UserChameleonTrap {
    Element x;
    Element y;
    //秘密碎片
    Element share;

    HashMap<String, Element> KMap;

    public UserChameleonTrap(Element x, Element y, Element share) {
        this.x = x;
        this.y = y;
        this.share = share;
        this.KMap = new HashMap<>();
    }
}
