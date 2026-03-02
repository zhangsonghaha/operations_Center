-- 为应用表添加配置文件路径字段
-- 执行时间：2026-03-01
-- 功能：支持指定外部配置文件，启动时使用 --spring.config.location 参数

-- 添加配置文件路径字段
ALTER TABLE `t_ops_app` 
ADD COLUMN `config_file_path` varchar(500) DEFAULT NULL COMMENT '外部配置文件路径（如：/home/config/application.yml，支持多个用逗号分隔）' 
AFTER `stop_script`;

-- 查看更新结果
SELECT app_id, app_name, config_file_path FROM `t_ops_app`;

-- 示例：更新某个应用的配置文件路径
-- UPDATE `t_ops_app` SET `config_file_path` = '/home/config/application.yml' WHERE app_id = 100;
