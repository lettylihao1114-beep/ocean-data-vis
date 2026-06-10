# 个人中心 — 产品需求文档 (PRD)

> **版本**：v1.0  
> **日期**：2026-06-10  
> **状态**：待开发  
> **关联系统**：南海海洋环境数据可视化系统

---

## 1. 背景与目标

### 1.1 现状分析

| 维度 | 现状 |
|------|------|
| 用户信息存储 | `users` 表已含 username / email / phone / avatar 字段 |
| 后端 API | `UserController` 仅为 ADMIN 专用（`/api/admin/users`），普通用户无任何操作自身信息的接口 |
| 前端状态 | `authStore` 仅存 token / username / role，无 email / phone / avatar |
| 前端页面 | 无个人中心页面，Banner 顶部仅显示用户名 + 退出按钮 |
| 导航入口 | 无"个人中心"导航项 |

### 1.2 目标

为所有已登录用户提供**个人中心**页面，支持：
- 查看个人资料
- 修改基本信息（邮箱、手机号）
- 修改密码
- 查看个人操作历史（导出记录）

---

## 2. 功能范围

### 2.1 功能列表

| 编号 | 功能 | 优先级 | 说明 |
|------|------|:--:|------|
| F1 | 查看个人资料 | P0 | 展示用户名、角色、邮箱、手机号、注册时间 |
| F2 | 修改基本信息 | P0 | 修改邮箱、手机号 |
| F3 | 修改密码 | P0 | 旧密码验证 + 新密码确认 |
| F4 | 个人信息入口 | P0 | Banner 用户名 → 下拉菜单（个人中心 + 退出） 或 导航栏新增图标入口 |
| F5 | 个人操作历史 | P1 | 查看本人的导出记录日志 |

### 2.2 不做（本期）

- ❌ 头像上传（需文件存储，后续迭代）
- ❌ 账号注销（安全风险高，需二次确认机制）
- ❌ 第三方绑定

---

## 3. 页面设计

### 3.1 布局 — 左侧菜单 + 右侧内容

```
┌──────────────────────────────────────────────┐
│  个人中心                                     │
│                                              │
│  ┌──────────┐  ┌─────────────────────────┐  │
│  │  📋 个人资料 │  │                         │  │
│  │  🔒 修改密码 │  │   右侧内容区             │  │
│  │  📊 我的导出 │  │   （根据左侧 Tab 切换）   │  │
│  │            │  │                         │  │
│  └──────────┘  └─────────────────────────┘  │
└──────────────────────────────────────────────┘
```

### 3.2 Tab 1：个人资料（默认）

```
┌─────────────────────────────────────────┐
│  基本信息                                │
│  ┌──────────────────────────────────┐   │
│  │  默认头像（SVG 图标）              │   │
│  │  用户名：admin                    │   │
│  │  角色：管理员 [标签]               │   │
│  │  邮箱：admin@ocean.cn  [编辑按钮]  │   │
│  │  手机号：138xxxx1234   [编辑按钮]  │   │
│  │  注册时间：2025-06-01             │   │
│  └──────────────────────────────────┘   │
│                                          │
│  [编辑资料] 按钮 → 弹出对话框              │
│  ┌──────────────────────────────┐        │
│  │  编辑个人资料                  │        │
│  │  邮箱：[________]             │        │
│  │  手机号：[________]            │        │
│  │         [取消] [保存]          │        │
│  └──────────────────────────────┘        │
└─────────────────────────────────────────┘
```

### 3.3 Tab 2：修改密码

```
┌─────────────────────────────────────────┐
│  修改密码                                │
│                                          │
│  旧密码：[________]                       │
│  新密码：[________]                       │
│  确认密码：[________]                     │
│                                          │
│  密码规则提示：至少 6 位，含字母和数字     │
│                                          │
│  [取消] [确认修改]                        │
└─────────────────────────────────────────┘
```

### 3.4 Tab 3：我的导出（P1）

