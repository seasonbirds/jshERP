# 国产开源ERP-管伊佳技术分析报告

## 一、技术依赖

### 1.1 后端技术栈/框架

管伊佳ERP后端采用Java开发，基于Spring Boot框架构建，主要技术栈如下：

| 技术/框架 | 版本号 | 用途说明 |
|-----------|--------|----------|
| Spring Boot | 2.0.0.RELEASE | 核心框架，提供自动配置和快速开发能力 |
| MyBatis-Plus | 3.0.7.1 | 持久层框架，简化数据库操作 |
| Spring Boot Starter Redis | 1.4.1.RELEASE | Redis缓存支持，用于Session管理和数据缓存 |
| Springfox Swagger2 | 2.7.0 | API文档生成工具 |
| Swagger Bootstrap UI | 1.6 | Swagger UI增强 |
| PageHelper | 1.2.13 | 分页插件 |
| Lombok | 1.18.12 | 简化Java代码，自动生成getter/setter等 |

### 1.2 后端依赖中间件

| 中间件 | 版本要求 | 用途说明 |
|--------|----------|----------|
| MySQL | 8.0.24 | 主数据库，存储所有业务数据 |
| Redis | 6.2.1 | 缓存和Session管理 |
| Nginx | 1.12.2 | 反向代理和静态资源服务 |

### 1.3 后端第三方依赖包

| 依赖包 | 版本号 | 用途说明 |
|--------|--------|----------|
| fastjson | 1.2.83 | JSON序列化/反序列化 |
| mysql-connector-java | 8.0.33 | MySQL数据库驱动 |
| httpclient | 4.5.2 | HTTP客户端 |
| jxl | 2.6.12 | Excel文件操作（旧版） |
| log4j-to-slf4j | 2.15.0 | 日志桥接 |
| jul-to-slf4j | 1.7.25 | 日志桥接 |
| aliyun-sdk-oss | 3.10.1 | 阿里云OSS对象存储 |
| itextpdf | 5.5.13.1 | PDF生成 |
| commons-io | 1.3.2 | IO工具类 |
| pinyin4j | 2.5.1 | 汉字转拼音 |
| javax.mail | 1.6.2 | 邮件发送 |
| springboot-plugin-framework | 2.2.1-RELEASE | 插件框架（支持插件扩展） |
| springboot-plugin-framework-extension-mybatis | 2.2.1-RELEASE | 插件框架MyBatis扩展 |

### 1.4 前端技术栈/框架

管伊佳ERP前端采用Vue.js框架开发，主要技术栈如下：

| 技术/框架 | 版本号 | 用途说明 |
|-----------|--------|----------|
| Vue | 2.7.16 | 核心前端框架 |
| Ant Design Vue | 1.5.2 | UI组件库 |
| Vue Router | 3.0.1 | 路由管理 |
| Vuex | 3.1.0 | 状态管理 |
| Axios | 0.18.0 | HTTP客户端 |

### 1.5 前端依赖软件包列表

| 软件包名称 | 版本号 | 用途说明 |
|-----------|--------|----------|
| @antv/data-set | ^0.11.2 | 数据可视化数据集处理 |
| @tinymce/tinymce-vue | ^2.0.0 | 富文本编辑器Vue组件 |
| ant-design-vue | 1.5.2 | Ant Design Vue UI组件库 |
| area-data | ^5.0.6 | 中国行政区划数据 |
| axios | ^0.18.0 | HTTP请求库 |
| clipboard | ^2.0.4 | 剪贴板操作 |
| codemirror | ^5.46.0 | 代码编辑器组件 |
| dayjs | ^1.8.0 | 日期处理库（轻量级） |
| enquire.js | ^2.1.6 | 响应式媒体查询 |
| intro.js | ^4.2.2 | 用户引导组件 |
| jquery | ^1.12.4 | jQuery库 |
| js-cookie | ^2.2.0 | Cookie操作 |
| lodash.get | ^4.4.2 | Lodash get工具函数 |
| lodash.pick | ^4.4.0 | Lodash pick工具函数 |
| md5 | ^2.2.1 | MD5加密 |
| nprogress | ^0.2.0 | 页面加载进度条 |
| viser-vue | ^2.4.4 | 数据可视化图表库 |
| vue | ^2.7.16 | Vue.js核心框架 |
| vue-area-linkage | ^5.1.0 | 地区选择联动组件 |
| vue-cropper | ^0.4.8 | 图片裁剪组件 |
| vue-draggable-resizable | ^2.3.0 | 拖拽缩放组件 |
| vue-i18n | ^8.7.0 | 国际化 |
| vue-loader | ^15.7.0 | Vue单文件组件加载器 |
| vue-ls | ^3.2.0 | Vue本地存储 |
| vue-photo-preview | ^1.1.3 | 图片预览组件 |
| vue-print-nb-jeecg | ^1.0.9 | 打印组件 |
| vue-router | ^3.0.1 | 路由管理 |
| vue-splitpane | ^1.0.4 | 分割面板组件 |
| vuedraggable | ^2.20.0 | 拖拽排序组件 |
| vuex | ^3.1.0 | 状态管理 |

### 1.6 前端开发依赖

| 软件包名称 | 版本号 | 用途说明 |
|-----------|--------|----------|
| @babel/polyfill | ^7.2.5 | Babel polyfill |
| @vue/cli-plugin-babel | ^3.3.0 | Vue CLI Babel插件 |
| @vue/cli-plugin-eslint | ^3.3.0 | Vue CLI ESLint插件 |
| @vue/cli-service | ^3.3.0 | Vue CLI服务 |
| @vue/eslint-config-standard | ^4.0.0 | Vue ESLint标准配置 |
| babel-eslint | ^10.0.1 | Babel ESLint解析器 |
| compression-webpack-plugin | ^3.1.0 | Webpack压缩插件 |
| eslint | ^5.16.0 | 代码检查工具 |
| eslint-plugin-vue | ^5.1.0 | Vue ESLint插件 |
| html-webpack-plugin | ^4.2.0 | HTML模板插件 |
| less | ^3.9.0 | Less预处理器 |
| less-loader | ^4.1.0 | Less加载器 |
| vue-template-compiler | ^2.6.10 | Vue模板编译器 |

---

## 二、已实现的功能模块

### 2.1 模块总览

管伊佳ERP系统已实现以下核心功能模块：

