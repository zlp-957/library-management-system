package com.library.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购买订单视图对象（含图书、用户信息）
 */
@Data
public class PurchaseVO {

    private Long id;
    private Long userId;
    private String userAccount;
    private String userName;
    private Long bookId;
    private String bookName;
    private String bookCover;
    private String orderNo;
    private Integer buyType;
    private BigDecimal price;
    private LocalDateTime createTime;
}
