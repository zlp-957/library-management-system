package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.BusinessException;
import com.library.common.Constants;
import com.library.entity.*;
import com.library.mapper.BookMapper;
import com.library.mapper.BookReserveMapper;
import com.library.mapper.UserMapper;
import com.library.service.ReserveService;
import com.library.util.UserContext;
import com.library.vo.ReserveVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图书预约服务实现
 */
@Service
public class ReserveServiceImpl implements ReserveService {

    @Autowired
    private BookReserveMapper bookReserveMapper;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void reserve(Long bookId) {
        Long userId = UserContext.getUserId();
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (book.getStock() != null && book.getStock() > 0) {
            throw new BusinessException("图书有库存，可直接借阅，无需预约");
        }
        // 是否已有排队中的预约
        Long exists = bookReserveMapper.selectCount(new LambdaQueryWrapper<BookReserve>()
                .eq(BookReserve::getUserId, userId)
                .eq(BookReserve::getBookId, bookId)
                .eq(BookReserve::getStatus, Constants.RESERVE_STATUS_QUEUING));
        if (exists != null && exists > 0) {
            throw new BusinessException("您已预约该图书，请勿重复预约");
        }
        BookReserve reserve = new BookReserve();
        reserve.setUserId(userId);
        reserve.setBookId(bookId);
        reserve.setReserveTime(LocalDateTime.now());
        reserve.setStatus(Constants.RESERVE_STATUS_QUEUING);
        reserve.setCreateTime(LocalDateTime.now());
        bookReserveMapper.insert(reserve);
    }

    @Override
    public void cancel(Long reserveId) {
        Long userId = UserContext.getUserId();
        BookReserve reserve = bookReserveMapper.selectById(reserveId);
        if (reserve == null || !reserve.getUserId().equals(userId)) {
            throw new BusinessException("预约记录不存在");
        }
        if (reserve.getStatus() != Constants.RESERVE_STATUS_QUEUING
                && reserve.getStatus() != Constants.RESERVE_STATUS_NOTIFIED) {
            throw new BusinessException("当前状态不可取消");
        }
        reserve.setStatus(Constants.RESERVE_STATUS_CANCELED);
        bookReserveMapper.updateById(reserve);
    }

    @Override
    public List<ReserveVO> myReserves() {
        Long userId = UserContext.getUserId();
        List<BookReserve> reserves = bookReserveMapper.selectList(new LambdaQueryWrapper<BookReserve>()
                .eq(BookReserve::getUserId, userId)
                .orderByDesc(BookReserve::getCreateTime));
        return convert(reserves, userId);
    }

    @Override
    public Page<ReserveVO> pageReserves(Integer page, Integer size, String keyword, Integer status) {
        LambdaQueryWrapper<BookReserve> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(BookReserve::getStatus, status);
        }
        wrapper.orderByDesc(BookReserve::getCreateTime);
        Page<BookReserve> reservePage = bookReserveMapper.selectPage(new Page<>(page, size), wrapper);

        List<ReserveVO> voList = convert(reservePage.getRecords(), null);
        if (StringUtils.hasText(keyword)) {
            voList = voList.stream()
                    .filter(v -> (v.getUserName() != null && v.getUserName().contains(keyword))
                            || (v.getBookName() != null && v.getBookName().contains(keyword))
                            || (v.getUserAccount() != null && v.getUserAccount().contains(keyword)))
                    .collect(Collectors.toList());
        }
        Page<ReserveVO> voPage = new Page<>(page, size, reservePage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private List<ReserveVO> convert(List<BookReserve> reserves, Long currentUserId) {
        if (reserves.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = reserves.stream().map(BookReserve::getUserId).distinct().collect(Collectors.toList());
        List<Long> bookIds = reserves.stream().map(BookReserve::getBookId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Book> bookMap = bookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        return reserves.stream().map(r -> {
            ReserveVO vo = new ReserveVO();
            vo.setId(r.getId());
            vo.setUserId(r.getUserId());
            vo.setBookId(r.getBookId());
            vo.setReserveTime(r.getReserveTime());
            vo.setStatus(r.getStatus());
            vo.setValidDeadline(r.getValidDeadline());
            User u = userMap.get(r.getUserId());
            if (u != null) {
                vo.setUserAccount(u.getAccount());
                vo.setUserName(u.getName());
            }
            Book book = bookMap.get(r.getBookId());
            if (book != null) {
                vo.setBookName(book.getName());
                vo.setBookCover(book.getCover());
            }
            // 计算排队位次
            if (r.getStatus() == Constants.RESERVE_STATUS_QUEUING) {
                Long queueNo = bookReserveMapper.selectCount(new LambdaQueryWrapper<BookReserve>()
                        .eq(BookReserve::getBookId, r.getBookId())
                        .eq(BookReserve::getStatus, Constants.RESERVE_STATUS_QUEUING)
                        .le(BookReserve::getReserveTime, r.getReserveTime()));
                vo.setQueueNo(queueNo == null ? 1 : queueNo.intValue());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void processExpired() {
        LocalDateTime now = LocalDateTime.now();
        // 查询待借阅状态但已过有效期的预约
        List<BookReserve> expired = bookReserveMapper.selectList(new LambdaQueryWrapper<BookReserve>()
                .eq(BookReserve::getStatus, Constants.RESERVE_STATUS_NOTIFIED)
                .isNotNull(BookReserve::getValidDeadline)
                .lt(BookReserve::getValidDeadline, now));
        for (BookReserve reserve : expired) {
            reserve.setStatus(Constants.RESERVE_STATUS_EXPIRED);
            bookReserveMapper.updateById(reserve);
            // 唤醒下一位排队用户
            Book book = bookMapper.selectById(reserve.getBookId());
            if (book != null) {
                List<BookReserve> queuing = bookReserveMapper.selectList(new LambdaQueryWrapper<BookReserve>()
                        .eq(BookReserve::getBookId, book.getId())
                        .eq(BookReserve::getStatus, Constants.RESERVE_STATUS_QUEUING)
                        .orderByAsc(BookReserve::getReserveTime));
                if (!queuing.isEmpty()) {
                    BookReserve next = queuing.get(0);
                    next.setStatus(Constants.RESERVE_STATUS_NOTIFIED);
                    next.setValidDeadline(now.plusDays(3));
                    bookReserveMapper.updateById(next);
                }
            }
        }
    }
}