```
┌─────────────────────────────────────────┐
│  我的导出记录                            │
│                                          │
│  ┌─────────┬────────┬──────┬──────────┐ │
│  │ 导出时间 │ 导出类型 │ 文件名 │ 状态    │ │
│  ├─────────┼────────┼──────┼──────────┤ │
│  │ 06-10   │ Excel  │ ocean.xlsx │ ✅  │ │
│  │ 06-08   │ CSV    │ data.csv   │ ✅  │ │
│  └─────────┴────────┴──────┴──────────┘ │
│                                          │
│  分页：< 1 2 3 >                         │
└─────────────────────────────────────────┘
```

### 3.5 导航入口

**方案**：Banner 顶部用户名改为可点击的下拉菜单：

```
┌─────────────────────────────────────┐
│  [👤 用户名 ▾]  日期   退出          │
│   ├─ 个人中心                        │
│   └─ 退出登录                        │
└─────────────────────────────────────┘
```

### 3.6 样式规范（保持现有 NMDIS 海洋风）

| 元素 | 规范 |
|------|------|
| 背景 | Shell `#f0f5fa`，卡片 `#ffffff` |
| 主色 | 深海蓝 `#0a3d62` / 海洋蓝 `#0ea5e9` |
| Tab 激活 | 左侧蓝色边框 `border-left: 3px solid #0ea5e9` |
| 卡片阴影 | `0 1px 6px rgba(6,32,58,.06)` |
| 圆角 | 12px |
| 按钮 | 主按钮 `#0ea5e9`，次按钮白底边框 |
| 输入框 | Element Plus 默认 + focus 蓝色边框 |

---

## 4. API 设计

### 4.1 获取个人资料

```
GET /api/user/profile

Response 200:
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@ocean.cn",
    "phone": "13800000000",
    "avatar": null,
    "role": "ADMIN",
    "createTime": "2025-06-01T08:00:00"
  }
}
```

### 4.2 更新个人资料

```
PUT /api/user/profile
Content-Type: application/json

{
  "email": "newemail@ocean.cn",
  "phone": "13900000000"
}

Response 200: { "code": 200, "message": "资料已更新" }
Response 400: { "code": 400, "message": "邮箱格式不正确" }
```

### 4.3 修改密码

```
PUT /api/user/password
Content-Type: application/json

{
  "oldPassword": "admin123",
  "newPassword": "newPass456"
}

Response 200: { "code": 200, "message": "密码已修改，请重新登录" }
Response 400: { "code": 400, "message": "旧密码不正确" }
```

### 4.4 我的导出记录（P1）

