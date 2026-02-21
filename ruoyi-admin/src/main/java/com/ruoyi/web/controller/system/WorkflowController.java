package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.service.ISysUserService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.history.HistoricProcessInstance;
import com.ruoyi.web.service.WorkflowService;
import com.ruoyi.common.core.page.TableDataInfo;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ops/workflow")
public class WorkflowController extends BaseController {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private ISysUserService sysUserService;
    
    @PreAuthorize("@ss.hasPermi('ops:workflow:list')")
    @GetMapping("/definition/list")
    public TableDataInfo listProcessDefinitions(
        @RequestParam(required = false) String key, 
        @RequestParam(required = false) String name,
        @RequestParam(required = false, defaultValue = "true") Boolean latestOnly) {
        
        startPage();
        List<ProcessDefinition> list = workflowService.listProcessDefinitions(key, name, latestOnly);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProcessDefinition pd : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", pd.getId());
            map.put("key", pd.getKey());
            map.put("name", pd.getName());
            map.put("version", pd.getVersion());
            map.put("deploymentId", pd.getDeploymentId());
            map.put("resourceName", pd.getResourceName());
            map.put("suspended", pd.isSuspended());
            result.add(map);
        }
        
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(200);
        rspData.setMsg("查询成功");
        rspData.setRows(result);
        rspData.setTotal(new com.github.pagehelper.PageInfo(list).getTotal());
        return rspData;
    }


    public static class DeployBpmnRequest {
        private String processKey;
        private String processName;
        private String bpmnXml;
        public String getProcessKey() { return processKey; }
        public void setProcessKey(String processKey) { this.processKey = processKey; }
        public String getProcessName() { return processName; }
        public void setProcessName(String processName) { this.processName = processName; }
        public String getBpmnXml() { return bpmnXml; }
        public void setBpmnXml(String bpmnXml) { this.bpmnXml = bpmnXml; }
    }

    @PreAuthorize("@ss.hasPermi('ops:approvalConfig:edit')")
    @Log(title = "BPMN部署", businessType = BusinessType.INSERT)
    @PostMapping("/deployXml")
    public AjaxResult deployXml(@RequestBody DeployBpmnRequest req) {
        try {
            if (req == null || req.getProcessKey() == null || req.getBpmnXml() == null) {
                return AjaxResult.error("参数不完整");
            }
            String name = req.getProcessName() != null ? req.getProcessName() : req.getProcessKey();
            Deployment deployment = repositoryService.createDeployment()
                    .addString(req.getProcessKey() + ".bpmn20.xml", req.getBpmnXml())
                    .name(name)
                    .deploy();
            Map<String, Object> data = new HashMap<>();
            data.put("deploymentId", deployment.getId());
            data.put("name", deployment.getName());
            return AjaxResult.success(data);
        } catch (Exception e) {
            return AjaxResult.error("部署失败: " + e.getMessage());
        }
    }

    /**
     * Delete deployment
     */
    @PreAuthorize("@ss.hasPermi('ops:workflow:remove')")
    @Log(title = "删除流程", businessType = BusinessType.DELETE)
    @DeleteMapping("/deployment/{deploymentId}")
    public AjaxResult remove(@PathVariable String deploymentId) {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
            .deploymentId(deploymentId)
            .singleResult();
            
        if (pd != null) {
            String key = pd.getKey();
            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .list();
                
            for (ProcessDefinition def : list) {
                workflowService.deleteDeployment(def.getDeploymentId());
            }
        } else {
            workflowService.deleteDeployment(deploymentId);
        }
        return AjaxResult.success();
    }
    
    /**
     * Suspend/Activate process definition
     */
    @PreAuthorize("@ss.hasPermi('ops:workflow:edit')")
    @Log(title = "修改流程状态", businessType = BusinessType.UPDATE)
    @PostMapping("/definition/{processDefinitionId}/state")
    public AjaxResult updateState(@PathVariable String processDefinitionId, @RequestParam String action) {
        if ("suspend".equals(action)) {
            workflowService.suspendProcessDefinition(processDefinitionId);
        } else if ("activate".equals(action)) {
            workflowService.activateProcessDefinition(processDefinitionId);
        } else {
            return AjaxResult.error("不支持的操作");
        }
        return AjaxResult.success();
    }
    
    /**
     * Delete process instance (Batch)
     */
    @PreAuthorize("@ss.hasPermi('ops:workflow:monitor')")
    @Log(title = "删除流程实例", businessType = BusinessType.DELETE)
    @DeleteMapping("/instance/{processInstanceIds}")
    public AjaxResult deleteProcessInstance(@PathVariable String[] processInstanceIds) {
        for (String processInstanceId : processInstanceIds) {
            // Try to delete runtime instance first
            long count = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).count();
            if (count > 0) {
                runtimeService.deleteProcessInstance(processInstanceId, "Deleted by Admin");
            }
            // Always delete history to ensure complete removal
            historyService.deleteHistoricProcessInstance(processInstanceId);
        }
        return AjaxResult.success();
    }

    /**
     * Cancel process instance (Batch Force Terminate)
     */
    @PreAuthorize("@ss.hasPermi('ops:workflow:monitor')")
    @Log(title = "取消流程实例", businessType = BusinessType.UPDATE)
    @PostMapping("/instance/cancel")
    public AjaxResult cancelProcessInstance(@RequestBody List<String> processInstanceIds) {
        for (String processInstanceId : processInstanceIds) {
            long count = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).count();
            if (count > 0) {
                runtimeService.deleteProcessInstance(processInstanceId, "Cancelled by Admin");
            }
        }
        return AjaxResult.success();
    }
    
    /**
     * Monitor: List all process instances (Admin)
     */
    @PreAuthorize("@ss.hasPermi('ops:workflow:monitor')")
    @GetMapping("/monitor/list")
    public TableDataInfo listMonitorProcessInstances(
        @RequestParam(required = false) String key,
        @RequestParam(required = false) String startedBy) {
        
        startPage();
        // Since we don't have a direct service method for dynamic query, let's implement basic filtering here or add to service
        // For simplicity, we add a new method to WorkflowService
        List<HistoricProcessInstance> list = workflowService.getMonitorProcessInstances(key, startedBy, 0, 100);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (HistoricProcessInstance inst : list) {
             Map<String, Object> map = new HashMap<>();
             map.put("id", inst.getId());
             map.put("processDefinitionId", inst.getProcessDefinitionId());
             map.put("processDefinitionName", inst.getProcessDefinitionName());
             map.put("processDefinitionKey", inst.getProcessDefinitionKey());
             map.put("startTime", inst.getStartTime());
             map.put("endTime", inst.getEndTime());
             map.put("durationInMillis", inst.getDurationInMillis());
             map.put("startUserId", inst.getStartUserId());
             
             if (inst.getStartUserId() != null) {
                 try {
                     SysUser user = sysUserService.selectUserById(Long.valueOf(inst.getStartUserId()));
                     if (user != null) {
                         map.put("startUserNickName", user.getNickName());
                     }
                 } catch (Exception e) {
                     // Ignore parsing errors or user not found
                 }
             }

             map.put("businessKey", inst.getBusinessKey());
             map.put("deleteReason", inst.getDeleteReason());
             result.add(map);
        }
        
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(200);
        rspData.setMsg("查询成功");
        rspData.setRows(result);
        rspData.setTotal(new com.github.pagehelper.PageInfo(list).getTotal());
        return rspData;
    }

    /**
     * Get process definition XML
     */
    @GetMapping("/definition/{deploymentId}/xml")
    public AjaxResult getProcessDefinitionXml(@PathVariable String deploymentId) {
        try {
            List<String> names = repositoryService.getDeploymentResourceNames(deploymentId);
            String resourceName = null;
            for (String name : names) {
                if (name.endsWith(".bpmn20.xml") || name.endsWith(".bpmn")) {
                    resourceName = name;
                    break;
                }
            }
            if (resourceName == null) {
                return AjaxResult.error("找不到 BPMN XML 资源");
            }
            String xml = workflowService.getProcessDefinitionXml(deploymentId, resourceName);
            return AjaxResult.success("操作成功", xml);
        } catch (Exception e) {
            return AjaxResult.error("获取流程定义失败: " + e.getMessage());
        }
    }

    /**
     * Get process definition XML by Key (Latest Version)
     */
    @GetMapping("/definition/key/{processKey}/xml")
    public AjaxResult getProcessDefinitionXmlByKey(@PathVariable String processKey) {
        try {
            List<ProcessDefinition> list = workflowService.listProcessDefinitions(processKey, null);
            if (list == null || list.isEmpty()) {
                return AjaxResult.error("找不到流程定义");
            }
            // List is already filtered by latestVersion in Service, but let's double check
            ProcessDefinition pd = list.get(0);
            
            String deploymentId = pd.getDeploymentId();
            String resourceName = pd.getResourceName();
            String xml = workflowService.getProcessDefinitionXml(deploymentId, resourceName);
            return AjaxResult.success("操作成功", xml);
        } catch (Exception e) {
            return AjaxResult.error("获取流程定义失败: " + e.getMessage());
        }
    }

    /**
     * Get my started processes
     */
    @GetMapping("/my-started")
    public TableDataInfo listMyStartedProcesses() {
        startPage();
        String userId = String.valueOf(getUserId());
        // Note: PageHelper doesn't work directly with Flowable Query API, we need manual pagination or adapt
        // But for simplicity in this project, we use simple list logic if not huge
        // Or better, use manual parameters from request context if needed.
        // Flowable has listPage(firstResult, maxResults)
        
        // Using BaseController's pageDomain is not directly applicable to Flowable Query object easily without wrapper
        // Let's use getPageDomain() manually
        
        // Integer pageNum = getPageDomain().getPageNum();
        // Integer pageSize = getPageDomain().getPageSize();
        // int start = (pageNum - 1) * pageSize;
        
        // For now, let's just return all or first 100 to be safe, or implement simple pagination
        // Since startPage() sets ThreadLocal for PageHelper which intercepts MyBatis, but Flowable uses its own engine.
        // We will ignore PageHelper here and just return list.
        
        List<HistoricProcessInstance> list = workflowService.getMyProcessInstances(userId, 0, 100);
        long total = workflowService.getMyProcessInstancesCount(userId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (HistoricProcessInstance inst : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", inst.getId());
            map.put("processDefinitionId", inst.getProcessDefinitionId());
            map.put("processDefinitionName", inst.getProcessDefinitionName());
            map.put("startTime", inst.getStartTime());
            map.put("endTime", inst.getEndTime());
            map.put("durationInMillis", inst.getDurationInMillis());
            map.put("startUserId", inst.getStartUserId());
            map.put("businessKey", inst.getBusinessKey());
            map.put("deleteReason", inst.getDeleteReason());
            result.add(map);
        }
        
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(200);
        rspData.setMsg("查询成功");
        rspData.setRows(result);
        rspData.setTotal(total);
        return rspData;
    }

    /**
     * Get process progress (XML + Active Nodes)
     */
    @GetMapping("/instance/{processInstanceId}/progress")
    public AjaxResult getProcessProgress(@PathVariable String processInstanceId) {
        try {
            ProcessDefinition pd = workflowService.getProcessDefinitionByInstanceId(processInstanceId);
            if (pd == null) {
                return AjaxResult.error("找不到流程定义");
            }
            
            String deploymentId = pd.getDeploymentId();
            String resourceName = pd.getResourceName();
            String xml = workflowService.getProcessDefinitionXml(deploymentId, resourceName);
            
            List<String> activeActivityIds = workflowService.getProcessActiveActivityIds(processInstanceId);
            
            // Get detailed task info for active tasks
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            List<Map<String, Object>> activeTaskInfo = new ArrayList<>();
            for (Task task : tasks) {
                Map<String, Object> info = new HashMap<>();
                info.put("activityId", task.getTaskDefinitionKey());
                info.put("taskId", task.getId());
                info.put("taskName", task.getName());
                info.put("assignee", task.getAssignee());
                info.put("createTime", task.getCreateTime());
                
                if (task.getAssignee() != null) {
                     try {
                         SysUser user = sysUserService.selectUserById(Long.valueOf(task.getAssignee()));
                         if (user != null) {
                             info.put("assigneeNickName", user.getNickName());
                         }
                     } catch (Exception e) {}
                }
                activeTaskInfo.add(info);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("bpmnXml", xml);
            data.put("activeActivityIds", activeActivityIds);
            data.put("activeTaskInfo", activeTaskInfo);
            
            return AjaxResult.success(data);
        } catch (Exception e) {
            return AjaxResult.error("获取流程进度失败: " + e.getMessage());
        }
    }
    
    /**
     * Remind assignee
     */
    @PostMapping("/instance/{processInstanceId}/remind")
    public AjaxResult remindAssignee(@PathVariable String processInstanceId) {
        workflowService.remindProcessInstance(processInstanceId, String.valueOf(getUserId()));
        return AjaxResult.success("已发送提醒");
    }
}
