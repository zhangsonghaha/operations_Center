package com.ruoyi.web.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.domain.dto.DockerEnvInfo;
import com.ruoyi.web.service.IDockerEnvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Docker环境检测Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/ops/docker/env")
public class DockerEnvController extends BaseController
{
    @Autowired
    private IDockerEnvService dockerEnvService;
    
    /**
     * 检测Docker环境
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:env:check')")
    @GetMapping("/check/{serverId}")
    public AjaxResult checkEnvironment(@PathVariable Long serverId)
    {
        try
        {
            DockerEnvInfo envInfo = dockerEnvService.checkDockerEnvironment(serverId);
            return success(envInfo);
        }
        catch (Exception e)
        {
            logger.error("检测Docker环境失败", e);
            return error("环境检测失败: " + e.getMessage());
        }
    }
}
