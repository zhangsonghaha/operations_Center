package com.ruoyi.web.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.DockerContainer;
import com.ruoyi.web.domain.dto.DockerDeployConfig;
import com.ruoyi.web.domain.dto.DockerDeployResult;
import com.ruoyi.web.service.IDockerContainerService;
import com.ruoyi.web.service.IDockerDeployService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Docker容器管理Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/ops/docker/container")
public class DockerContainerController extends BaseController
{
    @Autowired
    private IDockerContainerService dockerContainerService;
    
    @Autowired
    private IDockerDeployService dockerDeployService;
    
    /**
     * 查询Docker容器列表
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:list')")
    @GetMapping("/list")
    public TableDataInfo list(DockerContainer dockerContainer)
    {
        startPage();
        List<DockerContainer> list = dockerContainerService.selectDockerContainerList(dockerContainer);
        return getDataTable(list);
    }
    
    /**
     * 导出Docker容器列表
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:export')")
    @Log(title = "Docker容器", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DockerContainer dockerContainer)
    {
        List<DockerContainer> list = dockerContainerService.selectDockerContainerList(dockerContainer);
        ExcelUtil<DockerContainer> util = new ExcelUtil<DockerContainer>(DockerContainer.class);
        util.exportExcel(response, list, "Docker容器数据");
    }
    
    /**
     * 获取Docker容器详细信息
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:query')")
    @GetMapping(value = "/{containerId}")
    public AjaxResult getInfo(@PathVariable("containerId") Long containerId)
    {
        return success(dockerContainerService.selectDockerContainerById(containerId));
    }
    
    /**
     * 部署Docker容器
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:deploy')")
    @Log(title = "Docker容器", businessType = BusinessType.INSERT)
    @PostMapping("/deploy")
    public AjaxResult deploy(@RequestBody DockerDeployConfig config)
    {
        try
        {
            DockerDeployResult result = dockerDeployService.deployContainer(config);
            return success(result);
        }
        catch (Exception e)
        {
            logger.error("部署Docker容器失败", e);
            return error("部署失败: " + e.getMessage());
        }
    }
    
    /**
     * 启动Docker容器
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:start')")
    @Log(title = "Docker容器", businessType = BusinessType.UPDATE)
    @PutMapping("/start/{containerId}")
    public AjaxResult start(@PathVariable Long containerId)
    {
        try
        {
            DockerContainer container = dockerContainerService.selectDockerContainerById(containerId);
            if (container == null)
            {
                return error("容器不存在");
            }
            
            boolean success = dockerDeployService.startContainer(containerId, container.getServerId());
            return success ? success() : error("启动失败");
        }
        catch (Exception e)
        {
            logger.error("启动Docker容器失败", e);
            return error("启动失败: " + e.getMessage());
        }
    }
    
    /**
     * 停止Docker容器
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:stop')")
    @Log(title = "Docker容器", businessType = BusinessType.UPDATE)
    @PutMapping("/stop/{containerId}")
    public AjaxResult stop(@PathVariable Long containerId)
    {
        try
        {
            DockerContainer container = dockerContainerService.selectDockerContainerById(containerId);
            if (container == null)
            {
                return error("容器不存在");
            }
            
            boolean success = dockerDeployService.stopContainer(containerId, container.getServerId());
            return success ? success() : error("停止失败");
        }
        catch (Exception e)
        {
            logger.error("停止Docker容器失败", e);
            return error("停止失败: " + e.getMessage());
        }
    }
    
    /**
     * 重启Docker容器
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:restart')")
    @Log(title = "Docker容器", businessType = BusinessType.UPDATE)
    @PutMapping("/restart/{containerId}")
    public AjaxResult restart(@PathVariable Long containerId)
    {
        try
        {
            DockerContainer container = dockerContainerService.selectDockerContainerById(containerId);
            if (container == null)
            {
                return error("容器不存在");
            }
            
            boolean success = dockerDeployService.restartContainer(containerId, container.getServerId());
            return success ? success() : error("重启失败");
        }
        catch (Exception e)
        {
            logger.error("重启Docker容器失败", e);
            return error("重启失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除Docker容器
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:remove')")
    @Log(title = "Docker容器", businessType = BusinessType.DELETE)
    @DeleteMapping("/{containerId}")
    public AjaxResult remove(@PathVariable Long containerId)
    {
        try
        {
            DockerContainer container = dockerContainerService.selectDockerContainerById(containerId);
            if (container == null)
            {
                return error("容器不存在");
            }
            
            boolean success = dockerDeployService.removeContainer(containerId, container.getServerId(), true);
            return success ? success() : error("删除失败");
        }
        catch (Exception e)
        {
            logger.error("删除Docker容器失败", e);
            return error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询容器日志
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:logs')")
    @GetMapping("/logs/{containerId}")
    public AjaxResult getLogs(@PathVariable Long containerId, @RequestParam(required = false, defaultValue = "100") Integer tail)
    {
        try
        {
            String logs = dockerContainerService.getContainerLogs(containerId, tail);
            return success(logs);
        }
        catch (Exception e)
        {
            logger.error("获取容器日志失败", e);
            return error("获取日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询容器统计信息
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:stats')")
    @GetMapping("/stats/{containerId}")
    public AjaxResult getStats(@PathVariable Long containerId)
    {
        try
        {
            return success(dockerContainerService.getContainerStats(containerId));
        }
        catch (Exception e)
        {
            logger.error("获取容器统计信息失败", e);
            return error("获取统计信息失败: " + e.getMessage());
        }
    }
}
