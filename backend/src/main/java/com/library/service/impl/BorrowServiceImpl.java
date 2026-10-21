package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.BusinessException;
import com.library.common.Constants;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.BorrowService;
import com.library.util.UserContext;
import com.library.vo.BorrowVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 借阅服务实现
 */
@Service
public class BorrowServiceImpl implements BorrowService {

    @Autowired
    private BorrowMapper borrowMapper;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;
    @Autowired
    private BookReserveMapper bookReserveMapper;

    @Value("${library.borrow.max-count}")
    private int maxCount;
    @Value("${library.borrow.days}")
    private int borrowDays;
    @Value("${library.punish.days}")
    private int punishDays;

    @Override
    @Transactional
    public void borrow(Long bookId) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() == Constants.USER_STATUS_DISABLED) {
            throw new BusinessException("账号已被禁用");
        }
        // 惩罚期内禁止借阅
        if (user.getPunishEndTime() != null && user.getPunishEndTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("您处于逾期惩罚期内，暂不能借阅（仍可购买）");
        }
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (book.getStock() == null || book.getStock() <= 0) {
            throw new BusinessException("图书库存不足，可提交预约");
        }
        // 已借数量校验
        Long borrowingCount = borrowMapper.selectCount(new LambdaQueryWrapper<Borrow>()
                .eq(Borrow::getUserId, userId)
                .eq(Borrow::getStatus, Constants.BORROW_STATUS_BORROWING));
        if (borrowingCount != null && borrowingCount >= maxCount) {
            throw new BusinessException("已借图书数量达到上限（" + maxCount + "本）");
        }
        // 库存 -1
        book.setStock(book.getStock() - 1);
        bookMapper.updateById(book);
        // 新增借阅记录
        Borrow borrow = new Borrow();
        borrow.setUserId(userId);
        borrow.setBookId(bookId);
        borrow.setBorrowTime(LocalDateTime.now());
        borrow.setDeadlineTime(LocalDateTime.now().plusDays(borrowDays));
        borrow.setStatus(Constants.BORROW_STATUS_BORROWING);
        borrow.setIsOverdue(0);
        borrow.setCreateTime(LocalDateTime.now());
        borrowMapper.insert(borrow);
    }

    @Override
    @Transactional
    public void returnBook(Long borrowId) {
        Long userId = UserContext.getUserId();
        Borrow borrow = borrowMapper.selectById(borrowId);
        if (borrow == null || !borrow.getUserId().equals(userId)) {
            throw new BusinessException("借阅记录不存在");
        }
        if (borrow.getStatus() != Constants.BORROW_STATUS_BORROWING) {
            throw new BusinessException("该记录已归还或已处理");
        }
        // 标记归还
        borrow.setStatus(Constants.BORROW_STATUS_RETURNED);
        borrow.setReturnTime(LocalDateTime.now());
        borrowMapper.updateById(borrow);
        // 库存 +1
        Book book = bookMapper.selectById(borrow.getBookId());
        book.setStock(book.getStock() + 1);
        bookMapper.updateById(book);
        // 触发预约唤醒：通知排队第一位
        notifyNextReserve(book);
    }

    /**
     * 归还后唤醒队列第一位预约用户
     */
    private void notifyNextReserve(Book book) {
        List<BookReserve> queuing = bookReserveMapper.selectList(new LambdaQueryWrapper<BookReserve>()
                .eq(BookReserve::getBookId, book.getId())
                .eq(BookReserve::getStatus, Constants.RESERVE_STATUS_QUEUING)
                .orderByAsc(BookReserve::getReserveTime));
        if (!queuing.isEmpty()) {
            BookReserve first = queuing.get(0);
            first.setStatus(Constants.RESERVE_STATUS_NOTIFIED);
            first.setValidDeadline(LocalDateTime.now().plusDays(3));
            bookReserveMapper.updateById(first);
        }
    }

    @Override
    public List<BorrowVO> myBorrows(Integer status) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Borrow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Borrow::getUserId, userId);
        if (status != null) {
            wrapper.eq(Borrow::getStatus, status);
        }
        wrapper.orderByDesc(Borrow::getCreateTime);
        List<Borrow> borrows = borrowMapper.selectList(wrapper);
        return convert(borrows);
    }

    @Override
    public Page<BorrowVO> pageBorrows(Integer page, Integer size, String keyword, Integer status) {
        LambdaQueryWrapper<Borrow> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Borrow::getStatus, status);
        }
        wrapper.orderByDesc(Borrow::getCreateTime);
        Page<Borrow> borrowPage = borrowMapper.selectPage(new Page<>(page, size), wrapper);

        List<BorrowVO> voList = convert(borrowPage.getRecords());
        // 关键词过滤（用户名/书名）
        if (StringUtils.hasText(keyword)) {
            voList = voList.stream()
                    .filter(v -> (v.getUserName() != null && v.getUserName().contains(keyword))
                            || (v.getBookName() != null && v.getBookName().contains(keyword))
                            || (v.getUserAccount() != null && v.getUserAccount().contains(keyword)))
                    .collect(Collectors.toList());
        }
        Page<BorrowVO> voPage = new Page<>(page, size, borrowPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private List<BorrowVO> convert(List<Borrow> borrows) {
        if (borrows.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = borrows.stream().map(Borrow::getUserId).distinct().collect(Collectors.toList());
        List<Long> bookIds = borrows.stream().map(Borrow::getBookId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Book> bookMap = bookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        return borrows.stream().map(b -> {
            BorrowVO vo = new BorrowVO();
            vo.setId(b.getId());
            vo.setUserId(b.getUserId());
            vo.setBookId(b.getBookId());
            vo.setBorrowTime(b.getBorrowTime());
            vo.setDeadlineTime(b.getDeadlineTime());
            vo.setReturnTime(b.getReturnTime());
            vo.setStatus(b.getStatus());
            vo.setIsOverdue(b.getIsOverdue());
            User u = userMap.get(b.getUserId());
            if (u != null) {
                vo.setUserAccount(u.getAccount());
                vo.setUserName(u.getName());
            }
            Book book = bookMap.get(b.getBookId());
            if (book != null) {
                vo.setBookName(book.getName());
                vo.setBookCover(book.getCover());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void processOverdue() {
        LocalDateTime now = LocalDateTime.now();
        // 查询逾期未还记录
        List<Borrow> overdueBorrows = borrowMapper.selectList(new LambdaQueryWrapper<Borrow>()
                .eq(Borrow::getStatus, Constants.BORROW_STATUS_BORROWING)
                .eq(Borrow::getIsOverdue, 0)
                .lt(Borrow::getDeadlineTime, now));
        for (Borrow borrow : overdueBorrows) {
            Book book = bookMapper.selectById(borrow.getBookId());
            if (book == null) {
                continue;
            }
            // 借阅记录标记为逾期转购买
            borrow.setStatus(Constants.BORROW_STATUS_OVERDUE);
            borrow.setIsOverdue(1);
            borrowMapper.updateById(borrow);
            // 创建逾期购买订单
            PurchaseOrder order = new PurchaseOrder();
            order.setUserId(borrow.getUserId());
            order.setBookId(borrow.getBookId());
            order.setOrderNo(generateOrderNo());
            order.setBuyType(Constants.BUY_TYPE_OVERDUE);
            order.setPrice(book.getPrice());
            order.setCreateTime(now);
            purchaseOrderMapper.insert(order);
            // 扣余额（若足够）
            User user = userMapper.selectById(borrow.getUserId());
            if (user != null) {
                if (user.getBalance().compareTo(book.getPrice()) >= 0) {
                    user.setBalance(user.getBalance().subtract(book.getPrice()));
                } else {
                    // 余额不足，置为负数表示欠费（毕设简化）
                    user.setBalance(user.getBalance().subtract(book.getPrice()));
                }
                // 设置惩罚：30天禁借
                user.setPunishEndTime(now.plusDays(punishDays));
                userMapper.updateById(user);
            }
            // 库存不变（图书已被用户买下）
        }
    }

    private String generateOrderNo() {
        return "PO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + (int) (Math.random() * 9000 + 1000);
    }
}
