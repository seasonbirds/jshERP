# jshERP前端代码分析报告

## 1. 技术栈与框架

### 1.1 核心框架

| 技术/框架 | 版本号 | 说明 |
|---------|-------|------|
| Vue.js | 2.7.16 | 前端核心框架 |
| Vue Router | 3.0.1 | 路由管理 |
| Vuex | 3.1.0 | 状态管理 |
| Ant Design Vue | 1.5.2 | UI组件库 |
| Vue CLI | 3.3.0 | 项目构建工具 |

### 1.2 主要开发工具

| 工具 | 版本号 | 说明 |
|-----|-------|------|
| Webpack | (通过Vue CLI) | 模块打包工具 |
| Babel | 7.x | JavaScript编译器 |
| ESLint | 5.16.0 | 代码检查工具 |
| Less | 3.9.0 | CSS预处理器 |

## 2. 依赖的软件包

### 2.1 生产依赖

```
@antv/data-set: ^0.11.2
@tinymce/tinymce-vue: ^2.0.0
ant-design-vue: 1.5.2
area-data: ^5.0.6
axios: ^0.18.0
clipboard: ^2.0.4
codemirror: ^5.46.0
dayjs: ^1.8.0
enquire.js: ^2.1.6
intro.js: ^4.2.2
jquery: ^1.12.4
js-cookie: ^2.2.0
lodash.get: ^4.4.2
lodash.pick: ^4.4.2
md5: ^2.2.1
nprogress: ^0.2.0
viser-vue: ^2.4.4
vue: ^2.7.16
vue-area-linkage: ^5.1.0
vue-cropper: ^0.4.8
vue-draggable-resizable: ^2.3.0
vue-i18n: ^8.7.0
vue-loader: ^15.7.0
vue-ls: ^3.2.0
vue-photo-preview: ^1.1.3
vue-print-nb-jeecg: ^1.0.9
vue-router: ^3.0.1
vue-splitpane: ^1.0.4
vuedraggable: ^2.20.0
vuex: ^3.1.0
```

### 2.2 开发依赖

```
@babel/polyfill: ^7.2.5
@vue/cli-plugin-babel: ^3.3.0
@vue/cli-plugin-eslint: ^3.3.0
@vue/cli-service: ^3.3.0
@vue/eslint-config-standard: ^4.0.0
babel-eslint: ^10.0.1
compression-webpack-plugin: ^3.1.0
eslint: ^5.16.0
eslint-plugin-vue: ^5.1.0
html-webpack-plugin: ^4.2.0
less: ^3.9.0
less-loader: ^4.1.0
vue-template-compiler: ^2.6.10
```

## 3. 已实现的功能模块

### 3.1 系统管理模块

- **用户管理**: 用户列表、新增、编辑、删除、分配仓库、分配客户、重置密码
- **角色管理**: 角色列表、新增、编辑、权限配置
- **权限管理**: 菜单权限、按钮权限
- **机构管理**: 组织架构树、新增、编辑、删除
- **系统配置**: 系统参数配置
- **日志管理**: 操作日志查询
- **租户管理**: 多租户支持
- **插件管理**: 插件安装与配置

### 3.2 基础信息模块

- **商品管理**: 商品列表、新增、编辑、分类、属性管理
- **仓库管理**: 仓库列表、新增、编辑
- **单位管理**: 计量单位管理
- **收支项目**: 收支项目配置
- **往来单位**: 供应商、客户、会员管理
- **人员管理**: 人员信息管理

### 3.3 业务单据模块

- **采购业务**: 采购订单、采购入库、采购退货
- **销售业务**: 销售订单、销售出库、销售退货
- **库存业务**: 其他入库、其他出库、调拨、组装、拆卸
- **财务业务**: 收款、付款、转账、预付款、预收款

### 3.4 报表查询模块

- **库存报表**: 库存查询、库存预警、库存盘点
- **采购报表**: 采购统计、采购明细
- **销售报表**: 销售统计、销售明细
- **财务报表**: 收支统计、应收应付
- **往来报表**: 客户对账、供应商对账

### 3.5 其他功能

- **图表分析**: 数据可视化分析
- **消息管理**: 系统消息通知
- **打印功能**: 单据打印
- **导入导出**: Excel导入导出
- **多语言**: 国际化支持
- **主题切换**: 多主题支持

## 4. 角色与权限控制分析

### 4.1 实现情况分析

**结论**: 系统确实实现了对角色和权限的细致控制，精确到按钮和菜单级别。

### 4.2 实现机制详解

#### 4.2.1 权限数据获取

1. **用户登录后获取权限** (`src/permission.js:21-41`):
   - 通过 `GetPermissionList` action 获取菜单权限
   - 通过 `GetUserBtnList` action 获取按钮权限
   - 权限数据存储在 Vuex store 和 sessionStorage 中

2. **权限数据存储** (`src/store/mutation-types.js:16-17`):
   - `USER_AUTH`: 存储用户按钮权限
   - `SYS_BUTTON_AUTH`: 存储系统按钮权限

#### 4.2.2 菜单权限控制

1. **动态路由生成** (`src/utils/util.js:83-155`):
   - 根据后端返回的菜单数据动态生成路由
   - 支持嵌套菜单结构
   - 支持外部链接和iframe嵌入