```
┌─────────────────────────────────────────────────────────────┐
│                      管伊佳ERP功能架构                       │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │
│  │  系统管理   │  │  基础资料   │  │  商品管理   │       │
│  └─────────────┘  └─────────────┘  └─────────────┘       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │
│  │  采购管理   │  │  销售管理   │  │  零售管理   │       │
│  └─────────────┘  └─────────────┘  └─────────────┘       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │
│  │  仓库管理   │  │  财务管理   │  │  报表查询   │       │
│  └─────────────┘  └─────────────┘  └─────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 功能模块清单

| 模块名称 | 子模块 | 功能描述 |
|----------|--------|----------|
| **系统管理** | 角色管理 | 角色的增删改查、启用/禁用、权限分配 |
| | 用户管理 | 用户的增删改查、重置密码、启用/禁用、机构关联 |
| | 日志管理 | 操作日志查询、记录用户操作行为 |
| | 功能管理 | 菜单功能管理、按钮权限配置 |
| | 机构管理 | 组织架构管理、部门设置 |
| | 租户管理 | 多租户管理（用于云平台） |
| | 系统配置 | 系统参数配置、多级审核开关 |
| | 商品属性 | 商品属性定义（颜色、尺寸等） |
| | 插件管理 | 插件扩展管理 |
| | 平台配置 | 云平台配置（邮件、微信等） |
| **商品管理** | 商品类别 | 商品分类管理，支持多级分类 |
| | 商品信息 | 商品基础信息、价格、库存、条码等 |
| | 多单位 | 商品多计量单位管理 |
| | 多属性 | 商品多属性（SKU）管理 |
| **基础资料** | 供应商信息 | 供应商档案管理 |
| | 客户信息 | 客户档案管理 |
| | 会员信息 | 会员档案管理 |
| | 仓库信息 | 仓库档案管理、负责人设置 |
| | 收支项目 | 收支项目定义 |
| | 结算账户 | 资金账户管理 |
| | 经手人管理 | 经手人档案管理 |
| **采购管理** | 请购单 | 采购申请单据 |
| | 采购订单 | 采购订单管理 |
| | 采购入库 | 采购入库单、验收入库 |
| | 采购退货 | 采购退货出库 |
| **销售管理** | 销售订单 | 销售订单管理 |
| | 销售出库 | 销售出库单、发货管理 |
| | 销售退货 | 销售退货入库 |
| **零售管理** | 零售出库 | 零售销售出库 |
| | 零售退货 | 零售退货入库 |
| **仓库管理** | 其它入库 | 非采购类入库（如赠品、盘盈） |
| | 其它出库 | 非销售类出库（如领用、盘亏） |
| | 调拨出库 | 仓库间调拨 |
| | 组装单 | 商品组装（生产模块基础） |
| | 拆卸单 | 商品拆卸 |
| **财务管理** | 收入单 | 其他收入记账 |
| | 支出单 | 其他支出记账 |
| | 收款单 | 应收款收款 |
| | 付款单 | 应付款付款 |
| | 转账单 | 账户间资金转账 |
| | 收预付款 | 预收/预付款管理 |
| **报表查询** | 进销存统计 | 商品进销存汇总统计 |
| | 账户统计 | 资金账户统计 |
| | 采购统计 | 采购数据统计分析 |
| | 销售统计 | 销售数据统计分析 |
| | 零售统计 | 零售数据统计分析 |
| | 入库明细 | 入库单据明细查询 |
| | 出库明细 | 出库单据明细查询 |
| | 入库汇总 | 入库汇总统计 |
| | 出库汇总 | 出库汇总统计 |
| | 客户对账 | 客户往来对账 |
| | 供应商对账 | 供应商往来对账 |
| | 库存预警 | 库存上下限预警 |
| | 商品库存 | 当前库存查询 |
| | 调拨明细 | 调拨单据明细 |

---

## 三、进销存、财务、生产模块详细分析

### 3.1 进销存模块

#### 3.1.1 采购管理

**核心功能：**

| 功能点 | 功能描述 | 数据结构 |
|--------|----------|----------|
| 请购单 | 采购申请单据，支持多级审核 | `depot_head` 表，`sub_type='请购'` |
| 采购订单 | 正式采购合同/订单，可关联请购单 | `depot_head` 表，`sub_type='采购订单'` |
| 采购入库 | 货物验收入库，更新库存，关联订单 | `depot_head` + `depot_item` |
| 采购退货 | 退货出库，扣减库存 | `depot_head` 表，`sub_type='采购退货'` |

**业务流程：**
```
请购单 → 采购订单 → 采购入库
                ↓
         生成应付账款
                ↓
         财务模块付款单核销
```

**关键代码位置：**
- 控制器：`DepotHeadController.java`
- 服务层：`DepotHeadService.java`
- 核心方法：`addDepotHeadAndDetail()`、`updateDepotHeadAndDetail()`、`batchSetStatus()`

#### 3.1.2 销售管理

**核心功能：**

| 功能点 | 功能描述 | 数据结构 |
|--------|----------|----------|
| 销售订单 | 销售合同/订单管理 | `depot_head` 表，`sub_type='销售订单'` |
| 销售出库 | 发货出库，扣减库存 | `depot_head` + `depot_item` |
| 销售退货 | 客户退货入库，增加库存 | `depot_head` 表，`sub_type='销售退货'` |

**业务流程：**
```
销售订单 → 销售出库
                ↓
         生成应收账款
                ↓
         财务模块收款单核销
