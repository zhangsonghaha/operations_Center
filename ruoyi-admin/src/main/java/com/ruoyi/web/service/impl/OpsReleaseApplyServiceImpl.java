package com.ruoyi.web.service.impl;

import java.util.Date;
import java.util.List;
import com.ruoyi.common.utils.DateUtils;
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
    private WorkflowService workflowService;

    @Autowired
    private ISysRoleService roleService;

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
            throw new RuntimeException("Applicant cannot approve their own request");
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
                 throw new RuntimeException("No permission to handle this task");
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
                 apply.setCurrentStep(apply.getTotalSteps()); // Ensure it shows completed
             } else {
                 // Moved to next step
                 apply.setCurrentStep(apply.getCurrentStep() + 1);
                 apply.setApprovalStatus("1");
             }
             apply.setAuditReason(comment);
             apply.setAuditUserId(SecurityUtils.getUserId());
             apply.setAuditTime(DateUtils.getNowDate());
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
            // Use appId in key to differentiate, but if fallback used, we might want to reuse process def or create new one.
            // Let's create specific key for this app even if using global config structure
            String processKey = "release_" + opsReleaseApply.getAppId() + "_" + opsReleaseApply.getEnvironment();
            List<Map<String, String>> steps = new ArrayList<>();
            for (OpsApprovalConfig config : configs) {
                Map<String, String> step = new HashMap<>();
                step.put("type", config.getApproverType());
                step.put("id", String.valueOf(config.getApproverId()));
                steps.add(step);
            }
            workflowService.deployDynamicProcess(processKey, "Release Process " + processKey, steps);
            
            // Start Process (will be started after insert to get ID? No, we can start now or after)
            // Let's start after insert to have businessKey
        } else {
            opsReleaseApply.setTotalSteps(0);
            opsReleaseApply.setCurrentStep(0);
            opsReleaseApply.setApprovalStatus("2"); // Passed
            opsReleaseApply.setStatus("2"); // Approved
        }
        
        int rows = opsReleaseApplyMapper.insertOpsReleaseApply(opsReleaseApply);
        
        // Start Process if needed
        if (rows > 0 && opsReleaseApply.getTotalSteps() > 0) {
             String processKey = "release_" + opsReleaseApply.getAppId() + "_" + opsReleaseApply.getEnvironment();
             String procId = workflowService.startProcess(processKey, String.valueOf(opsReleaseApply.getId()), new HashMap<>());
             opsReleaseApply.setProcessInstanceId(procId);
             opsReleaseApplyMapper.updateOpsReleaseApply(opsReleaseApply);
        }
        
        return rows;
    }

    @Override
    public List<OpsReleaseApply> selectPendingApprovals(OpsReleaseApply query)
    {
        Long userId = SecurityUtils.getUserId();
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        List<Long> roleIds = roles.stream().map(SysRole::getRoleId).collect(Collectors.toList());
        
        return opsReleaseApplyMapper.selectReleasePendingList(userId, roleIds, query);
    }

    private List<String> getCurrentUserRoleIds()
    {
        Long userId = SecurityUtils.getUserId();
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> ids = new HashSet<>();
        for (SysRole role : roles) {
            if (role.getRoleId() != null) {
                ids.add(String.valueOf(role.getRoleId()));
            }
        }
        return new ArrayList<>(ids);
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
