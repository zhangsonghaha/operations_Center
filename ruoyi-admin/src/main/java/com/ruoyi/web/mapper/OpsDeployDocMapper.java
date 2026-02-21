package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.OpsDeployDoc;

/**
 * 部署模板文档Mapper接口
 * 
 * @author ruoyi
 */
public interface OpsDeployDocMapper 
{
    /**
     * 查询部署模板文档
     * 
     * @param docId 部署模板文档主键
     * @return 部署模板文档
     */
    public OpsDeployDoc selectOpsDeployDocByDocId(Long docId);

    /**
     * 查询部署模板文档列表
     * 
     * @param opsDeployDoc 部署模板文档
     * @return 部署模板文档集合
     */
    public List<OpsDeployDoc> selectOpsDeployDocList(OpsDeployDoc opsDeployDoc);

    /**
     * 新增部署模板文档
     * 
     * @param opsDeployDoc 部署模板文档
     * @return 结果
     */
    public int insertOpsDeployDoc(OpsDeployDoc opsDeployDoc);

    /**
     * 修改部署模板文档
     * 
     * @param opsDeployDoc 部署模板文档
     * @return 结果
     */
    public int updateOpsDeployDoc(OpsDeployDoc opsDeployDoc);

    /**
     * 删除部署模板文档
     * 
     * @param docId 部署模板文档主键
     * @return 结果
     */
    public int deleteOpsDeployDocByDocId(Long docId);

    /**
     * 批量删除部署模板文档
     * 
     * @param docIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOpsDeployDocByDocIds(Long[] docIds);
}
