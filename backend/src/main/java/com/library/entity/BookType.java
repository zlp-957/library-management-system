package com.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图书分类表
 */
@Data
@TableName("book_type")
public class BookType {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String remark;

    @TableLogic
    private Integer isDelete;

    private LocalDateTime createTime;
}
