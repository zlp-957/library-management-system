# 图书管理系统

基于 **SpringBoot 2.7 + Vue3 + Element Plus + MyBatis-Plus + MySQL** 的前后端分离图书管理系统。

支持 **免费借阅**、**付费购买**、**图书预约排队**（库存0可预约）、**逾期自动转购买 + 惩罚机制**、**图书封面上传** 等核心功能。

## 一、技术栈

| 层 | 技术 |
|----|------|
| 后端 | SpringBoot 2.7.18、SpringMVC、MyBatis-Plus 3.5.5、MySQL 8.0、JWT、BCrypt |
| 前端 | Vue3、Element Plus、Axios、Vue Router、Pinia、Vite |
| 其他 | 本地文件存储（图书封面）、@Scheduled 定时任务 |

> 说明：文档中的 Redis 仅用于「预约排队 / 验证码 / 消息提醒缓存」，本实现改用 **MySQL 表 + 定时任务** 实现排队队列（`book_reserve` 表的 `reserve_time` 字段天然支持 FIFO 排序），功能完全等价，且无需额外安装 Redis。

## 二、账号规则（论文可写正则）

- 读者账号：`^R\d{8}$`，示例 `R20260001`
- 管理员账号：`^A\d{6}$`，示例 `A000001`
- 前端表单 + 后端接口双重校验

## 三、核心业务规则

1. **借阅免费**：最多同时借 5 本，期限 30 天
2. **购买收费**：扣除读者余额，库存 -1，图书归读者
3. **逾期转购买**：借阅到期未还 → 自动生成购买订单（buy_type=1）、扣余额、触发惩罚（30天禁借）
4. **预约排队**：库存=0 时预约按钮激活 → FIFO 排队 → 归还后库存恢复，自动通知队列第一位（状态=待借阅，3天有效）→ 超期失效顺延下一位
5. **封面上传**：仅 jpg/png，单张 ≤2MB

## 四、测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | A000001 | admin123 |
| 读者 | R20260001 | reader123 |
| 读者 | R20260002 | reader123 |

## 五、目录结构

```
图书管理系统/
├── backend/              # SpringBoot 后端
│   ├── src/main/java/com/library/
│   │   ├── config/       # 配置类（拦截器、定时任务、MyBatis-Plus、CORS）
│   │   ├── controller/   # 8个控制器
│   │   ├── service/      # 服务接口 + impl 实现
│   │   ├── mapper/       # MyBatis-Plus Mapper
│   │   ├── entity/       # 7个实体
│   │   ├── vo/           # 视图对象
│   │   ├── util/         # JWT、文件上传、用户上下文
│   │   └── common/       # 统一返回、异常、常量
│   ├── src/main/resources/application.yml
│   └── pom.xml
├── frontend/             # Vue3 前端
│   ├── src/
│   │   ├── api/          # 接口封装
│   │   ├── router/       # 路由
│   │   ├── stores/       # Pinia 状态
│   │   ├── components/   # 布局组件
│   │   └── views/        # 页面（reader/ admin/）
│   ├── vite.config.js
│   └── package.json
└── sql/init.sql          # 建表 + 初始化数据脚本
```

## 六、运行步骤

### ⭐ 0. 一键启动（推荐）

项目根目录提供了 3 个启动脚本，**双击即可运行**，无需手动配置环境变量：

| 脚本 | 作用 |
|------|------|
| `start-all.bat` | 同时启动后端（8080）+ 前端（5173），最省事 |
| `start-backend.bat` | 只启动后端 |
| `start-frontend.bat` | 只启动前端 |

> 说明：
> - 脚本已内置 JDK 11 和 Node 的绝对路径，双击直接可用。
> - 若换电脑，请修改脚本开头的 `JAVA_HOME` 和 `NODE_HOME` 两行路径。
> - 前端依赖已安装过，若首次运行会自动执行 `npm install`。
> - 后端已打包好 `target/library-management-1.0.0.jar`，脚本直接运行 jar，无需重新编译。

### 1. 初始化数据库

```bash
mysql -uroot -p < sql/init.sql
```

（会自动创建 `library` 库、7张表、管理员/读者账号、示例图书和分类）

### 2. 启动后端

需要 JDK 11+ 和 Maven。

```bash
cd backend
mvn spring-boot:run
# 或打包后运行
mvn clean package -DskipTests
java -jar target/library-management-1.0.0.jar
```

后端启动在 `http://localhost:8080`。

> 修改数据库密码：编辑 `backend/src/main/resources/application.yml` 中的 `spring.datasource.password`。

### 3. 启动前端

需要 Node 16+。

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`，已配置代理转发 `/api` 到后端 8080。

## 七、核心接口一览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /auth/login | 登录 |
| POST | /auth/register | 读者注册 |
| GET | /book/page | 图书分页检索（书名/作者/ISBN/分类） |
| POST | /book/uploadCover | 封面上传（multipart） |
| POST | /borrow/add | 借阅图书（免费） |
| POST | /borrow/return/{id} | 归还图书 |
| POST | /purchase/buy/{bookId} | 购买图书（扣余额） |
| POST | /reserve/add | 提交预约 |
| POST | /reserve/cancel/{id} | 取消预约 |
| GET | /stat/overview | 数据统计（管理员） |

## 八、定时任务

| 任务 | 频率 | 说明 |
|------|------|------|
| 逾期转购买 | 每天凌晨1点 | 扫描 status=0 且超过 deadline 的借阅，转购买+惩罚 |
| 预约失效处理 | 每小时 | 待借阅超期失效，顺延下一位排队用户 |

管理员也可在后台「借阅管理 / 预约管理」页面手动触发这两个任务（方便答辩演示）。

## 九、数据库表

| 表 | 说明 |
|----|------|
| user | 用户表（含余额、惩罚截止时间） |
| book_type | 图书分类表 |
| book | 图书表（含封面、售价、库存） |
| borrow | 借阅记录表（含逾期标记） |
| purchase_order | 购买订单表（含购买类型） |
| book_reserve | 图书预约表（含排队状态、有效期） |
| notice | 公告表 |
