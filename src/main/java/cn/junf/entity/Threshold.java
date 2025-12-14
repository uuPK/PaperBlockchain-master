package cn.junf.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class Threshold implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "transaction_id", type = IdType.ID_WORKER_STR)
    private String transactionId;

    private Integer thresholdT;


}
