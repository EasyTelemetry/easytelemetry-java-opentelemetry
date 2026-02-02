
## 功能说明
本功能支持通过配置文件灵活指定需要采集的 HTTP 请求/响应上下文数据，采集完成后会自动将数据写入业务入口 Span 的 Tag 中持久化；若配置的待采集数据不存在，将直接忽略该条采集规则，不影响其他配置和服务运行。

核心特性：
1.  多类型配置可共存：支持同时配置采集 Header、Body、Param 等不同来源的数据；
2.  同类型配置可批量配置：针对同一数据来源（如 Request Body），可同时配置多条规则采集多个属性数据。

## 配置格式
```json
{
  "request_data_extract": [
    {
      "sourceType": 3,
      "expression": "$.names[1]",
      "rootSpanName": "POST /httpclient/postUser",
      "tagKey": "user.name[1]"
    }
  ]
}
```

### 配置项详细说明
| 配置项       | 必填性 | 说明                                                                 |
|--------------|--------|----------------------------------------------------------------------|
| `sourceType` | 必须   | 数据来源类型，取值为整数（目前支持 1~9）。其中 1~6 对应 HTTP 相关数据采集，7~9 对应 Java 方法数据采集。 |
| `expression` | 非必须 | 数据采集表达式：<br>1.  从 Request Body/Response Body 采集时，采用 JSON Path 格式，以 `$.` 开头，后续可拼接属性 Key、Map Key 或 List/Array 索引；<br>2.  从 Header/Param 采集时，直接填写对应 Key 即可；<br>3.  采集 Request Url 时，无需配置该参数。 |
| `rootSpanName` | 非必须 | 业务入口 Span 名称。配置后，仅对该名称对应的业务请求执行采集逻辑；未配置时，对所有业务请求统一执行采集。 |
| `tagKey`     | 必须   | 采集数据写入业务入口 Span 时对应的 Attribute Key，用于后续查询和筛选该条采集数据。 |

## 支持的 HTTP 数据采集来源
### 1. Request Header（HTTP 请求头）
采集 HTTP 请求头中的指定字段，写入业务入口 Span。
```json
{
  "request_data_extract": [
    {
      "sourceType": 1,
      "expression": "requestId",
      "rootSpanName": "POST /httpclient/postUser",
      "tagKey": "request.id"
    }
  ]
}
```

### 2. Response Header（HTTP 响应头）
采集 HTTP 响应头中的指定字段，写入业务入口 Span。
```json
{
  "request_data_extract": [
    {
      "sourceType": 2,
      "expression": "responseId",
      "rootSpanName": "POST /httpclient/postUser",
      "tagKey": "response.id"
    }
  ]
}
```

### 3. Request Param（HTTP 请求参数）
采集 HTTP 请求参数中的指定字段，写入业务入口 Span。
```json
{
  "request_data_extract": [
    {
      "sourceType": 3,
      "expression": "userId",
      "rootSpanName": "POST /httpclient/postUser",
      "tagKey": "user.id"
    }
  ]
}
```

### 4. Request Url（HTTP 请求地址）
采集完整的 HTTP 请求地址，写入业务入口 Span（无需配置 `expression`）。
```json
{
  "request_data_extract": [
    {
      "sourceType": 4,
      "rootSpanName": "POST /httpclient/postUser",
      "tagKey": "request.url"
    }
  ]
}
```

### 5. Request Body（HTTP 请求体）
从 JSON 格式的 HTTP 请求体中，通过 JSON Path 采集指定属性值，写入业务入口 Span。
```json
{
  "request_data_extract": [
    {
      "sourceType": 5,
      "expression": "$.roles[0].roleId",
      "rootSpanName": "POST /system/user/add",
      "tagKey": "roleId"
    }
  ]
}
```

### 6. Response Body（HTTP 响应体）
从 JSON 格式的 HTTP 响应体中，通过 JSON Path 采集指定属性值，写入业务入口 Span。
```json
{
  "request_data_extract": [
    {
      "sourceType": 6,
      "expression": "$.rows[0].loginName",
      "rootSpanName": "POST /system/user/list",
      "tagKey": "user.loginName"
    }
  ]
}
```

## 功能演示案例
以开源业务系统 [RuoYi](https://gitee.com/y_project/RuoYi) 作为演示工程，演示从 HTTP 响应体（Response Body）中采集指定数据的完整效果。

### 演示准备
1.  采用上述「Response Body」对应的配置项，写入项目配置文件；
2.  加载本项目探针，启动 RuoYi 应用。

### 原始代码与数据
<img width="1538" height="1294" alt="image" src="https://github.com/user-attachments/assets/3d8e4dc3-58f1-422f-9515-04e0307e0053" />


### 最终采集效果
<img width="3440" height="1248" alt="image" src="https://github.com/user-attachments/assets/f93d9cf3-2e2a-4344-af60-5fcc67ddc250" />


从演示结果可见，本功能已成功从 HTTP 响应体中采集到 `loginName` 字段，并写入业务入口 Span 的对应 Tag 中，采集效果符合预期。

---
