package com.library.common;

/**
 * 系统常量
 */
public class Constants {

    /** 用户角色：管理员 */
    public static final int ROLE_ADMIN = 1;
    /** 用户角色：读者 */
    public static final int ROLE_READER = 0;

    /** 用户状态：正常 */
    public static final int USER_STATUS_NORMAL = 0;
    /** 用户状态：禁用 */
    public static final int USER_STATUS_DISABLED = 1;

    /** 借阅状态：借阅中 */
    public static final int BORROW_STATUS_BORROWING = 0;
    /** 借阅状态：已归还 */
    public static final int BORROW_STATUS_RETURNED = 1;
    /** 借阅状态：逾期转购买 */
    public static final int BORROW_STATUS_OVERDUE = 2;

    /** 预约状态：排队中 */
    public static final int RESERVE_STATUS_QUEUING = 0;
    /** 预约状态：待借阅（已通知） */
    public static final int RESERVE_STATUS_NOTIFIED = 1;
    /** 预约状态：已借阅 */
    public static final int RESERVE_STATUS_BORROWED = 2;
    /** 预约状态：已取消 */
    public static final int RESERVE_STATUS_CANCELED = 3;
    /** 预约状态：已失效 */
    public static final int RESERVE_STATUS_EXPIRED = 4;

    /** 购买类型：直接购买 */
    public static final int BUY_TYPE_DIRECT = 0;
    /** 购买类型：逾期自动转购买 */
    public static final int BUY_TYPE_OVERDUE = 1;

    /** 账号格式正则：读者 R + 8位数字 */
    public static final String REGEX_READER_ACCOUNT = "^R\\d{8}$";
    /** 账号格式正则：管理员 A + 6位数字 */
    public static final String REGEX_ADMIN_ACCOUNT = "^A\\d{6}$";
}
