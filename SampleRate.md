
# 自定义采样率功能
## 服务全局采样
可通过修改配置文件中的 `sample_rate` 字段，动态调整全局服务采样率。该字段取值范围为 `0~1`（包含 0 和 1），最多保留 2 位小数。

```json
{
	"sample_rate": 0.1
}
```
采样规则说明：
1.  若当前请求来自已接入本探针的上游服务，将优先沿用上游服务的采样配置结果；
2.  若当前服务是链路中的首个服务，则直接使用配置文件中 `sample_rate` 配置的采样率；
3.  探针会实时监听配置文件的修改，配置变更后，采样率生效延迟一般不超过 3 秒。

## 业务入口采样
可通过修改配置文件中的 `service_sample_rate` 字段，为不同业务入口配置差异化采样率，具体配置格式如下：

```json
{
 "service_sample_rate": {
  "etel_topic process": 0.1,
  "com.etel.agent.grpc.HelloService/hello": 0.1,
  "GET /etel/get": 0.3
 }
}
```
### 配置说明
1.  字段结构：`service_sample_rate` 为一个键值对对象，其中 **key 对应业务入口的 Span 名称**，value 对应该入口的采样率（取值范围 `0~1`，最多保留 2 位小数）；
2.  支持的中间件：目前已兼容 HTTP Web 服务器（Tomcat、Undertow、Servlet）、gRPC Server、Dubbo Server、消息队列消费者（RocketMQ、Kafka、RabbitMQ）；
3.  注意事项：针对 HTTP 请求，暂不支持包含路径变量（Path Variable）的 URL 格式，也不支持带查询参数的 `/{path}/?` 形式 URL。

### 采样命中优先级（从高到低）
1.  上游服务已接入本探针：按照 W3C 规范判断上游请求的采样结果，当前服务直接沿用该结果，不触发本地采样判断；
2.  上游服务未接入探针 / 当前服务是链路首个服务：
    -  优先检查是否为当前业务入口的 Span 名称配置了 `service_sample_rate` 自定义采样率，若有配置，使用该采样率进行判断；
    -  若无业务入口自定义采样配置，沿用 `sample_rate` 配置的服务全局采样率；
    -  若既无业务入口自定义采样，也无全局采样配置，默认采用「全量采集」策略。
