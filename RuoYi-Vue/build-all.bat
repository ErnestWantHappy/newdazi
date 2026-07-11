echo === 开始构建后端 ===
cd /d "%~dp0"
call mvn -pl ruoyi-admin -am -DskipTests clean package
if %ERRORLEVEL% NEQ 0 (
    echo 后端构建失败！
    pause
    exit /b 1
)

echo === 开始构建前端 ===
cd /d "%~dp0..\RuoYi-Vue3"
call npm run build:prod
if %ERRORLEVEL% NEQ 0 (
    echo 前端构建失败！
    pause
    exit /b 1
)

echo === 构建完成 ===
echo 后端 JAR: RuoYi-Vue\ruoyi-admin\target\ruoyi-admin.jar
echo 前端 DIST: RuoYi-Vue3\dist3\
pause
