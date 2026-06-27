# 部署说明

## 1. 环境要求

- JDK 8+
- Maven 3.8+
- MySQL 5.7+/8.x
- 可选 SQLite

## 2. 打包

```bash
cd /root/project/aidex-admin
/root/tools/apache-maven-3.9.9/bin/mvn clean package -DskipTests
```

产物默认位置：

```text
target/aidex-admin.jar
```

## 3. 配置

编辑 `src/main/resources/application.yml` 或外部同名配置，重点修改：

- `server.port`
- `aidex.datasource.primary`
- `aidex.datasource.sources.master.*`
- `aidex.datasource.sources.sqlite.*`
- `aidex.generator.output-root`

默认值说明：

- 默认主数据源是 `sqlite`
- 默认 MySQL 数据源 `master` 为关闭状态，按需启用
- 默认 SQLite 库文件路径为 `runtime/sqlite/aidex-admin.db`

自动生成配置：

- `aidex.generator.auto-generate.enabled`
- `aidex.generator.auto-generate.datasource`
- `aidex.generator.auto-generate.tables`
- `aidex.generator.auto-generate.package-name`
- `aidex.generator.auto-generate.module-name`
- `aidex.generator.auto-generate.author`

## 4. 启动

Linux:

```bash
chmod +x bin/*.sh
./bin/start.sh
```

仅验证 SQLite 启动：

```bash
java -jar target/aidex-admin.jar \
  --server.port=8888 \
  --aidex.datasource.primary=sqlite \
  --aidex.datasource.sources.master.enabled=false
```

启动即自动生成示例，推荐直接写入 `application.yml`：

```yaml
aidex:
  generator:
    auto-generate:
      enabled: true
      datasource: sqlite
      tables: user,biz_profile
      package-name: com.aidex.template
      module-name: demo
      author: aidex-admin
```

停止：

```bash
./bin/stop.sh
```

Windows:

```bat
bin\start.bat
bin\stop.bat
```

## 5. 输出目录

生成代码下载为 zip，同时同样会写入 `aidex.generator.output-root` 对应目录。