```
GET /api/user/exports?pageNum=1&pageSize=10

Response 200:
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "exportType": "EXCEL",
        "fileName": "ocean_data_20260610.xlsx",
        "status": "SUCCESS",
        "createTime": "2026-06-10T15:30:00"
      }
    ],
    "total": 25,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

## 5. 后端实现方案

### 5.1 新增文件

| 文件 | 位置 | 说明 |
|------|------|------|
| `UserProfileController.java` | `controller/` | 个人中心专用接口（`/api/user/*`） |
| `UserProfileService.java` | `service/` | 个人资料查询、更新、改密 |
| `UpdateProfileRequest.java` | `dto/` | 更新资料请求 DTO |
| `ChangePasswordRequest.java` | `dto/` | 改密请求 DTO |

### 5.2 修改文件

| 文件 | 改动 |
|------|------|
| `SecurityConfig.java` | `/api/user/**` 放行给已认证用户（非 ADMIN 专享） |
| `UserMapper.java` | 可能需要新增按 ID 查完整信息的方法（已有 MyBatis-Plus 自带） |

### 5.3 关键逻辑

**改密流程**：
1. 从 SecurityContext 获取当前用户 ID
2. 查 DB 获取当前密码哈希
3. BCrypt.matches(oldPassword, hash) 验证
4. BCrypt.encode(newPassword) 加密
5. 更新 DB → 返回成功

**安全性**：
- 所有接口从 `SecurityContextHolder` 获取当前用户，防止越权
- 改密后不清除 token（保留用户状态，提示重新登录为建议性提示）
- 邮箱格式校验（`@` + 域名）
- 手机号格式校验（11 位数字）

---

## 6. 前端实现方案

### 6.1 新增文件

| 文件 | 说明 |
|------|------|
| `views/Profile.vue` | 个人中心页面（含 3 个 Tab） |

### 6.2 修改文件

| 文件 | 改动 |
|------|------|
| `router/index.ts` | 新增路由 `/profile` |
| `layout/MainLayout.vue` | Banner 用户名改为下拉菜单（个人中心 + 退出） |
| `store/auth.ts` | 新增 `email` / `phone` 字段 + `fetchProfile()` / `updateProfile()` 方法 |
| `api/user.ts` | 新增 `getProfile` / `updateProfile` / `changePassword` / `getMyExports` 方法 |
| `types/index.ts` | 新增 `UserProfile` / `ChangePasswordRequest` / `ExportLog` 类型 |

### 6.3 组件树

```
Profile.vue
├─ 左侧菜单（Tab 切换）
│   ├─ Tab: 个人资料（active）
│   ├─ Tab: 修改密码
│   └─ Tab: 我的导出
├─ 右侧内容区
│   ├─ Tab 1: 个人资料
│   │   └─ EditProfileDialog（弹窗）
│   ├─ Tab 2: 修改密码表单
│   └─ Tab 3: 导出记录列表 + 分页
```

### 6.4 状态管理

```typescript
// authStore 扩展
const email = ref('')
const phone = ref('')
const avatar = ref('')

const fetchProfile = async () => {
  const res = await userAPI.getProfile()
  email.value = res.data.email
  phone.value = res.data.phone
  avatar.value = res.data.avatar
}

const updateProfile = async (data) => {
  await userAPI.updateProfile(data)
  email.value = data.email
  phone.value = data.phone
}
```

---

## 7. 权限矩阵

| 操作 | GUEST | USER | ADMIN |
|------|:--:|:--:|:--:|
| 查看个人资料 | ❌ | ✅ | ✅ |
| 修改个人资料 | ❌ | ✅ | ✅ |
| 修改密码 | ❌ | ✅ | ✅ |
| 查看我的导出 | ❌ | ✅ | ✅ |

> 所有登录用户均可访问个人中心，与角色无关。

---

## 8. 路由设计

```typescript
{
  path: 'profile',
  name: 'Profile',
  component: () => import('@/views/Profile.vue'),
  meta: { title: '个人中心' }  // 无 role 限制
}
```

---

## 9. 验收标准

| 编号 | 验收项 | 条件 |
|------|--------|------|
| AC1 | 查看资料 | 登录后进入个人中心，正确显示用户名、角色、邮箱、手机号、注册时间 |
| AC2 | 修改资料 | 编辑邮箱/手机号后保存，刷新页面数据保持 |
| AC3 | 旧密码验证 | 输入错误旧密码 → 提示"旧密码不正确" |
| AC4 | 改密成功 | 输入正确旧密码 + 新密码 → 提示修改成功 |
| AC5 | 新密码登录 | 改密后使用新密码可正常登录 |
| AC6 | 导航入口 | 点击 Banner 用户名 → 下拉菜单 → "个人中心" 跳转 |
| AC7 | 退出保持 | 退出按钮仍在下拉菜单中可点击 |
| AC8 | 权限 | 未登录用户无法访问 /profile（跳转登录页） |
| AC9 | 样式 | 与现有海洋风设计语言一致 |

---

## 10. 工作量评估

| 模块 | 内容 | 预估 |
|------|------|:--:|
| 后端 | 新建 Controller + Service + DTO（3 个接口） | 1h |
| 前端 | 新建 Profile.vue + 路由 + store 扩展 | 2h |
| 前端 | MainLayout Banner 下拉菜单改造 | 0.5h |
| 联调 | 接口联调 + 样式微调 | 0.5h |
| **合计** | | **~4h** |

---

## 11. 后续迭代（V2）

- 头像上传（MinIO/OSS + 裁剪）
- 操作日志（登录历史、查询历史）
- 账号注销（7 天冷静期）
- 消息通知中心
