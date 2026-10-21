package com.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.RequireAdmin;
import com.library.common.Result;
import com.library.service.PurchaseService;
import com.library.vo.PurchaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购买订单接口
 */
@RestController
@RequestMapping("/purchase")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    /** 直接购买图书 */
    @PostMapping("/buy/{bookId}")
    public Result<?> buy(@PathVariable Long bookId) {
        purchaseService.buy(bookId);
        return Result.success("购买成功", null);
    }

    /** 我的订单 */
    @GetMapping("/my")
    public Result<List<PurchaseVO>> my() {
        return Result.success(purchaseService.myOrders());
    }

    /** 管理员：分页查询订单 */
    @RequireAdmin
    @GetMapping("/page")
    public Result<Page<PurchaseVO>> page(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer buyType) {
        return Result.success(purchaseService.pageOrders(page, size, keyword, buyType));
    }
}
