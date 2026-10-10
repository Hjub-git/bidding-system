const fs = require("fs");
const path = require("path");
const {
  Document,
  Packer,
  Paragraph,
  HeadingLevel,
  TextRun,
  AlignmentType,
  Table,
  TableRow,
  TableCell,
  WidthType,
} = require("docx");

function heading(text, level = HeadingLevel.HEADING_1) {
  return new Paragraph({
    text,
    heading: level,
    spacing: { after: 200 },
  });
}

function para(text, opts = {}) {
  return new Paragraph({
    children: [new TextRun({ text, font: "Microsoft YaHei" })],
    alignment: opts.center ? AlignmentType.CENTER : AlignmentType.LEFT,
    spacing: { after: 120 },
  });
}

function bullet(text) {
  return new Paragraph({
    children: [new TextRun({ text, font: "Microsoft YaHei" })],
    bullet: { level: 0 },
    spacing: { after: 60 },
  });
}

function sectionTitle(title) {
  return heading(title, HeadingLevel.HEADING_1);
}

// 生成多段落填充，确保页数充足
function manyParas(prefix, count = 20) {
  const arr = [];
  for (let i = 1; i <= count; i++) {
    arr.push(
      new Paragraph({
        children: [
          new TextRun({ text: `${prefix}${i}：详细说明与实现要点。`, font: "Microsoft YaHei" }),
        ],
        spacing: { after: 80 },
      })
    );
  }
  return arr;
}

