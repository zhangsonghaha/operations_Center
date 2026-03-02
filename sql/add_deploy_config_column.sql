-- 为 t_ops_app 表添加 deploy_config 字段
-- 用于存储可视化部署配置的 JSON 数据

ALTER TABLE t_ops_app ADD COLUMN deploy_config TEXT COMMENT '部署配置JSON' AFTER retry_count;
