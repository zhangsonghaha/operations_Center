package com.ruoyi.web.controller.system;

import java.util.List;
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
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.SysBatchTask;
import com.ruoyi.web.domain.SysBatchTaskDetail;
import com.ruoyi.web.domain.SysCommandTemplate;
import com.ruoyi.web.service.ISysBatchService;

/**
 * 批量操作Controller
 */
@RestController
@RequestMapping("/ops/batch")
public class SysBatchController extends BaseController
{
    @Autowired
    private ISysBatchService sysBatchService;

    /**
     * 查询批量命令模板列表
     */
    @PreAuthorize("@ss.hasPermi('ops:batch:template')")
    @GetMapping("/template/list")
    public TableDataInfo templateList(SysCommandTemplate sysCommandTemplate)
    {
        startPage();
        List<SysCommandTemplate> list = sysBatchService.selectSysCommandTemplateList(sysCommandTemplate);
        return getDataTable(list);
    }

    /**
     * 新增批量命令模板
     */
    @PreAuthorize("@ss.hasPermi('ops:batch:template')")
    @Log(title = "批量命令模板", businessType = BusinessType.INSERT)
    @PostMapping("/template")
    public AjaxResult addTemplate(@RequestBody SysCommandTemplate sysCommandTemplate)
    {
        sysCommandTemplate.setCreateBy(getUsername());
        return toAjax(sysBatchService.insertSysCommandTemplate(sysCommandTemplate));
    }

    /**
     * 修改批量命令模板
     */
    @PreAuthorize("@ss.hasPermi('ops:batch:template')")
    @Log(title = "批量命令模板", businessType = BusinessType.UPDATE)
    @PutMapping("/template")
    public AjaxResult editTemplate(@RequestBody SysCommandTemplate sysCommandTemplate)
    {
        sysCommandTemplate.setUpdateBy(getUsername());
        return toAjax(sysBatchService.updateSysCommandTemplate(sysCommandTemplate));
    }

    /**
     * 删除批量命令模板
     */
    @PreAuthorize("@ss.hasPermi('ops:batch:template')")
    @Log(title = "批量命令模板", businessType = BusinessType.DELETE)
	@DeleteMapping("/template/{templateIds}")
    public AjaxResult removeTemplate(@PathVariable Long[] templateIds)
    {
        return toAjax(sysBatchService.deleteSysCommandTemplateByIds(templateIds));
    }

    /**
     * 查询批量任务列表
     */
    @PreAuthorize("@ss.hasPermi('ops:batch:list')")
    @GetMapping("/task/list")
    public TableDataInfo taskList(SysBatchTask sysBatchTask)
    {
        startPage();
        List<SysBatchTask> list = sysBatchService.selectSysBatchTaskList(sysBatchTask);
        return getDataTable(list);
    }

    /**
     * 查询批量任务详细
     */
    @PreAuthorize("@ss.hasPermi('ops:batch:list')")
    @GetMapping("/task/{taskId}")
    public AjaxResult getTask(@PathVariable("taskId") Long taskId)
    {
        return AjaxResult.success(sysBatchService.selectSysBatchTaskById(taskId));
    }

    /**
     * 查询任务明细列表
     */
    @PreAuthorize("@ss.hasPermi('ops:batch:list')")
    @GetMapping("/task/detail/list")
    public TableDataInfo taskDetailList(SysBatchTaskDetail sysBatchTaskDetail)
    {
        startPage();
        List<SysBatchTaskDetail> list = sysBatchService.selectSysBatchTaskDetailList(sysBatchTaskDetail);
        return getDataTable(list);
    }

    /**
     * 创建并执行批量任务
     */
    @PreAuthorize("@ss.hasPermi('ops:batch:execute')")
    @Log(title = "批量任务", businessType = BusinessType.INSERT)
    @PostMapping("/execute")
    public AjaxResult execute(@RequestBody SysBatchTaskRequest request)
    {
        SysBatchTask task = new SysBatchTask();
        task.setTaskName(request.getTaskName());
        task.setTaskType(request.getTaskType());
        task.setCommandContent(request.getCommandContent());
        task.setSourceFile(request.getSourceFile());
        task.setDestPath(request.getDestPath());
        task.setCreateBy(getUsername());
        
        // 插入任务
        sysBatchService.insertSysBatchTask(task);
        
        // 异步执行
        sysBatchService.executeBatchTask(task.getTaskId(), request.getServerIds());
        
        return AjaxResult.success("任务已提交", task.getTaskId());
    }

    /**
     * 请求体内部类
     */
    public static class SysBatchTaskRequest {
        private String taskName;
        private String taskType;
        private String commandContent;
        private String sourceFile;
        private String destPath;
        private Long[] serverIds;

        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        
        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }
        
        public String getCommandContent() { return commandContent; }
        public void setCommandContent(String commandContent) { this.commandContent = commandContent; }
        
        public String getSourceFile() { return sourceFile; }
        public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }
        
        public String getDestPath() { return destPath; }
        public void setDestPath(String destPath) { this.destPath = destPath; }
        
        public Long[] getServerIds() { return serverIds; }
        public void setServerIds(Long[] serverIds) { this.serverIds = serverIds; }
    }
}
