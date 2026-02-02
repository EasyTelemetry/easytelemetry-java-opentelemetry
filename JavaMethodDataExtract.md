

# Java Method Data Extract（Java 方法数据采集功能）
## 功能说明
本功能支持通过配置文件灵活采集指定 Java 方法的入参、返回值及局部变量信息，并将采集结果写入业务入口 Span 的 Attribute 中，便于业务链路的精细化可观测分析。

核心特性：
1.  **多类型配置兼容**：方法参数、返回值、局部变量的采集规则可共存，互不干扰；
2.  **批量配置支持**：支持同时采集单个方法的多个参数属性、返回值多维度属性，以及多个局部变量的信息；
3.  **低性能损耗**：采集逻辑经过轻量化设计，对服务运行性能的影响可忽略不计。

## 配置格式
```json
{
  "request_data_extract": [
    {
      "sourceType": 9,
      "expression": "$.roles[0].roleName",
      "javaMethodDesc": "com.ruoyi.system.service.impl.SysUserServiceImpl#selectUserList(com.ruoyi.common.core.domain.entity.SysUser)",
      "rootSpanName": "POST /system/user/list",
      "tagKey": "ruoyi.roleName",
      "lineNumber": 76
    }
  ]
}
```

### 配置项详细说明
| 配置项           | 必填性       | 说明                                                                 |
|------------------|--------------|----------------------------------------------------------------------|
| `sourceType`     | 必须         | 数据来源类型，取值为整数（目前支持 1~9）：<br>• 1~6：HTTP 相关数据采集；<br>• 7~9：Java 方法数据采集（7=参数、8=返回值、9=局部变量）。 |
| `expression`     | 必须         | 数据采集表达式，统一采用 JSON Path 格式：<br>• 以 `$.` 开头；<br>• 支持拼接属性 Key、Map Key 或 List/Array 索引，用于定位目标数据。 |
| `rootSpanName`   | 非必须       | 业务入口 Span 名称。配置后仅对该 Span 对应的业务请求执行采集；未配置时，对所有业务请求中匹配的方法统一采集。 |
| `tagKey`         | 必须         | 采集数据写入业务入口 Span 时的 Attribute Key，用于后续检索和筛选该条采集数据。 |
| `javaMethodDesc` | 必须         | 目标 Java 方法的唯一标识，格式为：`类全限定名#方法名(参数类型1,参数类型2...)`；<br>• 内部类需将最后一个 `.` 替换为 `$`；<br>• 基本数据类型（如 int、long）直接填写类型名；<br>• 多个参数类型用英文逗号 `,` 分隔。 |
| `lineNumber`     | 采集局部变量时必须 | 目标局部变量的定义行号（仅支持采集定义行的局部变量）；下个版本将支持根据变量名，在任意行采集指定局部变量的属性值。 |

## 支持的 Java 方法数据采集类型
### 1. 采集方法入参
采集指定 Java 方法中某个入参的指定属性，写入业务入口 Span。
```json
{
  "request_data_extract": [
    {
      "sourceType": 7,
      "expression": "$.[0].loginName",
      "javaMethodDesc": "com.ruoyi.system.service.impl.SysUserServiceImpl#selectUserList(com.ruoyi.common.core.domain.entity.SysUser)",
      "rootSpanName": "POST /system/user/list",
      "tagKey": "ruoyi.loginName"
    }
  ]
}
```
**配置说明**：
方法入参以数组形式存储，序号从 0 开始，需通过 `[索引]` 指定具体入参；示例中 `$.[0]` 表示采集第一个入参对象的 `loginName` 属性，若目标属性不存在则自动忽略。

### 2. 采集方法返回值
采集指定 Java 方法返回值的指定属性，写入业务入口 Span。
```json
{
  "request_data_extract": [
    {
      "sourceType": 8,
      "expression": "$.[0].userName",
      "javaMethodDesc": "com.ruoyi.system.service.impl.SysUserServiceImpl#selectUserList(com.ruoyi.common.core.domain.entity.SysUser)",
      "rootSpanName": "POST /system/user/list",
      "tagKey": "ruoyi.userName"
    }
  ]
}
```
**配置说明**：
示例中方法返回值为 `List<SysUser>` 类型，`$.[0]` 表示采集列表第一个元素的 `userName` 属性；若返回值为空或目标属性不存在，将自动忽略该采集规则。

### 3. 采集方法局部变量
采集指定 Java 方法内部局部变量的数值或其属性值，写入业务入口 Span。
```json
{
  "request_data_extract": [
    {
      "sourceType": 9,
      "expression": "$.loginName",
      "javaMethodDesc": "com.ruoyi.web.controller.system.SysUserController#authRole(java.lang.Long,org.springframework.ui.ModelMap)",
      "tagKey": "ruoyi.userName",
      "lineNumber": 252
    },
    {
      "sourceType": 9,
      "expression": "$.[0].roleName",
      "javaMethodDesc": "com.ruoyi.web.controller.system.SysUserController#authRole(java.lang.Long,org.springframework.ui.ModelMap)",
      "tagKey": "ruoyi.roleName",
      "lineNumber": 254
    }
  ]
}
```
**配置说明**：
需通过 `lineNumber` 指定局部变量的定义行号，仅能采集该行定义的变量；表达式直接定位变量的目标属性，若变量为集合类型，可通过索引（如 `[0]`）定位元素。
一个局部变量目前只支持一个采集配置。

## 功能演示案例
以开源业务系统 [RuoYi](https://gitee.com/y_project/RuoYi) 为演示工程，完整展示采集 Java 方法入参、返回值、局部变量的效果。

### 1. 采集方法入参
#### 演示准备
1.  将「采集方法入参」对应的配置项写入项目配置文件；
2.  加载本项目探针，启动 RuoYi 应用。

##### 原始代码与数据
<img width="2080" height="1412" alt="image" src="https://github.com/user-attachments/assets/9e579c0f-bfca-4f38-97ad-d038aca84d7a" />


##### 采集效果
<img width="3428" height="1726" alt="image" src="https://github.com/user-attachments/assets/5fcad930-2ded-4459-b4b8-00c94cca38ea" />


### 2. 采集方法返回值
#### 演示准备
1.  将「采集方法返回值」对应的配置项写入项目配置文件；
2.  加载本项目探针，启动 RuoYi 应用。

##### 原始代码与数据
<img width="2430" height="1836" alt="image" src="https://github.com/user-attachments/assets/9cf0dd10-b25e-40ff-ba5e-5342fc51f7c7" />


##### 采集效果
<img width="3446" height="1710" alt="image" src="https://github.com/user-attachments/assets/79a49525-b764-4d35-82af-aa175a53f272" />


### 3. 采集方法局部变量
#### 演示准备
1.  将「采集方法局部变量」对应的配置项写入项目配置文件；
2.  加载本项目探针，启动 RuoYi 应用。

##### 原始代码与数据
<img width="2432" height="1860" alt="image" src="https://github.com/user-attachments/assets/d6561238-9047-458d-abe2-a958be7fe074" />


##### 字节码编辑效果
<img width="2294" height="780" alt="image" src="https://github.com/user-attachments/assets/5b0b02a1-e6fd-4cd2-896b-cf86fb2bd5c9" />


##### 采集效果
<img width="3450" height="1672" alt="a96a8fa7d7a049d4b2eded43c00a1cd1" src="https://github.com/user-attachments/assets/60a5d8c9-2d06-4011-84f2-f91275bb27b3" />

