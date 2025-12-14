package cn.junf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author junf
 * @since 2023-06-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Vote implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String userPk;

    private String transactionId;

    /**
     * 陷门碎片 字符串类型
     */
    private String trapY;

    /**
     * 陷门私钥 字符串类型
     */
    private String trapX;

    /**
     * Element类型的陷门碎片
     */
    private Element EtrapY;

    /**
     * Element类型的 陷门秘钥
     */
    private Element EtrapX;


}
