package com.ruoyi.web.controller.system;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.OpsDeployDoc;
import com.ruoyi.web.service.IOpsDeployDocService;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.file.FileUtils;

/**
 * 部署模板文档Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/ops/deployDoc")
public class OpsDeployDocController extends BaseController
{
    @Autowired
    private IOpsDeployDocService opsDeployDocService;

    /**
     * 查询部署模板文档列表
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:list')")
    @GetMapping("/list")
    public TableDataInfo list(OpsDeployDoc opsDeployDoc)
    {
        startPage();
        List<OpsDeployDoc> list = opsDeployDocService.selectOpsDeployDocList(opsDeployDoc);
        return getDataTable(list);
    }

    /**
     * 获取部署模板文档详细信息
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:query')")
    @GetMapping(value = "/{docId}")
    public AjaxResult getInfo(@PathVariable("docId") Long docId)
    {
        return AjaxResult.success(opsDeployDocService.selectOpsDeployDocByDocId(docId));
    }

    /**
     * 新增部署模板文档
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:add')")
    @Log(title = "部署模板文档", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OpsDeployDoc opsDeployDoc)
    {
        return toAjax(opsDeployDocService.insertOpsDeployDoc(opsDeployDoc));
    }

    /**
     * 修改部署模板文档
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:edit')")
    @Log(title = "部署模板文档", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OpsDeployDoc opsDeployDoc)
    {
        return toAjax(opsDeployDocService.updateOpsDeployDoc(opsDeployDoc));
    }

    /**
     * 删除部署模板文档
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:remove')")
    @Log(title = "部署模板文档", businessType = BusinessType.DELETE)
	@DeleteMapping("/{docIds}")
    public AjaxResult remove(@PathVariable Long[] docIds)
    {
        return toAjax(opsDeployDocService.deleteOpsDeployDocByDocIds(docIds));
    }
    
    /**
     * 上传文档
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:add')")
    @Log(title = "部署模板文档上传", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(MultipartFile file, Long templateId, String version) throws Exception
    {
        if (file == null) {
             return AjaxResult.error("上传文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = FileUploadUtils.getExtension(file);
        
        // Check extension
        if (!"pdf".equalsIgnoreCase(extension) && 
            !"doc".equalsIgnoreCase(extension) && 
            !"docx".equalsIgnoreCase(extension) && 
            !"md".equalsIgnoreCase(extension)) {
            return AjaxResult.error("只支持 PDF, Word (doc/docx), Markdown (md) 格式");
        }
        
        // Upload file
        String filePath = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);
        
        // Save metadata
        OpsDeployDoc doc = new OpsDeployDoc();
        doc.setTemplateId(templateId);
        doc.setVersion(version);
        doc.setDocName(originalFilename);
        doc.setDocPath(filePath);
        doc.setDocType(extension);
        doc.setFileSize(file.getSize());
        
        opsDeployDocService.insertOpsDeployDoc(doc);
        
        return AjaxResult.success(doc);
    }
}