```

**特殊功能：**
- 支持"以销定购"模式：销售订单可直接生成采购订单
- 支持多账户收款：一笔订单可使用多个账户收款
- 支持优惠管理：支持优惠率、优惠金额

#### 3.1.3 零售管理

**核心功能：**

| 功能点 | 功能描述 | 数据结构 |
|--------|----------|----------|
| 零售出库 | 门店零售销售，简化流程 | `depot_head` 表，`sub_type='零售出库'` |
| 零售退货 | 零售退货处理 | `depot_head` 表，`sub_type='零售退货'` |

**零售与销售的区别：**
- 零售流程更简化，无需订单环节
- 零售支持会员管理
- 零售通常面向个人客户

#### 3.1.4 仓库管理

**核心功能：**

| 功能点 | 功能描述 | 数据结构 |
|--------|----------|----------|
| 其它入库 | 非采购类入库（赠品、盘盈等） | `depot_head` 表，`sub_type='其它入库'` |
| 其它出库 | 非销售类出库（领用、盘亏等） | `depot_head` 表，`sub_type='其它出库'` |
| 调拨出库 | 仓库间库存调拨 | `depot_head` 表，`sub_type='调拨出库'` |
| 组装单 | 多商品组合成套装 | `depot_head` 表，`sub_type='组装'` |
| 拆卸单 | 套装拆分为单个商品 | `depot_head` 表，`sub_type='拆卸'` |

**库存管理特性：**

1. **多仓库支持**
   - 支持多个仓库独立管理
   - 仓库间调拨功能
   - 按仓库查询库存

2. **库存追踪**
   - 序列号管理：支持商品序列号追踪
   - 批号管理：支持批次号管理
   - 有效期管理：支持商品有效期

3. **库存预警**
   - 库存上下限设置
   - 预警报表自动生成

#### 3.1.5 库存数据结构

**核心表结构：**

```
jsh_depot_head          # 单据主表
├── id                  # 主键
├── type                # 类型（入库/出库）
├── sub_type            # 子类型（采购/销售/零售等）
├── number              # 单据号
├── organ_id            # 往来单位ID
├── total_price         # 单据总金额
├── status              # 状态（0未审核/1已审核）
└── ...

jsh_depot_item          # 单据明细
├── id                  # 主键
├── header_id           # 主表ID
├── material_id         # 商品ID
├── oper_number         # 数量
├── unit_price          # 单价
├── all_price           # 金额
├── depot_id            # 仓库ID
├── another_depot_id    # 对方仓库ID（调拨用）
├── sn_list             # 序列号列表
├── batch_number        # 批号
├── expiration_date     # 有效期
└── ...

jsh_material_current_stock  # 当前库存表
├── id                      # 主键
├── material_id             # 商品ID
├── depot_id                # 仓库ID
├── current_number          # 当前库存数量
└── ...
```

### 3.2 财务模块

#### 3.2.1 功能概述

财务模块主要处理资金往来和账务管理，与进销存模块紧密集成。

**核心功能：**

| 功能点 | 功能描述 | 数据结构 |
|--------|----------|----------|
| 收入单 | 非销售类收入记账 | `account_head` 表，`type='收入'` |
| 支出单 | 非采购类支出记账 | `account_head` 表，`type='支出'` |
| 收款单 | 应收账款收款核销 | `account_head` 表，`type='收款'` |
| 付款单 | 应付账款付款核销 | `account_head` 表，`type='付款'` |
| 转账单 | 账户间资金转账 | `account_head` 表，`type='转账'` |
| 收预付款 | 预收/预付款管理 | `account_head` 表，`type='预收/预付'` |

#### 3.2.2 应收/应付管理

**业务流程：**

```
销售出库 → 生成应收账款 → 收款单核销
                ↓
         客户对账报表

采购入库 → 生成应付账款 → 付款单核销
                ↓
         供应商对账报表
```

**核心数据结构：**

```
jsh_account_head        # 财务主表
├── id                  # 主键
├── type                # 类型（收入/支出/收款/付款/转账）
├── organ_id            # 往来单位ID
├── hands_person_id     # 经手人ID
├── change_amount       # 变动金额
├── discount_money      # 优惠金额
├── total_price         # 合计金额
├── account_id          # 账户ID
├── bill_no             # 单据编号
├── bill_time           # 单据日期
├── status              # 状态（0未审核/1已审核）
└── ...

jsh_account_item        # 财务子表明细
├── id                  # 主键
├── header_id           # 主表ID
├── account_id          # 账户ID
├── in_out_item_id      # 收支项目ID
├── bill_id             # 关联单据ID（用于核销）
├── need_debt           # 应收欠款
├── finish_debt         # 已收欠款
├── each_amount         # 单项金额
└── ...

jsh_account             # 账户表
├── id                  # 主键
├── name                # 账户名称
├── initial_amount      # 期初金额
├── current_amount      # 当前余额
├── is_default          # 是否默认账户
└── ...
```

#### 3.2.3 资金账户管理

**功能特性：**
- 多账户管理：支持现金、银行存款等多个账户
- 账户余额实时更新
- 转账业务：账户间资金划转
- 默认账户设置

#### 3.2.4 财务报表

**已实现报表：**

| 报表名称 | 功能描述 |
|----------|----------|
| 账户统计 | 各账户收支统计、余额查询 |
| 客户对账 | 客户应收账款对账明细 |
| 供应商对账 | 供应商应付账款对账明细 |

#### 3.2.5 财务数据结构

**核心表结构：**

```
jsh_account_head        # 财务主表（收入/支出/收款/付款/转账）
├── id                  # 主键
├── type                # 类型（收入/支出/收款/付款/转账/预收/预付）
├── organ_id            # 往来单位ID（客户/供应商）
├── hands_person_id     # 经手人ID
├── change_amount       # 变动金额
├── discount_money      # 优惠金额
├── total_price         # 合计金额
├── account_id          # 账户ID
├── bill_no             # 单据编号
├── bill_time           # 单据日期
├── status              # 状态（0未审核/1已审核）
├── remark              # 备注
└── ...

jsh_account_item        # 财务子表明细
├── id                  # 主键
├── header_id           # 主表ID（关联jsh_account_head）
├── account_id          # 账户ID
├── in_out_item_id      # 收支项目ID
├── bill_id             # 关联单据ID（用于核销，关联jsh_depot_head）
├── need_debt           # 应收欠款
├── finish_debt         # 已收欠款
├── each_amount         # 单项金额
├── remark              # 备注
└── ...

jsh_account             # 账户表
├── id                  # 主键
├── name                # 账户名称（如：现金、工商银行、支付宝等）
├── serial_no           # 账号/卡号
├── initial_amount      # 期初金额
├── current_amount      # 当前余额
├── is_default          # 是否默认账户
├── remark              # 备注
└── ...

