package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.BusinessException;
import com.library.common.Constants;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.service.UserService;
import com.library.util.UserContext;
import com.library.vo.RegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public Page<User> pageReaders(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, Constants.ROLE_READER);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getAccount, keyword)
                    .or().like(User::getName, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        // 隐藏密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    @Override
    public void addReader(RegisterVO vo) {
        // 后端二次校验
        if (!vo.getAccount().matches(Constants.REGEX_READER_ACCOUNT)) {
            throw new BusinessException("读者账号格式不正确，应为 R + 8位数字，如 R20260001");
        }
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getAccount, vo.getAccount()));
        if (exists != null && exists > 0) {
            throw new BusinessException("该账号已存在");
        }
        User user = new User();
        user.setAccount(vo.getAccount());
        user.setPassword(encoder.encode(vo.getPassword()));
        user.setName(vo.getName());
        user.setRole(Constants.ROLE_READER);
        user.setStatus(Constants.USER_STATUS_NORMAL);
        user.setBalance(new BigDecimal("0.00"));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public void updateReader(Long id, String name, String password) {
        User user = userMapper.selectById(id);
        if (user == null || user.getRole() != Constants.ROLE_READER) {
            throw new BusinessException("读者不存在");
        }
        if (StringUtils.hasText(name)) {
            user.setName(name);
        }
        if (StringUtils.hasText(password)) {
            user.setPassword(encoder.encode(password));
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void toggleStatus(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("读者不存在");
        }
        user.setStatus(user.getStatus() == Constants.USER_STATUS_NORMAL
                ? Constants.USER_STATUS_DISABLED : Constants.USER_STATUS_NORMAL);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void recharge(Long id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充值金额必须大于0");
        }
        User user = userMapper.selectById(id);
        if (user == null || user.getRole() != Constants.ROLE_READER) {
            throw new BusinessException("读者不存在");
        }
        user.setBalance(user.getBalance().add(amount));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public User getCurrentUser() {
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateProfile(String name) {
        User user = userMapper.selectById(UserContext.getUserId());
        if (StringUtils.hasText(name)) {
            user.setName(name);
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        User user = userMapper.selectById(UserContext.getUserId());
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }
        user.setPassword(encoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
