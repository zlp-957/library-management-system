package com.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.vo.ReserveVO;

import java.util.List;

/**
 * 图书预约服务
 */
public interface ReserveService {

    /**
     * 提交预约（库存=0时）
     */
    void reserve(Long bookId);

    /**
     * 取消预约
     */
    void cancel(Long reserveId);

    /**
     * 我的预约列表
     */
    List<ReserveVO> myReserves();

    /**
     * 管理员分页查询预约
     */
    Page<ReserveVO> pageReserves(Integer page, Integer size, String keyword, Integer status);

    /**
     * 处理失效预约（超过有效期未借阅，顺延下一位）
     */
    void processExpired();
}
