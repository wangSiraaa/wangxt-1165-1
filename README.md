# 奶牛场生鲜乳抗生素检测系统

## 项目简介

本系统实现奶牛场生鲜乳抗生素检测的全流程管理，连接牧场、化验室和收奶司机三方角色，确保生鲜乳质量安全。

## 核心功能

### 三方角色协作
- **牧场**：提交挤奶批次信息，选择可用奶罐
- **化验室**：录入抗生素快检结果
- **司机**：确认装车操作

### 三条核心业务规则
1. **快检未通过不能装车** - 抗生素检测不合格的批次不允许装车
2. **同一奶罐未清洗不能接新批次** - 奶罐使用后必须清洗合格才能接入新批次
3. **装车后检测结果不能改为合格** - 检测结果一旦装车锁定，防止篡改

## 技术栈

### 后端
- Spring Boot 2.7.18
- MyBatis Plus 3.5.3.2
- MySQL 8.0
- Redis 6.2
- JWT 认证
- Knife4j API文档

### 前端
- Vue 2.x
- Element UI
- Axios

## 默认账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | 系统管理员 | 系统管理 |
| pasture1 | 123456 | 牧场用户 | 提交挤奶批次 |
| lab1 | 123456 | 化验员 | 录入检测结果 |
| driver1 | 123456 | 司机 | 确认装车 |

## 快速开始

### 方式一：Docker部署

```bash
# 1. 打包后端
mvn clean package -DskipTests

# 2. 启动服务
docker-compose up -d
```

### 方式二：本地运行

```bash
# 1. 创建数据库
CREATE DATABASE dairyfarm DEFAULT CHARACTER SET utf8mb4;

# 2. 修改application.yml中的数据库配置

# 3. 启动后端
mvn spring-boot:run
```

## 访问地址

- 后端服务: http://localhost:19465
- API文档: http://localhost:19465/doc.html
- 前端页面: http://localhost:19465/index.html

## 业务流程图

```
牧场提交批次 → 奶罐状态变为接奶中 → 批次完成待检测
        ↓
化验室录入检测结果 → 合格→批次检测通过 / 不合格→生成拒收记录
        ↓
司机确认装车 → 校验检测合格 → 锁定检测结果 → 更新批次和奶罐状态
        ↓
奶罐变为待清洗 → 清洗合格 → 奶罐变为空闲可接新批次
```

## API接口说明

### 认证接口
- `POST /auth/login` - 用户登录

### 批次接口
- `POST /batch/submit` - 牧场提交批次
- `GET /batch/list` - 查询批次列表
- `GET /batch/{id}` - 查询批次详情

### 检测接口
- `POST /test/submit` - 提交检测结果
- `PUT /test/{id}` - 修改检测结果
- `GET /test/list` - 查询检测列表

### 装车接口
- `POST /loading/confirm` - 司机确认装车
- `GET /loading/list` - 查询装车记录

### 清洗接口
- `POST /cleaning/start` - 开始清洗奶罐
- `POST /cleaning/finish/{id}` - 完成清洗
- `GET /cleaning/list` - 查询清洗记录

### 拒收接口
- `GET /reject/list` - 查询拒收记录（展示拒收原因）
- `POST /reject/handle/{id}` - 处理拒收记录

### 奶罐接口
- `GET /tank/list` - 查询奶罐列表
- `GET /tank/available` - 查询可用奶罐
- `GET /tank/check-cleaned/{tankId}` - 检查奶罐是否已清洗
