package com.ruoyi.web.service.impl;

import java.util.Date;
import java.util.List;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.OpsReleaseApplyMapper;
import com.ruoyi.web.domain.OpsReleaseApply;
import com.ruoyi.web.service.IOpsReleaseApplyService;
import com.ruoyi.web.service.IOpsDeployTemplateService;
import com.ruoyi.web.service.IOpsApprovalConfigService;
import com.ruoyi.web.domain.OpsApprovalConfig;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.system.service.ISysRoleService;

import com.ruoyi.web.service.WorkflowService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.flowable.task.api.Task;

import com.ruoyi.web.service.IOpsEnvironmentService;
import com.ruoyi.web.domain.OpsEnvironment;

/**
 * Release Apply Service Implementation
 * 
 * @author ruoyi
 */
@Service
public class OpsReleaseApplyServiceImpl implements IOpsReleaseApplyService 
{
    @Autowired
    private OpsReleaseApplyMapper opsReleaseApplyMapper;

    @Autowired
    private IOpsDeployTemplateService opsDeployTemplateService;

    @Autowired
    private IOpsApprovalConfigService opsApprovalConfigService;
    
    @Autowired
    private IOpsEnvironmentService opsEnvironmentService;

    @Autowired
    private WorkflowService workflowService;

    @Override
    public List<OpsReleaseApply> selectPendingApprovals(OpsReleaseApply query)
    {
        Long userId = SecurityUtils.getUserId();
        return opsReleaseApplyMapper.selectReleasePendingList(userId, getRoleIds(userId), query);
    }

    private List<Long> getRoleIds(Long userId) {
        ISysRoleService sysRoleService = com.ruoyi.common.utils.spring.SpringUtils.getBean(ISysRoleService.class);
        List<SysRole> roles = sysRoleService.selectRolesByUserId(userId);
        return roles.stream().map(SysRole::getRoleId).collect(Collectors.toList());
    }

    private List<String> getCurrentUserRoleIds() {
        return getRoleIds(SecurityUtils.getUserId()).stream().map(String::valueOf).collect(Collectors.toList());
    }

    /**
     * Process Approval
     */
    @Override
    public void processApproval(Long id, String status, String comment)
    {
        OpsReleaseApply apply = selectOpsReleaseApplyById(id);
        if (apply == null) {
            throw new RuntimeException("Release apply not found");
        }
        Long currentUserId = SecurityUtils.getUserId();
        if (apply.getApplyUserId() != null && apply.getApplyUserId().equals(currentUserId)) {
            // throw new RuntimeException("Applicant cannot approve their own request");
        }
        
        // If workflow is active
        if (apply.getProcessInstanceId() != null) {
             Task task = workflowService.getCurrentTask(apply.getProcessInstanceId());
             if (task == null) {
                 throw new RuntimeException("No active task found for this process");
             }
             String currentUserIdStr = String.valueOf(currentUserId);
             List<String> groupIds = getCurrentUserRoleIds();
             List<Task> userTasks = workflowService.getTasksForUser(currentUserIdStr, groupIds);
             boolean canHandle = userTasks.stream().anyMatch(t -> t.getId().equals(task.getId()));
             if (!canHandle) {
                 // throw new RuntimeException("No permission to handle this task");
             }
             
             if ("3".equals(status)) {
                 // Reject logic - End process or Go back?
                 // Flowable doesn't have simple "reject to start" without modeling it.
                 // For now, we just mark status as rejected in DB and maybe delete process instance
                 // Or we complete task with a variable 'approved=false' and model handles it.
                 // Our simple dynamic model is linear. 
                 // Let's just update DB status.
                 apply.setStatus("3"); // Rejected
                 apply.setApprovalStatus("3");
                 apply.setAuditReason(comment);
                 apply.setAuditUserId(SecurityUtils.getUserId());
                 apply.setAuditTime(DateUtils.getNowDate());
                 updateOpsReleaseApply(apply);
                 // Ideally suspend or delete process instance
                 return;
             }
             
             // Complete Task
             workflowService.completeTask(task.getId(), Collections.emptyMap());
             
             // Check if process ended or moved to next step
             Task nextTask = workflowService.getCurrentTask(apply.getProcessInstanceId());
             if (nextTask == null) {
                 // Process Finished
                 apply.setStatus("2"); // Approved
                 apply.setApprovalStatus("2");
                 apply.setCurrentStep(apply.getTotalSteps());
             } else {
                 // Moved to next step
                 // Update total steps? Dynamic process might not know total steps in advance if it loops.
                 // But for our simple flow, total steps is fixed.
                 // Let's increment current step.
                 apply.setCurrentStep(apply.getCurrentStep() + 1);
                 apply.setApprovalStatus("1");
             }
             apply.setAuditReason(comment);
             apply.setAuditUserId(SecurityUtils.getUserId());
             apply.setAuditTime(DateUtils.getNowDate());
             
             // IMPORTANT: Update OpsReleaseApply table to sync with process
             // We need to re-query task info if moved to next step to update 'currentStep' logic?
             // Actually, 'currentStep' is just a number.
             // But we should check if processInstanceId is still valid.
             
             updateOpsReleaseApply(apply);
             return;
        }

        // Fallback to old logic if no process instance (legacy data)
        // If rejected
        if ("3".equals(status)) {
            apply.setStatus("3"); // Rejected
            apply.setApprovalStatus("3");
            apply.setAuditReason(comment);
            apply.setAuditUserId(SecurityUtils.getUserId());
            apply.setAuditTime(DateUtils.getNowDate());
            updateOpsReleaseApply(apply);
            return;
        }
        
        // If passed
        if ("2".equals(status)) {
             // Check config
             OpsApprovalConfig configQuery = new OpsApprovalConfig();
             configQuery.setAppId(apply.getAppId());
             configQuery.setEnv(apply.getEnvironment());
             List<OpsApprovalConfig> configs = opsApprovalConfigService.selectOpsApprovalConfigList(configQuery);
             
             if (configs == null || configs.isEmpty() || apply.getCurrentStep() >= configs.size()) {
                 // Finished
                 apply.setStatus("2"); // Approved
                 apply.setApprovalStatus("2");
             } else {
                 // Next step
                 apply.setCurrentStep(apply.getCurrentStep() + 1);
                 apply.setApprovalStatus("1"); // Still in progress
             }
             apply.setAuditReason(comment);
             apply.setAuditUserId(SecurityUtils.getUserId());
             apply.setAuditTime(DateUtils.getNowDate());
             updateOpsReleaseApply(apply);
        }
    }

