
# easytelemetry-java-opentelemetry
## 项目介绍
本项目基于 **[opentelemetry-java-instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation)**（tag: [v2.23.0](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/v2.23.0)）进行二次开发，在**不改动原项目核心逻辑、完全兼容开源版本**的前提下，扩展了大量实用自定义功能。后续项目将保持与 OpenTelemetry 官方版本的定期同步更新，同时持续丰富自定义能力，兼顾兼容性与功能性。

## 使用方式
项目采用**配置监听模式**控制自定义功能的启停，目前支持文件配置方式，通过实时监听配置文件的修改，实现配置内容的动态更新与功能同步，无需重启应用。

### 配置文件相关说明
1.  **配置目录指定**：可通过启动参数指定配置文件存放目录：
    > `-Detel.service.config_dir=xxx`
    若未主动指定，默认使用 **xxx-javaagent.jar** 所在目录作为配置目录。
2.  **配置文件命名**：配置文件名称以服务名称为前缀，后缀为 `.json`，服务名称可通过以下启动参数指定：
    > `-Dotel.service.name=xxx`
    对应配置文件名为 `xxx.json`。
3.  **自动初始化与动态更新**：项目探针启动时，会自动在配置目录中创建空白的 `xxx.json` 配置文件，并开启文件修改监听。当配置文件内容修改并保存后，系统将自动感知并加载最新配置，实时同步功能变更，无需重启应用生效。

## 自定义功能列表
-  **动态调整服务采样率**：支持通过配置文件动态修改全局服务采样率，无需重启应用即可生效。
-  **个性化接口采样率**：支持为服务内不同接口配置差异化采样率，采样优先级遵循「上游采样优先，无上游采样时使用接口自定义采样率」。
-  **HTTP 请求全量数据采集**：完整采集 HTTP 请求链路中的核心数据，包括 Request Header、Response Header、Request Param、Request Url、Request Body 以及 Response Body。
-  **Java 方法精细化数据采集**：支持采集指定 Java 方法的入参、返回值及局部变量信息，满足方法级别的可观测性需求。
-  **代码轨迹追踪**：支持采集指定 Java 方法内部的代码执行轨迹，功能类似 Arthas 的 `trace` 命令，可统计每行代码的执行次数、执行耗时及时间占比，助力性能瓶颈定位。
