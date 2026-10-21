package com.library.vo;

import lombok.Data;

import java.util.Map;

/**
 * 统计数据视图对象
 */
@Data
public class StatVO {

    /** 图书总数 */
    private Long bookCount;
    /** 读者总数 */
    private Long readerCount;
    /** 借阅中数量 */
    private Long borrowingCount;
    /** 逾期转购买数量 */
    private Long overdueCount;
    /** 预约排队中数量 */
    private Long queuingReserveCount;
    /** 直接购买订单数 */
    private Long directPurchaseCount;
    /** 逾期转购买订单数 */
    private Long overduePurchaseCount;
    /** 购买总金额 */
    private java.math.BigDecimal totalPurchaseAmount;
}