jsh_in_out_item         # 收支项目表
├── id                  # 主键
├── name                # 项目名称（如：办公费、差旅费、运费等）
├── type                # 类型（收入/支出）
├── remark              # 备注
└── ...
```

### 3.3 生产模块

#### 3.3.1 当前实现状态

**重要说明：** 根据代码分析，管伊佳ERP目前**尚未实现完整的生产模块**。README文档中提到"后面将会推出ERP的全部功能"，说明生产模块是后续规划的功能。

#### 3.3.2 已有的生产相关功能

当前系统中只实现了**简单的组装/拆卸功能**，作为生产模块的基础雏形：

| 功能点 | 功能描述 | 限制说明 |
|--------|----------|----------|
| 组装单 | 将多个商品组合成一个套装商品 | 无BOM管理、无生产计划、无成本核算 |
| 拆卸单 | 将套装商品拆分为多个单品 | 同上 |

**组装/拆卸业务流程：**
```
组装单：
商品A（1个） + 商品B（2个） → 套装商品C（1个）
    ↓
库存：A-1, B-2, C+1

拆卸单：
套装商品C（1个） → 商品A（1个） + 商品B（2个）
    ↓
库存：C-1, A+1, B+1
```

#### 3.3.3 生产模块缺失的核心功能

要实现完整的生产管理，需要补充以下功能：

| 功能模块 | 核心功能点 |
|----------|------------|
| **BOM管理** | 物料清单定义、多级BOM、BOM版本管理 |
| **生产计划** | MPS主生产计划、MRP物料需求计划 |
| **生产订单** | 生产工单管理、派工、报工 |
| **车间管理** | 工序管理、产能管理、车间调度 |
| **成本核算** | 材料成本、人工成本、制造费用分摊 |
| **质量管理** | 检验标准、质检单、不合格品处理 |

---

## 四、角色和权限控制分析

### 4.1 权限控制实现评估

**结论：管伊佳ERP确实实现了细致全面的权限控制，精确到每个按钮和菜单。**

该系统采用了**菜单权限 + 按钮权限 + 数据权限**的三层权限控制模型，实现了细粒度的访问控制。

### 4.2 权限控制架构

#### 4.2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                      权限控制整体架构                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐      ┌──────────────┐      ┌──────────────┐ │
│  │  菜单权限    │      │  按钮权限    │      │  数据权限    │ │
│  │  (Menu)     │      │  (Button)   │      │  (Data)     │ │
│  └──────────────┘      └──────────────┘      └──────────────┘ │
│         │                    │                    │            │
│         └────────────────────┼────────────────────┘            │
│                              │                                 │
│                    ┌─────────┴─────────┐                       │
│                    │   角色(Role)      │                       │
│                    │  权限分配中心     │                       │
│                    └─────────┬─────────┘                       │
│                              │                                 │
│                    ┌─────────┴─────────┐                       │
│                    │   用户(User)      │                       │
│                    │  权限最终载体     │                       │
│                    └───────────────────┘                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### 4.2.2 数据模型设计

**核心表结构：**

```
jsh_function              # 功能/菜单表
├── id                    # 主键
├── number                # 功能编号（用于层级关系）
├── name                  # 功能名称
├── parent_number         # 上级编号
├── url                   # 路由URL
├── component             # Vue组件路径
├── type                  # 类型（电脑版/手机版）
├── push_btn              # 功能按钮标识（哪些按钮可用）
├── icon                  # 图标
└── ...

jsh_role                  # 角色表
├── id                    # 主键
├── name                  # 角色名称
├── type                  # 角色类型
├── price_limit           # 价格查看权限控制
└── ...

jsh_user_business         # 用户业务关联表（核心权限表）
├── id                    # 主键
├── type                  # 关联类型
│                         │   - "UserRole": 用户-角色关联
│                         │   - "RoleFunctions": 角色-功能关联
│                         │   - "UserDepot": 用户-仓库关联
│                         │   - "UserOrg": 用户-机构关联
├── key_id                # 主体ID
├── value                 # 关联值（格式：[1][2][3]）
├── btn_str               # 按钮权限字符串（JSON格式）
└── ...
```

### 4.3 菜单权限实现

#### 4.3.1 实现原理

菜单权限通过**角色-功能关联**实现，具体流程如下：

```
1. 用户登录成功
       ↓
2. 获取用户角色（UserBusiness: type="UserRole"）
       ↓
3. 获取角色拥有的功能ID列表（UserBusiness: type="RoleFunctions"）
       ↓
4. 从jsh_function表查询完整菜单信息
       ↓
5. 构建菜单树返回前端
       ↓
