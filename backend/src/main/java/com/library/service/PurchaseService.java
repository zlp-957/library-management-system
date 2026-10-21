package com.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.vo.PurchaseVO;

import java.util.List;

/**
 * 购买订单服务
 */
public interface PurchaseService {

    /**
     * 直接购买图书（扣余额）
     */
    void buy(Long bookId);

    /**
     * 我的订单
     */
    List<PurchaseVO> myOrders();

    /**
     * 管理员分页查询订单
     */
    Page<PurchaseVO> pageOrders(Integer page, Integer size, String keyword, Integer buyType);
}
