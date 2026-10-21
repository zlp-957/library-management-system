package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.BusinessException;
import com.library.common.Constants;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.service.AuthService;
import com.library.util.JwtUtil;
import com.library.vo.LoginVO;
import com.library.vo.RegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public Map<String, Object> login(LoginVO vo) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getAccount, vo.getAccount()));
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        if (!encoder.matches(vo.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        if (user.getStatus() == Constants.USER_STATUS_DISABLED) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getAccount(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("account", user.getAccount());
        result.put("name", user.getName());
        result.put("role", user.getRole());
        result.put("balance", user.getBalance());
        return result;
    }

    @Override
    public void register(RegisterVO vo) {
        // 后端二次校验账号格式
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
}
