package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.OpsDeployRecord;

/**
 * 部署记录 Service接口
 * 
 * @author ruoyi
 */
public interface IOpsDeployRecordService 
{
    /**
     * 查询部署记录
     * 
     * @param id 部署记录主键
     * @return 部署记录
     */
    public OpsDeployRecord selectOpsDeployRecordById(Long id);

    /**
     * 查询部署记录列表
     * 
     * @param opsDeployRecord 部署记录
     * @return 部署记录集合
     */
    public List<OpsDeployRecord> selectOpsDeployRecordList(OpsDeployRecord opsDeployRecord);
}
