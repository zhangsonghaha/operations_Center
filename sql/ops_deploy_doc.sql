-- ----------------------------
-- 部署模板文档表
-- ----------------------------
drop table if exists ops_deploy_doc;
create table ops_deploy_doc (
  doc_id           bigint(20)      not null auto_increment    comment '文档ID',
  template_id      bigint(20)      default null               comment '模板ID',
  version          varchar(20)     default null               comment '版本号',
  doc_name         varchar(255)    default ''                 comment '文档名称',
  doc_path         varchar(500)    default ''                 comment '文档路径',
  doc_type         varchar(20)     default ''                 comment '文档类型(pdf,docx,md)',
  file_size        bigint(20)      default 0                  comment '文件大小',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (doc_id)
) engine=innodb auto_increment=100 comment = '部署模板文档表';
