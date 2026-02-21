package com.ruoyi.web.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.OpsDeployDocMapper;
import com.ruoyi.web.domain.OpsDeployDoc;
import com.ruoyi.web.service.IOpsDeployDocService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部署模板文档Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class OpsDeployDocServiceImpl implements IOpsDeployDocService 
{
    @Autowired
    private OpsDeployDocMapper opsDeployDocMapper;

    /**
     * 查询部署模板文档
     * 
     * @param docId 部署模板文档主键
     * @return 部署模板文档
     */
    @Override
    public OpsDeployDoc selectOpsDeployDocByDocId(Long docId)
    {
        return opsDeployDocMapper.selectOpsDeployDocByDocId(docId);
    }

    /**
     * 查询部署模板文档列表
     * 
     * @param opsDeployDoc 部署模板文档
     * @return 部署模板文档
     */
    @Override
    public List<OpsDeployDoc> selectOpsDeployDocList(OpsDeployDoc opsDeployDoc)
    {
        return opsDeployDocMapper.selectOpsDeployDocList(opsDeployDoc);
    }

    /**
     * 新增部署模板文档
     * 
     * @param opsDeployDoc 部署模板文档
     * @return 结果
     */
    @Override
    public int insertOpsDeployDoc(OpsDeployDoc opsDeployDoc)
    {
        opsDeployDoc.setCreateTime(DateUtils.getNowDate());
        opsDeployDoc.setCreateBy(SecurityUtils.getUsername());
        return opsDeployDocMapper.insertOpsDeployDoc(opsDeployDoc);
    }

    /**
     * 修改部署模板文档
     * 
     * @param opsDeployDoc 部署模板文档
     * @return 结果
     */
    @Override
    public int updateOpsDeployDoc(OpsDeployDoc opsDeployDoc)
    {
        opsDeployDoc.setUpdateTime(DateUtils.getNowDate());
        opsDeployDoc.setUpdateBy(SecurityUtils.getUsername());
        return opsDeployDocMapper.updateOpsDeployDoc(opsDeployDoc);
    }

    /**
     * 批量删除部署模板文档
     * 
     * @param docIds 需要删除的部署模板文档主键
     * @return 结果
     */
    @Override
    public int deleteOpsDeployDocByDocIds(Long[] docIds)
    {
        return opsDeployDocMapper.deleteOpsDeployDocByDocIds(docIds);
    }

    /**
     * 删除部署模板文档信息
     * 
     * @param docId 部署模板文档主键
     * @return 结果
     */
    @Override
    public int deleteOpsDeployDocByDocId(Long docId)
    {
        return opsDeployDocMapper.deleteOpsDeployDocByDocId(docId);
    }
    
    /**
     * 复制文档到新版本
     */
    @Override
    @Transactional
    public void copyDocsToNewVersion(Long templateId, String oldVersion, String newVersion) {
        OpsDeployDoc query = new OpsDeployDoc();
        query.setTemplateId(templateId);
        query.setVersion(oldVersion);
        List<OpsDeployDoc> docList = opsDeployDocMapper.selectOpsDeployDocList(query);
        
        for (OpsDeployDoc oldDoc : docList) {
            OpsDeployDoc newDoc = new OpsDeployDoc();
            newDoc.setTemplateId(templateId);
            newDoc.setVersion(newVersion);
            newDoc.setDocName(oldDoc.getDocName());
            newDoc.setDocPath(oldDoc.getDocPath()); // Point to same file
            newDoc.setDocType(oldDoc.getDocType());
            newDoc.setFileSize(oldDoc.getFileSize());
            newDoc.setRemark(oldDoc.getRemark());
            newDoc.setCreateTime(DateUtils.getNowDate());
            newDoc.setCreateBy(SecurityUtils.getUsername());
            opsDeployDocMapper.insertOpsDeployDoc(newDoc);
        }
    }
}
