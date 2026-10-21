package com.library.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 读者注册请求
 */
@Data
public class RegisterVO {

    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^R\\d{8}$", message = "读者账号格式不正确，应为 R + 8位数字，如 R20260001")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;
}
