package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.BusinessException;
import com.library.common.Constants;
import com.library.entity.*;
import com.library.mapper.BookMapper;
import com.library.mapper.PurchaseOrderMapper;
import com.library.mapper.UserMapper;
import com.library.service.PurchaseService;
import com.library.util.UserContext;
import com.library.vo.PurchaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购买订单服务实现
 */
@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void buy(Long bookId) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() == Constants.USER_STATUS_DISABLED) {
            throw new BusinessException("账号已被禁用");
        }
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (book.getStock() == null || book.getStock() <= 0) {
            throw new BusinessException("图书库存不足");
        }
        if (book.getPrice() == null) {
            throw new BusinessException("图书未设置售价");
        }
        if (user.getBalance().compareTo(book.getPrice()) < 0) {
            throw new BusinessException("余额不足，请联系管理员充值");
        }
        // 扣余额
        user.setBalance(user.getBalance().subtract(book.getPrice()));
        userMapper.updateById(user);
        // 库存 -1
        book.setStock(book.getStock() - 1);
        bookMapper.updateById(book);
        // 生成订单
        PurchaseOrder order = new PurchaseOrder();
        order.setUserId(userId);
        order.setBookId(bookId);
        order.setOrderNo(generateOrderNo());
        order.setBuyType(Constants.BUY_TYPE_DIRECT);
        order.setPrice(book.getPrice());
        order.setCreateTime(LocalDateTime.now());
        purchaseOrderMapper.insert(order);
    }

    @Override
    public List<PurchaseVO> myOrders() {
        Long userId = UserContext.getUserId();
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getUserId, userId)
                .orderByDesc(PurchaseOrder::getCreateTime));
        return convert(orders);
    }

    @Override
    public Page<PurchaseVO> pageOrders(Integer page, Integer size, String keyword, Integer buyType) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (buyType != null) {
            wrapper.eq(PurchaseOrder::getBuyType, buyType);
        }
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);
        Page<PurchaseOrder> orderPage = purchaseOrderMapper.selectPage(new Page<>(page, size), wrapper);

        List<PurchaseVO> voList = convert(orderPage.getRecords());
        if (StringUtils.hasText(keyword)) {
            voList = voList.stream()
                    .filter(v -> (v.getUserName() != null && v.getUserName().contains(keyword))
                            || (v.getBookName() != null && v.getBookName().contains(keyword))
                            || (v.getUserAccount() != null && v.getUserAccount().contains(keyword))
                            || (v.getOrderNo() != null && v.getOrderNo().contains(keyword)))
                    .collect(Collectors.toList());
        }
        Page<PurchaseVO> voPage = new Page<>(page, size, orderPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private List<PurchaseVO> convert(List<PurchaseOrder> orders) {
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = orders.stream().map(PurchaseOrder::getUserId).distinct().collect(Collectors.toList());
        List<Long> bookIds = orders.stream().map(PurchaseOrder::getBookId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Book> bookMap = bookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        return orders.stream().map(o -> {
            PurchaseVO vo = new PurchaseVO();
            vo.setId(o.getId());
            vo.setUserId(o.getUserId());
            vo.setBookId(o.getBookId());
            vo.setOrderNo(o.getOrderNo());
            vo.setBuyType(o.getBuyType());
            vo.setPrice(o.getPrice());
            vo.setCreateTime(o.getCreateTime());
            User u = userMap.get(o.getUserId());
            if (u != null) {
                vo.setUserAccount(u.getAccount());
                vo.setUserName(u.getName());
            }
            Book book = bookMap.get(o.getBookId());
            if (book != null) {
                vo.setBookName(book.getName());
                vo.setBookCover(book.getCover());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private String generateOrderNo() {
        return "PO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + (int) (Math.random() * 9000 + 1000);
    }
}
