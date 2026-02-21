package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.OpsDeployRecord;

/**
 * 部署记录Mapper接口
 * 
 * @author ruoyi
 */
public interface OpsDeployRecordMapper 
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

    /**
     * 新增部署记录
     * 
     * @param opsDeployRecord 部署记录
     * @return 结果
     */
    public int insertOpsDeployRecord(OpsDeployRecord opsDeployRecord);

    /**
     * 修改部署记录
     * 
     * @param opsDeployRecord 部署记录
     * @return 结果
     */
    public int updateOpsDeployRecord(OpsDeployRecord opsDeployRecord);

    /**
     * 删除部署记录
     * 
     * @param id 部署记录主键
     * @return 结果
     */
    public int deleteOpsDeployRecordById(Long id);

    /**
     * 批量删除部署记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOpsDeployRecordByIds(Long[] ids);
}
