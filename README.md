
# easytelemetry-java-opentelemetry
## 项目介绍
本项目基于 **[opentelemetry-java-instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation)**（tag: [v2.23.0](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/v2.23.0)）进行二次开发，在**不改动原项目核心逻辑、完全兼容开源版本**的前提下，扩展了大量高实用性的自定义功能。后续项目将保持与 OpenTelemetry 官方版本的定期同步更新，同时持续丰富自定义能力，兼顾生态兼容性与业务功能性。

## 使用方式
项目采用**配置监听模式**控制自定义功能的启停与参数调整，目前支持文件配置方式，通过实时监听配置文件的修改，实现配置内容的动态更新与功能同步，全程无需重启应用。

### 配置文件相关说明
1.  **配置目录指定**：可通过如下启动参数指定配置文件存放目录：
    > `-Detel.service.config_dir=xxx`
    若未主动指定，默认将 **xxx-javaagent.jar** 所在目录作为配置目录。
2.  **配置文件命名**：配置文件以服务名称为前缀、`.json` 为后缀命名，其中服务名称通过以下启动参数指定：
    > `-Dotel.service.name=xxx`
    对应配置文件名为 `xxx.json`。
3.  **自动初始化与动态更新**：项目探针启动时，会自动在配置目录中创建空白的 `xxx.json` 配置文件，并开启文件修改监听机制。当配置文件内容修改并保存后，系统将自动感知并加载最新配置，实时同步功能变更，无需重启应用即可生效。
4.  **配置加载延迟**：默认在服务启动后 300 秒开始监听并加载配置，可通过启动参数 `-Detel.delay=60` 调整延迟时长。**建议将该时间配置为服务完全启动后的时段**，否则部分依赖服务运行环境的配置（如 Java 方法数据采集等），会因目标 Java 方法未完成加载而无法正常生效。

## 自定义功能列表
> 注：以下所有功能均支持动态配置生效，无需重启服务。
-  **[动态调整服务采样率](https://github.com/EasyTelemetry/easytelemetry-java-opentelemetry/blob/main/sampleRate.md)**：支持通过配置文件动态修改全局服务采样率，配置更新后即时生效。
-  **[个性化接口采样率](https://github.com/EasyTelemetry/easytelemetry-java-opentelemetry/blob/main/sampleRate.md)**：支持为服务内不同接口配置差异化采样率，采样优先级遵循「上游采样优先，无上游采样时启用接口自定义采样率」的规则。
-  **HTTP 请求全量数据采集**：完整采集 HTTP 请求链路中的核心数据，涵盖 Request Header、Response Header、Request Param、Request Url、Request Body 及 Response Body，满足请求链路的全维度可观测需求。
-  **Java 方法精细化数据采集**：支持采集指定 Java 方法的入参、返回值及局部变量信息，实现方法级别的精细化可观测，助力问题定位与方法运行状态监控。
-  **代码轨迹追踪**：支持采集指定 Java 方法内部的代码执行轨迹，功能类似 Arthas 的 `trace` 命令，可精准统计经过了哪些行代码，每行代码的执行次数、执行耗时及时间占比，高效助力性能瓶颈定位与代码优化。
