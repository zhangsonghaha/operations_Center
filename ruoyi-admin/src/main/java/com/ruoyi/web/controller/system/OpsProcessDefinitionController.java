package com.ruoyi.web.controller.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.web.service.WorkflowService;
import org.flowable.engine.repository.ProcessDefinition;

/**
 * Process Definition Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ops/processDefinition")
public class OpsProcessDefinitionController extends BaseController {

    @Autowired
    private WorkflowService workflowService;

    /**
     * List process definitions
     */
    @PreAuthorize("@ss.hasPermi('ops:processDefinition:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String key, @RequestParam(required = false) String name) {
        startPage();
        List<ProcessDefinition> list = workflowService.listProcessDefinitions(key, name);
        // Convert to simple map to avoid JSON serialization issues with ProcessDefinition interface
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (ProcessDefinition pd : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", pd.getId());
            map.put("key", pd.getKey());
            map.put("name", pd.getName());
            map.put("version", pd.getVersion());
            map.put("deploymentId", pd.getDeploymentId());
            map.put("suspended", pd.isSuspended());
            map.put("description", pd.getDescription());
            resultList.add(map);
        }
        return getDataTable(resultList);
    }

    /**
     * Suspend process definition
     */
    @PreAuthorize("@ss.hasPermi('ops:processDefinition:edit')")
    @PutMapping("/suspend/{processDefinitionId}")
    public AjaxResult suspend(@PathVariable String processDefinitionId) {
        workflowService.suspendProcessDefinition(processDefinitionId);
        return AjaxResult.success();
    }

    /**
     * Activate process definition
     */
    @PreAuthorize("@ss.hasPermi('ops:processDefinition:edit')")
    @PutMapping("/activate/{processDefinitionId}")
    public AjaxResult activate(@PathVariable String processDefinitionId) {
        workflowService.activateProcessDefinition(processDefinitionId);
        return AjaxResult.success();
    }

    /**
     * Delete deployment
     */
    @PreAuthorize("@ss.hasPermi('ops:processDefinition:remove')")
    @DeleteMapping("/deployment/{deploymentId}")
    public AjaxResult remove(@PathVariable String deploymentId) {
        workflowService.deleteDeployment(deploymentId);
        return AjaxResult.success();
    }
}