6. 前端动态生成路由
```

#### 4.3.2 核心代码分析

**后端菜单查询接口：**

位置：`FunctionController.java:143`

```java
@PostMapping(value = "/findMenuByPNumber")
public JSONArray findMenuByPNumber(@RequestBody JSONObject jsonObject,
                                    HttpServletRequest request) throws Exception {
    String pNumber = jsonObject.getString("pNumber");
    String userId = jsonObject.getString("userId");
    
    // 1. 获取用户角色
    List<UserBusiness> roleList = userBusinessService.getBasicData(userId, "UserRole");
    String roleIdStr = value.replace("[", "").replace("]", "");
    
    // 2. 获取角色拥有的功能列表，格式如：[1][2][5]
    List<UserBusiness> funList = userBusinessService.getBasicData(roleIdStr, "RoleFunctions");
    String fc = funList.get(0).getValue();
    
    // 3. 查询所有功能并根据权限过滤
    List<Function> dataList = functionService.getRoleFunction(pNumber);
    JSONArray dataArray = getMenuByFunction(dataList, fc, approvalFlag, funIdMap, userInfo);
    
    return dataArray;
}
```

**菜单权限过滤逻辑：**

位置：`FunctionController.java:195`

```java
public JSONArray getMenuByFunction(List<Function> dataList, String fc, 
                                     String approvalFlag, Map<Long, Long> funIdMap, 
                                     User userInfo) throws Exception {
    JSONArray dataArray = new JSONArray();
    for (Function function : dataList) {
        // 超管或租户拥有全部权限
        if ("admin".equals(userInfo.getLoginName()) || 
            userInfo.getId().equals(userInfo.getTenantId()) || 
            funIdMap.get(function.getId()) != null) {
            
            JSONObject item = new JSONObject();
            List<Function> newList = functionService.getRoleFunction(function.getNumber());
            
            if (newList.size() > 0) {
                // 递归处理子菜单
                JSONArray childrenArr = getMenuByFunction(newList, fc, approvalFlag, funIdMap, userInfo);
                if (childrenArr.size() > 0) {
                    item.put("children", childrenArr);
                    dataArray.add(item);
                }
            } else {
                // 叶子节点：检查是否在权限列表中
                // fc格式：[1][2][3]
                if (fc.indexOf("[" + function.getId().toString() + "]") != -1) {
                    dataArray.add(item);
                }
            }
        }
    }
    return dataArray;
}
```

**前端路由动态生成：**

位置：`permission.js:13`

```javascript
router.beforeEach((to, from, next) => {
    if (Vue.ls.get(USER_ID)) {
        if (store.getters.permissionList.length === 0) {
            // 动态获取菜单权限
            store.dispatch('GetPermissionList').then(res => {
                const menuData = res;
                // 缓存用户的按钮权限
                store.dispatch('GetUserBtnList').then(res => {
                    Vue.ls.set('winBtnStrList', res.data.userBtn, 7 * 24 * 60 * 60 * 1000);
                });
                // 生成路由表
                let constRoutes = generateIndexRouter(menuData);
                store.dispatch('UpdateAppRouter', { constRoutes }).then(() => {
                    router.addRoutes(store.getters.addRouters);
                    next({ path: redirect });
                });
            });
        }
    }
});
```

### 4.4 按钮权限实现

#### 4.4.1 实现原理

按钮权限是管伊佳ERP权限控制的**核心亮点**，实现了精确到每个按钮的控制。

**权限存储格式：**

按钮权限存储在 `jsh_user_business` 表的 `btn_str` 字段，格式为JSON：

```json
[
    {"funId": 23, "btnStr": "1,2,3,7"},
    {"funId": 33, "btnStr": "1,2,3,7"},
    {"funId": 41, "btnStr": "1,2,3,7"}
]
```

**按钮标识含义：**

| 按钮标识 | 按钮名称 | 功能描述 |
|----------|----------|----------|
| 1 | 新增 | 添加记录 |
| 2 | 编辑 | 修改记录 |
| 3 | 删除 | 删除记录 |
| 7 | 审核/反审核 | 单据审核操作 |

#### 4.4.2 核心代码分析

**后端按钮权限获取：**

位置：`UserService.java:865`

```java
public JSONArray getBtnStrArrById(Long userId) throws Exception {
    JSONArray btnStrArr = new JSONArray();
    
    // 1. 获取用户角色
    List<UserBusiness> userRoleList = userBusinessService.getBasicData(userId.toString(), "UserRole");
    if (userRoleList != null && userRoleList.size() > 0) {
        String roleValue = userRoleList.get(0).getValue();
        // 解析角色ID：[5] -> 5
        roleValue = roleValue.replace("[", "").replace("]", "");
        
        // 2. 获取角色的按钮权限字符串
        List<UserBusiness> roleFunctionsList = userBusinessService.getBasicData(roleValue, "RoleFunctions");
        if (roleFunctionsList != null && roleFunctionsList.size() > 0) {
            String btnStr = roleFunctionsList.get(0).getBtnStr();
            if (StringUtil.isNotEmpty(btnStr)) {
                btnStrArr = JSONArray.parseArray(btnStr);
            }
        }
    }
    
    // 3. 将funId转换为url（前端使用）
    JSONArray btnStrWithUrlArr = new JSONArray();
    if (btnStrArr.size() > 0) {
        List<Function> functionList = functionService.getFunction();
        Map<Long, String> functionMap = new HashMap<>();
        for (Function function : functionList) {
            functionMap.put(function.getId(), function.getUrl());
        }
        for (Object obj : btnStrArr) {
            JSONObject btnStrObj = JSONObject.parseObject(obj.toString());
            Long funId = btnStrObj.getLong("funId");
            JSONObject btnStrWithUrlObj = new JSONObject();
            btnStrWithUrlObj.put("url", functionMap.get(funId));
            btnStrWithUrlObj.put("btnStr", btnStrObj.getString("btnStr"));
            btnStrWithUrlArr.add(btnStrWithUrlObj);
        }
    }
    return btnStrWithUrlArr;
}
```

**前端按钮权限控制：**

位置：`hasPermission.js:3`

```javascript
const hasPermission = {
    install(Vue, options) {
        // 注册v-has指令
        Vue.directive('has', {
            inserted: (el, binding, vnode) => {
                // 节点权限处理，如果命中则不进行全局权限处理
                if (!filterNodePermission(el, binding, vnode)) {
                    filterGlobalPermission(el, binding, vnode);
                }
            }
        });
    }
};
```

位置：`hasPermission.js:70`

```javascript
export function filterGlobalPermission(el, binding, vnode) {
    let permissionList = [];
    let allPermissionList = [];
    
    // 从sessionStorage获取权限列表
    let authList = JSON.parse(sessionStorage.getItem(USER_AUTH) || "[]");
    for (let auth of authList) {
        if (auth.type != '2') {
            permissionList.push(auth);
        }
    }
    
    // 获取全局配置权限
    let allAuthList = JSON.parse(sessionStorage.getItem(SYS_BUTTON_AUTH) || "[]");
    for (let gauth of allAuthList) {
        if (gauth.type != '2') {
            allPermissionList.push(gauth);
        }
    }
    
    // 检查权限
    if (permissionList === null || permissionList === "" || 
        permissionList === undefined || permissionList.length <= 0) {
        // 没有权限，从DOM中移除元素
        el.parentNode.removeChild(el);
        return;
    }
    
    let permissions = [];
    for (let item of permissionList) {
        if (item.type != '2') {
            permissions.push(item.action);
        }
    }
    
    // 检查当前按钮权限是否在列表中
    if (!permissions.includes(binding.value)) {
        el.parentNode.removeChild(el);
    }
}
```

**Vue组件中使用示例：**

```vue
<template>
    <a-button v-has="'add'" type="primary">新增</a-button>
    <a-button v-has="'edit'" type="primary">编辑</a-button>
    <a-button v-has="'delete'" type="danger">删除</a-button>
    <a-button v-has="'approve'" type="primary">审核</a-button>
