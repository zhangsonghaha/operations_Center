-- 更新启动脚本为优化版本
-- 此脚本解决日志捕获不完整的问题

-- 备份当前脚本（可选）
-- CREATE TABLE t_ops_app_backup AS SELECT * FROM t_ops_app WHERE app_name = 'calltask';

-- 更新 calltask 应用的启动脚本
UPDATE t_ops_app 
SET start_script = '#!/bin/bash
# 优化的 Spring Boot 应用启动脚本
# 特点: 分段输出、详细日志、避免 SSH 缓冲问题

APP_NAME="calltask"
APP_JAR="calltask.jar"
APP_HOME="/opt/apps/calltask"
LOG_DIR="${APP_HOME}/logs"
PID_FILE="${APP_HOME}/${APP_NAME}.pid"

# 启用命令跟踪和输出合并
set -x
exec 2>&1

echo "[INFO] ========== 开始启动应用 =========="
echo "[INFO] 应用名称: ${APP_NAME}"
echo "[INFO] 应用路径: ${APP_HOME}"
echo "[INFO] 当前时间: $(date ''+%Y-%m-%d %H:%M:%S'')"

# 创建日志目录
mkdir -p ${LOG_DIR}
echo "[INFO] 日志目录已创建: ${LOG_DIR}"

# 检查 JAR 文件
if [ ! -f "${APP_HOME}/${APP_JAR}" ]; then
    echo "[ERROR] JAR 文件不存在: ${APP_HOME}/${APP_JAR}"
    exit 1
fi
echo "[INFO] JAR 文件: ${APP_JAR} ($(du -h ${APP_HOME}/${APP_JAR} | cut -f1))"

# 停止旧进程
if [ -f ${PID_FILE} ]; then
    OLD_PID=$(cat ${PID_FILE})
    echo "[INFO] 发现旧进程 PID: ${OLD_PID}"
    if ps -p ${OLD_PID} > /dev/null 2>&1; then
        echo "[INFO] 停止旧进程..."
        kill ${OLD_PID}
        sleep 2
        if ps -p ${OLD_PID} > /dev/null 2>&1; then
            echo "[WARN] 强制停止旧进程"
            kill -9 ${OLD_PID}
        fi
        echo "[SUCCESS] 旧进程已停止"
    else
        echo "[INFO] 旧进程已不存在"
    fi
    rm -f ${PID_FILE}
fi

# 启动应用
echo "[INFO] 启动应用..."
cd ${APP_HOME}

nohup java -Xms512m -Xmx1024m \
    -XX:+UseG1GC \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=${LOG_DIR}/heap_dump.hprof \
    -Dspring.profiles.active=prod \
    -jar ${APP_JAR} \
    1>${LOG_DIR}/app.log 2>&1 &

NEW_PID=$!
echo ${NEW_PID} 1>${PID_FILE}
echo "[SUCCESS] 应用已启动，PID: ${NEW_PID}"

# 等待应用启动（分段输出，每秒一次）
echo "[INFO] 等待应用启动（15秒检查期）..."
for i in {1..15}; do
    echo "[INFO] 启动检查进度: ${i}/15 秒"
    sleep 1
    
    # 检查进程是否存活
    if ! ps -p ${NEW_PID} > /dev/null 2>&1; then
        echo "[ERROR] 应用进程已退出"
        echo "[ERROR] 最后 20 行日志:"
        tail -n 20 ${LOG_DIR}/app.log
        exit 1
    fi
    
    # 每 5 秒输出一次日志摘要
    if [ $((i % 5)) -eq 0 ]; then
        echo "[INFO] 应用日志最后 3 行:"
        tail -n 3 ${LOG_DIR}/app.log | sed ''s/^/  /''
    fi
done

# 最终检查
echo "[INFO] 执行最终检查..."
if ps -p ${NEW_PID} > /dev/null 2>&1; then
    echo "[SUCCESS] ========== 应用启动成功 =========="
    echo "[INFO] 进程 PID: ${NEW_PID}"
    echo "[INFO] 日志文件: ${LOG_DIR}/app.log"
    echo "[INFO] 应用日志最后 10 行:"
    tail -n 10 ${LOG_DIR}/app.log | sed ''s/^/  /''
    echo "[INFO] 进程信息:"
    ps -p ${NEW_PID} -o pid,ppid,cmd,%cpu,%mem,etime | sed ''s/^/  /''
else
    echo "[ERROR] 应用启动失败"
    echo "[ERROR] 完整日志:"
    cat ${LOG_DIR}/app.log
    exit 1
fi

echo "[INFO] ========== 启动流程完成 =========="'
WHERE app_name = 'calltask';

-- 验证更新
SELECT app_id, app_name, 
       SUBSTRING(start_script, 1, 100) as script_preview,
       update_time
FROM t_ops_app 
WHERE app_name = 'calltask';

-- 如果需要更新所有 Spring Boot 应用，可以使用:
-- UPDATE t_ops_app 
-- SET start_script = '...'
-- WHERE app_type = 'springboot';
