package com.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.RequireAdmin;
import com.library.common.Result;
import com.library.entity.User;
import com.library.service.UserService;
import com.library.vo.RegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /** 当前登录用户信息 */
    @GetMapping("/info")
    public Result<User> info() {
        return Result.success(userService.getCurrentUser());
    }

    /** 修改个人信息 */
    @PostMapping("/profile")
    public Result<?> updateProfile(@RequestBody Map<String, String> body) {
        userService.updateProfile(body.get("name"));
        return Result.success();
    }

    /** 修改密码 */
    @PostMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> body) {
        userService.updatePassword(body.get("oldPassword"), body.get("newPassword"));
        return Result.success("密码修改成功", null);
    }

    /** 管理员：分页查询读者 */
    @RequireAdmin
    @GetMapping("/readers")
    public Result<Page<User>> readers(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String keyword) {
        return Result.success(userService.pageReaders(page, size, keyword));
    }

    /** 管理员：新增读者 */
    @RequireAdmin
    @PostMapping("/reader")
    public Result<?> addReader(@Validated @RequestBody RegisterVO vo) {
        userService.addReader(vo);
        return Result.success("新增读者成功", null);
    }

    /** 管理员：编辑读者 */
    @RequireAdmin
    @PutMapping("/reader/{id}")
    public Result<?> updateReader(@PathVariable Long id,
                                  @RequestBody Map<String, String> body) {
        userService.updateReader(id, body.get("name"), body.get("password"));
        return Result.success("编辑成功", null);
    }

    /** 管理员：启用/禁用读者 */
    @RequireAdmin
    @PostMapping("/reader/{id}/toggle")
    public Result<?> toggleStatus(@PathVariable Long id) {
        userService.toggleStatus(id);
        return Result.success();
    }

    /** 管理员：充值 */
    @RequireAdmin
    @PostMapping("/reader/{id}/recharge")
    public Result<?> recharge(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        userService.recharge(id, body.get("amount"));
        return Result.success("充值成功", null);
    }
}
