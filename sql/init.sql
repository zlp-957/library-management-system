-- ============================================
-- 图书管理系统 数据库初始化脚本
-- 数据库：library
-- 说明：免费借阅 + 付费购买 + 预约排队
-- ============================================

CREATE DATABASE IF NOT EXISTS `library` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `library`;

-- --------------------------------------------
-- 1. user 用户表
-- --------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `account`       VARCHAR(32)  NOT NULL COMMENT '账号：读者R+8位数字 / 管理员A+6位数字',
  `password`      VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `name`          VARCHAR(50)  NOT NULL COMMENT '姓名',
  `role`          TINYINT      NOT NULL DEFAULT 0 COMMENT '角色：0读者 1管理员',
  `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0正常 1禁用',
  `balance`       DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
  `punish_end_time` DATETIME   DEFAULT NULL COMMENT '惩罚截止时间（此前禁止借阅）',
  `is_delete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  `create_time`   DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`   DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- --------------------------------------------
-- 2. book_type 图书分类表
-- --------------------------------------------
DROP TABLE IF EXISTS `book_type`;
CREATE TABLE `book_type` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(50) NOT NULL COMMENT '分类名称',
  `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `is_delete`   TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME    DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- --------------------------------------------
-- 3. book 图书表
-- --------------------------------------------
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(100)  NOT NULL COMMENT '书名',
  `author`      VARCHAR(50)   DEFAULT NULL COMMENT '作者',
  `isbn`        VARCHAR(32)   DEFAULT NULL COMMENT 'ISBN',
  `type_id`     BIGINT        DEFAULT NULL COMMENT '分类id',
  `price`       DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '售价',
  `stock`       INT           NOT NULL DEFAULT 0 COMMENT '库存',
  `cover`       VARCHAR(255)  DEFAULT NULL COMMENT '封面图片地址',
  `intro`       TEXT          DEFAULT NULL COMMENT '简介',
  `is_delete`   TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME      DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME      DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type_id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- --------------------------------------------
