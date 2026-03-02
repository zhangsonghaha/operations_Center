package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.OpsDeployRecordMapper;
import com.ruoyi.web.domain.OpsDeployRecord;
import com.ruoyi.web.service.IOpsDeployRecordService;

/**
 * 部署记录 Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class OpsDeployRecordServiceImpl implements IOpsDeployRecordService
{
    @Autowired
    private OpsDeployRecordMapper opsDeployRecordMapper;

    /**
     * 查询部署记录
     * 
     * @param id 部署记录主键
     * @return 部署记录
     */
    @Override
    public OpsDeployRecord selectOpsDeployRecordById(Long id)
    {
        return opsDeployRecordMapper.selectOpsDeployRecordById(id);
    }

    /**
     * 查询部署记录列表
     * 
     * @param opsDeployRecord 部署记录
     * @return 部署记录
     */
    @Override
    public List<OpsDeployRecord> selectOpsDeployRecordList(OpsDeployRecord opsDeployRecord)
    {
        return opsDeployRecordMapper.selectOpsDeployRecordList(opsDeployRecord);
    }
}