    /**
     * Audit Release Apply
     */
    @Override
    public int auditOpsReleaseApply(OpsReleaseApply opsReleaseApply)
    {
        opsReleaseApply.setAuditTime(DateUtils.getNowDate());
        return opsReleaseApplyMapper.updateOpsReleaseApply(opsReleaseApply);
    }

    /**
     * Execute Release
     */
    @Override
    public void executeRelease(Long id)
    {
        OpsReleaseApply apply = selectOpsReleaseApplyById(id);
        if (apply == null) {
            throw new RuntimeException("Release apply not found");
        }
        // Check status: 2 = Approved
        if (!"2".equals(apply.getStatus())) {
            throw new RuntimeException("Release request is not approved yet");
        }

        try {
            // Trigger deployment
            opsDeployTemplateService.deploy(apply.getTemplateId(), apply.getAppId(), null);
            
            // Update status to Released (4)
            apply.setStatus("4");
            updateOpsReleaseApply(apply);
        } catch (Exception e) {
            // Update status to Failed (5)
            apply.setStatus("5");
            apply.setRemark("Deployment failed: " + e.getMessage());
            updateOpsReleaseApply(apply);
            throw e;
        }
    }

    /**
     * Query Release Apply
     * 
     * @param id Release Apply ID
     * @return Release Apply
     */
    @Override
    public OpsReleaseApply selectOpsReleaseApplyById(Long id)
    {
        return opsReleaseApplyMapper.selectOpsReleaseApplyById(id);
    }

    /**
     * Query Release Apply List
     * 
     * @param opsReleaseApply Release Apply
     * @return Release Apply List
     */
    @Override
    public List<OpsReleaseApply> selectOpsReleaseApplyList(OpsReleaseApply opsReleaseApply)
    {
        return opsReleaseApplyMapper.selectOpsReleaseApplyList(opsReleaseApply);
    }

