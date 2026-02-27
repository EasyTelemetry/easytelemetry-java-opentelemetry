# Online Debug
## 功能说明
本功能可实现线上生产环境类远程debug能力，精准定位应用内部代码问题，且规避传统debug的请求阻塞、应用安全、环境隔离等隐患。

本功能基于EasyTelemetry多个功能组合并优化实现：
1. 方法参数和返回值采集
2. 方法局部变量采集
3. 请求经过方法的代码轨迹记录
4. 为指定方法动态生成Span，将上述3类数据写入当前Span

上述能力合力模拟还原Debug效果，且代码经过性能优化，资源消耗控制在指定方法的10%以下。

## 配置格式
```json
{
  "debug": [
    {
      "rootSpanName": "POST /system/dept/edit",
      "javaMethodDesc": "com.ruoyi.system.service.impl.SysDeptServiceImpl#updateDept(com.ruoyi.common.core.domain.entity.SysDept)",
      "trace": true,
      "traceTagKey": "dept.updateDept",
      "tags": [
        {
          "sourceType": 9,
          "tagKey": "dept.ancestors",
          "variable": "dept",
          "lineNumber": 223,
          "expression": "$.ancestors"
        },
        {
          "sourceType": 7,
          "tagKey": "param[0].deptId",
          "expression": "$.[0].deptId"
        },
        {
          "sourceType": 8,
          "tagKey": "updateDept.ret",
          "expression": "$."
        },
        {
          "sourceType": 0,
          "tagKey": "etel.service",
          "tagValue": "ruoyi"
        }
      ]
    }
  ]
}
```

### 配置项详细说明
| 配置项         | 必填性                  | 说明                                                                 |
|----------------|-------------------------|----------------------------------------------------------------------|
| `rootSpanName` | 非必须                  | 业务入口 Span 名称。配置后仅对该 Span 对应的业务请求执行轨迹采集；未配置时，对所有业务请求中匹配的方法统一采集。 |
| `javaMethodDesc` | 必须                   | 目标 Java 方法的唯一标识，格式为：`类全限定名#方法名(参数类型1,参数类型2...)`；<br>• 内部类需将最后一个 `.` 替换为 `$`；<br>• 基本数据类型（如 int、long）直接填写类型名；<br>• 多个参数类型用英文逗号 `,` 分隔。 |
| `trace`        | 非必须                  | 是否记录代码轨迹                                                     |
| `traceTagKey`  | 当`trace=true`时必须    | 轨迹数据存储的 Span Attribute Key：采集的轨迹数据会编码为 16 进制字符串存入该 Key；配套提供解码工具类，运行其 main 方法即可解析出代码轨迹明细。 |
| `tags`         | 非必须                  | 自定义生成span的tag列表                                              |
| `sourceType`   | 必须                    | 自定义tag的数据来源类型：<br>`0`-简单键值对；<br>`7`-从参数采集；<br>`8`-从方法返回值采集；<br>`9`-从局部变量采集 |
| `tagKey`       | 必须                    | 存储在Span中tag的attribute键                                        |
| `tagValue`     | 当`sourceType==0`时必须 | 简单键值对类型tag的值，仅支持数字、字符串、boolean类型                |
| `expression`   | 当`sourceType!=0`时必须 | JSON表达式，指定从Java对象中提取指定数据的规则                       |
| `variable`     | 当`sourceType==9`时必须 | 局部变量名称                                                         |
| `lineNumber`   | 当`sourceType==9`时必须 | 行号，在指定行开始时采集该局部变量的数据                             |

## 功能演示案例
以开源业务系统 [RuoYi](https://gitee.com/y_project/RuoYi) 为例，展示代码轨迹追踪的功能效果。

### 演示准备
1. 将上述代码轨迹追踪配置项写入项目配置文件；
2. 加载本项目探针，启动 RuoYi 应用。

### 原始代码与数据
<img width="1994" height="1512" alt="原始代码与数据" src="https://github.com/user-attachments/assets/45f88d27-f5d7-413b-923d-d898fc18dd98" />

### 字节码增强效果
<img width="2032" height="1872" alt="字节码增强效果" src="https://github.com/user-attachments/assets/6ac6bc5f-12f0-42f2-84e0-21e5f50723da" />

**字节码修改说明：**
- 方法开始时创建Span，并插入提取参数的字节码；
- 每一行代码开始时插入记录代码轨迹的字节码；
- 方法结束时插入提取返回值的字节码，并结束Span；
- 通过try-catch保证Span正常结束。

### Debug效果展示
<img width="3454" height="1820" alt="Debug效果展示" src="https://github.com/user-attachments/assets/f7dd491d-5d55-4ecc-91cd-02b8f7ff4862" />

**Span及Tag说明：**
- Span默认名称：`Package.Class/Method`；
- `dept.ancestors`：第223行从局部变量`dept`中，按表达式`$.ancestors`采集到的值；
- `dept.updateDept`：trace功能存储代码轨迹的tag；
- `etel.service`：简单键值对类型的Tag配置；
- `param[0].deptId`：从参数中，按表达式`$.[0].deptId`采集第一个参数的`deptId`属性值。

### 代码轨迹Decode结果
<img width="1216" height="684" alt="代码轨迹Decode结果" src="https://github.com/user-attachments/assets/7e9b6592-d359-40e0-b69a-02a70f8d7152" />
