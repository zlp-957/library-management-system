package com.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.RequireAdmin;
import com.library.common.Result;
import com.library.service.BorrowService;
import com.library.vo.BorrowVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 借阅接口
 */
@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    /** 借阅图书 */
    @PostMapping("/add")
    public Result<?> borrow(@RequestBody java.util.Map<String, Long> body) {
        borrowService.borrow(body.get("bookId"));
        return Result.success("借阅成功", null);
    }

    /** 归还图书 */
    @PostMapping("/return/{borrowId}")
    public Result<?> returnBook(@PathVariable Long borrowId) {
        borrowService.returnBook(borrowId);
        return Result.success("归还成功", null);
    }

    /** 我的借阅 */
    @GetMapping("/my")
    public Result<List<BorrowVO>> my(@RequestParam(required = false) Integer status) {
        return Result.success(borrowService.myBorrows(status));
    }

    /** 管理员：分页查询借阅记录 */
    @RequireAdmin
    @GetMapping("/page")
    public Result<Page<BorrowVO>> page(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer status) {
        return Result.success(borrowService.pageBorrows(page, size, keyword, status));
    }

    /** 管理员：手动触发逾期处理 */
    @RequireAdmin
    @PostMapping("/processOverdue")
    public Result<?> processOverdue() {
        borrowService.processOverdue();
        return Result.success("逾期处理完成", null);
    }
}
