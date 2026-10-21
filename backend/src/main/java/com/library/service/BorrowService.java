package com.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.vo.BorrowVO;

import java.util.List;

/**
 * 借阅服务
 */
public interface BorrowService {

    /**
     * 借阅图书（免费）
     */
    void borrow(Long bookId);

    /**
     * 归还图书
     */
    void returnBook(Long borrowId);

    /**
     * 我的借阅记录
     */
    List<BorrowVO> myBorrows(Integer status);

    /**
     * 管理员分页查询借阅记录
     */
    Page<BorrowVO> pageBorrows(Integer page, Integer size, String keyword, Integer status);

    /**
     * 逾期定时任务：扫描逾期未还记录，转为购买 + 惩罚
     */
    void processOverdue();
}
