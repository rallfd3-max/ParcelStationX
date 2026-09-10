# MySQL 验证指南

## 1. 安装或启动 MySQL

安装 MySQL 8 Community Server。Windows 已安装服务时，以管理员 PowerShell 执行：

```powershell
Start-Service MySQL
Get-NetTCPConnection -LocalPort 3306 -State Listen
```

## 2. 创建并初始化数据库

使用 MySQL Workbench 打开并依次执行：

1. `src/main/resources/db/schema.sql`
2. `src/main/resources/db/seed.sql`

两个脚本均选择整个文件执行，schema 会创建并切换到 `parcel_station_x`。

## 3. 配置应用

复制 `src/main/resources/application.example.properties` 为同目录下的 `application.properties`：

```properties
db.url=jdbc:mysql://localhost:3306/parcel_station_x?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
db.username=root
db.password=本机密码
```

该文件已被 `.gitignore` 排除。也可设置 `PARCEL_DB_URL`、`PARCEL_DB_USERNAME`、`PARCEL_DB_PASSWORD`。

## 4. 验证 JDBC 和测试

```powershell
mvn clean test
mvn -Pintegration-test verify
```

集成测试 profile 只应在本机数据库凭据已配置后运行。

## 5. 启动 UI

在 IDE 运行 `com.parcelstationx.app.ParcelStationApplication`。先以 seed 中的管理员账号登录，再执行客户、入库、查询和出库演示。

## 本次自动审计结果

系统存在 `MySQL` Windows 服务，但当前进程没有启动服务的权限；服务保持停止，3306 未监听，且没有可用数据库凭据。因此未声称完成真实 MySQL 验证。
