package com.ruoyi.web.service;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.engine.ProcessEngineConfiguration;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Collections;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.HistoryService;

/**
 * Workflow Service to handle dynamic process generation and execution
 */
@Service
public class WorkflowService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private HistoryService historyService;

    /**
     * Deploy a dynamic process based on approval steps
     * @param processKey Unique key for the process (e.g. "release_approval_appId_env")
     * @param processName Process Name
     * @param steps List of approver IDs/Roles in order
     * @return Deployment ID
     */
    @Transactional
    public String deployDynamicProcess(String processKey, String processName, List<Map<String, String>> steps) {
        BpmnModel model = new BpmnModel();
        org.flowable.bpmn.model.Process process = new org.flowable.bpmn.model.Process();
        process.setId(processKey);
        process.setName(processName);
        model.addProcess(process);

        // Start Event
        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent");
        process.addFlowElement(startEvent);

        String lastNodeId = "startEvent";

        // Dynamic User Tasks
        for (int i = 0; i < steps.size(); i++) {
            Map<String, String> step = steps.get(i);
            String type = step.get("type"); // user or role
            String id = step.get("id");
            String stepName = "Approval Step " + (i + 1);
            
            UserTask userTask = new UserTask();
            userTask.setId("step_" + (i + 1));
            userTask.setName(stepName);
            
            // Assignee
            if ("user".equals(type)) {
                userTask.setAssignee(id); // Assign to specific user
            } else {
                userTask.getCandidateGroups().add(id); // Assign to role/group
            }
            
            process.addFlowElement(userTask);
            
            // Sequence Flow
            process.addFlowElement(new SequenceFlow(lastNodeId, userTask.getId()));
            lastNodeId = userTask.getId();
        }

        // End Event
        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");
        process.addFlowElement(endEvent);
        
        // Final Sequence
        process.addFlowElement(new SequenceFlow(lastNodeId, "endEvent"));

        // Deploy
        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel(processKey + ".bpmn20.xml", model)
                .name(processName)
                .deploy();
                
        return deployment.getId();
    }

    /**
     * Start a process instance
     * @param processKey Process Key
     * @param businessKey Business Key (e.g. releaseId)
     * @param variables Variables
     * @return ProcessInstanceId
     */
    @Transactional
    public String startProcess(String processKey, String businessKey, Map<String, Object> variables) {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey, businessKey, variables);
        return instance.getId();
    }

    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;

    /**
     * Generate process image with highlighted active activities
     */
    public InputStream generateProcessImage(String processInstanceId) {
        // Get Process Instance
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        
        List<String> activeActivityIds = new ArrayList<>();
        String processDefinitionId;
        
        if (processInstance == null) {
             // If instance finished, check history
             HistoricProcessInstance historicInstance = 
                 ProcessEngines.getDefaultProcessEngine().getHistoryService()
                 .createHistoricProcessInstanceQuery()
                 .processInstanceId(processInstanceId)
                 .singleResult();
             
             if (historicInstance == null) {
                 return null;
             }
             processDefinitionId = historicInstance.getProcessDefinitionId();
             // No active activities if finished
        } else {
             processDefinitionId = processInstance.getProcessDefinitionId();
             activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
        }

        // Get BpmnModel
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        
        // Generate Image
        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        return diagramGenerator.generateDiagram(
            bpmnModel, 
            "png", 
            activeActivityIds, 
            Collections.emptyList(), 
            processEngineConfiguration.getActivityFontName(), 
            processEngineConfiguration.getLabelFontName(), 
            processEngineConfiguration.getAnnotationFontName(), 
            processEngineConfiguration.getClassLoader(), 
            1.0,
            true);
    }
    public List<Task> getTasksForUser(String userId, List<String> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return taskService.createTaskQuery()
                    .taskCandidateOrAssigned(userId)
                    .orderByTaskCreateTime().desc()
                    .list();
        }
        return taskService.createTaskQuery()
                .or()
                .taskCandidateOrAssigned(userId)
                .taskCandidateGroupIn(groupIds)
                .endOr()
                .orderByTaskCreateTime().desc()
                .list();
    }
    
    /**
     * Complete a task
     */
    @Transactional
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }
    
    /**
     * Get current active task for a process instance
     */
    public Task getCurrentTask(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
    }

    /**
     * List process definitions
     */
    public List<ProcessDefinition> listProcessDefinitions(String key, String name) {
        return listProcessDefinitions(key, name, true);
    }
    
    public List<ProcessDefinition> listProcessDefinitions(String key, String name, boolean latestOnly) {
        org.flowable.engine.repository.ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        if (latestOnly) {
            query.latestVersion();
        }
        if (key != null && !key.isEmpty()) {
            query.processDefinitionKeyLike("%" + key + "%");
        }
        if (name != null && !name.isEmpty()) {
            query.processDefinitionNameLike("%" + name + "%");
        }
        query.orderByProcessDefinitionKey().asc();
        query.orderByProcessDefinitionVersion().desc();
        return query.list();
    }

    /**
     * Suspend process definition
     */
    public void suspendProcessDefinition(String processDefinitionId) {
        repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);
    }

    /**
     * Activate process definition
     */
    public void activateProcessDefinition(String processDefinitionId) {
        repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);
    }

    /**
     * Delete deployment
     */
    public void deleteDeployment(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
    }

    /**
     * Get Process Definition XML
     */
    public String getProcessDefinitionXml(String deploymentId, String resourceName) {
        try (InputStream inputStream = repositoryService.getResourceAsStream(deploymentId, resourceName)) {
             return new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Error reading process definition XML", e);
        }
    }

    /**
     * Get my started process instances
     */
    public List<HistoricProcessInstance> getMyProcessInstances(String userId, int start, int limit) {
        return historyService.createHistoricProcessInstanceQuery()
            .startedBy(userId)
            .orderByProcessInstanceStartTime().desc()
            .listPage(start, limit);
    }

    /**
     * Get my started process instances count
     */
    public long getMyProcessInstancesCount(String userId) {
        return historyService.createHistoricProcessInstanceQuery()
            .startedBy(userId)
            .count();
    }

    /**
     * Get all process instances for monitor
     */
    public List<HistoricProcessInstance> getMonitorProcessInstances(String processKey, String startedBy, int start, int limit) {
        org.flowable.engine.history.HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
            .orderByProcessInstanceStartTime().desc();
            
        if (processKey != null && !processKey.isEmpty()) {
            query.processDefinitionKey(processKey);
        }
        if (startedBy != null && !startedBy.isEmpty()) {
            query.startedBy(startedBy);
        }
        
        return query.listPage(start, limit);
    }
    
    /**
     * Get process active activity ids (for highlighting)
     */
    public List<String> getProcessActiveActivityIds(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();
        
        if (processInstance != null) {
            return runtimeService.getActiveActivityIds(processInstanceId);
        }
        return new ArrayList<>();
    }

    /**
     * Get Process Definition by Instance ID
     */
    public ProcessDefinition getProcessDefinitionByInstanceId(String processInstanceId) {
        HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();
        if (historicInstance != null) {
            return repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(historicInstance.getProcessDefinitionId())
                .singleResult();
        }
        return null;
    }
    
    /**
     * Remind current assignee
     */
    public void remindProcessInstance(String processInstanceId, String userId) {
        List<Task> tasks = taskService.createTaskQuery()
            .processInstanceId(processInstanceId)
            .active()
            .list();
            
        for (Task task : tasks) {
            // Log reminder or send notification
            // For now, we just print to console, in real app send email/sms
            System.out.println("User " + userId + " reminded task " + task.getId() + " assigned to " + task.getAssignee());
        }
    }
}
