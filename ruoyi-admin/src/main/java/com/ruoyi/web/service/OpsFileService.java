package com.ruoyi.web.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.domain.OpsServer;
import com.ruoyi.web.domain.vo.RemoteFileVo;
import com.ruoyi.web.mapper.OpsServerMapper;

@Service
public class OpsFileService {
    
    private static final Logger log = LoggerFactory.getLogger(OpsFileService.class);
    
    @Autowired
    private OpsServerMapper opsServerMapper;

// ... imports

    /**
     * 获取文件列表
     */
    public Map<String, Object> listFiles(Long serverId, String path) {
        Session session = null;
        ChannelSftp sftp = null;
        try {
            OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
            if (server == null) throw new ServiceException("服务器不存在");
            
            session = createSession(server);
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(5000);
            
            // 如果路径为空或点，则获取Home目录
            if (StringUtils.isEmpty(path) || ".".equals(path)) {
                path = sftp.getHome();
            }
            
            // 切换到目标目录并获取绝对路径
            sftp.cd(path);
            String currentPath = sftp.pwd();
            
            Vector<ChannelSftp.LsEntry> entries = sftp.ls(currentPath);
            List<RemoteFileVo> fileList = new ArrayList<>();
            
            for (ChannelSftp.LsEntry entry : entries) {
                if (".".equals(entry.getFilename()) || "..".equals(entry.getFilename())) continue;
                
                RemoteFileVo vo = new RemoteFileVo();
                vo.setName(entry.getFilename());
                SftpATTRS attrs = entry.getAttrs();
                vo.setSize(attrs.getSize());
                vo.setPermission(attrs.getPermissionsString());
                vo.setModTime(attrs.getMtimeString());
                vo.setDir(attrs.isDir());
                fileList.add(vo);
            }
            
            // 排序：文件夹在前，文件在后
            Collections.sort(fileList, new Comparator<RemoteFileVo>() {
                @Override
                public int compare(RemoteFileVo o1, RemoteFileVo o2) {
                    if (o1.isDir() && !o2.isDir()) return -1;
                    if (!o1.isDir() && o2.isDir()) return 1;
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
            
            Map<String, Object> result = new HashMap<>();
            result.put("path", currentPath);
            result.put("files", fileList);
            return result;
            
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            throw new ServiceException("获取文件列表失败: " + e.getMessage());
        } finally {
            closeChannel(sftp);
            closeSession(session);
        }
    }
    
    /**
     * 上传文件
     */
    public void uploadFile(Long serverId, String path, MultipartFile file) {
        Session session = null;
        ChannelSftp sftp = null;
        try {
            OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
            session = createSession(server);
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(5000);
            
            String remotePath = path + "/" + file.getOriginalFilename();
            // 处理Windows路径分隔符问题
            remotePath = remotePath.replace("//", "/");
            
            sftp.put(file.getInputStream(), remotePath);
            
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new ServiceException("上传文件失败: " + e.getMessage());
        } finally {
            closeChannel(sftp);
            closeSession(session);
        }
    }
    
    /**
     * 下载文件
     */
    public void downloadFile(Long serverId, String path, HttpServletResponse response) {
        Session session = null;
        ChannelSftp sftp = null;
        try {
            OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
            session = createSession(server);
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(5000);
            
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1") + "\"");
            
            sftp.get(path, response.getOutputStream());
            
        } catch (Exception e) {
            log.error("下载文件失败", e);
            // 这里不能抛出异常，因为Response可能已经写入了部分数据
        } finally {
            closeChannel(sftp);
            closeSession(session);
        }
    }
    
    /**
     * 删除文件或目录
     */
    public void deleteFile(Long serverId, String path, boolean isDir) {
        Session session = null;
        ChannelSftp sftp = null;
        try {
            OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
            session = createSession(server);
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(5000);
            
            if (isDir) {
                // SFTP rmdir只能删除空目录，如果要递归删除需要用Shell执行rm -rf
                // 这里为了安全和简便，暂时尝试用Shell执行
                execShellCommand(session, "rm -rf \"" + path + "\"");
            } else {
                sftp.rm(path);
            }
            
        } catch (Exception e) {
            log.error("删除文件失败", e);
            throw new ServiceException("删除文件失败: " + e.getMessage());
        } finally {
            closeChannel(sftp);
            closeSession(session);
        }
    }
    
    /**
     * 压缩文件/目录
     */
    public void compress(Long serverId, String path, String type) {
        Session session = null;
        try {
            OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
            session = createSession(server);
            
            String parentDir = path.substring(0, path.lastIndexOf("/"));
            String targetName = path.substring(path.lastIndexOf("/") + 1);
            String cmd = "";
            
            if ("zip".equals(type)) {
                cmd = String.format("cd \"%s\" && zip -r \"%s.zip\" \"%s\"", parentDir, targetName, targetName);
            } else { // tar.gz
                cmd = String.format("cd \"%s\" && tar -czvf \"%s.tar.gz\" \"%s\"", parentDir, targetName, targetName);
            }
            
            execShellCommand(session, cmd);
            
        } catch (Exception e) {
            log.error("压缩失败", e);
            throw new ServiceException("压缩失败: " + e.getMessage());
        } finally {
            closeSession(session);
        }
    }
    
    /**
     * 解压文件
     */
    public void extract(Long serverId, String path) {
        Session session = null;
        try {
            OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
            session = createSession(server);
            
            String parentDir = path.substring(0, path.lastIndexOf("/"));
            String cmd = "";
            
            if (path.endsWith(".zip")) {
                cmd = String.format("cd \"%s\" && unzip \"%s\"", parentDir, path);
            } else if (path.endsWith(".tar.gz") || path.endsWith(".tgz")) {
                cmd = String.format("cd \"%s\" && tar -xzvf \"%s\"", parentDir, path);
            } else {
                throw new ServiceException("不支持的文件格式");
            }
            
            execShellCommand(session, cmd);
            
        } catch (Exception e) {
            log.error("解压失败", e);
            throw new ServiceException("解压失败: " + e.getMessage());
        } finally {
            closeSession(session);
        }
    }

    private Session createSession(OpsServer server) throws Exception {
        JSch jsch = new JSch();
        if ("1".equals(server.getAuthType()) && StringUtils.isNotEmpty(server.getPrivateKey())) {
            jsch.addIdentity("key", server.getPrivateKey().getBytes(), null, null);
        }
        
        String host = StringUtils.isNotEmpty(server.getPrivateIp()) ? server.getPrivateIp() : server.getPublicIp();
        int port = server.getServerPort() != null ? server.getServerPort() : 22;
        
        Session session = jsch.getSession(server.getUsername(), host, port);
        if ("0".equals(server.getAuthType())) {
            session.setPassword(server.getPassword());
        }
        
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect(5000);
        return session;
    }
    
    private void execShellCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.connect();
        
        // 等待命令执行完成（简单的同步等待）
        while (!channel.isClosed()) {
            Thread.sleep(100);
        }
        
        channel.disconnect();
        if (channel.getExitStatus() != 0) {
            throw new Exception("命令执行失败，退出码: " + channel.getExitStatus());
        }
    }
    
    private void closeChannel(Channel channel) {
        if (channel != null && channel.isConnected()) channel.disconnect();
    }
    
    private void closeSession(Session session) {
        if (session != null && session.isConnected()) session.disconnect();
    }
}