</template>
```

### 4.5 数据权限实现

#### 4.5.1 实现原理

数据权限主要通过**仓库权限**和**机构权限**实现，限制用户只能查看和操作有权限范围内的数据。

**核心关联类型：**
- `UserDepot`：用户-仓库关联
- `UserOrg`：用户-机构关联

#### 4.5.2 仓库权限控制

位置：`DepotHeadService.java:112`

```java
public List<DepotHeadVo4List> select(...) throws Exception {
    // ...
    // 获取当前用户有权限的仓库列表
    String[] depotArray = getDepotArray(subType);
    // ...
}
```

**查询时自动过滤：**

位置：`DepotHeadController.java:206`

```java
@GetMapping(value = "/findInOutDetail")
public BaseResponseInfo findInOutDetail(...) throws Exception {
    List<Long> depotList = new ArrayList<>();
    if (depotId != null) {
        depotList.add(depotId);
    } else {
        // 未选择仓库时，默认为当前用户有权限的仓库
        JSONArray depotArr = depotService.findDepotByCurrentUser();
        for (Object obj : depotArr) {
            JSONObject object = JSONObject.parseObject(obj.toString());
            depotList.add(object.getLong("id"));
        }
    }
    // ...
}
```

#### 4.5.3 价格查看权限

系统还实现了**价格字段级权限控制**，通过角色的 `price_limit` 字段控制用户是否能看到价格信息。

位置：`RoleService.java`

```java
public String getCurrentPriceLimit(HttpServletRequest request) {
    // 获取当前用户角色的价格限制设置
    // 控制用户是否能查看采购价、销售价等敏感价格信息
}
```

### 4.6 登录认证机制

#### 4.6.1 登录过滤器

位置：`LogCostFilter.java:36`

```java
@Override
public void doFilter(ServletRequest request, ServletResponse response,
                     FilterChain chain) throws IOException, ServletException {
    HttpServletRequest servletRequest = (HttpServletRequest) request;
    HttpServletResponse servletResponse = (HttpServletResponse) response;
    String requestUrl = servletRequest.getRequestURI();
    
    // 路径遍历攻击防护
    if (requestUrl.contains("..") || requestUrl.contains("%2e") || requestUrl.contains("%2E")) {
        servletResponse.setStatus(500);
        servletResponse.getWriter().write("loginOut");
        return;
    }
    
    // 检查是否已登录（从Redis获取session）
    Object userId = redisService.getObjectFromSessionByKey(servletRequest, "userId");
    if (userId != null) {
        chain.doFilter(request, response);
        return;
    }
    
    // 白名单URL（无需登录）
    if (requestUrl.equals("/jshERP-boot/doc.html") || 
        requestUrl.equals("/jshERP-boot/user/login") ||
        requestUrl.equals("/jshERP-boot/user/register") ||
        // ... 其他白名单
    ) {
        chain.doFilter(request, response);
        return;
    }
    
    // 返回登录状态
    servletResponse.setStatus(500);
    servletResponse.getWriter().write("loginOut");
}
```

#### 4.6.2 Token管理

位置：`RedisService.java:51`

```java
public Object getObjectFromSessionByKey(HttpServletRequest request, String key) {
    Object obj = null;
    if (request == null) {
        return null;
    }
    // 从请求头获取Token
    String token = request.getHeader(ACCESS_TOKEN);  // X-Access-Token
    if (token != null) {
        // 从Redis获取用户信息
        if (redisTemplate.opsForHash().hasKey(token, key)) {
            obj = redisTemplate.opsForHash().get(token, key);
            // 刷新Token过期时间
            redisTemplate.expire(token, BusinessConstants.MAX_SESSION_IN_SECONDS, TimeUnit.SECONDS);
        }
    }
    return obj;
}
```

### 4.7 权限控制总结

管伊佳ERP的权限控制实现了以下特性：

| 权限类型 | 控制粒度 | 实现方式 | 完整性评估 |
|----------|----------|----------|------------|
| **菜单权限** | 模块/页面级 | 角色-功能关联 | ✅ 完整实现 |
| **按钮权限** | 操作级 | v-has指令 + btn_str字段 | ✅ 完整实现（核心亮点） |
| **数据权限** | 仓库/机构级 | UserDepot/UserOrg关联 | ✅ 基本实现 |
| **字段权限** | 价格字段 | price_limit角色属性 | ✅ 部分实现 |
| **接口权限** | API级别 | 未实现 | ❌ 缺失（重要安全风险） |

**权限控制优势：**
1. 设计合理，采用RBAC模型
2. 按钮权限控制精细，实现了"增删改查审核"等操作级控制
3. 前后端权限校验联动
4. 支持多租户权限隔离

**权限控制不足：**
1. **缺少API接口级权限校验**：后端Controller层没有对每个接口进行权限验证，只依赖前端按钮隐藏和登录过滤器，存在越权访问风险
2. 缺少操作日志的权限上下文记录
3. 缺少权限变更审计日志

---

## 五、系统安全问题分析

### 5.1 安全问题总览

| 安全问题类型 | 严重程度 | 是否存在 | 位置 |
|--------------|----------|----------|------|
| XSS跨站脚本攻击 | 中高 | 是 | 前端+后端 |
| CSRF跨站请求伪造 | 高 | 是 | 全站 |
| 点击劫持 | 中 | 是 | 全站 |
| 敏感信息泄露 | 高 | 是 | 多处 |
| 依赖不安全的第三方库 | 高 | 是 | pom.xml/package.json |
| SQL注入 | 中 | 是 | 数据库操作 |
| 认证机制弱点 | 中高 | 是 | 登录机制 |
| 授权机制弱点 | 高 | 是 | API接口 |

### 5.2 详细安全问题分析

#### 5.2.1 依赖不安全的第三方库（高风险）

**问题描述：** 系统使用了多个存在已知安全漏洞的第三方库。

**具体问题：**

| 依赖库 | 当前版本 | 漏洞风险 | CVE编号 | 风险等级 |
|--------|----------|----------|---------|----------|
| **Fastjson** | 1.2.83 | 反序列化远程代码执行 | CVE-2022-25845等 | 🔴 严重 |
| **Spring Boot** | 2.0.0.RELEASE | 多个已知漏洞 | 多个 | 🟠 高 |
| **Log4j** | 2.15.0 | Log4Shell漏洞（接近修复版本） | CVE-2021-44228 | 🟠 高 |
| **Apache HttpClient** | 4.5.2 | 多个已知漏洞 | CVE-2020-13956等 | 🟡 中 |
| **Commons IO** | 1.3.2 | 版本过旧，多个漏洞 | - | 🟡 中 |
| **Axios** | 0.18.0 | 存在SSRF等漏洞 | - | 🟡 中 |

**关键漏洞说明 - Fastjson：**

Fastjson 1.2.83版本虽然修复了大部分反序列化漏洞，但仍然存在安全风险：

```java
// 后端大量使用Fastjson进行JSON解析
// 位置：多处Controller和Service
JSONObject obj = JSON.parseObject(jsonString);  // 反序列化
```

**建议升级版本：**
- Fastjson：升级到 1.2.83 以上或 2.0.x 版本
- Spring Boot：升级到 2.7.x 或 3.x 稳定版本
- Log4j：升级到 2.17.1 以上

#### 5.2.2 CSRF跨站请求伪造（高风险）

**问题描述：** 系统完全没有CSRF防护机制。

**风险分析：**

CSRF攻击利用用户已登录的身份，在用户不知情的情况下执行恶意操作。

**攻击场景示例：**

```html
<!-- 攻击者构造的恶意页面 -->
<form action="http://erp.example.com/depotHead/delete" method="POST">
    <input type="hidden" name="id" value="1" />