2. **路由过滤** (`src/store/modules/permission.js:40-51`):
   - `filterAsyncRouter` 函数根据用户权限过滤路由
   - 检查路由的 `meta.permission` 字段

#### 4.2.3 按钮权限控制

1. **自定义指令实现** (`src/utils/hasPermission.js`):
   - 注册 `v-has` 指令用于按钮权限控制
   - 支持两种权限检查模式:
     - **流程节点权限**: 从 `formData.permissionList` 获取权限
     - **全局权限**: 从 sessionStorage 获取权限

2. **权限检查流程**:
   ```javascript
   // 1. 检查流程节点权限
   if (!filterNodePermission(el, binding, vnode)) {
     // 2. 检查全局权限
     filterGlobalPermission(el, binding, vnode);
   }
   ```

3. **权限数据结构**:
   - 权限包含 `type` 和 `action` 字段
   - `type != '2'` 的权限被视为有效权限
   - 支持禁用权限功能 (`status == '0'`)

#### 4.2.4 权限使用示例

在 `src/views/system/UserList.vue:30-33` 中:
```vue
<a-button v-if="btnEnableList.indexOf(1)>-1" @click="handleAdd" type="primary" icon="plus">新增</a-button>
<a-button v-if="btnEnableList.indexOf(1)>-1" @click="batchDel" icon="delete">删除</a-button>
```

在 `src/views/bill/PurchaseInList.vue:123` 中:
```vue
<a-button v-if="quickBtn.purchaseBack.indexOf(1)>-1 && btnEnableList.indexOf(1)>-1" icon="share-alt" @click="transferBill('转采购', quickBtn.purchaseBack)">转采购</a-button>
```

### 4.3 权限控制的不足

虽然系统实现了细致的权限控制，但仍存在以下不足:

1. **前端权限可被绕过**: 前端权限控制仅用于UI展示，后端仍需进行权限验证
2. **缺乏权限缓存机制**: 每次路由变化都重新获取权限
3. **权限变更不实时**: 需要重新登录才能更新权限
4. **缺乏权限审计**: 没有权限变更日志记录

## 5. 安全问题分析

### 5.1 XSS (跨站脚本攻击)

**风险等级**: 中

**分析**:
- 系统使用 Vue.js 的模板语法，默认会对用户输入进行转义
- 但存在以下风险点:
  - `src/utils/util.js:113`: 使用 `eval()` 解析URL模板变量
  - `src/utils/util.js:270-315`: `jsExpand` 函数允许执行任意JS代码
  - 部分组件可能使用 `v-html` 渲染用户内容

**示例风险代码**:
```javascript
// src/utils/util.js:113
let URL = (item.url || '').replace(/{{([^}}]+)?}}/g, (s1, s2) => eval(s2))
```

### 5.2 CSRF (跨站请求伪造)

**风险等级**: 低

**分析**:
- 系统使用自定义的 `X-Access-Token` 请求头进行身份验证
- Token 存储在 localStorage 中
- 但存在以下改进空间:
  - 未使用 CSRF Token
  - 未验证请求来源

### 5.3 点击劫持

**风险等级**: 低

**分析**:
- 未设置 `X-Frame-Options` 响应头
- 但系统主要是内部ERP系统，风险相对较低

### 5.4 敏感信息泄露

**风险等级**: 中

**分析**:
- **Token存储**: Token存储在localStorage中，容易被XSS窃取
- **敏感数据**: 用户信息、权限等存储在localStorage中
- **错误信息**: 错误响应中可能包含敏感信息
- **控制台日志**: 代码中存在大量 `console.log` 语句

**示例风险代码**:
```javascript
// src/store/modules/user.js:48
Vue.ls.set(ACCESS_TOKEN, result.token, 7 * 24 * 60 * 60 * 1000)
// Token存储在localStorage中，没有加密
```

### 5.5 依赖不安全的第三方库

**风险等级**: 中

**分析**:
- **jQuery 1.12.4**: 存在已知安全漏洞
- **axios 0.18.0**: 版本较老，存在安全问题
- **其他老版本依赖**: 多个依赖库版本较老

**具体风险依赖**:
| 依赖 | 版本 | 风险 |
|-----|------|------|
| jquery | 1.12.4 | 存在多个已知漏洞 |
| axios | 0.18.0 | 存在安全问题 |
| vuex | 3.1.0 | 较老版本 |
| vue-router | 3.0.1 | 较老版本 |

### 5.6 其他安全问题

1. **文件上传安全**: 未看到文件上传的安全验证
2. **密码安全**: 密码传输未加密（仅依赖HTTPS）
3. **会话管理**: Token过期时间设置为7天，过长
4. **API权限**: 前端API调用缺乏细粒度权限验证
5. **代码混淆**: 生产环境代码未混淆，容易被逆向分析

## 6. 总结

jshERP前端系统是一个功能完善的ERP前端应用，具有以下特点:

1. **技术栈成熟**: 基于Vue 2.x和Ant Design Vue，生态完善
2. **功能全面**: 覆盖ERP核心业务流程
3. **权限控制细致**: 实现了菜单和按钮级别的权限控制
4. **存在安全隐患**: 需要加强XSS防护、Token安全、依赖升级等方面
5. **代码可维护性**: 整体结构清晰，但存在一些代码质量问题