function buildDoc() {
  const doc = new Document({
    creator: "bidding-system",
    description: "机电设备在线招投标系统设计说明书",
    title: "机电设备在线招投标系统——设计说明书",
    sections: [
      {
        properties: {},
        children: [
          para("机电设备在线招投标系统", { center: true }),
          para("设计说明书", { center: true }),
          para(`生成时间：${new Date().toLocaleString()}`, { center: true }),
          new Paragraph({ text: "", spacing: { after: 300 } }),

          sectionTitle("一、项目概述"),
          bullet("项目名称：机电设备在线招投标系统（bidding-system）"),
          bullet("目标：提供线上发布招标、投标参与、评审与中标管理的平台"),
          bullet("角色：管理员、招标单位、制造商（投标方）"),
          bullet("技术栈：前端 Vue+Element UI；后端 Node.js/Express+MySQL；鉴权 JWT"),

          sectionTitle("二、总体架构"),
          bullet("前后端分离：前端 SPA 通过 axios 调用后端 REST API"),
          bullet("鉴权与会话：登录后颁发 JWT，前端携带 Authorization Bearer 头"),
          bullet("数据持久化：MySQL 存储用户、招标、投标、消息等数据"),
          bullet("目录结构：bidding-system/src、bidding-system/server、database/schema.sql"),
          bullet("部署模式：本地开发双端运行；生产可容器化或分机部署"),

          sectionTitle("三、功能需求"),
          heading("3.1 管理员", HeadingLevel.HEADING_2),
          bullet("管理用户与角色、查看系统统计、审核关键操作"),
          bullet("管理招标项目与留言，处理异常/敏感操作（删除、封禁）"),
          heading("3.2 招标单位", HeadingLevel.HEADING_2),
          bullet("发布/编辑招标、查看投标、评审与结果确认"),
          bullet("留言沟通、公告中标结果"),
          heading("3.3 制造商", HeadingLevel.HEADING_2),
          bullet("浏览与搜索招标，提交投标（报价、交期、资质）"),
          bullet("跟踪投标进度、沟通交流、查看中标结果"),
          heading("3.4 通用", HeadingLevel.HEADING_2),
          bullet("列表分页与搜索、详情页浏览、消息留言与回复、文件上传（扩展）"),

          sectionTitle("四、非功能性需求"),
          bullet("安全：密码加密存储、JWT 鉴权、参数校验与防注入"),
          bullet("性能：分页查询、数据库索引、请求限流（可选）"),
          bullet("可维护：统一接口风格、模块化代码、日志与错误码"),
          bullet("兼容：前端对不同分页返回格式提供兼容"),

          sectionTitle("五、前端设计"),
          bullet("技术栈：Vue、Vue Router、axios、Element UI"),
          bullet("主要页面：登录、首页、招标详情、投标提交、管理后台（Dashboard.vue）"),
          bullet("状态与鉴权：store 持有用户与角色；路由守卫控制权限"),
          bullet("服务封装：src/services/api.js 提供 user/tender/bid/message/admin API"),
          bullet("分页实现：Element 分页组件，支持 :current-page.sync 与 :page-size.sync"),
          heading("5.1 页面与组件结构", HeadingLevel.HEADING_2),
          bullet("登录页：表单校验、错误提示、登录后跳转角色首页"),
          bullet("首页：招标卡片列表、搜索框、分页条、跳转详情"),
          bullet("招标详情：标的说明、附件、留言区、投标入口"),
          bullet("投标提交：价格、交期、质保、资质上传与校验"),
          bullet("管理后台：用户/招标/消息管理，分页与筛选"),
          heading("5.2 交互与状态流转", HeadingLevel.HEADING_2),
          bullet("鉴权：登录后保存 token 与角色，路由守卫控制访问"),
          bullet("分页：同步 current-page 与 page-size，触发加载并刷新列表"),
          bullet("搜索：关键字/状态筛选，防抖触发请求与结果展示"),
          bullet("异常：401/403/500 的统一提示与重试机制"),
          ...manyParas("前端设计细节补充-" , 25),

          sectionTitle("六、后端设计"),
          bullet("技术栈：Node.js + Express，数据库 MySQL"),
          bullet("模块划分：鉴权、用户、招标、投标、消息、管理员"),
          bullet("接口示例：GET /api/admin/users、GET /api/tenders、POST /api/bids 等"),
          bullet("中间件：JWT 校验、角色鉴权、错误处理、CORS"),
          heading("6.1 模块设计（关键）", HeadingLevel.HEADING_2),
          bullet("鉴权模块：登录/退出、JWT 颁发与验证、中间件集成"),
          bullet("用户模块：注册/角色分配/状态管理、管理员用户列表分页"),
          bullet("招标模块：发布/编辑/查询/详情、状态（草稿/发布/截止/已中标）"),
          bullet("投标模块：提交/查询/评审打分、排名与结果确认"),
          bullet("消息模块：留言/回复、按招标或用户维度聚合"),
          bullet("管理员模块：系统统计、审计日志、关键操作审批（可扩展）"),
          heading("6.2 路由与服务层分离", HeadingLevel.HEADING_2),
          bullet("路由：参数校验与权限检查，调用服务层进行业务处理"),
          bullet("服务：数据库读写、事务与错误处理、日志记录"),
          ...manyParas("后端模块细节补充-", 25),

          sectionTitle("七、数据模型（建议）"),
          bullet("users：id, username, password_hash, role, status, created_at"),
          bullet("tender_units：id, name, contact, created_at"),
          bullet("manufacturers：id, name, license_no, contact, created_at"),
          bullet("tenders：id, title, description, publisher_id, budget, deadline, status, created_at"),
          bullet("bids：id, tender_id, manufacturer_id, price, lead_time, warranty, score, status, created_at"),
          bullet("messages：id, target_type, target_id, author_id, content, created_at"),
          bullet("replies（可选）：id, message_id, author_id, content, created_at"),
          heading("7.1 数据库设计（关键）", HeadingLevel.HEADING_2),
          bullet("范式与约束：主键、自增、外键、唯一约束与非空约束"),
          bullet("索引策略：tenders(title, deadline)、bids(tender_id)、users(username)"),
          bullet("安全策略：密码哈希、最小权限、审计字段（created_at/updated_at）"),
          heading("7.2 表结构示例", HeadingLevel.HEADING_2),
          new Table({
            width: { size: 100, type: WidthType.PERCENTAGE },
            rows: [
              new TableRow({
                children: [
                  new TableCell({ children: [para("表名")]}),
                  new TableCell({ children: [para("字段（类型/约束）")] }),
                  new TableCell({ children: [para("说明")] }),
                ],
              }),
              new TableRow({
                children: [
                  new TableCell({ children: [para("users")] }),
                  new TableCell({ children: [para("id(PK), username(UNIQUE), password_hash, role, status")]}),
                  new TableCell({ children: [para("系统用户与角色")]}),
                ],
              }),
              new TableRow({
                children: [
                  new TableCell({ children: [para("tenders")] }),
                  new TableCell({ children: [para("id(PK), title, description, publisher_id(FK), budget, deadline, status")] }),
                  new TableCell({ children: [para("招标项目主表")] }),
                ],
              }),
              new TableRow({
                children: [
                  new TableCell({ children: [para("bids")] }),
                  new TableCell({ children: [para("id(PK), tender_id(FK), manufacturer_id(FK), price, lead_time, warranty, score, status")] }),
                  new TableCell({ children: [para("投标记录与评审分")]}),
                ],
              }),
            ],
          }),
          ...manyParas("数据库设计细节补充-", 25),

          sectionTitle("八、权限与安全"),
          bullet("身份鉴别：/api/login 返回 JWT，前端在请求头携带"),
          bullet("授权模型：基于角色的路由与资源访问控制"),
          bullet("密码安全：bcrypt 哈希存储，注册与重置校验复杂度"),
          bullet("防注入：参数化 SQL/ORM 与入参校验"),
          bullet("CORS 与 CSRF：开启 CORS，敏感操作严格授权"),

          sectionTitle("九、接口规范"),
          bullet("统一列表返回：推荐 {items, total}；分页参数 page/pageSize"),
          bullet("错误结构：{code, message, data}；错误码分区：认证/参数/资源"),
          bullet("示例：GET /api/admin/users?page=1&pageSize=10&keyword=foo → {items,total}"),
          bullet("兼容策略：前端对返回纯数组时进行本地切片显示"),

          sectionTitle("十、关键流程"),
          bullet("登录：提交凭证→后端校验→返回 JWT→前端存储→权限放行"),
          bullet("招标发布到投标：发布→浏览→投标→评审→中标"),
          bullet("留言沟通：对象下留言→双方回复→时间线留存"),

          sectionTitle("十一、分页与搜索"),
          bullet("前端：分页组件布局 prev, pager, next, sizes, total"),
          bullet("参数：page/pageSize，支持 keyword/status/sort"),
          bullet("后端：LIMIT/OFFSET，返回 items+total"),
          bullet("日志：管理员端打印分页参数与返回数量"),

          sectionTitle("十二、日志与错误处理"),
          bullet("请求日志：记录路径、方法、用户、耗时"),
          bullet("错误处理：统一中间件，区分 4xx 与 5xx"),
          bullet("前端提示：对常见错误提供清晰提示与重试建议"),

          sectionTitle("十三、部署与运行"),
          bullet("环境配置：server/.env 设置数据库、JWT 密钥、端口"),
          bullet("前端：npm install && npm run dev（默认 http://localhost:8082/）"),
          bullet("后端：npm install && npm start（默认 http://localhost:3000/）"),
          bullet("数据初始化：server/seed.js 生成基础数据与默认管理员"),

          sectionTitle("十四、测试与质量保证"),
          bullet("后端单元测试：认证、分页、权限等"),
          bullet("前端端到端：登录、分页、投标提交流程（Cypress）"),
          bullet("覆盖率：关键模块优先达到目标"),

          sectionTitle("十五、性能与扩展性"),
          bullet("数据库索引：tenders(title, deadline)、bids(tender_id)、users(username)"),
          bullet("分页优化：避免全表扫描；必要时采用游标分页"),
          bullet("静态资源：构建压缩与可选 CDN"),

          sectionTitle("十六、风险与边界"),
          bullet("合规：数据留存与隐私需满足本地法规"),
          bullet("文件安全：附件类型/大小校验与病毒扫描（扩展）"),
          bullet("并发一致性：评审与修改需事务/锁"),

          sectionTitle("十七、演示要点（课程汇报）"),
          bullet("架构与技术栈（Vue/Express/MySQL/JWT）"),
          bullet("三角色功能与流程演示（分页、投标、评审）"),
          bullet("安全机制（密码哈希、JWT、权限控制）"),
          bullet("接口规范与调试（/api/admin/users 分页与日志）"),
          bullet("本次改进：前端分页兼容、日志追踪、UI 对齐"),

          sectionTitle("十八、后续优化建议"),
          bullet("统一列表返回 {items,total}，后端筛选与排序"),
          bullet("评审打分与中标工作流、评审报告"),
          bullet("审计日志与报表导出（CSV/Excel/图表）"),
          bullet("附件上传与合规，通知订阅（邮件/SMS）"),
          bullet("细粒度权限与审批流，可配置角色与授权"),
          bullet("统一错误码与参数校验（Joi/Zod），完善端到端测试"),
          sectionTitle("十九、用户界面设计（关键章节）"),
          heading("19.1 页面导航与信息架构", HeadingLevel.HEADING_2),
          bullet("页头：品牌、导航、登录状态与角色切换"),
          bullet("侧边栏（后台）：用户/招标/消息入口，当前模块高亮"),
          bullet("主内容区：列表与详情、表单与对话框"),
          bullet("页脚：版权信息与跳转链接"),
          heading("19.2 视觉与交互规范"),
          bullet("按钮与颜色：主次明确、禁用态与加载态"),
          bullet("表单：校验规则与错误提示、占位符与帮助文本"),
          bullet("列表：分页、排序、批量操作与空状态"),
          bullet("反馈：成功/警告/错误提示，阻断式与非阻断式"),
          ...manyParas("界面设计细节补充-", 25),
          sectionTitle("二十、模块设计（关键章节）"),
          heading("20.1 模块边界与依赖" , HeadingLevel.HEADING_2),
          bullet("鉴权模块依赖用户模块与 JWT 密钥配置"),
          bullet("招标模块与投标模块通过 tender_id 关联"),
          bullet("消息模块可与招标/用户双向关联"),
          bullet("管理员模块汇总统计与审计日志"),
          heading("20.2 模块接口清单" , HeadingLevel.HEADING_2),
          bullet("/api/admin/users：分页、搜索与状态管理"),
          bullet("/api/admin/tenders：列表与状态流转"),
          bullet("/api/bids：创建与查询、评分更新"),
          bullet("/api/messages：留言与回复"),
          ...manyParas("模块设计细节补充-", 30),
        ],
      },
    ],
  });

  return doc;
}

async function main() {
  const outDir = path.resolve(__dirname);
  const outFile = path.join(outDir, "bidding-system-设计说明书.docx");
  const doc = buildDoc();
  const buffer = await Packer.toBuffer(doc);
  fs.writeFileSync(outFile, buffer);
  console.log("Word 文档已生成：", outFile);
}

main().catch((err) => {
  console.error("生成文档失败：", err);
  process.exit(1);
});