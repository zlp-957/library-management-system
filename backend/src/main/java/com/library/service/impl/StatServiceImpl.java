package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.Constants;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.StatService;
import com.library.vo.StatVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 统计服务实现
 */
@Service
public class StatServiceImpl implements StatService {

    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BorrowMapper borrowMapper;
    @Autowired
    private BookReserveMapper bookReserveMapper;
    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Override
    public StatVO stat() {
        StatVO vo = new StatVO();
        vo.setBookCount(bookMapper.selectCount(null));
        vo.setReaderCount(userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, Constants.ROLE_READER)));
        vo.setBorrowingCount(borrowMapper.selectCount(new LambdaQueryWrapper<Borrow>()
                .eq(Borrow::getStatus, Constants.BORROW_STATUS_BORROWING)));
        vo.setOverdueCount(borrowMapper.selectCount(new LambdaQueryWrapper<Borrow>()
                .eq(Borrow::getStatus, Constants.BORROW_STATUS_OVERDUE)));
        vo.setQueuingReserveCount(bookReserveMapper.selectCount(new LambdaQueryWrapper<BookReserve>()
                .eq(BookReserve::getStatus, Constants.RESERVE_STATUS_QUEUING)));
        vo.setDirectPurchaseCount(purchaseOrderMapper.selectCount(new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getBuyType, Constants.BUY_TYPE_DIRECT)));
        vo.setOverduePurchaseCount(purchaseOrderMapper.selectCount(new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getBuyType, Constants.BUY_TYPE_OVERDUE)));

        // 购买总金额
        BigDecimal total = purchaseOrderMapper.selectList(null).stream()
                .map(PurchaseOrder::getPrice)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalPurchaseAmount(total);
        return vo;
    }
}
