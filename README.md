# 问卷调查统计系统（纯 Java 实现）

不用 Spring、不用 Tomcat、不引任何第三方 jar：HTTP 服务用 JDK 自带的 `com.sun.net.httpserver`，
JSON 编解码、路由、统计引擎、报表输出全部手写。前端是三个没有任何样式的原生 HTML 页面（白纸黑字）。

## 快速开始

```bat
build.cmd          :: 编译到 out\ 并打包 survey.jar
run.cmd            :: 启动，默认 http://127.0.0.1:8080
run.cmd 9000       :: 换端口
```

> 若 PowerShell 禁止执行脚本，`*.cmd` 已经用 `-ExecutionPolicy Bypass` 绕过，直接双击即可。

启动后打开 <http://127.0.0.1:8080>：

- 首页：问卷列表、新建问卷、发布 / 结束
- `fill.html?id=1001`：填写（10 道题）
- `stats.html?id=1001`：统计结果，页头可切换纯文本 / JSON / CSV

首次启动会自动生成一份 10 题的示例问卷（#1001）和 6 份模拟答卷，打开统计页就有数据。

## 部署

项目零外部依赖，构建产物 `survey.jar` 拷到服务器直接运行：

```bash
java -jar survey.jar --port 8080
```

建议用 systemd 守护（`Restart=always`），前面按需挂一层 Nginx 反向代理。
Linux 下构建：

```bash
javac --release 21 -encoding UTF-8 -d out $(find src -name "*.java")
jar --create --file survey.jar --main-class com.example.survey.App -C out .
```
## 目录结构

```
survey/
├── src/main/java/com/example/survey/
│   ├── App.java                  入口：手工装配 + 启动 HttpServer
│   ├── DemoData.java             示例问卷与模拟答卷
│   ├── transport/                接入层：Router、Request、HttpResult、StaticFiles
│   ├── api/                      接口层：Questionnaire / Submission / Statistics 三个 Controller
│   ├── service/                  应用服务：校验、状态流转、统计编排
│   ├── statistics/               统计引擎：Grouper、三种 Accumulator、ZeroGuard、报告模型
│   ├── output/                   输出层：JSON / 文本 / CSV 三种 Formatter
│   ├── domain/                   领域模型：Questionnaire、Question、Option、Submission、Answer
│   ├── repository/               仓储接口 + 内存实现
│   └── common/                   Json、ApiException、Ids、Numbers、TimeFormat
├── web/                          三个原生 HTML 页面
├── build.ps1 / build.cmd
└── run.ps1 / run.cmd
```

## HTTP 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/questionnaires` | 问卷列表 |
| POST | `/api/questionnaires` | 创建问卷（DRAFT） |
| GET | `/api/questionnaires/{id}` | 问卷详情（含题目与选项，填写页用） |
| POST | `/api/questionnaires/{id}/publish` | 发布，进入收集中 |
| POST | `/api/questionnaires/{id}/close` | 结束收集 |
| POST | `/api/questionnaires/{id}/responses` | 提交答卷 |
| GET | `/api/questionnaires/{id}/statistics?format=json\|text\|csv` | 统计报告 |

成功响应统一为 `{"ok":true,"data":...}`，失败为 `{"ok":false,"status":400,"error":"..."}`。
统计接口例外：它直接返回报告原文（JSON / text / csv），方便下载和二次处理。

### 创建问卷的请求体

```json
{
  "title": "团队协作满意度调查",
  "questions": [
    {"type": "SCALE",  "title": "整体满意度", "min": 1, "max": 5},
    {"type": "SINGLE", "title": "最常用的时段",
     "options": [{"key": "A", "label": "上午"}, {"key": "B", "label": "下午"}]},
    {"type": "MULTI",  "title": "希望增加的功能",
     "options": [{"key": "A", "label": "图表看板"}, {"key": "B", "label": "导出 PDF"}]},
    {"type": "TEXT",   "title": "其他建议"}
  ]
}
```

### 提交答卷的请求体

```json
{
  "submitter": "小明",
  "answers": [
    {"questionId": 101, "values": ["4"]},
    {"questionId": 102, "values": ["B"]},
    {"questionId": 103, "values": ["A", "D"]},
    {"questionId": 110, "values": ["希望支持导出 Excel"]}
  ]
}
```

## 统计口径

- **分组**：`Grouper.byQuestion` 一次遍历把所有答案按题目 ID 分到 `Map<Long, List<Answer>>`。
- **汇总**：每道题只遍历自己那份答案，分派给对应累加器，平均 / 最值 / 分布一趟算完。
- **打分题**：样本数、合计、平均分、最高分、最低分、分值分布；比例分母是本题答题人数。
- **选择题**：按选项定义顺序展开分布；多选题的比例分母是答题人数，各项之和可以超过 100%。
- **填空题**：有效条数、平均字数、前 20 条样例。
- **除零保护**：`ZeroGuard` 统一兜底。0 份答卷时，数值统计全部返回 0，分布返回空数组，
  文本统计返回 `count = 0`，永远不会出现 `NaN`、`Infinity` 或 500 错误。
- **输出规范**：JSON 键序固定、小数保留 4 位、可空字段直接省略；CSV 带 UTF-8 BOM 方便 Excel；
  文本报告按中英文显示宽度对齐。

## 示例问卷的 10 道题

| # | 题型 | 题目 |
| --- | --- | --- |
| 1 | 打分 1~5 | 我对本系统的整体表现感到满意 |
| 2 | 单选 | 我最常使用系统的时段 |
| 3 | 多选 | 我使用过下面哪些功能 |
| 4 | 单选 | 我愿意把它推荐给同事 |
| 5 | 填空 | 我认为最需要改进的地方是 |
| 6 | 打分 1~10 | 系统易用性评分 |
| 7 | 单选 | 我的岗位角色 |
| 8 | 多选 | 希望优先增加的功能 |
| 9 | 打分 1~5 | 系统响应速度令我满意 |
| 10 | 填空 | 其他建议 |

## 已覆盖的校验

- 昵称必填、长度 ≤ 32、同一问卷内昵称不可重复提交（409）
- 问卷只能是「收集中」才能填写（草稿 / 已结束返回 409）
- 每道题必须作答，单选题恰好一项、多选题至少一项、选项 key 必须存在
- 打分题必须落在 `min ~ max` 区间内，且必须是整数
- 填空题非空、长度 ≤ 500
- 建卷校验：标题非空、选择题至少 2 个选项且 key 唯一、打分题 `max > min`

## 扩展点

- 换存储：实现 `QuestionnaireRepository` / `SubmissionRepository` 即可，业务层零改动。
- 加题型：新增一个 `Accumulator` 实现，在 `AccumulatorFactory` 和 `QuestionType` 各加一个分支。
- 加输出格式：实现 `ReportFormatter`，在 `ReportFormat` 里注册。
- 加分组维度（按部门、按时间段交叉统计）：在 `statistics` 包里再加一个 `Grouper` 策略。