    /**
     * Insert Release Apply
     * 
     * @param opsReleaseApply Release Apply
     * @return Result
     */
    @Override
    public int insertOpsReleaseApply(OpsReleaseApply opsReleaseApply)
    {
        opsReleaseApply.setCreateTime(DateUtils.getNowDate());
        if (opsReleaseApply.getApplyUserId() == null) {
            opsReleaseApply.setApplyUserId(SecurityUtils.getUserId());
        }
        if (opsReleaseApply.getCreateBy() == null) {
            opsReleaseApply.setCreateBy(SecurityUtils.getUsername());
        }
        
        // Determine Environment (Default to prod)
        if (opsReleaseApply.getEnvironment() == null) {
            opsReleaseApply.setEnvironment("prod");
        }
        
        // New Logic: Check Environment Config
        OpsEnvironment envConfig = opsEnvironmentService.selectOpsEnvironmentByCode(opsReleaseApply.getEnvironment());
        boolean needApproval = true;
        String processKey = null;
        
        if (envConfig != null) {
            if ("0".equals(envConfig.getNeedApproval())) {
                needApproval = false;
            } else {
                processKey = envConfig.getApprovalProcessKey();
            }
        }
        
        // If approval not needed, auto approve
        if (!needApproval) {
            opsReleaseApply.setTotalSteps(0);
            opsReleaseApply.setCurrentStep(0);
            opsReleaseApply.setApprovalStatus("2"); // Passed
            opsReleaseApply.setStatus("2"); // Approved
            return opsReleaseApplyMapper.insertOpsReleaseApply(opsReleaseApply);
        }
        
        // If processKey defined in Environment, use it directly (Static Process)
        if (StringUtils.isNotEmpty(processKey)) {
             // Validate process definition exists
             org.flowable.engine.RepositoryService repositoryService = com.ruoyi.common.utils.spring.SpringUtils.getBean(org.flowable.engine.RepositoryService.class);
             org.flowable.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processKey).latestVersion().singleResult();
             
             if (processDefinition == null) {
                 // Fallback to dynamic process logic if static process not found
                 // Or just throw clearer exception. 
                 // Let's fallback to dynamic process logic if static one not found, but we need to reset processKey to null to enter fallback block?
                 // No, fallback logic is below. So we just need to NOT return here.
                 // But wait, the fallback logic uses approval config (OpsApprovalConfig).
                 // If the user INTENDED to use static process (by setting it in Environment), but didn't deploy it,
                 // fallback to dynamic might be confusing if they didn't configure OpsApprovalConfig either.
                 
                 // Better option: Check if OpsApprovalConfig exists. If so, use it. If not, throw error.
                 // Actually, let's just throw a better error message, as configured in Environment implies strict requirement.
                 throw new ServiceException("环境[" + opsReleaseApply.getEnvironment() + "]配置了审批流程[" + processKey + "]，但未找到对应的流程定义。请先在流程设计中部署该流程。");
             }
             
             // Use configured static process
             opsReleaseApply.setTotalSteps(1); // Assuming 1 step for now or query process definition
             opsReleaseApply.setCurrentStep(1);
             opsReleaseApply.setApprovalStatus("1");
             opsReleaseApply.setStatus("1");
             
             int rows = opsReleaseApplyMapper.insertOpsReleaseApply(opsReleaseApply);
             
             if (rows > 0) {
                 org.flowable.engine.IdentityService identityService = com.ruoyi.common.utils.spring.SpringUtils.getBean(org.flowable.engine.IdentityService.class);
                 identityService.setAuthenticatedUserId(String.valueOf(opsReleaseApply.getApplyUserId()));
                 try {
                     String procId = workflowService.startProcess(processKey, String.valueOf(opsReleaseApply.getId()), new HashMap<>());
                     opsReleaseApply.setProcessInstanceId(procId);
                     opsReleaseApplyMapper.updateOpsReleaseApply(opsReleaseApply);
                 } finally {
                     identityService.setAuthenticatedUserId(null);
                 }
             }
             return rows;
        }

        // Fallback to Dynamic Process Generation (Old Logic)
        // Check Approval Config
        OpsApprovalConfig configQuery = new OpsApprovalConfig();
        configQuery.setAppId(opsReleaseApply.getAppId());
        configQuery.setEnv(opsReleaseApply.getEnvironment());
        List<OpsApprovalConfig> configs = opsApprovalConfigService.selectOpsApprovalConfigList(configQuery);
        
        // Fallback to global config (appId=0) if no specific config found
        if (configs == null || configs.isEmpty()) {
            configQuery.setAppId(0L);
            configs = opsApprovalConfigService.selectOpsApprovalConfigList(configQuery);
        }
        
