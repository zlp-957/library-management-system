package com.library.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 借阅记录视图对象（含图书、用户信息）
 */
@Data
public class BorrowVO {

    private Long id;
    private Long userId;
    private String userAccount;
    private String userName;
    private Long bookId;
    private String bookName;
    private String bookCover;
    private LocalDateTime borrowTime;
    private LocalDateTime deadlineTime;
    private LocalDateTime returnTime;
    private Integer status;
    private Integer isOverdue;
}
