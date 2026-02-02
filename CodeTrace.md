
# 代码轨迹追踪
## 功能说明
本功能可深度追踪指定 Java 方法内部的代码执行轨迹，精准记录方法内每行代码的执行状态：包括「是否执行」「执行次数」「累计耗时」及「耗时占比」，为方法性能瓶颈定位、代码执行逻辑分析提供精细化数据支撑。

核心特性：
1.  **高精度轨迹记录**：针对每次请求，精准捕获目标方法内代码的执行轨迹，完整统计每行代码的执行次数、耗时时长及耗时百分比；
2.  **极致低性能损耗**：经过多轮性能优化，功能开启后对目标方法的额外资源消耗可控制在 1% 以内，无感知影响业务运行。

## 配置格式
```json
{
  "trace":[
    {
      "rootSpanName":"GET /system/user/authRole/{userId}",
      "javaMethodDesc": "com.ruoyi.web.controller.system.SysUserController#authRole(java.lang.Long,org.springframework.ui.ModelMap)",
      "traceTagKey":"ruoyi.authRole",
      "triggerOptimizeTimes":10
    }
  ]
}
```

### 配置项详细说明
| 配置项               | 必填性 | 说明                                                                 |
|----------------------|--------|----------------------------------------------------------------------|
| `rootSpanName`       | 非必须 | 业务入口 Span 名称。配置后仅对该 Span 对应的业务请求执行轨迹采集；未配置时，对所有业务请求中匹配的方法统一采集。 |
| `traceTagKey`        | 必须   | 轨迹数据存储的 Span Attribute Key：采集的轨迹数据会编码为 16 进制字符串存入该 Key；配套提供解码工具类，运行其 main 方法即可解析出代码轨迹明细。 |
| `javaMethodDesc`     | 必须   | 目标 Java 方法的唯一标识，格式为：`类全限定名#方法名(参数类型1,参数类型2...)`；<br>• 内部类需将最后一个 `.` 替换为 `$`；<br>• 基本数据类型（如 int、long）直接填写类型名；<br>• 多个参数类型用英文逗号 `,` 分隔。 |
| `triggerOptimizeTimes` | 非必须 | 触发采集优化的方法执行次数阈值，默认值为 100；当目标方法的执行次数超过该阈值时，自动触发性能优化逻辑，进一步降低资源消耗。 |

## 功能演示案例
以开源业务系统 [RuoYi](https://gitee.com/y_project/RuoYi) 为演示工程，完整展示代码轨迹追踪的功能效果。

### 演示准备
1.  将上述代码轨迹追踪的配置项写入项目配置文件；
2.  加载本项目探针，启动 RuoYi 应用。

### 原始代码与数据
<img width="2414" height="932" alt="image" src="https://github.com/user-attachments/assets/18d7b1fe-4b32-41ad-bc01-7637af165c2d" />


### 轨迹采集效果
<img width="3434" height="1730" alt="image" src="https://github.com/user-attachments/assets/cb56ecdd-954b-4899-9fd0-ad17a59ab1c9" />


### 代码轨迹解码效果
#### 解码工具
[ETelCodeTraceDecoder.java](https://github.com/EasyTelemetry/easytelemetry-java-opentelemetry/blob/main/ETelCodeTraceDecoder.java)

#### 解码结果展示
<img width="1022" height="436" alt="image" src="https://github.com/user-attachments/assets/e52e7267-706a-40f1-a1ba-a388f01362d4" />


### 注意事项

- **方法多次调用的数据覆盖**：若目标方法在单次请求链路中被多次调用，代码轨迹数据会被重复记录并覆盖，最终仅保留该次请求中方法最后一次执行的轨迹信息；
- **递归方法的采集限制**：针对存在递归调用的方法，当前版本仅能采集最外层方法执行的代码轨迹，递归调用层级内部的执行轨迹暂不支持采集；
- **配置错误的修复建议**：若配置信息填写错误导致采集功能异常，建议直接删除错误配置项后重新新增，而非在原配置上直接修改 —— 因配置监听机制的特性，直接修改可能导致配置更新不生效；