</form>
<script>document.forms[0].submit();</script>
```

**当前防护情况：**

位置：`request.js:73`

```javascript
// 请求拦截器只添加了Token，没有CSRF Token
service.interceptors.request.use(config => {
    const token = Vue.ls.get(ACCESS_TOKEN)
    if (token) {
        config.headers['X-Access-Token'] = token
    }
    return config
})
```

**问题：**
- 没有使用 CSRF Token
- 没有验证 Referer 头
- 没有使用 SameSite Cookie 属性

#### 5.2.3 XSS跨站脚本攻击（中高风险）

**问题描述：** 系统存在XSS攻击风险。

**风险分析：**

**1. 存储型XSS风险：**

位置：多个Controller接收用户输入

```java
// 示例：商品信息录入
// 没有对用户输入进行XSS过滤
@PostMapping(value = "/add")
public String addResource(@RequestBody JSONObject obj, HttpServletRequest request) {
    // obj直接包含用户输入，没有进行HTML编码或过滤
    material.setName(obj.getString("name"));  // 直接存储
}
```

**2. 反射型XSS风险：**

前端使用 `v-html` 或直接操作DOM时可能存在风险。

**3. DOM型XSS风险：**

位置：前端多处使用用户输入

```javascript
// 示例：前端路由处理
// 用户输入可能被直接插入DOM
let search = this.$route.query.search;
this.$refs.searchInput.value = search;  // 可能存在DOM XSS
```

#### 5.2.4 点击劫持（中风险）

**问题描述：** 系统没有点击劫持防护。

**风险分析：**

攻击者可以通过iframe嵌套ERP系统，使用透明层欺骗用户点击。

```html
<!-- 攻击者页面 -->
<iframe src="http://erp.example.com" style="opacity:0;"></iframe>
<div style="position:absolute;top:100px;left:100px;">
    点击领取红包
</div>
```

**当前防护情况：**

- 没有设置 `X-Frame-Options` 响应头
- 没有使用 `Content-Security-Policy: frame-ancestors`
- 没有前端frame-busting防护代码

#### 5.2.5 敏感信息泄露（高风险）

**问题描述：** 系统存在多处敏感信息泄露风险。

**具体问题：**

**1. 密码加密方式弱：**

位置：`Tools.java`

```java
// 使用MD5加密，且没有加盐
public static String md5Encryp(String str) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(str.getBytes());
    return new BigInteger(1, md.digest()).toString(16);
}
```

**风险：**
- MD5已被破解，存在彩虹表攻击风险
- 没有加盐，相同密码产生相同哈希

**2. 默认密码风险：**

位置：`UserService.java:236`

```java
// 重置密码为固定值
public String resetPwd(@RequestBody JSONObject jsonObject, ...) {
    String password = "123456";  // 硬编码默认密码
    String md5Pwd = Tools.md5Encryp(password);
    // ...
}
```

位置：`BusinessConstants.java`

```java
public static final String USER_DEFAULT_PASSWORD = "123456";
```

**3. 前端存储敏感信息：**

位置：`permission.js:29`

```javascript
// 权限信息存储在sessionStorage
Vue.ls.set('winBtnStrList', res.data.userBtn, 7 * 24 * 60 * 60 * 1000);
```

位置：`store/modules/user.js`

```javascript
// 用户信息可能包含敏感数据
```

**4. 错误信息泄露：**

位置：`GlobalExceptionHandler.java`

```java
// 异常处理可能泄露系统信息
@ExceptionHandler(value = Exception.class)
public String handleException(HttpServletRequest request, Exception e) {
    logger.error(e.getMessage(), e);
    // 可能返回过多的错误详情
}
```

#### 5.2.6 SQL注入风险（中风险）

**问题描述：** 系统部分代码存在SQL注入风险。

**风险分析：**

**1. 部分使用字符串拼接SQL：**

位置：`SqlUtil.java`

```java
public static String SQL_REGEX = "and |extractvalue|updatexml|exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |or |+|user()";

public static String escapeOrderBySql(String value) {
    if (StringUtil.isNotEmpty(value) && !isValidOrderBySql(value)) {
        throw new UtilException("参数不符合规范，不能进行查询");
    }
    // ...
}
```

**问题：**
- 虽然有SQL关键字过滤，但过滤规则不完整
- 正则匹配可能被绕过

**2. MyBatis直接使用${}：**

在XML Mapper中，如果使用 `${}` 而不是 `#{}`，会导致SQL注入。

位置：多个mapper XML文件（需要检查）

```xml
<!-- 风险写法 -->
<select id="findByCondition">
    SELECT * FROM jsh_user WHERE name = ${name}
</select>

<!-- 安全写法 -->
<select id="findByCondition">
    SELECT * FROM jsh_user WHERE name = #{name}
</select>
```

#### 5.2.7 认证机制弱点（中高风险）

**问题描述：** 登录认证机制存在弱点。

**具体问题：**

**1. 没有登录失败锁定：**

位置：`UserService.java:403`

```java
public int validateUser(String loginName, String password) throws Exception {
    // 没有登录失败次数限制，存在暴力破解风险
    // ...
}
```

**2. 没有验证码强制要求：**

位置：`UserService.java:297`

```java
public void validateCaptcha(String code, String uuid) throws Exception {
    PlatformConfig platformConfig = platformConfigService.getInfoByKey("checkcode_flag");
    // 验证码是可选的，可以通过配置关闭
    if (platformConfig != null && "1".equals(platformConfig.getPlatformValue())) {
        // 才验证验证码
    }
}
```

**3. Session管理问题：**

位置：`RedisService.java:154`

