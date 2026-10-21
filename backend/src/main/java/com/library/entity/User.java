package com.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账号：读者 R+8位数字 / 管理员 A+6位数字 */
    private String account;

    private String password;

    private String name;

    /** 角色：0读者 1管理员 */
    private Integer role;

    /** 状态：0正常 1禁用 */
    private Integer status;

    /** 余额 */
    private BigDecimal balance;

    /** 惩罚截止时间（在此之前禁止借阅） */
    private LocalDateTime punishEndTime;

    @TableLogic
    private Integer isDelete;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
