package com.library.controller;

import com.library.common.Result;
import com.library.service.AuthService;
import com.library.vo.LoginVO;
import com.library.vo.RegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginVO vo) {
        return Result.success(authService.login(vo));
    }

    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterVO vo) {
        authService.register(vo);
        return Result.success("注册成功", null);
    }
}