```java
public void deleteObjectByUserAndIp(Long userId, String clientIp) {
    Set<String> tokens = redisTemplate.keys("*");  // 使用keys("*")性能问题
    // ...
}
```

**问题：**
- `keys("*")` 在Redis数据量大时会严重阻塞
- 没有实现单用户登录限制（同一账号可多处登录）

#### 5.2.8 授权机制弱点（高风险）

**问题描述：** API接口缺少细粒度权限验证。

**核心问题：越权访问风险**

位置：多个Controller

```java
@GetMapping(value = "/info")
public String getList(@RequestParam("id") Long id, HttpServletRequest request) throws Exception {
    // 只检查是否登录，没有检查数据权限
    // 问题：用户A可以通过修改ID参数查看用户B的数据
    User user = userService.getUser(id);
    // ...
}
```

**具体风险场景：**

**1. 水平越权：**

```
用户A（ID=100）登录后
正常访问：/user/info?id=100（查看自己的信息）
恶意访问：/user/info?id=101（查看用户B的信息）
```

**2. 垂直越权：**

```
普通用户登录后
通过直接访问URL：/system/tenant/list
可能获取到只有管理员才能看到的租户列表
```

**当前权限验证情况：**

位置：`LogCostFilter.java`

```java
@Override
public void doFilter(...) {
    // 只验证是否登录
    Object userId = redisService.getObjectFromSessionByKey(servletRequest, "userId");
    if (userId != null) {
        chain.doFilter(request, response);  // 已登录就放行
        return;
    }
    // ...
}
```

**问题：**
- 只有登录验证，没有接口级权限验证
- 没有验证用户是否有权限访问特定资源
- 没有验证数据所有权

#### 5.2.9 文件上传安全（中风险）

**问题描述：** 文件上传功能存在安全风险。

**风险分析：**

位置：`commonUploadFile.js` 及相关后端

```javascript
// 前端文件上传
// 可能缺少文件类型验证或验证可绕过
```

**可能存在的问题：**
1. 文件类型验证不严格（只检查扩展名）
2. 文件大小限制不当
3. 上传路径可预测
4. 可能存在文件覆盖风险

#### 5.2.10 不安全的配置

**问题描述：** 存在不安全的默认配置。

**具体问题：**

**1. Swagger接口暴露：**

位置：`Swagger2Config.java`

```java
@Configuration
@EnableSwagger2
public class Swagger2Config {
    // Swagger接口在生产环境可能暴露
    // 没有根据环境条件启用
}
```

**2. 调试信息：**

位置：前端代码

```javascript
// hasPermission.js:8
console.log("页面权限控制----");  // 生产环境不应保留

// hasPermission.js:45
console.log("流程节点页面权限--NODE--");  // 调试信息
```

**3. 硬编码密钥：**

检查是否存在硬编码的API密钥、密码等。

### 5.3 安全问题汇总与建议

#### 5.3.1 问题优先级排序

| 优先级 | 安全问题 | 建议措施 |
|--------|----------|----------|
| 🔴 P0 | 第三方库漏洞（Fastjson、Spring Boot） | 立即升级依赖库 |
| 🔴 P0 | API接口缺少权限验证 | 添加接口级权限校验 |
| 🟠 P1 | CSRF防护缺失 | 实现CSRF Token机制 |
| 🟠 P1 | 密码加密方式弱 | 改用BCrypt/Argon2加密 |
| 🟠 P1 | SQL注入风险 | 全面检查并使用参数化查询 |
| 🟡 P2 | XSS防护不足 | 添加输入过滤和输出编码 |
| 🟡 P2 | 点击劫持防护缺失 | 添加安全响应头 |
| 🟡 P2 | 登录暴力破解风险 | 添加登录失败锁定 |
| 🟢 P3 | 调试信息泄露 | 清理生产环境调试代码 |
| 🟢 P3 | Swagger接口暴露 | 生产环境禁用Swagger |

#### 5.3.2 安全架构改进建议

```
建议的安全架构：

┌─────────────────────────────────────────────────────────────┐
│                      安全防护层                              │
├─────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    Web安全防护                           │ │
│  │  • CSP内容安全策略                                       │ │
│  │  • X-Frame-Options                                      │ │
│  │  • X-XSS-Protection                                     │ │
│  │  • CSRF Token验证                                       │ │
│  └─────────────────────────────────────────────────────────┘ │
│                              │                                  │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    认证授权层                           │ │
│  │  • JWT/Token认证（增强）                                │ │
│  │  • 接口级权限校验（AOP切面）                            │ │
│  │  • 数据权限校验（所有者检查）                            │ │
│  │  • 登录失败锁定                                         │ │
│  └─────────────────────────────────────────────────────────┘ │
│                              │                                  │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    输入验证层                           │ │
│  │  • 参数校验（JSR-303）                                  │ │
│  │  • XSS过滤（HTML编码）                                   │ │
│  │  • SQL注入防护（参数化查询）                             │ │
│  └─────────────────────────────────────────────────────────┘ │
│                              │                                  │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    审计日志层                           │ │
│  │  • 操作日志增强                                         │ │
│  │  • 安全事件审计                                         │ │
│  │  • 异常监控                                             │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 六、总结

### 6.1 系统优势

1. **功能完整**：进销存+财务核心业务流程完整
2. **权限控制精细**：实现了菜单+按钮的细粒度权限控制
3. **架构清晰**：前后端分离，代码结构清晰
4. **多租户支持**：支持云平台多租户模式
5. **插件化设计**：具备插件扩展能力
6. **国际化支持**：支持多语言切换

### 6.2 系统劣势

1. **生产模块缺失**：仅支持简单组装/拆卸，无完整生产管理
2. **安全风险高**：存在多处安全漏洞（详见第五章）
3. **技术栈陈旧**：使用较老版本的框架和库
4. **接口权限不足**：缺少API级别的权限验证
5. **密码保护弱**：使用MD5加密，无盐值

### 6.3 适用场景

管伊佳ERP适合以下场景：
- 中小型企业进销存管理
- 简单财务管理
- 需要多租户的云平台场景
- 学习Java Web开发的教学案例

### 6.4 不适用场景

- 需要完整生产管理的制造企业
- 对安全要求极高的金融/政府行业
- 需要复杂成本核算的企业
- 需要多级BOM管理的企业

---

**报告生成时间：** 2026-04-18

**分析依据：** 代码库全量分析（jshERP-boot后端 + jshERP-web前端）
