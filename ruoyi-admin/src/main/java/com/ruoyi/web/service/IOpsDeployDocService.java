package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.OpsDeployDoc;

/**
 * 部署模板文档Service接口
 * 
 * @author ruoyi
 */
public interface IOpsDeployDocService 
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
     * 批量删除部署模板文档
     * 
     * @param docIds 需要删除的部署模板文档主键集合
     * @return 结果
     */
    public int deleteOpsDeployDocByDocIds(Long[] docIds);

    /**
     * 删除部署模板文档信息
     * 
     * @param docId 部署模板文档主键
     * @return 结果
     */
    public int deleteOpsDeployDocByDocId(Long docId);
    
    /**
     * 复制文档到新版本
     * @param templateId 模板ID
     * @param oldVersion 旧版本号
     * @param newVersion 新版本号
     */
    public void copyDocsToNewVersion(Long templateId, String oldVersion, String newVersion);
}
