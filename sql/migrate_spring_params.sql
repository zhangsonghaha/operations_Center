-- 迁移启动脚本中的 Spring Boot 参数
-- 将 -Dspring.profiles.active 和 -Dserver.port 从启动脚本移到 spring_params 字段

-- 备份当前数据
SELECT 
    app_id,
    app_name,
    start_script,
    spring_params,
    '备份时间' as note,
    NOW() as backup_time
FROM t_ops_app 
WHERE start_script LIKE '%spring.profiles.active%' 
   OR start_script LIKE '%server.port%';

-- 更新启动脚本：移除 -Dspring.profiles.active=prod
UPDATE t_ops_app 
SET start_script = REPLACE(start_script, '    -Dspring.profiles.active=prod \\', '')
WHERE start_script LIKE '%Dspring.profiles.active=prod%';

-- 更新启动脚本：移除 -Dserver.port=8012
UPDATE t_ops_app 
SET start_script = REPLACE(start_script, '    -Dserver.port=8012 \\', '')
WHERE start_script LIKE '%Dserver.port=8012%';

-- 更新启动脚本：移除可能的其他端口配置
UPDATE t_ops_app 
SET start_script = REPLACE(start_script, '    -Dserver.port=${PORT} \\', '')
WHERE start_script LIKE '%Dserver.port=${PORT}%';

-- 设置 spring_params 字段（如果当前为空）
UPDATE t_ops_app 
SET spring_params = '--spring.profiles.active=prod --server.port=8012'
WHERE (spring_params IS NULL OR spring_params = '')
  AND app_name = 'calltask';

-- 验证修改结果
SELECT 
    app_id,
    app_name,
    CASE 
        WHEN start_script LIKE '%spring.profiles.active%' THEN '❌ 仍包含 profiles'
        WHEN start_script LIKE '%server.port%' THEN '❌ 仍包含 port'
        ELSE '✅ 已清理'
    END as script_status,
    spring_params,
    CASE 
        WHEN spring_params IS NOT NULL AND spring_params != '' THEN '✅ 已配置'
        ELSE '⚠️ 未配置'
    END as params_status
FROM t_ops_app 
WHERE app_name = 'calltask';
