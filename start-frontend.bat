@echo off
rem ============================================================
rem  Start the Vue frontend dev server on port 8081
rem  (proxies /api to the backend on 8080 — start backend first)
rem  - stop with Ctrl+C
rem ============================================================
cd /d "%~dp0"
call npm run serve
