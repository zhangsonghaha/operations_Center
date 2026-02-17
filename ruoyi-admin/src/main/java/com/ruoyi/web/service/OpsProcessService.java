package com.ruoyi.web.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.web.domain.OpsProcess;
import com.ruoyi.web.domain.OpsServer;
import com.ruoyi.web.mapper.OpsServerMapper;

/**
 * 进程管理Service业务层处理
 */
@Service
public class OpsProcessService
{
    private static final Logger log = LoggerFactory.getLogger(OpsProcessService.class);

    @Autowired
    private OpsServerMapper opsServerMapper;

    /**
     * 查询服务器进程列表
     * 
     * @param serverId 服务器ID
     * @param query 查询条件
     * @return 进程列表
     */
    public List<OpsProcess> selectProcessList(Long serverId, OpsProcess query)
    {
        OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
        if (server == null) {
            throw new ServiceException("服务器不存在");
        }

        List<OpsProcess> processList = new ArrayList<>();
        Session session = null;
        try {
            session = createSession(server);
            
            // 简单判断操作系统类型
            String osType = executeCommand(session, "uname -s");
            boolean isLinux = osType != null && osType.contains("Linux");
            
            if (isLinux) {
                // Linux: PID, User, %CPU, %MEM, Start Time, Command
                // 使用 ps -eo pid,user,%cpu,%mem,lstart,comm --sort=-%cpu | head -n 100
                String cmd = "ps -eo pid,user,%cpu,%mem,lstart,comm --sort=-%cpu | head -n 50";
                if (query.getPid() != null && !query.getPid().isEmpty()) {
                    cmd = "ps -p " + query.getPid() + " -o pid,user,%cpu,%mem,lstart,comm";
                } else if (query.getName() != null && !query.getName().isEmpty()) {
                    cmd = "ps -eo pid,user,%cpu,%mem,lstart,comm --sort=-%cpu | grep " + query.getName() + " | head -n 50";
                }
                
                String output = executeCommand(session, cmd);
                processList = parseLinuxPsOutput(output, serverId);
            } else {
                // Windows (尝试使用 PowerShell)
                // Get-Process | Select-Object Id, ProcessName, CPU, WorkingSet, StartTime, Responding
                String cmd = "powershell \"Get-Process | Select-Object Id, ProcessName, CPU, WorkingSet, StartTime, Responding | ConvertTo-Csv -NoTypeInformation\"";
                String output = executeCommand(session, cmd);
                processList = parseWindowsPsOutput(output, serverId);
                
                // Windows 内存过滤 (PID/Name)
                if (query.getPid() != null && !query.getPid().isEmpty()) {
                    processList.removeIf(p -> !p.getPid().equals(query.getPid()));
                }
                if (query.getName() != null && !query.getName().isEmpty()) {
                    processList.removeIf(p -> !p.getName().toLowerCase().contains(query.getName().toLowerCase()));
                }
            }
            
        } catch (Exception e) {
            log.error("获取进程列表失败: " + server.getServerName(), e);
            throw new ServiceException("获取进程列表失败: " + e.getMessage());
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        
        return processList;
    }

    /**
     * 终止进程
     * 
     * @param serverId 服务器ID
     * @param pids 进程ID列表
     */
    public void killProcesses(Long serverId, List<String> pids)
    {
        OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
        if (server == null) {
            throw new ServiceException("服务器不存在");
        }

        Session session = null;
        try {
            session = createSession(server);
            
            String osType = executeCommand(session, "uname -s");
            boolean isLinux = osType != null && osType.contains("Linux");
            
            for (String pid : pids) {
                String cmd;
                if (isLinux) {
                    cmd = "kill -9 " + pid;
                } else {
                    cmd = "taskkill /F /PID " + pid;
                }
                executeCommand(session, cmd);
            }
            
        } catch (Exception e) {
            log.error("终止进程失败: " + server.getServerName(), e);
            throw new ServiceException("终止进程失败: " + e.getMessage());
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private Session createSession(OpsServer server) throws Exception {
        JSch jsch = new JSch();
        if ("1".equals(server.getAuthType()) && server.getPrivateKey() != null) {
            jsch.addIdentity("key_" + server.getServerId(), server.getPrivateKey().getBytes(), null, null);
        }
        
        Session session = jsch.getSession(server.getUsername(), server.getPublicIp(), server.getServerPort());
        session.setConfig("StrictHostKeyChecking", "no");
        
        if ("0".equals(server.getAuthType())) {
            session.setPassword(server.getPassword());
        }
        
        session.connect(5000);
        return session;
    }

    private String executeCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(System.err);
        
        InputStream in = channel.getInputStream();
        channel.connect();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        channel.disconnect();
        return output.toString().trim();
    }

    private List<OpsProcess> parseLinuxPsOutput(String output, Long serverId) {
        List<OpsProcess> list = new ArrayList<>();
        String[] lines = output.split("\n");
        // Skip header if present (PID USER %CPU %MEM STARTED COMMAND)
        int start = 0;
        if (lines.length > 0 && lines[0].trim().startsWith("PID")) {
            start = 1;
        }
        
        for (int i = start; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            // Split by whitespace
            String[] parts = line.split("\\s+");
            if (parts.length >= 6) {
                OpsProcess p = new OpsProcess();
                p.setPid(parts[0]);
                // parts[1] is USER, skip
                p.setCpuUsage(parts[2]);
                p.setMemoryUsage(parts[3]);
                
                // lstart produces multiple parts: "Thu Feb 13 10:00:00 2025" -> 5 parts
                // PID USER %CPU %MEM [Day Month Date Time Year] COMMAND
                // Index: 0 1 2 3 [4 5 6 7 8] 9+
                
                if (parts.length >= 10) {
                   StringBuilder startTime = new StringBuilder();
                   for(int j=4; j<=8; j++) startTime.append(parts[j]).append(" ");
                   p.setStartTime(startTime.toString().trim());
                   
                   p.setName(parts[9]); // Command name
                } else {
                   // Fallback if parsing fails
                   p.setStartTime("-");
                   p.setName(parts[parts.length-1]);
                }
                
                p.setStatus("Running"); // Simple assumption or parse STAT
                p.setServerId(serverId);
                list.add(p);
            }
        }
        return list;
    }
    
    private List<OpsProcess> parseWindowsPsOutput(String output, Long serverId) {
        List<OpsProcess> list = new ArrayList<>();
        String[] lines = output.split("\n");
        // Skip header
        int start = 0;
        if (lines.length > 0 && lines[0].contains("Id")) {
            start = 1;
        }
        
        for (int i = start; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            // CSV parsing is tricky with regex, simpler split by ","
            String[] parts = line.split("\",\"");
            if (parts.length >= 6) {
                String pid = parts[0].replace("\"", "");
                String name = parts[1].replace("\"", "");
                
                OpsProcess p = new OpsProcess();
                // "Id","ProcessName","CPU","WorkingSet","StartTime","Responding"
                p.setPid(pid);
                p.setName(name);
                p.setCpuUsage(parts[2].replace("\"", ""));
                p.setMemoryUsage(parts[3].replace("\"", ""));
                p.setStartTime(parts[4].replace("\"", ""));
                
                String responding = parts[5].replace("\"", "");
                p.setStatus(Boolean.parseBoolean(responding) ? "Running" : "Not Responding");
                
                p.setServerId(serverId);
                list.add(p);
            }
        }
        return list;
    }
}
