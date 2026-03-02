-- 修复启动脚本 - 增加真正的进程存活验证
-- 解决"显示成功但进程不存在"的问题

UPDATE t_ops_app 
SET start_script = '#!/bin/bash
# 修复版启动脚本 - 增加真正的进程存活验证

APP_NAME="calltask"
APP_JAR="calltask-1.3.1_20260301103333A001.jar"
APP_HOME="/home"
LOG_DIR="${APP_HOME}/logs"
PID_FILE="${APP_HOME}/${APP_NAME}-1.3.1_20260301103333A001.pid"

# 启用详细输出
set -x
exec 2>&1

echo "[INFO] ========== 开始启动应用 =========="
echo "[INFO] 应用名称: ${APP_NAME}"
echo "[INFO] JAR 文件: ${APP_JAR}"
echo "[INFO] 应用路径: ${APP_HOME}"
echo "[INFO] 当前时间: $(date ''+%Y-%m-%d %H:%M:%S'')"

# 创建日志目录
mkdir -p ${LOG_DIR}
echo "[INFO] 日志目录: ${LOG_DIR}"

# 切换到应用目录
cd ${APP_HOME}
echo "[INFO] 当前目录: $(pwd)"

# 检查 JAR 文件是否存在
if [ ! -f "${APP_JAR}" ]; then
    echo "[ERROR] JAR 文件不存在: ${APP_HOME}/${APP_JAR}"
    echo "[ERROR] 目录内容:"
    ls -lh ${APP_HOME}
    exit 1
fi
echo "[INFO] JAR 文件存在: ${APP_JAR}"

# 检查端口是否被占用
PORT=8012
if netstat -tuln | grep -q ":${PORT} "; then
    echo "[WARN] 端口 ${PORT} 已被占用"
    echo "[INFO] 占用端口的进程:"
    netstat -tulnp | grep ":${PORT} "
fi

# 停止旧进程
if [ -f ${PID_FILE} ]; then
    OLD_PID=$(cat ${PID_FILE})
    echo "[INFO] 发现旧进程 PID: ${OLD_PID}"
    if ps -p ${OLD_PID} > /dev/null 2>&1; then
        echo "[INFO] 停止旧进程..."
        kill ${OLD_PID}
        sleep 3
        if ps -p ${OLD_PID} > /dev/null 2>&1; then
            echo "[WARN] 强制停止旧进程"
            kill -9 ${OLD_PID}
            sleep 1
        fi
        echo "[SUCCESS] 旧进程已停止"
    else
        echo "[INFO] 旧进程已不存在"
    fi
    rm -f ${PID_FILE}
fi

# 启动应用
echo "[INFO] 启动应用..."
nohup java -Xms512m -Xmx1024m \
    -XX:+UseG1GC \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=${LOG_DIR}/heap_dump.hprof \
    -Dspring.profiles.active=prod \
    -Dserver.port=${PORT} \
    -jar ${APP_JAR} \
    1>${LOG_DIR}/app.log 2>&1 &

NEW_PID=$!
echo ${NEW_PID} 1>${PID_FILE}
echo "[INFO] 应用已后台启动，PID: ${NEW_PID}"

# 等待 2 秒，让应用初始化
sleep 2

# 第一次检查：进程是否还存活
if ! ps -p ${NEW_PID} > /dev/null 2>&1; then
    echo "[ERROR] 应用进程已退出（启动失败）"
    echo "[ERROR] 最后 50 行日志:"
    tail -n 50 ${LOG_DIR}/app.log
    rm -f ${PID_FILE}
    exit 1
fi
echo "[INFO] 初步检查通过，进程仍在运行"

# 持续检查 15 秒，确保应用稳定运行
echo "[INFO] 开始 15 秒稳定性检查..."
for i in {1..15}; do
    echo "[INFO] 检查进度: ${i}/15 秒"
    
    # 检查进程是否存活
    if ! ps -p ${NEW_PID} > /dev/null 2>&1; then
        echo "[ERROR] 应用进程在第 ${i} 秒时退出"
        echo "[ERROR] 最后 50 行日志:"
        tail -n 50 ${LOG_DIR}/app.log
        rm -f ${PID_FILE}
        exit 1
    fi
    
    # 每 5 秒输出一次日志摘要
    if [ $((i % 5)) -eq 0 ]; then
        echo "[INFO] 应用日志最后 5 行:"
        tail -n 5 ${LOG_DIR}/app.log | sed ''s/^/  /''
    fi
    
    sleep 1
done

# 最终验证
echo "[INFO] ========== 执行最终验证 =========="

# 1. 检查进程是否存在
if ! ps -p ${NEW_PID} > /dev/null 2>&1; then
    echo "[ERROR] 最终检查失败：进程不存在"
    echo "[ERROR] 完整日志:"
    cat ${LOG_DIR}/app.log
    rm -f ${PID_FILE}
    exit 1
fi
echo "[SUCCESS] 进程存活检查通过"

# 2. 检查端口是否监听
sleep 2
if netstat -tuln | grep -q ":${PORT} "; then
    echo "[SUCCESS] 端口 ${PORT} 监听检查通过"
else
    echo "[WARN] 端口 ${PORT} 未监听，应用可能还在启动中"
fi

# 3. 输出进程信息
echo "[INFO] 进程详细信息:"
ps -p ${NEW_PID} -o pid,ppid,cmd,%cpu,%mem,etime | sed ''s/^/  /''

# 4. 输出最后的日志
echo "[INFO] 应用日志最后 20 行:"
tail -n 20 ${LOG_DIR}/app.log | sed ''s/^/  /''

echo "[SUCCESS] ========== 应用启动成功 =========="
echo "[INFO] PID: ${NEW_PID}"
echo "[INFO] 端口: ${PORT}"
echo "[INFO] 日志文件: ${LOG_DIR}/app.log"
echo "[INFO] PID 文件: ${PID_FILE}"'
WHERE app_name = 'calltask';

-- 验证更新
SELECT app_id, app_name, 
       SUBSTRING(start_script, 1, 100) as script_preview,
       update_time
FROM t_ops_app 
WHERE app_name = 'calltask';
