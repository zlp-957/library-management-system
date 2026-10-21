package com.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购买订单表
 */
@Data
@TableName("purchase_order")
public class PurchaseOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long bookId;

    /** 订单编号 */
    private String orderNo;

    /** 购买类型：0直接购买 1逾期自动转购买 */
    private Integer buyType;

    /** 成交价格 */
    private BigDecimal price;

    @TableLogic
    private Integer isDelete;

    private LocalDateTime createTime;
}
