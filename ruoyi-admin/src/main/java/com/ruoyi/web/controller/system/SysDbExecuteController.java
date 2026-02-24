package com.ruoyi.web.controller.system;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.IDbExecuteService;

/**
 * 数据库执行Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/db/execute")
public class SysDbExecuteController extends BaseController
{
    @Autowired
    private IDbExecuteService dbExecuteService;

    /**
     * 获取表列表
     */
    @PreAuthorize("@ss.hasPermi('system:db:execute:view')")
    @GetMapping("/tables")
    public AjaxResult getTables(@RequestParam Long connId)
    {
        return AjaxResult.success(dbExecuteService.getTableList(connId));
    }

    /**
     * 获取数据库元数据
     */
    @PreAuthorize("@ss.hasPermi('system:db:execute:view')")
    @GetMapping("/metadata")
    public AjaxResult getMetadata(@RequestParam Long connId)
    {
        return AjaxResult.success(dbExecuteService.getDatabaseMetadata(connId));
    }

    /**
     * 获取数据库结构（数据库名称、表、视图、存储过程、函数等）
     */
    @PreAuthorize("@ss.hasPermi('system:db:execute:view')")
    @GetMapping("/schema")
    public AjaxResult getSchema(@RequestParam Long connId)
    {
        return AjaxResult.success(dbExecuteService.getDatabaseSchema(connId));
    }

    /**
     * 获取表结构详情
     */
    @PreAuthorize("@ss.hasPermi('system:db:execute:view')")
    @GetMapping("/table/structure")
    public AjaxResult getTableStructure(@RequestParam Long connId, @RequestParam String tableName)
    {
        try {
            return AjaxResult.success(dbExecuteService.getTableStructure(connId, tableName));
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 获取存储过程定义
     */
    @PreAuthorize("@ss.hasPermi('system:db:execute:view')")
    @GetMapping("/procedure/definition")
    public AjaxResult getProcedureDefinition(@RequestParam Long connId, @RequestParam String procName)
    {
        try {
            return AjaxResult.success(dbExecuteService.getProcedureDefinition(connId, procName));
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 获取函数定义
     */
    @PreAuthorize("@ss.hasPermi('system:db:execute:view')")
    @GetMapping("/function/definition")
    public AjaxResult getFunctionDefinition(@RequestParam Long connId, @RequestParam String funcName)
    {
        try {
            return AjaxResult.success(dbExecuteService.getFunctionDefinition(connId, funcName));
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 更新存储过程
     */
    @PreAuthorize("@ss.hasPermi('system:db:execute:update')")
    @PostMapping("/procedure/update")
    public AjaxResult updateProcedure(@RequestBody Map<String, Object> params)
    {
        Long connId = Long.valueOf(params.get("connId").toString());
        String procName = (String) params.get("procName");
        String definition = (String) params.get("definition");
        
        try {
            dbExecuteService.updateProcedure(connId, procName, definition);
            return AjaxResult.success("存储过程更新成功");
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 更新函数
     */
    @PreAuthorize("@ss.hasPermi('system:db:execute:update')")
    @PostMapping("/function/update")
    public AjaxResult updateFunction(@RequestBody Map<String, Object> params)
    {
        Long connId = Long.valueOf(params.get("connId").toString());
        String funcName = (String) params.get("funcName");
        String definition = (String) params.get("definition");
        
        try {
            dbExecuteService.updateFunction(connId, funcName, definition);
            return AjaxResult.success("函数更新成功");
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 执行查询SQL
     */
    @PreAuthorize("@ss.hasPermi('system:db:execute:view')")
    @PostMapping("/select")
    public AjaxResult executeSelect(@RequestBody Map<String, Object> params)
    {
        Long connId = Long.valueOf(params.get("connId").toString());
        String sql = (String) params.get("sql");
        try {
            List<Map<String, Object>> result = dbExecuteService.executeSelect(connId, sql);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 执行增删改SQL
     */
    @PreAuthorize("@ss.hasPermi('system:db:execute:update')")
    @PostMapping("/update")
    public AjaxResult executeUpdate(@RequestBody Map<String, Object> params)
    {
        Long connId = Long.valueOf(params.get("connId").toString());
        String sql = (String) params.get("sql");
        try {
            int rows = dbExecuteService.executeUpdate(connId, sql);
            return AjaxResult.success("执行成功，影响行数：" + rows);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}
