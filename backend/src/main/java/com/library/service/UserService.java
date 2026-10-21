package com.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.User;
import com.library.vo.RegisterVO;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 分页查询读者
     */
    Page<User> pageReaders(Integer page, Integer size, String keyword);

    /**
     * 管理员新增读者
     */
    void addReader(RegisterVO vo);

    /**
     * 管理员编辑读者
     */
    void updateReader(Long id, String name, String password);

    /**
     * 启用/禁用读者
     */
    void toggleStatus(Long id);

    /**
     * 管理员给读者充值
     */
    void recharge(Long id, java.math.BigDecimal amount);

    /**
     * 获取当前登录用户信息
     */
    User getCurrentUser();

    /**
     * 修改当前用户信息
     */
    void updateProfile(String name);

    /**
     * 修改密码
     */
    void updatePassword(String oldPassword, String newPassword);
}