-- 4. borrow 借阅记录表
-- --------------------------------------------
DROP TABLE IF EXISTS `borrow`;
CREATE TABLE `borrow` (
  `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT   NOT NULL COMMENT '借阅用户id',
  `book_id`       BIGINT   NOT NULL COMMENT '图书id',
  `borrow_time`   DATETIME NOT NULL COMMENT '借阅时间',
  `deadline_time` DATETIME NOT NULL COMMENT '应还时间',
  `return_time`   DATETIME DEFAULT NULL COMMENT '实际归还时间',
  `status`        TINYINT  NOT NULL DEFAULT 0 COMMENT '状态：0借阅中 1已归还 2逾期转购买',
  `is_overdue`    TINYINT  NOT NULL DEFAULT 0 COMMENT '是否已逾期转购买：0否 1是',
  `create_time`   DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_book` (`book_id`),
  KEY `idx_status_deadline` (`status`, `deadline_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- --------------------------------------------
-- 5. purchase_order 购买订单表
-- --------------------------------------------
DROP TABLE IF EXISTS `purchase_order`;
CREATE TABLE `purchase_order` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT        NOT NULL COMMENT '购买用户id',
  `book_id`     BIGINT        NOT NULL COMMENT '图书id',
  `order_no`    VARCHAR(40)   NOT NULL COMMENT '订单编号',
  `buy_type`    TINYINT       NOT NULL DEFAULT 0 COMMENT '购买类型：0直接购买 1逾期自动转购买',
  `price`       DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '成交价格',
  `is_delete`   TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME      DEFAULT NULL COMMENT '下单时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_book` (`book_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购买订单表';

-- --------------------------------------------
-- 6. book_reserve 图书预约表
-- --------------------------------------------
DROP TABLE IF EXISTS `book_reserve`;
CREATE TABLE `book_reserve` (
  `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`        BIGINT   NOT NULL COMMENT '预约用户id',
  `book_id`        BIGINT   NOT NULL COMMENT '图书id',
  `reserve_time`   DATETIME NOT NULL COMMENT '预约时间（FIFO排队依据）',
  `status`         TINYINT  NOT NULL DEFAULT 0 COMMENT '状态：0排队中 1待借阅 2已借阅 3已取消 4已失效',
  `valid_deadline` DATETIME DEFAULT NULL COMMENT '通知后有效期限（3天内需借阅）',
  `is_delete`      TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time`    DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_book_status_time` (`book_id`, `status`, `reserve_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书预约表';

-- --------------------------------------------
-- 7. notice 公告表
-- --------------------------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`       VARCHAR(100) NOT NULL COMMENT '公告标题',
  `content`     TEXT         DEFAULT NULL COMMENT '公告内容',
  `is_delete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ============================================
-- 初始化数据
-- ============================================

-- 管理员账号 A000001 / 密码 admin123（BCrypt 加密）
INSERT INTO `user` (`account`, `password`, `name`, `role`, `status`, `balance`, `create_time`, `update_time`) VALUES
('A000001', '$2b$10$yG1f/2nNV2ARV9ZyWxathOYIBBcrg8cuE1nuLskyVRCVManNy3FvK', '系统管理员', 1, 0, 0.00, NOW(), NOW());

-- 示例读者（密码 reader123，R20260001 / R20260002）
INSERT INTO `user` (`account`, `password`, `name`, `role`, `status`, `balance`, `create_time`, `update_time`) VALUES
('R20260001', '$2b$10$3B6ofCFniPyayQlwCN0xAurjXjxrkR3RoOvRvvKuavpJNHW.Xz.E6', '张三', 0, 0, 100.00, NOW(), NOW()),
('R20260002', '$2b$10$3B6ofCFniPyayQlwCN0xAurjXjxrkR3RoOvRvvKuavpJNHW.Xz.E6', '李四', 0, 0, 50.00, NOW(), NOW());

-- 图书分类
INSERT INTO `book_type` (`name`, `remark`, `create_time`) VALUES
('计算机', '计算机类图书', NOW()),
('文学', '文学类图书', NOW()),
('历史', '历史类图书', NOW()),
('科学', '科学类图书', NOW()),
('经济管理', '经济管理类图书', NOW());

-- 示例图书
INSERT INTO `book` (`name`, `author`, `isbn`, `type_id`, `price`, `stock`, `intro`, `create_time`, `update_time`) VALUES
('Java核心技术 卷I', '凯·S.霍斯特曼', '9787111636664', 1, 149.00, 5, 'Java领域经典著作，全面讲解Java语言核心概念。', NOW(), NOW()),
('深入理解Java虚拟机', '周志明', '9787111641247', 1, 129.00, 3, 'JVM原理与调优的必读之作。', NOW(), NOW()),
('Spring实战（第6版）', '克雷格·沃斯', '9787115571243', 1, 139.00, 4, 'Spring框架的权威指南。', NOW(), NOW()),
('活着', '余华', '9787506365437', 2, 35.00, 8, '讲述一个人和他的命运之间的友情。', NOW(), NOW()),
('百年孤独', '加西亚·马尔克斯', '9787544253994', 2, 55.00, 6, '魔幻现实主义文学的代表作。', NOW(), NOW()),
('三体', '刘慈欣', '9787536692930', 2, 93.00, 0, '雨果奖获奖作品，中国科幻里程碑。', NOW(), NOW()),
('人类简史', '尤瓦尔·赫拉利', '9787508647357', 3, 68.00, 7, '从动物到上帝，重新审视人类历史。', NOW(), NOW()),
('明朝那些事儿', '当年明月', '9787505722460', 3, 358.00, 2, '用轻松幽默的语言讲述明朝历史。', NOW(), NOW()),
('时间简史', '史蒂芬·霍金', '9787535732309', 4, 45.00, 5, '探索宇宙起源与命运的科普经典。', NOW(), NOW()),
('经济学原理', '曼昆', '9787301287033', 5, 88.00, 4, '经济学入门经典教材。', NOW(), NOW());

-- 示例公告
INSERT INTO `notice` (`title`, `content`, `create_time`) VALUES
('欢迎使用图书管理系统', '本系统支持免费借阅、付费购买、图书预约等功能，欢迎各位读者使用！', NOW()),
('借阅规则说明', '每位读者最多同时借阅5本图书，借阅期限为30天，逾期未还将自动转为购买并触发惩罚机制。', NOW());