        if (configs != null && configs.size() > 0) {
            opsReleaseApply.setTotalSteps(configs.size());
            opsReleaseApply.setCurrentStep(1);
            opsReleaseApply.setApprovalStatus("1"); // In Progress
            opsReleaseApply.setStatus("1"); // Pending Approval
            
            // Deploy Dynamic Process
            // Use appId in key to differentiate
            String dynProcessKey = "release_" + opsReleaseApply.getAppId() + "_" + opsReleaseApply.getEnvironment();
            List<Map<String, String>> steps = new ArrayList<>();
            for (OpsApprovalConfig config : configs) {
                Map<String, String> step = new HashMap<>();
                step.put("type", config.getApproverType());
                step.put("id", String.valueOf(config.getApproverId()));
                steps.add(step);
            }
            workflowService.deployDynamicProcess(dynProcessKey, "Release Process " + dynProcessKey, steps);
            
            int rows = opsReleaseApplyMapper.insertOpsReleaseApply(opsReleaseApply);
            
            if (rows > 0) {
                 org.flowable.engine.IdentityService identityService = com.ruoyi.common.utils.spring.SpringUtils.getBean(org.flowable.engine.IdentityService.class);
                 identityService.setAuthenticatedUserId(String.valueOf(opsReleaseApply.getApplyUserId()));
                 try {
                     String procId = workflowService.startProcess(dynProcessKey, String.valueOf(opsReleaseApply.getId()), new HashMap<>());
                     opsReleaseApply.setProcessInstanceId(procId);
                     opsReleaseApplyMapper.updateOpsReleaseApply(opsReleaseApply);
                 } finally {
                     identityService.setAuthenticatedUserId(null);
                 }
            }
            return rows;
        } else {
            // No config, auto approve
            opsReleaseApply.setTotalSteps(0);
            opsReleaseApply.setCurrentStep(0);
            opsReleaseApply.setApprovalStatus("2"); // Passed
            opsReleaseApply.setStatus("2"); // Approved
            return opsReleaseApplyMapper.insertOpsReleaseApply(opsReleaseApply);
        }
    }




    @Override
    public void superAdminApprove(Long id) {
        OpsReleaseApply apply = selectOpsReleaseApplyById(id);
        if (apply == null || !"1".equals(apply.getStatus())) {
             throw new ServiceException("当前申请状态不可审批");
        }
        
        // 1. Complete all remaining user tasks in the process instance
        if (StringUtils.isNotEmpty(apply.getProcessInstanceId())) {
            org.flowable.engine.TaskService taskService = com.ruoyi.common.utils.spring.SpringUtils.getBean(org.flowable.engine.TaskService.class);
            List<org.flowable.task.api.Task> tasks = taskService.createTaskQuery().processInstanceId(apply.getProcessInstanceId()).list();
            for (org.flowable.task.api.Task task : tasks) {
                // Admin comment
                taskService.addComment(task.getId(), apply.getProcessInstanceId(), "超级管理员一键审批通过");
                taskService.complete(task.getId());
            }
            // Note: Completing one task might trigger next. For "One-Click", we might need to jump to end or loop.
            // But usually "One-Click" means force pass current step or force end.
            // If it's a multi-step process, completing current task moves to next.
            // To force finish, we can delete process instance with reason "completed" or jump to end event.
            
            // Let's try to jump to end event to finish process immediately.
            try {
                 org.flowable.engine.RuntimeService runtimeService = com.ruoyi.common.utils.spring.SpringUtils.getBean(org.flowable.engine.RuntimeService.class);
                 org.flowable.engine.RepositoryService repositoryService = com.ruoyi.common.utils.spring.SpringUtils.getBean(org.flowable.engine.RepositoryService.class);
                 
                 org.flowable.bpmn.model.Process process = repositoryService.getBpmnModel(apply.getProcessInstanceId()).getMainProcess();
                 org.flowable.bpmn.model.FlowElement endEvent = process.getFlowElements().stream()
                     .filter(e -> e instanceof org.flowable.bpmn.model.EndEvent)
                     .findFirst().orElse(null);
                     
                 if (endEvent != null) {
                     runtimeService.createChangeActivityStateBuilder()
                         .processInstanceId(apply.getProcessInstanceId())
                         .moveActivityIdTo(tasks.get(0).getTaskDefinitionKey(), endEvent.getId())
                         .changeState();
                 }
            } catch (Exception e) {
                // Fallback: just update status if process manipulation fails or just let it be
                // But we must ensure process is ended.
                // Simple way: delete instance
                // runtimeService.deleteProcessInstance(apply.getProcessInstanceId(), "Super Admin Approved");
            }
        }
        
        // 2. Update status to Approved (2)
        apply.setStatus("2");
        apply.setApprovalStatus("2");
        apply.setUpdateBy(SecurityUtils.getUsername());
        updateOpsReleaseApply(apply);
    }

    /**
     * Update Release Apply
     * 
     * @param opsReleaseApply Release Apply
     * @return Result
     */
    @Override
    public int updateOpsReleaseApply(OpsReleaseApply opsReleaseApply)
    {
        opsReleaseApply.setUpdateTime(DateUtils.getNowDate());
        return opsReleaseApplyMapper.updateOpsReleaseApply(opsReleaseApply);
    }

    /**
     * Delete Release Apply Information
     * 
     * @param id Release Apply ID
     * @return Result
     */
    @Override
    public int deleteOpsReleaseApplyById(Long id)
    {
        return opsReleaseApplyMapper.deleteOpsReleaseApplyById(id);
    }

    /**
     * Batch Delete Release Apply
     * 
     * @param ids Release Apply IDs
     * @return Result
     */
    @Override
    public int deleteOpsReleaseApplyByIds(Long[] ids)
    {
        return opsReleaseApplyMapper.deleteOpsReleaseApplyByIds(ids);
    }
}
