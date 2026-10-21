package com.library.service;

import com.library.vo.LoginVO;
import com.library.vo.RegisterVO;

import java.util.Map;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 登录
     */
    Map<String, Object> login(LoginVO vo);

    /**
     * 读者注册
     */
    void register(RegisterVO vo);
}
