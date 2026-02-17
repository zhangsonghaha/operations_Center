package com.ruoyi.web.controller.system;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.domain.vo.RemoteFileVo;
import com.ruoyi.web.service.OpsFileService;

/**
 * 服务器文件管理Controller
 */
@RestController
@RequestMapping("/system/server/file")
public class OpsFileController extends BaseController
{
    @Autowired
    private OpsFileService opsFileService;

    /**
     * 获取文件列表
     */
    @PreAuthorize("@ss.hasPermi('ops:server:query')")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam Long serverId, @RequestParam(defaultValue = "") String path)
    {
        // 返回包含 path 和 files 的 Map
        return AjaxResult.success(opsFileService.listFiles(serverId, path));
    }
    
    /**
     * 上传文件
     */
    @PreAuthorize("@ss.hasPermi('ops:server:edit')")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam Long serverId, @RequestParam String path, @RequestParam("file") MultipartFile file)
    {
        opsFileService.uploadFile(serverId, path, file);
        return AjaxResult.success();
    }
    
    /**
     * 下载文件
     */
    @PreAuthorize("@ss.hasPermi('ops:server:query')")
    @GetMapping("/download")
    public void download(@RequestParam Long serverId, @RequestParam String path, HttpServletResponse response)
    {
        opsFileService.downloadFile(serverId, path, response);
    }
    
    /**
     * 删除文件
     */
    @PreAuthorize("@ss.hasPermi('ops:server:edit')")
    @DeleteMapping("/remove")
    public AjaxResult remove(@RequestParam Long serverId, @RequestParam String path, @RequestParam boolean isDir)
    {
        opsFileService.deleteFile(serverId, path, isDir);
        return AjaxResult.success();
    }
    
    /**
     * 压缩文件
     */
    @PreAuthorize("@ss.hasPermi('ops:server:edit')")
    @PostMapping("/compress")
    public AjaxResult compress(@RequestParam Long serverId, @RequestParam String path, @RequestParam String type)
    {
        opsFileService.compress(serverId, path, type);
        return AjaxResult.success();
    }
    
    /**
     * 解压文件
     */
    @PreAuthorize("@ss.hasPermi('ops:server:edit')")
    @PostMapping("/extract")
    public AjaxResult extract(@RequestParam Long serverId, @RequestParam String path)
    {
        opsFileService.extract(serverId, path);
        return AjaxResult.success();
    }
}
