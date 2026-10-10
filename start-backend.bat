@echo off
rem ============================================================
rem  Start the Spring Boot backend on port 8080
rem  - pins JAVA_HOME to JDK 17 (required by Spring Boot 3)
rem  - uses the full Maven path (so you don't need mvn on PATH)
rem  - stop with Ctrl+C
rem ============================================================
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"
rem 本地 MySQL 密码：请通过环境变量注入，如 set "DB_PASSWORD=你的密码"，勿提交明文
cd /d "%~dp0backend"
"C:\Users\32571\tools\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
