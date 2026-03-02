-- 为应用表添加配置文件路径字段
-- 执行时间：2026-03-01

-- 添加配置文件路径字段
ALTER TABLE `t_ops_app` 
ADD COLUMN `config_file_path` varchar(500) COMMENT '外部配置文件路径（如：/home/config/application.yml）' AFTER `stop_script`;

-- 查看更新结果
SELECT app_id, app_name, config_file_path FROM `t_ops_app`;
