package com.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 借阅记录表
 */
@Data
@TableName("borrow")
public class Borrow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long bookId;

    /** 借阅时间 */
    private LocalDateTime borrowTime;

    /** 应还时间 */
    private LocalDateTime deadlineTime;

    /** 实际归还时间 */
    private LocalDateTime returnTime;

    /** 状态：0借阅中 1已归还 2逾期转购买 */
    private Integer status;

    /** 是否已逾期转购买：0否 1是 */
    private Integer isOverdue;

    private LocalDateTime createTime;
}
